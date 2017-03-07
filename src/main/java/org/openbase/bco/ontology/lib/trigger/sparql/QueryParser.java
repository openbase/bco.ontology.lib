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
package org.openbase.bco.ontology.lib.trigger.sparql;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;

import java.util.List;

/**
 * @author agatting on 07.03.17.
 */
public class QueryParser {

    private static String uriQuery =
            "PREFIX sp: <http://spinrdf.org/sp#> "
            + "SELECT ?y WHERE { "
                + "{ "
                    + "?x sp:object ?y . "
                    + "} UNION { "
                    + "?x sp:subject ?y . "
                + "} "
                + "FILTER(isURI(?y)) . "
                + "FILTER (regex(str(?y), \"http://www.openbase.org/bco/ontology#\")) . "
            + "} ";


    public QueryParser() {

        final Query queryUrisWithBcoNs = QueryFactory.create(uriQuery);

        final String queryString = AskQueryExample.QUERY_0;
        final Model model = ModelFactory.createDefaultModel();

        // convert ask query to rdf spin
        final Query arqQuery = ARQFactory.get().createQuery(model, queryString);
        final ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
        arq2SPIN.createQuery(arqQuery, null);

        // query all uris from beginning ask query
        final QueryExecution queryExecution = ARQFactory.get().createQueryExecution(queryUrisWithBcoNs, model);
        // resultSet contains all classes and instances of the query
        final ResultSet resultSet = queryExecution.execSelect();

        while (resultSet.hasNext()) {
            System.out.println(resultSet.next().toString());
        }

    }
}
