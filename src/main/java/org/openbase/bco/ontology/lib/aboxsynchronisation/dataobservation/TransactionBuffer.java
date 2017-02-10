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
package org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation;

import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.webcommunication.WebInterface;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author agatting on 17.01.17.
 */
public class TransactionBuffer {

    //TODO handling, if one element is "defect" and can't be send to server/accepted from server

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionBuffer.class);
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    public TransactionBuffer() {

        final WebInterface webInterface = new WebInterface();
        final Stopwatch stopwatch = new Stopwatch();

        GlobalCachedExecutorService.submit(() -> {

            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    final String sparqlUpdateExpr = queue.element();

                    final int httpResponseCode = webInterface.sparqlUpdate(sparqlUpdateExpr);
                    final boolean httpSuccess = webInterface.httpRequestSuccess(httpResponseCode);

                    if (httpSuccess) {
                        queue.poll();
                    } else {
                        throw new CouldNotProcessException("Could not upload sparql expression!");
                    }

                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);

                    stopwatch.waitForStop(ConfigureSystem.waitTimeMilliSeconds);
                    //TODO
                } catch (NoSuchElementException e) {
                    // queue is empty. wait ...
                    stopwatch.waitForStop(ConfigureSystem.waitTimeMilliSeconds);
                }
            }
        });
    }

    /**
     * Method inserts a sparql update expression to the queue.
     *
     * @param sparqlUpdateExpr The sparql update expression string.
     * @throws CouldNotProcessException CouldNotProcessException, if the expression could not insert into the queue.
     */
    public void insertData(final String sparqlUpdateExpr) throws CouldNotProcessException {
        boolean isElementInQueue = queue.offer(sparqlUpdateExpr);

        if (!isElementInQueue) {
            throw new CouldNotProcessException("Could not add element to queue!");
        }
    }
}
