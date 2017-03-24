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
package org.openbase.bco.ontology.lib.manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
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
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author agatting on 17.02.17.
 */
public interface OntologyToolkit {

    /**
     * Logger to print information.
     */
    Logger LOGGER = LoggerFactory.getLogger(OntologyToolkit.class);

    /**
     * Method converts a given string to a string with noun syntax. Thereby substrings, which are separated by special characters(-+*=_#/), will be processed
     * as independent words. For example 'ACTION_STATE-Super+Service#word' => 'ActionStateSuperServiceWord'.
     *
     * @param expression The string, which should be converted.
     * @return The converted string with noun syntax (each substring).
     * @throws IllegalArgumentException Exception is thrown, if the parameter is null.
     */
    static String convertToNounSyntax(final String expression) throws IllegalArgumentException {

        if (expression == null) {
            throw new IllegalArgumentException("Could not convert string to noun syntax, cause string is null!");
        }

        final Pattern pattern = Pattern.compile("[-+*/=_#]");
        final Matcher matcher = pattern.matcher(expression);

        if (!matcher.find()) {
            if (StringUtils.isAllUpperCase(expression) && (expression.length() > 0)) {
                return expression.substring(0, 1).toUpperCase() + expression.substring(1).toLowerCase();
            } else {
                return expression;
            }
        }

        final String[] stringParts = expression.toLowerCase().split("[-+*/=_#]");
        String convertString = "";

        for (final String buf : stringParts) {
            if (buf.length() >= 1) {
                convertString = convertString + buf.substring(0, 1).toUpperCase() + buf.substring(1);
            }
        }
        return convertString;
    }

    /**
     * Method adds the ontology namespace to an ontElement string. If there is the namespace already, the string is untreated returned.
     *
     * @param ontElement The string, which should be extended by ontology namespace.
     * @return The input string with starting namespace.
     * @throws IllegalArgumentException Exception is thrown, if the parameter is null.
     */
    static String addNamespace(final String ontElement) throws IllegalArgumentException {

        if (ontElement == null) {
            throw new IllegalArgumentException("Could not convert string to noun syntax, cause parameter is null!");
        }

        if (!ontElement.startsWith(OntConfig.NS) && !ontElement.startsWith(OntExpr.NS.getName())) {
             return OntConfig.NS + ontElement;
        } else {
            return ontElement;
        }
    }

    /**
     * Method converts a string to noun syntax and add the ontology namespace. See {@link #convertToNounSyntax(String)} and {@link #addNamespace(String)} methods.
     *
     * @param ontElementExpr The string, which should be converted to noun syntax and the namespace is added.
     * @return The converted and namespace added string.
     * @throws IllegalArgumentException Exception is thrown, if the parameter is null.
     */
    static String convertToNounAndAddNS(final String ontElementExpr) throws IllegalArgumentException {
        final String newExpr = convertToNounSyntax(ontElementExpr);
        return addNamespace(newExpr);
    }

    /**
     * Method takes the local name of the input ontology element. If the element contains no namespace, the string is unmodified returned. Hint: the jena
     * method getLocalName() doesn't work correctly by all names, cause of rdf historical reasons...
     *
     * @param ontElementExpr The ontology element, which contains a local name.
     * @return The local name of the ontology element.
     * @throws IllegalArgumentException Exception is thrown, if the parameter is null.
     */
    static String getLocalName(final String ontElementExpr) throws IllegalArgumentException {

        if (ontElementExpr == null) {
            throw new IllegalArgumentException("Could not get local name of ontology element, cause parameter is null!");
        }

        if (ontElementExpr.contains(OntConfig.NS)) {
            return ontElementExpr.substring(OntConfig.NS.length(), ontElementExpr.length());
        } else if (ontElementExpr.contains(OntExpr.NS.getName())) {
            return ontElementExpr.substring(OntExpr.NS.getName().length(), ontElementExpr.length());
        } else if (ontElementExpr.contains(OntConfig.XSD)) {
            return ontElementExpr.substring(OntConfig.XSD.length(), ontElementExpr.length());
        } else {
            return ontElementExpr;
        }
    }

