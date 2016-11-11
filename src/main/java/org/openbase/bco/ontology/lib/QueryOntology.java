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
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

/**
 * Created by agatting on 11.11.16.
 */
public class QueryOntology {

    private final OntModel ontModel;

    /**
     * Constructor for query the ontology model.
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

        final String queryString =
                "SELECT ?x"
                        + "WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#FN>  \"John Smith\" }";

        final Query query = QueryFactory.create(queryString);
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final ResultSet resultSet = queryExecution.execSelect();

        ResultSetFormatter.out(System.out, resultSet, query);

        queryExecution.close();
    }
}
