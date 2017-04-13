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
package org.openbase.bco.ontology.lib.commun.web;

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
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.ServerServiceForm;
import org.openbase.bco.ontology.lib.OntologyManagerController;
import org.openbase.bco.ontology.lib.jp.JPOntologyDatabaseURL;
import org.openbase.bco.ontology.lib.jp.JPOntologyTBoxDatabaseURL;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 12.12.16.
 */
public interface SparqlUpdateWeb {

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(OntologyManagerController.class);

    /**
     * Stopwatch for retries.
     */
    Stopwatch stopwatch = new Stopwatch();

    /**
     * Method processes a sparql update (update string) to the main database of the ontology server.
     *
     * @param updateString The sparql update string.
     * @param serviceForm The service form.
     * @return {@code true} if upload to the main database was successful. Otherwise {@code false}.
     * @throws CouldNotPerformException CouldNotPerformException
     * @throws JPServiceException JPServiceException
     */
    static boolean sparqlUpdateToMainOntology(final String updateString, final ServerServiceForm serviceForm) throws CouldNotPerformException
            , JPServiceException {

        final String serverServiceForm = getServerServiceForm(serviceForm);

        final HttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(JPService.getProperty(JPOntologyDatabaseURL.class).getValue() + serverServiceForm);

        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(serverServiceForm, updateString));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            final HttpResponse httpResponse = httpclient.execute(httpPost);
            final int responseCode = httpResponse.getStatusLine().getStatusCode();

            return isHttpRequestSuccess(responseCode);
        } catch (IOException e) {
            ExceptionPrinter.printHistory("Could not perform sparql update via http communication!", e, LOGGER, LogLevel.WARN);
            return false;
        }
    }

    /**
     * Method processes a sparql update (update string) to all databases of the ontology server (main and tbox databases).
     *
     * @param updateString The sparql update string.
     * @param serviceForm The service form.
     * @return {@code true} if upload to both databases was successful. Otherwise {@code false}.
     * @throws CouldNotPerformException CouldNotPerformException is thrown if request was not successful, because of e.g. update string is broken.
     * @throws JPServiceException JPServiceException
     */
    static boolean sparqlUpdateToAllDataBases(final String updateString, final ServerServiceForm serviceForm) throws CouldNotPerformException
            , JPServiceException {

        final String serverServiceForm = getServerServiceForm(serviceForm);

        final HttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPostMain = new HttpPost(JPService.getProperty(JPOntologyDatabaseURL.class).getValue() + serverServiceForm);
//        final HttpPost httpPostTBox = new HttpPost(JPService.getProperty(JPTBoxDatabaseURL.class).getValue() + serverServiceForm);

        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(serverServiceForm, updateString));

        try {
            httpPostMain.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//            httpPostTBox.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            final HttpResponse httpResponseMain = httpclient.execute(httpPostMain);
//            final HttpResponse httpResponseTBox = httpclient.execute(httpPostTBox);

            final int codeMain = httpResponseMain.getStatusLine().getStatusCode();
//            final int codeTBox = httpResponseTBox.getStatusLine().getStatusCode();

            return isHttpRequestSuccess(codeMain);
        } catch (IOException e) {
            ExceptionPrinter.printHistory("Could not perform sparql update via http communication!", e, LOGGER, LogLevel.WARN);
            return false;
        }
    }

    /**
     * Method processes a sparql query in select form and returns a resultSet. Query goes to ontology server.
     *
     * @param queryString The query String.
     * @return A resultSet with potential solutions.
     * @throws IOException IOException
     * @throws JPServiceException JPServiceException
     */
    static ResultSet sparqlQuerySelect(final String queryString) throws IOException, JPServiceException {
        try {
            final Query query = QueryFactory.create(queryString) ;
            final QueryExecution queryExecution = QueryExecutionFactory.sparqlService(JPService.getProperty(JPOntologyDatabaseURL.class).getValue()
                    + "sparql", query);
            return queryExecution.execSelect();
        } catch (QueryExceptionHTTP e) {
            throw new IOException("Connect to " + JPService.getProperty(JPOntologyDatabaseURL.class).getValue() + "sparql"
                    + " failed. Connection establishment refused. Server offline?");
        }
    }

    static ResultSet sparqlQuerySelectViaRetry(final String queryString) throws JPServiceException, InterruptedException {
        ResultSet resultSet = null;

        while (resultSet == null) {
            try {
                final Query query = QueryFactory.create(queryString) ;
                final QueryExecution queryExecution = QueryExecutionFactory.sparqlService(JPService.getProperty(JPOntologyDatabaseURL.class).getValue()
                        + "sparql", query);
                resultSet = queryExecution.execSelect();
            } catch (QueryExceptionHTTP e) {
                //retry
                ExceptionPrinter.printHistory("Connect to " + JPService.getProperty(JPOntologyDatabaseURL.class).getValue() + "sparql"
                        + " failed. Connection establishment refused. Server offline? Retry... ", e, LOGGER, LogLevel.WARN);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
        return resultSet;
    }

    /**
     * Method verifies the http response code. There are three returned answers to plan next steps to handle the http communication situation. First
     * boolean true, if the request was successfully. Than boolean false, if the request was not successfully, because of a server error (e.g. server down).
     * The third possibility is the thrown CouldNotPerformException to signal another error (e.g. client error). In the thrown error case, the data should be
     * dropped (ontologyManager: sparql update is broken) and the developer should be analyse the reason of it...
     *
     * @param responseCode The http response code.
     * @return {@code true} if request was successfully. {@code false} if request was not successfully, cause of server error (e.g. server down).
     * @throws CouldNotPerformException CouldNotPerformException is thrown, if the request was not successful, cause of another error.
     */
    static boolean isHttpRequestSuccess(final int responseCode) throws CouldNotPerformException {

        final int reducedCode = Integer.parseInt(Integer.toString(responseCode).substring(0, 1));

        switch (reducedCode) {
            case 2: // request successful
                return true;
            case 3: // request successful
                throw new CouldNotPerformException("Http bypass code. Client must do something for successfully process of request...");
            case 4: // client error
                throw new CouldNotPerformException("Client error by sending sparql update. String maybe wrong!");
            case 5: // server error
                LOGGER.error("Response code is bad, cause of server error! Maybe no connection?");
                return false;
            default:
                throw new CouldNotPerformException("Unknown response code. Check communication!");
        }
    }

    static String getServerServiceForm(final ServerServiceForm serviceForm) {
        switch (serviceForm) {
            case DATA:
                return "data";
            case SPARQL:
                return "sparql";
            case UPDATE:
                return "update";
            default:
                return "";
        }
    }
}
