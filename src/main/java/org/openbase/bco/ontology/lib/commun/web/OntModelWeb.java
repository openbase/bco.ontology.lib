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
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.jp.JPOntologyDatabaseURL;
import org.openbase.bco.ontology.lib.jp.JPTBoxDatabaseURL;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author agatting on 19.01.17.
 */
public interface OntModelWeb {

    //TODO extend retry methods with timeout function?

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(OntModelWeb.class);

    /**
     * Stopwatch for retries.
     */
    Stopwatch stopwatch = new Stopwatch();

    /**
     * Method returns the ontology model from the ontology server. Consider correct uri.
     *
     * @param uri The uri to the ontology server. Consider the different uri's to the services and dataSets!
     * @return The ontology model from the server. An empty ontModel, if the server model is defect (Avoid nullPointer).
     * @throws IOException Exception is thrown, if the ontModel could not uploaded (cause e.g. no connection - server offline).
     */
    static OntModel getOntologyModel(final String uri) throws IOException {

        try {
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(uri + "data");
            final Model model = datasetAccessor.getModel();

            if (model == null) {
                return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            } else {
                return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
            }
        } catch (Exception e) {
            throw new IOException("Could not get model from ontology server!", e);
        }
    }

    /**
     * Method tries to download the tbox from the ontology server. If there is no connection, the method retries in a specific interval with the download. If
     * the ontModel from the server is empty, the ontModel from the dependency is taken. Consider, if there is no connection, the method blocks.
     *
     * @return The TBox ontModel from the server or from the dependency, if the server ontModel is empty. If the file is not found, an empty ontModel is
     * returned.
     * @throws InterruptedException Exception is thrown, if the application is interrupted.
     * @throws JPServiceException Exception is thrown, if the JPService is not available.
     */
    static OntModel getTBoxModelViaRetry() throws InterruptedException, JPServiceException {
        OntModel ontModel = null;

        while (ontModel == null) {
            try {
                ontModel = OntModelWeb.getOntologyModel(JPService.getProperty(JPTBoxDatabaseURL.class).getValue());

                if (ontModel.isEmpty()) {
                    ontModel = OntologyToolkit.loadOntModelFromFile(null, null);
                }
            } catch (IOException e) {
                //retry
                ExceptionPrinter.printHistory("No connection to get tbox ontModel from server. Retry...", e, LOGGER, LogLevel.WARN);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            } catch (IllegalArgumentException e ){
                ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            }
        }
        return ontModel;
    }

    /**
     * Method adds (NOT replaces) the ontology model to the server databases. Normally both databases are extended with the same ontModel to ensure consistency.
     *
     * @param ontModel The ontModel, which should be uploaded to the ontology databases.
     * @param mainUri The main uri to the main ontology database.
     * @param tboxUri The tbox uri to the tbox ontology database.
     * @throws IOException Exception is thrown, if the ontModel could not uploaded (cause e.g. no connection - server offline).
     * @throws IllegalArgumentException Exception is thrown, if min. one uri is null.
     */
    static void addOntModel(final OntModel ontModel, final String mainUri, final String tboxUri) throws IOException, IllegalArgumentException {

        if (ontModel == null) {
            throw new IllegalArgumentException("Could not add model to ontology server, cause main ontModel is null!");
        } else if (mainUri == null) {
            throw new IllegalArgumentException("Could not add model to ontology server, cause main uri is null!");
        } else if (tboxUri == null) {
            throw new IllegalArgumentException("Could not add model to ontology server, cause tbox uri is null!");
        }
        try {
            DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(mainUri + "data");
            datasetAccessor.add(ontModel);

            datasetAccessor = DatasetAccessorFactory.createHTTP(tboxUri + "data");
            datasetAccessor.add(ontModel);

        } catch (Exception e) {
            throw new IOException("Could not add model to ontology server!", e);
        }
    }

    /**
     * Method uploads the input ontModel to all databases (2). If there is no connection, the method retries in a specific interval with the upload.
     * Consider, if there is no connection, the method blocks.
     *
     * @param ontModel The ontModel, which should be uploaded to all databases (2).
     * @throws InterruptedException Exception is thrown, if the application is interrupted.
     * @throws JPServiceException Exception is thrown, if the JPService is not available.
     */
    static void addOntModelViaRetry(final OntModel ontModel) throws InterruptedException, JPServiceException {
        boolean isUploaded = false;

        while (!isUploaded) {
            try {
                OntModelWeb.addOntModel(ontModel, JPService.getProperty(JPOntologyDatabaseURL.class).getValue()
                        , JPService.getProperty(JPTBoxDatabaseURL.class).getValue());
                isUploaded = true;
            } catch (IOException e) {
                //retry
                ExceptionPrinter.printHistory("No connection to upload ontModel to databases. Retry...", e, LOGGER, LogLevel.WARN);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
    }

    /**
     * Method verifies, if the server contains an ontology model.
     *
     * @param uri The uri to the server and dataSet.
     * @return {@code True} if the server contains an ontology model based on the uri parameter. Otherwise {@code false}.
     * @throws IOException Exception is thrown, if the ontModel could not uploaded (cause e.g. no connection - server offline).
     */
    static boolean isOntModelOnServer(final String uri) throws IOException {

        try {
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(uri);
            final Model model = datasetAccessor.getModel();

            return !model.isEmpty();
        } catch (Exception e) {
            throw new IOException("Could not verify, if server has an ontology. Maybe no http connection or wrong uri?", e);
        }
    }
}
