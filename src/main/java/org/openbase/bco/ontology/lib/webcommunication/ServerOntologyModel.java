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
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 * @author agatting on 19.01.17.
 */
public interface ServerOntologyModel {
    /**
     * Method returns the full ontology model from the ontology server.
     *
     * @return The ontology model (ABox & TBox).
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static OntModel getOntologyModel() throws CouldNotPerformException {

        try {
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(ConfigureSystem
                    .SERVER_ONTOLOGY_URI);
            final Model model = datasetAccessor.getModel();

            return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not get model from ontology server.", e);
        }
    }

    /**
     * Method returns the TBox ontology model from the ontology server.
     *
     * @return The ontology model (TBox).
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static OntModel getOntologyModelTBox() throws CouldNotPerformException {
        try {
            // access to fuseki server and download ontology model
            final DatasetAccessor datasetAccessor = DatasetAccessorFactory
                    .createHTTP(ConfigureSystem.SERVER_ONTOLOGY_TBOX_URI);
            final Model model = datasetAccessor.getModel();

            return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not get model from ontology server.", e);
        }
    }
}
