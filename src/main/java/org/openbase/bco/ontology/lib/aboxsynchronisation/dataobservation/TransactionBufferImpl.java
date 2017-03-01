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
package org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation;

import org.openbase.bco.ontology.lib.config.OntologyChange;
import org.openbase.bco.ontology.lib.commun.web.WebInterface;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 17.01.17.
 */
public class TransactionBufferImpl implements TransactionBuffer {

    //TODO handling, if one element is "defect" and can't be send to server/accepted from server

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionBufferImpl.class);
    private Future taskFuture;
    private boolean isTaskFutureInit = true;
    private final WebInterface webInterface;
    private final Queue<String> queue;

    public TransactionBufferImpl() {
        this.webInterface = new WebInterface();
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAndStartQueue(final RSBInformer<String> synchronizedInformer)
            throws CouldNotPerformException {

        try {
            GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

                while (queue.peek() != null) {
                    final String sparqlUpdateExpr = queue.peek();

                    try {
                        final int httpResponseCode = webInterface.sparqlUpdate(sparqlUpdateExpr);
                        final boolean httpSuccess = webInterface.httpRequestSuccess(httpResponseCode);

                        if (httpSuccess) {
                            queue.poll();

                            if (isTaskFutureInit) {
                                isTaskFutureInit = false;
                                setRSBInformerThread(synchronizedInformer);
                            } else if (!isTaskFutureInit && taskFuture.isCancelled()) {
                                setRSBInformerThread(synchronizedInformer);
                            }
                        } else {
                            throw new CouldNotProcessException("Could not upload sparql expression" +
                                    ", cause response code is bad!");
                        }

                    } catch (IOException e) {
                        throw new CouldNotProcessException("Could not upload sparql expression!", e);
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
    public void createAndStartQueue() throws CouldNotPerformException {

        try {
            GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

                while (queue.peek() != null) {
                    final String sparqlUpdateExpr = queue.peek();

                    try {
                        final int httpResponseCode = webInterface.sparqlUpdate(sparqlUpdateExpr);
                        final boolean httpSuccess = webInterface.httpRequestSuccess(httpResponseCode);

                        if (!httpSuccess) {
                            queue.poll();
                            LOGGER.warn("Could not upload queue entry, because response code is bad." +
                                    " Dropped to avoid endless loop.");
                        }

                    } catch (IOException e) {
                        throw new CouldNotProcessException("Could not upload sparql expression!", e);
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
    public void insertData(final String sparqlUpdateExpr) throws CouldNotProcessException {
        boolean isElementInQueue = queue.offer(sparqlUpdateExpr);

        if (!isElementInQueue) {
            throw new CouldNotProcessException("Could not add element to queue!");
        }
        //TODO check size...
    }

    private void setRSBInformerThread(final RSBInformer<String> synchronizedInformer) {

        final List<OntologyChange.Category> changeCategories = new ArrayList<>();
        changeCategories.add(OntologyChange.Category.UNKNOWN);

        try {
            taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

                try {
                    synchronizedInformer.publish("UNIT"); //TODO
                    taskFuture.cancel(true);
                } catch (CouldNotPerformException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, 0, 5, TimeUnit.SECONDS);
        } catch (NotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

}
