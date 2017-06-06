/**
 * ==================================================================
 * <p>
 * This file is part of org.openbase.bco.ontology.lib.
 * <p>
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 * <p>
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.manager.buffer;

import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.InstantiationException;
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
public class TransactionBuffer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionBuffer.class);
    private static final Queue<String> queue = new LinkedBlockingQueue<>();
    private static RSBInformer<OntologyChange> rsbInformer;

    static {
        try {
            rsbInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(OntConfig.ONTOLOGY_RSB_SCOPE, OntologyChange.class);
        } catch (InstantiationException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    public TransactionBuffer() throws CouldNotPerformException {
        startUploadQueueEntriesThread();
    }

    private void startUploadQueueEntriesThread() throws CouldNotPerformException {
        try {
            GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {
                while (!queue.isEmpty()) {
                    final String sparql = queue.peek();

                    try {
                        SparqlHttp.uploadSparqlRequest(sparql, OntConfig.ONTOLOGY_DB_URL);
                        queue.poll();
                    } catch (CouldNotPerformException e) {
                        queue.poll();
                        ExceptionPrinter.printHistory("Dropped broken queue entry.", e, LOGGER, LogLevel.ERROR);
                    } catch (IOException e) {
                        LOGGER.warn("IOException: no connection...Retry...");
                        break;
                    }

                    try {
                        if (queue.isEmpty()) {
                            final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(OntologyChange.Category.UNKNOWN).build();

                            rsbInformer.activate();
                            rsbInformer.publish(ontologyChange);
                            rsbInformer.deactivate();
                            LOGGER.info("Transaction buffer is empty. All entries send to server.");
                        }
                    } catch (InterruptedException | CouldNotPerformException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }
                }
            }, 0, OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
        } catch (RejectedExecutionException | IllegalArgumentException | CouldNotPerformException e) {
            throw new CouldNotProcessException("Could not process transactionBuffer thread!", e);
        }
    }

    /**
     * Method inserts a sparql expression to the transaction buffer (queue). If the queue reaches the capacity than the first entry is removed to avoid overflow.
     *
     * @param sparql is the sparql update expression.
     */
    public static void insertData(final String sparql) {
        if (queue.size() > Integer.MAX_VALUE - 100) {
            queue.poll();
        }
        queue.offer(sparql);
    }
}
