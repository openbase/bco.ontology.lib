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

import org.openbase.bco.ontology.lib.config.CategoryConfig.ChangeCategory;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;

import java.util.Collection;

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
    static RSBInformer<Collection<ChangeCategory>> createInformer(final String scope) throws CouldNotPerformException {

        try {
            final RSBInformer<Collection<ChangeCategory>> synchronizedInformer = RSBFactoryImpl.getInstance()
                    .createSynchronizedInformer(scope, (Class<Collection<ChangeCategory>>) (Class) Collection.class); //TODO check cast
            synchronizedInformer.activate();
            return synchronizedInformer;

        } catch (InterruptedException e) {
            throw new CouldNotPerformException("Could not create new RSB informer!", e);
        }
    }

    /**
     * Method starts the notification via RSB.
     *
     * @param synchronizedInformer the RSB informer object.
     * @return {@code true} if publish process is successful.
     */
    static boolean startInformerNotification(final RSBInformer<Collection<ChangeCategory>> synchronizedInformer
            , final Collection<ChangeCategory> changeCategory) {

        try {
            synchronizedInformer.publish(changeCategory);
            return true;
        } catch (CouldNotPerformException | InterruptedException e) {
            return false;
        }
    }

}
