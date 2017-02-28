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
package org.openbase.bco.ontology.lib.commun.rsb;

import org.openbase.bco.ontology.lib.config.CategoryConfig.ChangeCategory;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.jul.pattern.ObservableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author agatting on 09.02.17.
 */
public interface RsbCommunication {

    Logger LOGGER = LoggerFactory.getLogger(RsbCommunication.class); // make private in java 1.9

    /**
     * Method creates and activates an RSB informer of type string.
     *
     * @param scope The rsb scope.
     * @return The activated rsb informer.
     * @throws InterruptedException InterruptedException
     * @throws CouldNotPerformException CouldNotPerformException
     */
    static RSBInformer<String> createInformer(final String scope) throws InterruptedException, CouldNotPerformException {

        final RSBInformer<String> synchronizedInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(scope, String.class);
        synchronizedInformer.activate();

        return synchronizedInformer;
    }

    /**
     * Method creates and activates a rsb listener. Each received message triggers a notification to all registered observers of the changeCategoryObservable
     * with the received message (changeCategory).
     *
     * @param scope The rsb scope.
     * @throws InterruptedException InterruptedException
     * @throws CouldNotPerformException CouldNotPerformException
     */
    static void activateRsbListener(final String scope, final ObservableImpl<Collection<ChangeCategory>> changeCategoryObservable)
            throws InterruptedException, CouldNotPerformException {

        final RSBListener rsbListener = RSBFactoryImpl.getInstance().createSynchronizedListener(scope);

        rsbListener.activate();
        rsbListener.addHandler(event -> {
            try {
                final Collection<ChangeCategory> changeCategories = new ArrayList<>();
                final ChangeCategory changeCategory = ChangeCategory.valueOf(((String) event.getData()).toUpperCase());

                changeCategories.add(changeCategory);
                changeCategoryObservable.notifyObservers(changeCategories);
            } catch (MultiException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            } catch (IllegalArgumentException e) {
                // no valid changeCategory -> no change. ignore
            }
        }, false);
    }

    /**
     * Method creates and activates a rsb listener, which is returned.
     *
     * @param scope The rsb scope.
     * @return The activated rsb listener.
     * @throws InterruptedException InterruptedException
     * @throws CouldNotPerformException CouldNotPerformException
     */
    static RSBListener createRsbListener(final String scope) throws InterruptedException, CouldNotPerformException {

        final RSBListener rsbListener = RSBFactoryImpl.getInstance().createSynchronizedListener(scope);
        rsbListener.activate();

        return rsbListener;
    }
}