    /**
     * Method loads data into an ontModel from the fileSystem. The ontModel can be given by argument or a new default ontModel, based on OWL_DL_MEM, is created.
     *
     * @param ontModel The ontModel to load data from the fileSystem. If argument is {@code null}, then default ontModel, based on OWL_DL_MEM, is created.
     * @return The ontModel with data from the filesystem.
     * @throws JenaException Exception is thrown, if the ontModel could not be created.
     * @throws IllegalArgumentException Exception is thrown, if the file could not be loaded.
     */
    static OntModel loadOntModelFromFile(OntModel ontModel, final String path) throws JenaException, IllegalArgumentException {

        if (ontModel == null) {
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        }

        final InputStream input;
        if (path == null) {
            input = OntologyToolkit.class.getResourceAsStream("/Ontology.owl");
        } else {
            try {
                input = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("File not found!");
            }
        }

        LOGGER.info("Ontology file loaded from " + input);

        //load data into ontModel
        ontModel.read(input, null);

        return ontModel;
    }

    /**
     * Method saves the input ontModel with the fileName in the path src/fileName.owl. The file has the RDF/XML format.
     *
     * @param ontModel The ontModel, which has to be saved.
     * @param fileName The name of the file without file format (e.g. .owl).
     * @throws IOException Exception is thrown, if the ontModel could not be saved.
     */
    static void saveOntModel(final OntModel ontModel, final String fileName) throws IOException {

        final OutputStream output = new FileOutputStream("src/" + fileName + ".owl");
        ontModel.writeAll(output, "RDF/XML", null);
        output.close();
        LOGGER.info("Save ontModel in path src/" + fileName + ".owl ...");
    }

    /**
     * Method cleans existing ontology. Thereby the developer can delete all instances (ontClass is null) or the instances of a specific ontClass.
     *
     * @param ontModel The ontModel, which should be cleaned.
     * @param ontClass If {@code null} then all instances will be deleted. Otherwise the instances of a specific ontClass will be deleted only.
     * @throws CouldNotPerformException Exception is thrown, if the input ontModel is null or empty.
     */
    static void cleanOntModel(final OntModel ontModel, final OntClass ontClass) throws CouldNotPerformException {

        if (ontModel == null || ontModel.isEmpty()) {
            throw new CouldNotPerformException("Could not delete instances, cause ontModel is null or empty!");
        }

        final ExtendedIterator individualIterator;

        if (ontClass == null) {
            individualIterator = ontModel.listIndividuals();
        } else {
            individualIterator = ontClass.listInstances();
        }

        if (individualIterator.hasNext()) {
            while (individualIterator.hasNext()) {
                final Individual individual = (Individual) individualIterator.next();

                ontModel.removeAll(individual, null, null);
            }
        } else {
            LOGGER.info("Input ontology has no individuals.");
        }
    }

    /**
     * Method processes an ASK query on a local ontModel and returns the bool solution.
     *
     * @param queryString The ASK query.
     * @param ontModel The local ontModel, which has to be asked.
     * @throws JenaException Exception is thrown, if the ask query could not be performed (cause e.g. it is a select query...).
     */
    static boolean askQuery(final String queryString, final OntModel ontModel) throws JenaException {

        final Query query = QueryFactory.create(queryString);
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final boolean solution = queryExecution.execAsk();

        queryExecution.close();
        return solution;
    }

    /**
     * Method processes a SELECT query on a local ontModel and prints the result to the console.
     *
     * @param queryString The SELECT query.
     * @param ontModel The local ontModel, which has to be asked.
     * @throws JenaException Exception is thrown, if the select query could not be performed (cause e.g. it is an ask query...).
     */
    static void selectQuery(final String queryString, final OntModel ontModel) throws JenaException {

        final Query query = QueryFactory.create(queryString);
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final ResultSet resultSet = queryExecution.execSelect();

        ResultSetFormatter.out(System.out, resultSet, query);
        queryExecution.close();
    }

}
