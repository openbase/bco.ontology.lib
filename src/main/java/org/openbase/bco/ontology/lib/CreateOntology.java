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

package org.openbase.bco.ontology.lib;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by agatting on 24.10.16.
 */
public class CreateOntology {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOntology.class);
    private final OntModel ontModel;

    /**
     * Constructor for creating ontology model.
     */
    public CreateOntology() {
        this.ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    }

    /**
     * Get the ontology model.
     * @return ontology model
     */
    public OntModel getModel() {
        return ontModel;
    }

    /**
     * Method loads existing ontology file in ontology model to allow development.
     *
     * @param ontologyFilePath path of ontology file.
     */
    public void loadOntology(final String ontologyFilePath) {
        //TODO find a better way than local file path

        try {
            final InputStream inputStream = FileManager.get().open(ontologyFilePath);

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found!");
            } else {
                LOGGER.info("Ontology file loaded from " + ontologyFilePath);
            }

            ontModel.read(inputStream, null);
        } catch (JenaException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * Method is used to save the current developed ontology.
     */
    public void saveOntology() {
        LOGGER.info("Save ontology ...");
        try {
            final OutputStream output = new FileOutputStream("src/Ontology4.owl");
            //TODO format correct?
            ontModel.writeAll(output, "RDF/XML", null);
            output.close();
        } catch (IOException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * Method cleans existing ontology. All instances will be deleted.
     */
    public void cleanOntology() {
        LOGGER.info("Delete individuals ...");
        final ExtendedIterator individualIterator = ontModel.listIndividuals();

        if (individualIterator.hasNext()) {
            LOGGER.info("Ontology has some individuals");
            Individual individual;

            while (individualIterator.hasNext()) {
                individual = (Individual) individualIterator.next();
                //LOGGER.info(individual.toString());
                ontModel.removeAll(individual, null, null);
            }

            checkCleanProcessValidity();
        } else {
            LOGGER.info("Input ontology had no individuals.");
        }

        //TODO: delete properties?
    }

    private void checkCleanProcessValidity() {
        final ExtendedIterator individualIterator = ontModel.listIndividuals();

        if (individualIterator.hasNext()) {
            LOGGER.error("Clean process failed!");
        } else {
            LOGGER.info("Clean process successful");
        }
    }
}
