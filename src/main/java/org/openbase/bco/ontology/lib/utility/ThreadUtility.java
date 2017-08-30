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
package org.openbase.bco.ontology.lib.utility;

import org.openbase.jul.schedule.Timeout;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 * @author agatting on 22.05.17.
 */
public interface ThreadUtility {

    /**
     * Method limits the calculation time of the callable future thread by a timeout.
     *
     * @param timeout is the limited time for the thread. Can be set to {@code 0} if timeout should be ignore. Consider the method blocks in this time.
     * @param future is the callable thread.
     * @return the solution/object of the callable thread.
     * @throws InterruptedException is thrown in case the threads are interrupted.
     * @throws ExecutionException is thrown in case the given thread throws an exception.
     * @throws CancellationException is thrown in case the timeout is reached.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown if the timeout could not be attached to the future task.
     */
    static Object setTimeoutToCallable(final long timeout, final Future<?> future) throws InterruptedException, ExecutionException, CancellationException, CouldNotPerformException {
        if (timeout != 0) {
            Timeout timeoutThread = new Timeout(timeout) {
                @Override
                public void expired() throws InterruptedException {
                    if (!future.isDone()) {
                        future.cancel(true);
                    }
                }
            };

            try {
                timeoutThread.start();
                Object object = future.get();

                if (timeoutThread.isActive()) {
                    timeoutThread.cancel();
                }
                return object;
            } catch (ExecutionException ex) {
                timeoutThread.cancel();
                throw new ExecutionException(ex);
            }
        } else {
            return future.get();
        }
    }
}
