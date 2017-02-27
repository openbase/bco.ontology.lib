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
package org.openbase.bco.ontology.lib.trigger;

import org.openbase.bco.ontology.lib.config.OntConfig;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Remote;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 27.02.17.
 */
public class ServerConnectionObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnectionObserver.class);
    public static final ObservableImpl<Remote.ConnectionState> connectionStateObservable = new ObservableImpl<>();
    private Future taskFuture;

    public ServerConnectionObserver() {

        try {
            this.taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    final InetAddress inetAddress = InetAddress.getByName(OntConfig.getOntSparqlUri());
                    final boolean serverReachable = inetAddress.isReachable(500);

                    try {
                        if (serverReachable) {
                            connectionStateObservable.notifyObservers(Remote.ConnectionState.CONNECTED);
                        } else {
                            connectionStateObservable.notifyObservers(Remote.ConnectionState.DISCONNECTED);
                        }
                    } catch (MultiException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }

                } catch (IOException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    try {
                        connectionStateObservable.notifyObservers(Remote.ConnectionState.DISCONNECTED);
                    } catch (MultiException e1) {
                        ExceptionPrinter.printHistory(e1, LOGGER, LogLevel.ERROR);
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (NotAvailableException e) {

        }


    }
}
