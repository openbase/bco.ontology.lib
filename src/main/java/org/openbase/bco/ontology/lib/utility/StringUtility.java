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
package org.openbase.bco.ontology.lib.utility;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.processing.StringProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author agatting on 17.02.17.
 */
public interface StringUtility {

    /**
     * Logger to print information.
     */
    Logger LOGGER = LoggerFactory.getLogger(StringUtility.class);

    /**
     * Method converts a given string to a string with noun syntax. Thereby substrings, which are separated by special characters(-+*=_#/), will be processed
     * as independent words. For example 'ACTION_STATE-Super+Service#word' = 'ActionStateSuperServiceWord'.
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
     * Method returns the input string with the bco namespace prefix.
     *
     * @param input is the string, which should be extended with the bco namespace prefix.
     * @param fullNamespace sets the kind of namespace. If {@code true} the full namespace is added as prefix. Otherwise the short form ("NAMESPACE:") is added.
     * @return the string with namespace prefix.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String addBcoNamespace(final String input, final boolean fullNamespace) throws NotAvailableException {
        if (input == null) {
            assert false;
            throw new NotAvailableException("Input string is null.");
        }

        if (fullNamespace) {
            if (!input.startsWith(OntConfig.NAMESPACE) && !input.startsWith(OntExpr.NS.getName())) {
                return OntConfig.NAMESPACE + input;
            } else if (input.startsWith(OntExpr.NS.getName())) {
                return input.replaceFirst(OntExpr.NS.getName(), OntConfig.NAMESPACE);
            } else {
                return input;
            }
        } else {
            if (!input.startsWith(OntConfig.NAMESPACE) && !input.startsWith(OntExpr.NS.getName())) {
                return OntExpr.NS.getName() + input;
            } else if (input.startsWith(OntConfig.NAMESPACE)) {
                return input.replaceFirst(OntConfig.NAMESPACE, OntExpr.NS.getName());
            } else {
                return input;
            }
        }
    }

    /**
     * Method returns the local name of the input string. Means the prefix (bco namespace (long/short) and xsd (long) are cropped. Hint: the jena method
     * getLocalName() doesn't work correctly by all names, cause of rdf historical reasons...
     *
     * @param input is the input string with containing local name.
     * @return the local name.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String getLocalName(final String input) throws NotAvailableException {
        if (input == null) {
            assert false;
            throw new NotAvailableException("Input string is null.");
        }

        if (input.startsWith(OntConfig.NAMESPACE)) {
            return input.substring(OntConfig.NAMESPACE.length(), input.length());
        } else if (input.startsWith(OntExpr.NS.getName())) {
            return input.substring(OntExpr.NS.getName().length(), input.length());
        } else if (input.startsWith(OntConfig.XSD)) {
            return input.substring(OntConfig.XSD.length(), input.length());
        } else {
            return input;
        }
    }

    /**
     * Method transforms the input string to a literal with the data type "xsd:dateTime".
     *
     * @param dateTime is the dateTime, which should be transformed to a dateTime literal.
     * @return a literal string with data type "xsd:dateTime".
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String addXsdDateTime(final DateTime dateTime) throws NotAvailableException {
        if (dateTime == null) {
            assert false;
            throw new NotAvailableException("DateTime is null.");
        }
        return "\"" + dateTime.toString() + "\"^^xsd:dateTime";
    }

    /**
     * Method transforms the first char of the input string to lower case.
     *
     * @param input is the string, which should be transformed.
     * @return the input string with first char in lower case.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String firstCharToLowerCase(final String input) throws NotAvailableException {
        if (input == null) {
            assert false;
            throw new NotAvailableException("Input string is null.");
        }
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

    /**
     * Method returns the service type name in camel case.
     *
     * @param serviceType is the service type, which name should be returned.
     * @return the service type name in camel case.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String getServiceTypeName(final ServiceType serviceType) throws NotAvailableException {
        try {
            if (serviceType == null) {
                assert false;
                throw new NotAvailableException("Service type is null.");
            }
            return StringProcessor.transformUpperCaseToCamelCase(serviceType.name());
        } catch (CouldNotPerformException e) {
            throw new NotAvailableException("ServiceName", e);
        }
    }

    /**
     * Method transforms the input string from upper case to camel case.
     *
     * @param input is the string in upper case.
     * @return the input string in camel case.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String getCamelCaseName(final String input) throws NotAvailableException {
        try {
            if (input == null) {
                assert false;
                throw new NotAvailableException("Input string is null.");
            }
            return StringProcessor.transformUpperCaseToCamelCase(input);
        } catch (CouldNotPerformException e) {
            throw new NotAvailableException("Input name", e);
        }
    }

    /**
     * Method returns the input string in literal form, means quotation marks (e.g. "input").
     *
     * @param input is the string without quotation marks.
     * @return a literal string (input string with quotation marks).
     */
    static String addQuotationMarks(final String input) {
        if (!input.startsWith("\"") && !input.endsWith("\"")) {
            return "\"" + input + "\"";
        } else if (input.startsWith("\"") && !input.endsWith("\"")) {
            return input + "\"";
        } else if (!input.startsWith("\"") && input.endsWith("\"")) {
            return "\"" + input;
        } else {
            return input;
        }
    }

}
