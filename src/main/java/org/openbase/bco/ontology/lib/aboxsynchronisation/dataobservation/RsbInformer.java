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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;

/**
 * @author agatting on 09.02.17.
 */
public interface RsbInformer {

    /**
     * Method creates and activates an RSB informer.
     *
     * @param scope the RSB scope.
     * @return the rsb informer object.
     * @throws CouldNotPerformException if the rsb informer could not create.
     */
    static RSBInformer<String> createInformer(final String scope) throws CouldNotPerformException {

        try {
            final RSBInformer<String> synchronizedInformer = RSBFactoryImpl.getInstance()
                    .createSynchronizedInformer(scope, String.class);
            synchronizedInformer.activate();
            return synchronizedInformer;

        } catch (InterruptedException | CouldNotPerformException e) {
            throw new CouldNotPerformException("Could not create new RSB informer!", e);
        }
    }

    /**
     * Method starts the notification via RSB.
     *
     * @param synchronizedInformer the RSB informer object.
     * @throws CouldNotPerformException if the informer can't establish the notification process.
     */
    static void startInformer(final RSBInformer<String> synchronizedInformer) throws CouldNotPerformException {

        try {
            synchronizedInformer.publish("notification");
        } catch (InterruptedException e) {
            throw new CouldNotPerformException("Could not publish notification via RSB informer!", e);
        }
    }
}

//taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//@Override
//public void run() {
//        try {
//        synchronizedInformer.publish("notification");
//        taskFuture.cancel(true);
//        } catch (CouldNotPerformException | InterruptedException e) {
//        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
//        }
//        }
//        }, 0, 5, TimeUnit.SECONDS);