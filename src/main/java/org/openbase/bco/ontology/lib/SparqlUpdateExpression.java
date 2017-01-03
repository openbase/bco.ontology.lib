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
     * @param tripleArrayLists The triple information - subject, predicate, object.
     *
     * @return A list of strings, which are update expressions.
     */
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public List<String> getSparqlUpdateEx(final List<TripleArrayList> tripleArrayLists) {

        final List<String> expressionList = new ArrayList<>();

        for (final TripleArrayList triple : tripleArrayLists) {

            String subject = triple.getSubject();
            String predicate = triple.getPredicate();
            String object = triple.getObject();

            if (!subject.startsWith(ConfigureSystem.NS)) {
                subject = ConfigureSystem.ExprPattern.NS.getName() + triple.getSubject();
            }

            if (!subject.startsWith(ConfigureSystem.NS)) {
                object = ConfigureSystem.ExprPattern.NS.getName() + triple.getObject();
            }

            // if predicate isn't an "a" then it's an property with namespace needed. Info: predicate "a" is used to
            // insert an individual to a class.
            if (!predicate.equals(ConfigureSystem.ExprPattern.A.getName())
                    && !predicate.startsWith(ConfigureSystem.NS)) {
                predicate = ConfigureSystem.ExprPattern.NS.getName() + predicate;
            }

            //CHECKSTYLE.OFF: MultipleStringLiterals
            final String updateExpression =
                    "PREFIX NS: <" + ConfigureSystem.NS + "> "
                    + "INSERT DATA { "
                        + subject + " " + predicate + " " + object + " . "
                    + "} ";
            //CHECKSTYLE.ON: MultipleStringLiterals

            expressionList.add(updateExpression);
        }

        return expressionList;
    }
}
