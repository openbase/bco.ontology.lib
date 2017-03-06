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
import org.openbase.jul.exception.CouldNotPerformException;

import java.io.IOException;

/**
 * @author agatting on 19.01.17.
 */
public interface ServerOntologyModel {

    /**
     * Method returns the ontology model from the ontology server. Consider correct uri.
     *
     * @param uri The uri to the ontology server. Consider the different uri's to the services and dataSets!
     * @return The ontology model or null, if ontology model not available or rather empty.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static OntModel getOntologyModelFromServer(final String uri) throws IOException {

        try {
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(uri);
            final Model model = datasetAccessor.getModel();

            return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        } catch (Exception e) {
            throw new IOException("Could not get model from ontology server!", e);
        }
    }

    /**
     * Method adds (NOT replaces) the ontology model to the server databases. Normally both databases are extended with the same ontModel to ensure consistency.
     *
     * @param ontModel The ontModel, which should be uploaded to the ontology databases.
     * @param mainUri The main uri to the main ontology database.
     * @param tboxUri The tbox uri to the tbox ontology database.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static void addOntologyModel(final OntModel ontModel, final String mainUri, final String tboxUri) throws CouldNotPerformException {

        try {
            if (ontModel == null) {
                throw new CouldNotPerformException("Cause main ontModel is null!");
            } else if (mainUri == null) {
                throw new CouldNotPerformException("Cause main uri is null!");
            } else if (tboxUri == null){
                throw new CouldNotPerformException("Cause tbox uri is null!");
            }

            DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(mainUri);
            datasetAccessor.add(ontModel);

            datasetAccessor = DatasetAccessorFactory.createHTTP(tboxUri);
            datasetAccessor.add(ontModel);

        } catch (Exception e) {
            throw new CouldNotPerformException("Could not add model to ontology server!", e);
        }
    }

    /**
     * Method verifies, if the server contains an ontology model.
     *
     * @param uri The uri to the server and dataSet.
     * @return {@code True} if the server contains an ontology model based on the uri parameter. Otherwise {@code false}.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static boolean isOntModelOnServer(final String uri) throws CouldNotPerformException {

        try {
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(uri);
            final Model model = datasetAccessor.getModel();

            return !model.isEmpty();
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not verify, if server has an ontology. Maybe no http connection or wrong uri?", e);
        }
    }
}
