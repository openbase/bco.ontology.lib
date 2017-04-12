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
package org.openbase.bco.ontology.lib.commun.monitor;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.openbase.bco.ontology.lib.jp.JPOntologyPingURL;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
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
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 27.02.17.
 */
public class ServerConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnection.class);
    public static final ObservableImpl<ConnectionState> connectionStateObservable = new ObservableImpl<>();
    private ConnectionState lastConnectionState;

    public ServerConnection() throws NotAvailableException {

        this.lastConnectionState = ConnectionState.DISCONNECTED;

        createConnectionObserver();
    }

    private void createConnectionObserver() throws NotAvailableException {

        GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {
            boolean isReachable;

            try {
                final HttpClient httpclient = HttpClients.createDefault();
                final HttpGet httpGet = new HttpGet(JPService.getProperty(JPOntologyPingURL.class).getValue());
                final HttpResponse httpResponse = httpclient.execute(httpGet);
                // get response code and take the first number only
                final int responseCodeShort = Integer.parseInt(Integer.toString(httpResponse.getStatusLine().getStatusCode()).substring(0, 1));

                isReachable = responseCodeShort == 2;
            } catch (IOException | JPNotAvailableException e) {
                isReachable = false;
            }

            try {
                if (isReachable && (lastConnectionState.equals(ConnectionState.DISCONNECTED))) {
                    connectionStateObservable.notifyObservers(ConnectionState.CONNECTED);
                    lastConnectionState = ConnectionState.CONNECTED;
                } else if (!isReachable && (lastConnectionState.equals(ConnectionState.CONNECTED))) {
                    connectionStateObservable.notifyObservers(ConnectionState.DISCONNECTED);
                    lastConnectionState = ConnectionState.DISCONNECTED;
                }
            } catch (MultiException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
