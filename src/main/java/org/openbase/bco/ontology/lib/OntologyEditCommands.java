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

/**
 * @author agatting on 17.02.17.
 */
public interface OntologyEditCommands {

    /**
     * Method converts a given string to a string with noun syntax. Furthermore it replaces all signs, which aren't
     * alphabetic letters and numbers.
     *
     * @param expression The string, which should be convert.
     * @return The converted string.
     */
    static String wordToNounSyntax(String expression) {

        if (expression != null) {
            expression = expression.toLowerCase().replaceAll(ConfigureSystem.OntExpr.REMOVE.getName(), "");
            expression = expression.substring(0, 1).toUpperCase() + expression.substring(1);
        }

        return expression;
    }

}
