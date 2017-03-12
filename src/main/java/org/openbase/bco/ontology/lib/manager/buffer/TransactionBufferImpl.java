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

import javafx.util.Pair;
import org.openbase.bco.ontology.lib.commun.rsb.RsbCommunication;
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

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
    private final Queue<Pair<String, Boolean>> queue;
    private final OntologyChange.Category category;
    private final Stopwatch stopwatch;
    private Future future;

    public TransactionBufferImpl() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.category = OntologyChange.Category.UNKNOWN;
        this.stopwatch = new Stopwatch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAndStartQueue(final RSBInformer<OntologyChange> synchronizedInformer) throws CouldNotPerformException {

        try {
            future = GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {

                while (!queue.isEmpty()) {
                    final Pair<String, Boolean> pair = queue.peek();
                    final String sparqlUpdateExpr = pair.getKey();
                    final boolean isHttpSuccess;

                    try {
                        if (pair.getValue()) {
                            // send to all databases
                            isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToAllDataBases(sparqlUpdateExpr, OntConfig.ServerServiceForm.UPDATE);
                        } else {
                            // send to main database only
                            isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToMainOntology(sparqlUpdateExpr, OntConfig.ServerServiceForm.UPDATE);
                        }

                        if (isHttpSuccess) {
                            queue.poll();
                        } else {
                            stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                        }
                    } catch (InterruptedException | JPServiceException e) {
                        future.cancel(true);
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    } catch (CouldNotPerformException e) {
                        queue.poll();
                        ExceptionPrinter.printHistory("Dropped broken queue entry. Server could not perform, cause of client error... wrong update?" +
                                " Queue entry is: " + sparqlUpdateExpr, e, LOGGER, LogLevel.ERROR);
                    }

                    if (queue.isEmpty() && synchronizedInformer != null) {
                        final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(category).build();

                        RsbCommunication.startNotification(synchronizedInformer, ontologyChange);
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);

        } catch (RejectedExecutionException | IllegalArgumentException | CouldNotPerformException e) {
            throw new CouldNotProcessException("Could not process transactionBuffer thread!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertData(final Pair<String, Boolean> stringBooleanPair) throws CouldNotProcessException {
        boolean isElementInQueue = queue.offer(stringBooleanPair);

        if (!isElementInQueue) {
            throw new CouldNotProcessException("Could not add element to queue!");
        }
        //TODO check size...
    }

}
