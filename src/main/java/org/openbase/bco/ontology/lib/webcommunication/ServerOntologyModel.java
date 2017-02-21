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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.openbase.jul.exception.CouldNotPerformException;

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
    static OntModel getOntologyModelFromServer(final String uri) throws CouldNotPerformException {

        try {
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(uri);
            final Model model = datasetAccessor.getModel();

            return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not get model from ontology server!", e);
        }
    }

    /**
     * Method puts the ontology model to the server. Consider correct uri!
     *
     * @param ontModel The ontology model.
     * @param uri The uri to the ontology server. Consider the different uri's to the services and dataSets!
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static void addOntologyModel(final OntModel ontModel, final String uri) throws CouldNotPerformException {

        try {
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(uri);
            datasetAccessor.add(ontModel);
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not add model to ontology server!", e);
        }
    }

    /**
     * Method verifies, if the server contains an ontology model.
     *
     * @param uri The uri to the server and dataSet.
     * @return {@code True} if the server contains an ontology model based on the uri parameter. Otherwise
     * {@code false}.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static boolean isOntModelOnServer(final String uri) throws CouldNotPerformException {

        try {
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(uri);
            final Model model = datasetAccessor.getModel();

            return !model.isEmpty();
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not verify, if server has an ontology. " +
                    "Maybe no http connection or wrong uri?", e);
        }
    }
}
