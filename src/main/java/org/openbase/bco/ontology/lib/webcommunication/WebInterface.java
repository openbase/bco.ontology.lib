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
package org.openbase.bco.ontology.lib.webcommunication;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.OntologyManagerController;
import org.openbase.jul.exception.CouldNotProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 12.12.16.
 */
public class WebInterface {
    //CHECKSTYLE.OFF: MultipleStringLiterals
    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyManagerController.class);

//    private static final String UPDATE =
//            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
//                    + "INSERT DATA { "
//                    + "NS:bla a NS:OBSERVATION . "
//                    + "} ";

//    private static final String QUERY =
//            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
//                    + "ASK { "
//                    + "NS:o3 a NS:OBSERVATION . "
//                    + "} ";

    /**
     * WebInterface.
     */
    public WebInterface() {

        // ask query via remote SPARQL
//        final Query query = QueryFactory.create(QUERY);
//        HttpAuthenticator authenticator = new SimpleAuthenticator("admin", "admin".toCharArray());
//        QueryEngineHTTP qEngine = QueryExecutionFactory
//                .createServiceRequest("http://localhost:3030/myAppFuseki/query", query, authenticator);
//        System.out.println(qEngine.execAsk());
//
//        try (QueryExecution queryExecution = QueryExecutionFactory
//                  .sparqlService("http://localhost:3030/myAppFuseki/sparql",
//                "ASK { ?s a ?type }", authenticator)) {
//            System.out.println(queryExecution.execAsk());
//        }


//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        credsProvider.setCredentials(new AuthScope("http://localhost/myAppFuseki/update", 3030),
//          new UsernamePasswordCredentials("admin", "admin"));
//        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        Credentials unscopedCredentials = new UsernamePasswordCredentials("admin", "admin");
//        credsProvider.setCredentials(AuthScope.ANY, unscopedCredentials);
//        Credentials scopedCredentials = new UsernamePasswordCredentials("admin", "admin");
//        final String host = "http://localhost/myAppFuseki/update";
//        final int port = 3030;
//        AuthScope authscope = new AuthScope(host, port);
//        credsProvider.setCredentials(authscope, scopedCredentials);
//        HttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
//        HttpOp.setDefaultHttpClient(httpclient);

    }

    /**
     * Method processes a sparql update and returns the response status code of the http request.
     *
     * @param updateString The sparql update string.
     * @throws CouldNotProcessException CouldNotProcessException.
     * @return The status code of the http request.
     */
    public int sparqlUpdate(final String updateString) throws IOException {

        final HttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(ConfigureSystem.getOntUpdateUri());

        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("update", updateString));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            final HttpResponse httpResponse = httpclient.execute(httpPost);

            return httpResponse.getStatusLine().getStatusCode();

        } catch (IOException e) {
            throw new IOException("Could not perform sparql update!", e);
        }
    }

    /**
     * Method processes a sparql query in select form and returns a resultSet.
     *
     * @param queryString The query String.
     * @return A resultSet with potential solutions.
     * @throws CouldNotProcessException CouldNotProcessException.
     */
    public ResultSet sparqlQuerySelect(final String queryString) throws CouldNotProcessException {
        try {
            Query query = QueryFactory.create(queryString) ;
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ConfigureSystem.getOntSparqlUri(), query);
            return queryExecution.execSelect();
        } catch (Exception e) {
            throw new CouldNotProcessException("Could not get http response!", e);
        }
    }

    /**
     * Method checks a status code of a http request.
     *
     * @param statusCode The status code.
     * @return True, if success code, otherwise false.
     */
    public boolean httpRequestSuccess(final int statusCode) {

        return String.valueOf(statusCode).startsWith("2");
    }

    public void responseCodeHandling(final int responseCode) {

        final int reducedCode = Integer.parseInt(Integer.toString(responseCode).substring(0, 1));
        //TODO
        switch (reducedCode) {
            case 1: // request in process

                return;
            case 2: // request successful

                return;
            case 3: // bypass ... client should do something

                return;
            case 4: // client error

                return;
            case 5: // server error

                return;
            default:

                return; // abort
        }
    }

    //CHECKSTYLE.ON: MultipleStringLiterals
}
