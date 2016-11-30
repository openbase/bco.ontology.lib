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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by agatting on 11.11.16.
 */
public class QueryOntology {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ontology.class);
    private final OntModel ontModel;

    /**
     * Constructor to start queries to the ontology model.
     *
     * @param ontModel the ontology model.
     */
    public QueryOntology(final OntModel ontModel) {
        this.ontModel = ontModel;
    }

    /**
     * Method executes a simple query.
     */
    public void queryModel() {

        final String queryString = QueryStrings.REQ_7;
        selectQuery(queryString);
//        final boolean solution = askQuery(queryString);
//        System.out.println(solution);

        //System.out.println(getResultString(queryString, "unit"));

        ontModel.close();
    }

    /**
     * Get the first result (resource) of the query.
     * @param queryString The SPARQL query string.
     * @param solutionString The string to search for (e.g. "unit").
     * @return Returns the first resource as string.
     */
    public String getResultString(final String queryString, final String solutionString) {
        try {
            final Query query = QueryFactory.create(queryString);
            final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
            final ResultSet resultSet = queryExecution.execSelect();

            if (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.next();
                queryExecution.close();

                return querySolution.get(solutionString).toString();
            }

        } catch (QueryException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return null;
    }

    private boolean askQuery(final String queryString) {
        try {
            final Query query = QueryFactory.create(queryString);
            final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
            final boolean solution = queryExecution.execAsk();

            queryExecution.close();

            return solution;
        } catch (QueryException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            return false;
        }
    }

    private void selectQuery(final String queryString) {
        try {
            final Query query = QueryFactory.create(queryString);
            final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
            final ResultSet resultSet = queryExecution.execSelect();

            ResultSetFormatter.out(System.out, resultSet, query);

            queryExecution.close();
        } catch (QueryException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }
}
