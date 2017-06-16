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
package org.openbase.bco.ontology.lib.commun.trigger;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.openbase.bco.ontology.lib.commun.monitor.ServerConnection;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.trigger.TriggerFactory;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 27.02.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class OntologyRemoteImpl implements OntologyRemote {

    @Override
    public boolean match(final String query) throws IOException {

        boolean queryResult;
        final HttpEntity httpEntity = getHttpEntity(query);
        final InputStream inputStream = httpEntity.getContent();

        try {
            final String dataStream = IOUtils.toString(inputStream, "UTF-8");

            if (dataStream.contains("true")) {
                queryResult =  true;
            } else if (dataStream.contains("false")) {
                queryResult = false;
            } else {
                throw new IOException("Could not get query result, cause inputStream of http content has no valid content.");
            }
        } finally {
            inputStream.close();
        }
        return queryResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConnectionStateObserver(final Observer<ConnectionState> observer) {
        ServerConnection.SERVER_STATE_OBSERVABLE.addObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeConnectionStateObserver(final Observer<ConnectionState> observer) {
        ServerConnection.SERVER_STATE_OBSERVABLE.removeObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOntologyObserver(final Observer<OntologyChange> observer) {
        TriggerFactory.ONTOLOGY_CHANGE_OBSERVABLE.addObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeOntologyObserver(final Observer<OntologyChange> observer) {
        TriggerFactory.ONTOLOGY_CHANGE_OBSERVABLE.removeObserver(observer);
    }

    private HttpEntity getHttpEntity(final String query) throws IOException {

        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("query", query));

        final HttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpGet = new HttpGet(OntConfig.getOntologyDbUrl() + "sparql?" + URLEncodedUtils.format(params, "UTF-8"));

        final HttpEntity httpEntity = httpclient.execute(httpGet).getEntity();

        if (httpEntity != null) {
            return httpEntity;
        } else {
            throw new IOException("Could not get query result, cause http entity is null.");
        }
    }
}
