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
package org.openbase.bco.ontology.lib.manager.tbox;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author agatting on 16.02.17.
 */
public interface TBoxLoader {

    Logger LOGGER = LoggerFactory.getLogger(TBoxLoader.class);

    /**
     * Method loads data into a ontModel from the fileSystem. The ontModel can be given by argument or a new default
     * ontModel, based on OWL_DL_MEM, is created.
     *
     * @param ontModel The ontModel to load data from the fileSystem. If argument is {@code null}, then default
     *                 ontModel, based on OWL_DL_MEM, is created
     * @return The ontModel with data from the filesystem.
     * @throws JenaException JenaException.
     * @throws IllegalArgumentException IllegalArgumentException.
     */
    static OntModel loadOntModelFromFile(OntModel ontModel) throws JenaException, IllegalArgumentException {

        if (ontModel == null) {
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        }

        final InputStream input = TBoxLoader.class.getResourceAsStream("/Ontology.owl");

        if (input == null) {
            throw new IllegalArgumentException("File not found in " + input + "!");
        } else {
            LOGGER.info("Ontology file loaded from " + input);
        }

        //load data into ontModel
        ontModel.read(input, null);

        return ontModel;
    }
}
