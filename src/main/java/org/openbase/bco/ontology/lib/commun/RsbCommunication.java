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
package org.openbase.bco.ontology.lib.commun;

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
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

/**
 * @author agatting on 09.02.17.
 */
public class RsbCommunication {

    private static final Logger LOGGER = LoggerFactory.getLogger(RsbCommunication.class);

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(OntologyChange.newBuilder().build()));
    }

    /**
     * Method creates and activates an RSB informer of type string.
     *
     * @param scope The rsb scope.
     * @return The activated rsb informer.
     * @throws InterruptedException InterruptedException
     * @throws CouldNotPerformException CouldNotPerformException
     */
    public static RSBInformer<OntologyChange> createRsbInformer(final String scope) throws InterruptedException, CouldNotPerformException {

        final RSBInformer<OntologyChange> synchronizedInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(scope, OntologyChange.class);
        synchronizedInformer.activate();

        return synchronizedInformer;
    }

    /**
     * Method creates and activates a rsb listener. Each received message of type OntologyChange is taken by the Observable and notified the registered
     * observer.
     *
     * @param scope The rsb scope.
     * @param changeCategoryObservable The Observable, which notifies the registered observer with the incoming OntologyChange data.
     * @throws InterruptedException InterruptedException
     * @throws CouldNotPerformException CouldNotPerformException
     */
    public static void startRsbListener(final String scope, final ObservableImpl<OntologyChange> changeCategoryObservable)
            throws CouldNotPerformException, InterruptedException {

        final RSBListener rsbListener = RSBFactoryImpl.getInstance().createSynchronizedListener(scope);

        rsbListener.activate();
        rsbListener.addHandler(event -> {
            try {
                changeCategoryObservable.notifyObservers((OntologyChange) event.getData());
            } catch (MultiException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }
        }, false);
    }
}
