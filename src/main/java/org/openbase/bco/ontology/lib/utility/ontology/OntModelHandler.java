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
package org.openbase.bco.ontology.lib.utility.ontology;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author agatting on 18.05.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public interface OntModelHandler {

    /**
     * Logger to print information.
     */
    Logger LOGGER = LoggerFactory.getLogger(OntModelHandler.class);

    /**
     * Method loads the ontology from the fileSystem/external-resource into an ontModel. The ontModel can be given by argument or a new default
     * ontModel, based on OWL_DL_MEM, is created.
     *
     * @param ontModel is the base model to load the ontology. If {@code null} a default ontModel, based on OWL_DL_MEM, is created.
     * @param path is the path name to the file, which should be loaded.
     * @return an ontModel with data from the filesystem.
     * @throws NotAvailableException is thrown in case the ontModel could not be created or the file path is wrong.
     */
    static OntModel loadOntModelFromFile(OntModel ontModel, final String path) throws NotAvailableException {
        try {
            InputStream input = (path == null) ? StringModifier.class.getResourceAsStream("/Ontology.owl") : new FileInputStream(path);
            LOGGER.info("Ontology file loaded from " + input);

            if (ontModel == null) {
                ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            }
            //read data into ontModel
            ontModel.read(input, null);

            return ontModel;
        } catch (FileNotFoundException ex) {
            throw new NotAvailableException("File in path " + path + " not found!");
        } catch (JenaException ex) {
            throw new NotAvailableException("Could not create ontModel!");
        }
    }

    /**
     * Method saves the ontModel as file with name in a specific path. The file has the RDF/XML format and the format .owl.
     *
     * @param ontModel is the ontModel, which should be saved.
     * @param fileName is the name of the file.
     * @param filePath is the file path. If {@code null} the path is default: src/fileName.owl.
     * @throws IOException is thrown in case the ontModel could not be saved.
     */
    static void saveOntModel(final OntModel ontModel, final String fileName, String filePath) throws IOException {

        if (filePath == null) {
            filePath = "src/";
        }

        OutputStream output = new FileOutputStream(filePath + fileName + ".owl");
        ontModel.writeAll(output, "RDF/XML", null);
        output.close();
        LOGGER.info("Saved ontModel in path" + filePath + fileName + ".owl");
    }

    /**
     * Method processes an ASK query on a local ontModel and returns the bool solution.
     *
     * @param query is the ASK query.
     * @param ontModel is the local ontModel, which should be asked.
     * @return the solution boolean.
     * @throws JenaException is thrown in case the ASK query could not executed.
     */
    static boolean askQuery(final String query, final OntModel ontModel) throws JenaException {

        Query queryObj = QueryFactory.create(query);
        QueryExecution queryExecution = QueryExecutionFactory.create(queryObj, ontModel);
        boolean solution = queryExecution.execAsk();

        queryExecution.close();
        return solution;
    }

    /**
     * Method processes a SELECT query on a local ontModel and prints the result to the console.
     *
     * @param query is the SELECT query.
     * @param ontModel is the local ontModel, which should be asked.
     * @throws JenaException is thrown in case the SELECT query could not executed.
     */
    static void selectQuery(final String query, final OntModel ontModel) throws JenaException {

        Query queryObj = QueryFactory.create(query);
        QueryExecution queryExecution = QueryExecutionFactory.create(queryObj, ontModel);
        ResultSet resultSet = queryExecution.execSelect();

        ResultSetFormatter.out(System.out, resultSet, queryObj);
        queryExecution.close();
    }

}
