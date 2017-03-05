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
     * @throws IllegalArgumentException IllegalArgumentException.
     */
    static String convertWordToNounSyntax(String expression) throws IllegalArgumentException {

        if (expression != null) {
            expression = expression.toLowerCase().replaceAll(OntExpr.REMOVE.getName(), "");
            return expression.substring(0, 1).toUpperCase() + expression.substring(1);
        }

        throw new IllegalArgumentException("Could not convert string to noun syntax, cause parameter is null!");
    }

    /**
     * Method adds the ontology namespace to an ontElement string. If there is the namespace already, the string is
     * untreated returned.
     *
     * @param ontElement The string, which should be extended by ontology namespace.
     * @return The input string with starting namespace.
     * @throws IllegalArgumentException IllegalArgumentException.
     */
    static String addNamespaceToOntElement(final String ontElement) throws IllegalArgumentException {

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

}
