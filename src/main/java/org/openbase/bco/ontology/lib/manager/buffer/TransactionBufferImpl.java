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

import org.openbase.bco.ontology.lib.commun.rsb.RsbCommunication;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.jp.JPOntologyDatabaseURL;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 17.01.17.
 */
public class TransactionBufferImpl implements TransactionBuffer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionBufferImpl.class);
    private final Queue<String> queue;
    private final OntologyChange.Category category;
    private Future future;

    public TransactionBufferImpl() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.category = OntologyChange.Category.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAndStartQueue(final RSBInformer<OntologyChange> synchronizedInformer) throws CouldNotPerformException {

        try {
            future = GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {

                while (!queue.isEmpty()) {
                    final String updateExpression = queue.peek();

                    try {
                        SparqlHttp.uploadSparqlRequest(updateExpression, JPService.getProperty(JPOntologyDatabaseURL.class).getValue());
                        queue.poll();
                    } catch (JPServiceException e) {
                        future.cancel(true);
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    } catch (CouldNotPerformException e) {
                        queue.poll();
                        ExceptionPrinter.printHistory("Dropped broken queue entry.", e, LOGGER, LogLevel.ERROR);
                    } catch (IOException e) {
                        LOGGER.warn("IOException: no connection...Retry...");
                        break;
                    }

                    if (queue.isEmpty() && synchronizedInformer != null) {
                        final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(category).build();

                        RsbCommunication.startNotification(synchronizedInformer, ontologyChange);
                    }
                }
            }, 0, 2, TimeUnit.SECONDS);

        } catch (RejectedExecutionException | IllegalArgumentException | CouldNotPerformException e) {
            throw new CouldNotProcessException("Could not process transactionBuffer thread!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertData(final String updateExpression) throws CouldNotProcessException {
        boolean isElementInQueue = queue.offer(updateExpression);

        if (!isElementInQueue) {
            throw new CouldNotProcessException("Could not add element to queue!");
        }
        //TODO check size...
    }

}
