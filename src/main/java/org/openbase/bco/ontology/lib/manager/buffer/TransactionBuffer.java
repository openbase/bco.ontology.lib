/**
 * ==================================================================
 *
 * This file is part of org.openbase.bco.ontology.lib.
 *
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.manager.buffer;

import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 17.01.17.
 */
public final class TransactionBuffer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionBuffer.class);
    private static final Queue<String> QUEUE = new LinkedBlockingQueue<>();
    private static RSBInformer<OntologyChange> rsbInformer;

    static {
        try {
            new TransactionBuffer();
            rsbInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(OntConfig.getOntologyRsbScope(), OntologyChange.class);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * Constructor for TransactionBuffer.
     *
     * @throws CouldNotPerformException is thrown in case the thread to perform the transaction buffer could not be performed.
     */
    private TransactionBuffer() throws CouldNotPerformException {
        startUploadQueueEntriesThread();
    }

    private void startUploadQueueEntriesThread() throws CouldNotPerformException {
        try {
            GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {
                while (!QUEUE.isEmpty()) {
                    final String sparql = QUEUE.peek();

                    try {
                        SparqlHttp.uploadSparqlRequest(sparql, OntConfig.getOntologyDbUrl());
                        QUEUE.poll();
                    } catch (CouldNotPerformException ex) {
                        QUEUE.poll();
                        ExceptionPrinter.printHistory("Dropped broken QUEUE entry.", ex, LOGGER, LogLevel.ERROR);
                    } catch (IOException ex) {
                        LOGGER.warn("IOException: no connection...Retry...");
                        break;
                    }

                    try {
                        if (QUEUE.isEmpty()) {
                            final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(OntologyChange.Category.UNKNOWN).build();

                            rsbInformer.activate();
                            rsbInformer.publish(ontologyChange);
                            rsbInformer.deactivate();
                            LOGGER.info("Transaction buffer is empty. All entries send to server.");
                        }
                    } catch (InterruptedException | CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
                    }
                }
            }, 0, OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
        } catch (RejectedExecutionException | IllegalArgumentException | CouldNotPerformException ex) {
            throw new CouldNotProcessException("Could not process transactionBuffer thread!", ex);
        }
    }

    /**
     * Method inserts a sparql expression to the transaction buffer (QUEUE). If the QUEUE reaches the capacity than the first entry is removed to avoid overflow.
     *
     * @param sparql is the sparql update expression.
     */
    public static void insertData(final String sparql) {
        if (QUEUE.size() > OntConfig.TRANSACTION_BUFFER_SIZE) {
            QUEUE.poll();
        }
        QUEUE.offer(sparql);
    }
}
