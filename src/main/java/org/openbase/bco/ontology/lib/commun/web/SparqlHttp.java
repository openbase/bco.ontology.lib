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
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.ServerService;
import org.openbase.bco.ontology.lib.utility.ThreadUtility;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

final class SparqlHttpLogger {
    static final Logger LOGGER = LoggerFactory.getLogger(SparqlHttpLogger.class);
}

/**
 * @author agatting on 12.12.16.
 */
public interface SparqlHttp {

    /**
     * Method executes a sparql update/query to the ontology server.
     *
     * @param sparql is the sparql update/request string.
     * @param url is the url of the ontology database server without suffix (server service form).
     * @throws IOException is thrown in case there is no connection to the ontology server.
     * @throws CouldNotPerformException is thrown in case the httpResponse was not successfully (e.g. wrong sparql string...).
     */
    static void uploadSparqlRequest(final String sparql, final String url) throws IOException, CouldNotPerformException {

        final String serverServiceName = ServerService.UPDATE.getName();
        final HttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(url + serverServiceName);

        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(serverServiceName, sparql));

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        final HttpResponse httpResponse = httpclient.execute(httpPost);
        checkHttpRequest(httpResponse, sparql);
    }

    /**
     * Method uploads a sparql expression via function {@link #uploadSparqlRequest(String, String)} and inserts the input sparql expression to the transactionBuffer
     * in case of IOException. The URL is set to the database path of the fuseki server.
     *
     * @param sparql is the sparql update/request string.
     * @return true, if the upload was successfully. Otherwise false and inserts sparql expression to the transactionBuffer (or prints an exception if update
     * string is bad).
     */
    static boolean uploadSparqlRequest(final String sparql) {
        try {
            SparqlHttp.uploadSparqlRequest(sparql, OntConfig.getOntologyDbUrl());
            return true;
        } catch (IOException e) {
            // could not send to server - insert sparql update expression to buffer queue
            TransactionBuffer.insertData(sparql);
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("At least one element is null or whole update string is bad! SPARQL String: " + sparql, e, SparqlHttpLogger.LOGGER, LogLevel.ERROR);
        }
        return false;
    }

    /**
     * Method executes a sparql update/query to the ontology server via retries (timeout), if the upload could not be done in the first try.
     *
     * @param sparql is the sparql update/request string.
     * @param url is the url of the ontology database server without suffix (server service form).
     * @param timeout is the timeout to limit the time. If {@code 0} the method retries permanently until there is a result (maybe blocks forever!).
     * @throws CouldNotPerformException is thrown in case the httpResponse was not successfully (e.g. wrong sparql string...).
     * @throws InterruptedException is thrown in case the application is interrupted.
     * @throws CancellationException is thrown in case the timeout was reached and the upload trial was canceled.
     */
    static void uploadSparqlRequest(final String sparql, final String url, final long timeout) throws CouldNotPerformException, InterruptedException, CancellationException {
        final Stopwatch stopwatch = new Stopwatch();

        Future<Boolean> future = GlobalCachedExecutorService.submit(() -> {
            while (true) {
                try {
                    SparqlHttp.uploadSparqlRequest(sparql, url);
                    return true;
                } catch (IOException e) {
                    stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                }
            }
        });

        try {
            ThreadUtility.setTimeoutToCallable(timeout, future);
        } catch (ExecutionException e) {
            throw new CouldNotPerformException(e);
        }
    }

    /**
     * Method returns the result of a sparql SELECT query, which is send to the ontology server.
     *
     * @param query is the SELECT query.
     * @param url is the url of the ontology database server without suffix (server service form).
     * @return the result of the SELECT query.
     * @throws IOException is thrown in case there is no connection to the ontology server.
     */
    static ResultSet sparqlQuery(final String query, final String url) throws IOException {
        try {
            final String serverServiceName = ServerService.SPARQL.getName();
            final Query queryObject = QueryFactory.create(query) ;
            final QueryExecution queryExecution = QueryExecutionFactory.sparqlService(url + serverServiceName, queryObject);

            return queryExecution.execSelect();
        } catch (QueryExceptionHTTP e) {
            throw new IOException("Connection establishment refused. Server offline?");
        }
    }

    /**
     * Method returns the result of a sparql SELECT query via retries (timeout), if the query could not be done in the first try.
     *
     * @param query is the SELECT query.
     * @param url is the url of the ontology database server without suffix (server service form).
     * @param timeout is the timeout to limit the time. If {@code 0} the method retries permanently until there is a result (maybe blocks forever!).
     * @return the result of the SELECT query.
     * @throws InterruptedException is thrown in case the application is interrupted.
     * @throws ExecutionException is thrown in case the callable thread throws an unknown exception.
     * @throws CancellationException is thrown in case the timeout was reached and the upload trial was canceled.
     */
    static ResultSet sparqlQuery(final String query, final String url, final long timeout) throws InterruptedException, ExecutionException {
        final Stopwatch stopwatch = new Stopwatch();

        Future<ResultSet> future = GlobalCachedExecutorService.submit(() -> {
            while (true) {
                try {
                    return sparqlQuery(query, url);
                } catch (IOException e) {
                    stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                }
            }
        });

        return (ResultSet) ThreadUtility.setTimeoutToCallable(timeout, future);
    }

    /**
     * Method verifies the http response code. If the http request was not successfully an exception is thrown. Otherwise void. Consider that a httpResponse
     * based on connection. That means the method do not identify an possibly IOException!
     *
     * @param httpResponse is the response of the http request.
     * @param sparql is the sparql update string, which is used for terminal information in case of bad request.
     * @throws CouldNotPerformException is thrown in case the http request was not successfully.
     */
    static void checkHttpRequest(final HttpResponse httpResponse, final String sparql) throws CouldNotPerformException {
        //TODO maybe split client and server error code...
        final int responseCode = httpResponse.getStatusLine().getStatusCode();
        final int reducedCode = Integer.parseInt(Integer.toString(responseCode).substring(0, 1));

        switch (reducedCode) {
            case 2: // request successful
                return;
            case 3: // bypass
                throw new CouldNotPerformException("Http bypass code. Client must do something... Sparql update string: " + sparql);
            case 4: // client error
                throw new CouldNotPerformException("Client error code. Possibly sparql update string is wrong: " + sparql);
            case 5: // server error
                throw new CouldNotPerformException("Server error code. Possibly server is unavailable! Or sparql update bad?! " + sparql);
            default: // unknown error
                throw new CouldNotPerformException("Unknown status code. Sparql update string: " +sparql);
        }
    }

}
