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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by agatting on 23.12.16.
 */
public class SparqlUpdateExpression {

    /**
     * Method creates a list with sparql update expressions. Each list element is an valid update.
     *
     * @param tripleArrayLists The triple information - subject, predicate, object (with or without namespace).
     *
     * @return A list of strings, which are update expressions.
     */
    @SuppressWarnings({"PMD.UseStringBufferForStringAppends", "checkstyle:multiplestringliterals"})
    public List<String> getSparqlUpdateInsertEx(final List<TripleArrayList> tripleArrayLists) {

        final List<String> expressionList = new ArrayList<>();

        for (final TripleArrayList triple : tripleArrayLists) {

            String subject = triple.getSubject();
            String predicate = triple.getPredicate();
            String object = triple.getObject();
            final String updateExpression;

            if (!subject.startsWith(ConfigureSystem.NS)) {
                subject = ConfigureSystem.ExprPattern.NS.getName() + triple.getSubject();
            }

            if (!object.startsWith(ConfigureSystem.NS) && !object.startsWith("\"")) {
                object = ConfigureSystem.ExprPattern.NS.getName() + triple.getObject();
            }

            // if predicate isn't an "a" then it's an property with namespace needed. Info: predicate "a" is used to
            // insert an individual to a class.
            if (!predicate.equals(ConfigureSystem.ExprPattern.A.getName())
                    && !predicate.startsWith(ConfigureSystem.NS)) {
                predicate = ConfigureSystem.ExprPattern.NS.getName() + predicate;
            }

            updateExpression =
                    "PREFIX NS: <" + ConfigureSystem.NS + "> "
                    + "INSERT DATA { "
                        + subject + " " + predicate + " " + object + " . "
                    + "} ";

            expressionList.add(updateExpression);
        }

        return expressionList;
    }

    /**
     * Method creates an update sparql string to delete a triple. Subject, predicate and object can be selected by a
     * "name" (string) or can be placed as control variable by a "null" parameter in the tripleArrayList. The names of
     * s, p, o keep the namespace or not.
     *
     * Example in tripleArrayList: d930d217-02a8-4264-8d9f-240de7f0d0ca hasConnection null
     * leads to delete string: NS:d930d217-02a8-4264-8d9f-240de7f0d0ca NS:hasConnection ?object
     * and means: all triples with the named subject, named predicate and any object are deleted.
     *
     * @param tripleArrayList The triple information - subject, predicate, object (with or without namespace).
     *
     * @return A sparql update string to delete a triple.
     */
    @SuppressWarnings({"PMD.UseStringBufferForStringAppends", "checkstyle:multiplestringliterals"})
    public String getSparqlUpdateDeleteEx(final TripleArrayList tripleArrayList) {

        //TODO maybe special safety handling, because if s, p, o are all null => delete whole triple store

        String subject = tripleArrayList.getSubject();
        String predicate = tripleArrayList.getPredicate();
        String object = tripleArrayList.getObject();

        if (subject == null) {
            subject = "?subject";
        } else if (!subject.startsWith(ConfigureSystem.NS)) {
            subject = ConfigureSystem.ExprPattern.NS.getName() + subject;
        }

        if (predicate == null) {
            predicate = "?predicate";
        } else if (!predicate.equals(ConfigureSystem.ExprPattern.A.getName())
                && !predicate.startsWith(ConfigureSystem.NS)) {
            // if predicate isn't an "a" then it's an property with namespace needed.
            predicate = ConfigureSystem.ExprPattern.NS.getName() + predicate;
        }

        if (object == null) {
            object = "?object";
        } else if (!object.startsWith(ConfigureSystem.NS)) {
            object = ConfigureSystem.ExprPattern.NS.getName() + object;
        }

        return "PREFIX NS: <" + ConfigureSystem.NS + "> "
        + "DELETE DATA { "
            + subject + " " + predicate + " " + object + " . "
        + "} ";
    }
}
