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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.utility.ThreadUtility;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 * @author agatting on 19.01.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public interface OntModelHttp {

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(OntModelHttp.class);

    /**
     * Method returns the ontology model from the ontology server. Consider correct url. Consider the possible big size of the ontology (and download time).
     *
     * @param url is the url of the ontology database server without suffix (server service form).
     * @return the ontModel from the server.
     * @throws IOException is thrown in case the ontModel could not downloaded (because e.g. no connection, server offline, ...).
     * @throws NotAvailableException is thrown in case the server does not contain an ontModel.
     */
    static OntModel downloadModelFromServer(final String url) throws IOException, NotAvailableException {
        try {
            Model model = DatasetAccessorFactory.createHTTP(url + OntConfig.ServerService.DATA.getName()).getModel();

            if (model.isEmpty()) {
                throw new NotAvailableException("The server contains no ontology!");
            }

            return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        } catch (NullPointerException | NotAvailableException ex) {
            throw new NotAvailableException("The server contains no ontology!");
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Method returns the ontology from the server. If there is no connection, the method retries in an interval the download until the given timeout is
     * reached (or if null permanent). Consider, in this time the method blocks.
     *
     * @param url is the url of the ontology database server without suffix (server service form).
     * @param timeout is the timeout to limit the time. If {@code 0} the method retries permanently until there is a result (maybe blocks forever!).
     * @return the ontModel from the server.
     * @throws InterruptedException is thrown in case the application is interrupted.
     * @throws NotAvailableException is thrown in case the server does not keep an ontModel.
     * @throws CancellationException is thrown in case the timeout was reached and the download trial was canceled.
     */
    static OntModel downloadModelFromServer(final String url, final long timeout) throws InterruptedException, NotAvailableException, CancellationException {
        Stopwatch stopwatch = new Stopwatch();

        Future<OntModel> future = GlobalCachedExecutorService.submit(() -> {
            while (true) {
                try {
                    return OntModelHttp.downloadModelFromServer(url);
                } catch (IOException ex) {
                    //retry
                    ExceptionPrinter.printHistory("No connection to ontology server. Retry...", ex, LOGGER, LogLevel.WARN);
                    stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                }
            }
        });

        try {
            return (OntModel) ThreadUtility.setTimeoutToCallable(timeout, future);
        } catch (ExecutionException | CouldNotPerformException ex) {
            throw new NotAvailableException(ex);
        }
    }

    /**
     * Method adds (NOT replaces) the ontModel to the server.
     *
     * @param ontModel is the ontModel, which should be added to the server.
     * @param url is the url of the ontology database server without suffix (server service form).
     * @throws IOException is thrown in case the the ontModel could not be uploaded (because e.g. no connection, server offline, ...).
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static void addModelToServer(final OntModel ontModel, final String url) throws IOException, NotAvailableException {

        if (ontModel == null) {
            assert false;
            throw new NotAvailableException("OntModel is null.");
        }

        try {
            DatasetAccessorFactory.createHTTP(url + OntConfig.ServerService.DATA.getName()).add(ontModel);
        } catch (Exception ex) {
            throw new IOException("Could not add model to ontology server!", ex);
        }
    }

    /**
     * Method adds (NOT replaces) the ontModel to the server. If there is no connection, the method retries in an interval the upload until the given timeout
     * is reached. Consider, in this time the method blocks.
     *
     * @param ontModel is the ontModel, which should be added to the server.
     * @param url is the url of the ontology database server without suffix (server service form).
     * @param timeout is the timeout to limit the time. If {@code 0} the method retries permanently until there is a result (maybe blocks forever!).
     * @throws InterruptedException is thrown in case the application is interrupted.
     * @throws NotAvailableException is thrown in case the ontModel is null.
     * @throws CancellationException is thrown in case the timeout was reached and the upload trial was canceled.
     */
    static void addModelToServer(final OntModel ontModel, final String url, final long timeout) throws InterruptedException, NotAvailableException, CancellationException {
        Stopwatch stopwatch = new Stopwatch();

        Future<Boolean> future = GlobalCachedExecutorService.submit(() -> {
            while (true) {
                try {
                    OntModelHttp.addModelToServer(ontModel, url);
                    return true;
                } catch (IOException ex) {
                    //retry
                    ExceptionPrinter.printHistory("No connection to upload ontModel to ontology server. Retry...", ex, LOGGER, LogLevel.WARN);
                    stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                }
            }
        });

        try {
            ThreadUtility.setTimeoutToCallable(timeout, future);
        } catch (ExecutionException | CouldNotPerformException ex) {
            throw new NotAvailableException(ex);
        }
    }

}
