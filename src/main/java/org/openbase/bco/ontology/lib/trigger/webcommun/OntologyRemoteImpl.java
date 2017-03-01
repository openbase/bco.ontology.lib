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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.openbase.bco.ontology.lib.config.OntologyChange.Category;
import org.openbase.bco.ontology.lib.config.OntConfig;
import org.openbase.bco.ontology.lib.trigger.TriggerFactory;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author agatting on 27.02.17.
 */
public class OntologyRemoteImpl implements OntologyRemote {

    private static final String QUERY =
        "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "ASK { "
            + "?x a NS:Device . "
            + "} ";

    @Override
    public boolean match(String query) throws IOException {

        boolean queryResult;

        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("query", query));

        final HttpClient httpclient = HttpClients.createDefault();
        final HttpGet httpGet = new HttpGet(OntConfig.getOntSparqlUri() + "?" + URLEncodedUtils.format(params, "UTF-8"));

        final HttpResponse httpResponse = httpclient.execute(httpGet);
        final HttpEntity httpEntity = httpResponse.getEntity();

        if (httpEntity != null) {
            final InputStream inputStream = httpEntity.getContent();
            try {
                String dataStream = IOUtils.toString(inputStream, "UTF-8");
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
        throw new IOException("Could not get query result, cause http entity is null.");
    }

    @Override
    public void addConnectionStateObserver(Observer<Remote.ConnectionState> observer) {
        ServerConnection.connectionStateObservable.addObserver(observer);
    }

    @Override
    public void removeConnectionStateObserver(Observer<Remote.ConnectionState> observer) {
        ServerConnection.connectionStateObservable.removeObserver(observer);
    }

    @Override
    public void addOntologyObserver(Observer<Collection<Category>> observer) {
        TriggerFactory.changeCategoryObservable.addObserver(observer);
    }

    @Override
    public void removeOntologyObserver(Observer<Collection<Category>> observer) {
        TriggerFactory.changeCategoryObservable.removeObserver(observer);
    }
}
