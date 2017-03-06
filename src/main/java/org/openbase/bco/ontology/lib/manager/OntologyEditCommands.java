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

import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;

import java.util.Arrays;

/**
 * @author agatting on 17.02.17.
 */
public interface OntologyEditCommands {

    /**
     * Method converts a given string to a string with noun syntax. Thereby are all string parts, which are separate by "_", an independent word/noun. For
     * example 'ACTION_STATE' => 'ActionState'.
     *
     * @param expression The string, which should be converted.
     * @return The converted string.
     * @throws IllegalArgumentException IllegalArgumentException.
     */
    static String convertToNounSyntax(final String expression) throws IllegalArgumentException {

        if (expression == null) {
            throw new IllegalArgumentException("Could not convert string to noun syntax, cause string is null!");
        }
        if (!expression.contains("_")) { //TODO...
            return expression;
        }

        String convertString = "";
        final String[] stringParts = expression.toLowerCase().split("_");

        for (final String buf : stringParts) {
            convertString = convertString + buf.substring(0, 1).toUpperCase() + buf.substring(1);
        }

        return convertString;
    }

    /**
     * Method adds the ontology namespace to an ontElement string. If there is the namespace already, the string is untreated returned.
     *
     * @param ontElement The string, which should be extended by ontology namespace.
     * @return The input string with starting namespace.
     * @throws IllegalArgumentException IllegalArgumentException.
     */
    static String addNamespace(final String ontElement) throws IllegalArgumentException {

        if (ontElement != null) {
            if (!ontElement.startsWith(OntConfig.NS)) {
                 return OntConfig.NS + ontElement;
            } else {
                // input parameter has namespace already
                return ontElement;
            }
        }

        throw new IllegalArgumentException("Could not convert string to noun syntax, cause parameter is null!");
    }

    /**
     * Method converts a string to noun syntax and add the ontology namespace. See {@link #convertToNounSyntax(String)} and {@link #addNamespace(String)} methods.
     *
     * @param ontElementExpr The string, which should be converted to noun syntax and the namespace is added.
     * @return The converted and namespace added string.
     * @throws IllegalArgumentException IllegalArgumentException.
     */
    static String convertToNounAndAddNS(final String ontElementExpr) throws IllegalArgumentException {

        final String newExpr = convertToNounSyntax(ontElementExpr);
        return addNamespace(newExpr);
    }

}
