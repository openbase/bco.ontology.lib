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
package org.openbase.bco.ontology.lib.trigger.webcommun;

import org.openbase.bco.ontology.lib.config.OntConfig;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 27.02.17.
 */
public class ServerConnectionObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnectionObserver.class);
    public static final ObservableImpl<ConnectionState> connectionStateObservable = new ObservableImpl<>();
    private static final int timeOutMSec = 500;
    private ConnectionState connectionState;

    public ServerConnectionObserver() throws NotAvailableException {

        this.connectionState = ConnectionState.UNKNOWN;

        createConnectionObserver();
    }

    private void createConnectionObserver() throws NotAvailableException {

        GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                final InetAddress inetAddress = InetAddress.getByName(OntConfig.getOntSparqlUri());
                final boolean serverReachable = inetAddress.isReachable(timeOutMSec);

                try {
                    if (serverReachable && (connectionState.equals(ConnectionState.DISCONNECTED) || connectionState.equals(ConnectionState.UNKNOWN))) {
                        connectionStateObservable.notifyObservers(ConnectionState.CONNECTED);
                        connectionState = ConnectionState.CONNECTED;
                    } else if (!serverReachable && (connectionState.equals(ConnectionState.CONNECTED) || connectionState.equals(ConnectionState.UNKNOWN))) {
                        connectionStateObservable.notifyObservers(ConnectionState.DISCONNECTED);
                        connectionState = ConnectionState.DISCONNECTED;
                    }
                } catch (MultiException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }

            } catch (IOException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                try {
                    connectionStateObservable.notifyObservers(ConnectionState.DISCONNECTED);
                } catch (MultiException e1) {
                    ExceptionPrinter.printHistory(e1, LOGGER, LogLevel.ERROR);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
