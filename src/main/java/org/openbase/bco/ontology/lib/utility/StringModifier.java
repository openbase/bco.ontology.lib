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
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.MethodRegEx;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.processing.StringProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

/**
 * This interface contains methods to modify strings as one-liner. Mostly it's used to process the unit data, cause
 * they be liable to normed forms (e.g. nomenclature) to store them in the BCO ontology.
 *
 * @author agatting on 17.02.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public interface StringModifier {

    /**
     * Logger to print information.
     */
    Logger LOGGER = LoggerFactory.getLogger(StringModifier.class);

    /**
     * Method adds the BCO namespace prefix {@link OntConfig#NAMESPACE} or rather {@link OntConfig.OntExpr#NS} to the
     * input string. Furthermore, can be used to change the named prefixes ({@link OntConfig.OntExpr#NS} to
     * {@link OntConfig#NAMESPACE} and the other way around.
     *
     * @param input is the string, which should be extended with the BCO namespace prefix.
     * @param fullNamespace sets the kind of namespace. If {@code true} the full namespace {@link OntConfig#NAMESPACE}
     *                      will be added. Otherwise the short form {@link OntConfig.OntExpr#NS}.
     * @return the input string with selected namespace prefix.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String addBcoNamespace(final String input, final boolean fullNamespace) throws NotAvailableException {
        Preconditions.checkNotNull(input, "Couldn't add BCO namespace, cause input string is null!");

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
     * Method returns the local name of the ontology node name. In general, every node in the ontology is consisting of
     * the ontology uri and the local name. The method crops the ontology uri (detection of separator "#" (hash)) or
     * the defined prefix (currently {@link OntExpr#NS}).
     * Hint: Jena method getLocalName() doesn't work perfectly in this application for all names (e.g. local name
     * starts with numbers). RDF historical reasons.
     *
     * @param ontologyNodeName is the ontology node name (uri + local name), which should be returned.
     * @return the input ontology node name without uri. If no uri could be detected the input is returned unchanged.
     * @throws NotAvailableException is thrown in case the parameter is null.
     */
    static String getLocalName(final String ontologyNodeName) throws NotAvailableException {
        Preconditions.checkNotNull(ontologyNodeName, "Couldn't get local name, cause input uri is null!");

        if (ontologyNodeName.startsWith(OntExpr.NS.getName())) {
            return ontologyNodeName.substring(OntExpr.NS.getName().length(), ontologyNodeName.length());
        }
        if (ontologyNodeName.contains("#")) {
            return ontologyNodeName.substring(ontologyNodeName.indexOf("#") + 1);
        }
        return ontologyNodeName;
    }

    /**
     * Method transforms the first char of the input string to lower case.
     *
     * @param input is the string, which first char should be transformed in lower case.
     * @return the input string with first char in lower case.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String firstCharToLowerCase(final String input) throws NotAvailableException {
        Preconditions.checkHasContent(input, "Couldn't transform, cause invalid input string");

        if (input.length() == 1) {
            return input.toLowerCase();
        }
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    /**
     * Method returns the service type name in camel case, e.g. POWER_STATE_SERVICE to PowerStateService.
     *
     * @param serviceType is the service type for transformation.
     * @return the service type name in camel case.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String getServiceTypeName(final ServiceType serviceType) throws NotAvailableException {
        Preconditions.checkNotNull(serviceType, "Input service type couldn't transformed, cause null!");

        return StringProcessor.transformUpperCaseToCamelCase(serviceType.name());
    }

    /**
     * Method returns the unit type name in camel case, e.g. COLORABLE_LIGHT to ColorableLight.
     *
     * @param unitType is the unit type for transformation.
     * @return the unit type name in camel case.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String getUnitTypeName(final UnitType unitType) throws NotAvailableException {
        Preconditions.checkNotNull(unitType, "Input unit type couldn't transformed, cause null!");

        return StringProcessor.transformUpperCaseToCamelCase(unitType.name());
    }

    /**
     * Method transforms the input string from upper case to camel case, like e.g. HELLO_WORLD to HelloWorld.
     *
     * @param input is the string for transformation.
     * @return the input string in camel case.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String getCamelCaseName(final String input) throws NotAvailableException {
        Preconditions.checkNotNull(input, "Input string couldn't transformed, cause null!");

        return StringProcessor.transformUpperCaseToCamelCase(input);
    }

    /**
     * Method returns the input string in literal form, means in quotation marks (e.g. "input").
     *
     * @param input is the string for transformation. Existing quotation marks (ore or two) will be considered.
     * @return the input as literal string (in quotation marks). If input is null only quotation marks will be returned.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    static String addQuotationMarks(final String input) throws NotAvailableException {
        Preconditions.checkNotNull(input, "Couldn't add quotation marks, cause input string is null.");

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

    /**
     * Method converts the input data to a string based on the sparql literal syntax. The associated data type is set
     * by the input xsdType. Input data and xsdType must be matching.
     *
     * @param data is the input data, which should be convert to a literal syntax.
     * @param xsdType is the data type to set the kind of literal. Must match with the data type of the input data.
     * @return a SPARQL literal as string.
     * @throws NotAvailableException is thrown in case at least one parameter is invalid.
     */
    static String convertToLiteral(final Object data, final XsdType xsdType) throws NotAvailableException {
        Preconditions.checkNotNull(data, "Couldn't convert to literal cause input data is null!");
        Preconditions.checkNotNull(data, "Couldn't convert to literal cause input xsdType is null!");

        try {
            switch (xsdType) {
                case INT:
                    return addQuotationMarks(String.valueOf((int) data));
                case DOUBLE:
                    return addQuotationMarks(String.valueOf((double) data));
                case LONG:
                    return addQuotationMarks(String.valueOf((long) data));
                case STRING:
                    return addQuotationMarks((String) data);
                case DATE_TIME:
                    return addQuotationMarks((String) data);
                default:
                    throw new NotAvailableException("Input parameter xsdType unknown in implementation.");
            }
        } catch (Exception e) {
            throw new NotAvailableException("Couldn't convert to literal! Invalid parameter.", e);
        }
    }

    /**
     * Method extracts the service type name of the input state method name (e.g. getPowerState to powerStateService).
     *
     * @param stateMethodName is the state method name, which includes the needed service type name.
     * @return the service type name in camel case (first char lower case, e.g. powerStateService)
     * @throws NotAvailableException is thrown in case the input is null or no valid state (name).
     */
    static String getServiceTypeNameFromStateMethodName(final String stateMethodName) throws NotAvailableException {
        Preconditions.checkNotNull(stateMethodName, "Couldn't get service type name, cause input string is null.");
        String serviceTypeName = stateMethodName;

        if (StringUtils.containsIgnoreCase(serviceTypeName, MethodRegEx.GET.getName())) {
            final int indexOfGet = StringUtils.indexOfIgnoreCase(serviceTypeName, MethodRegEx.GET.getName());
            final int lengthOfGet = MethodRegEx.GET.getName().length();

            serviceTypeName = serviceTypeName.substring(indexOfGet + lengthOfGet);
            serviceTypeName = firstCharToLowerCase(serviceTypeName);

            if (StringUtils.contains(serviceTypeName, MethodRegEx.STATE.getName())) {
                final int indexOfState = serviceTypeName.indexOf(MethodRegEx.STATE.getName());
                final int lengthOfState = MethodRegEx.STATE.getName().length();

                serviceTypeName = serviceTypeName.substring(0, indexOfState + lengthOfState);
                serviceTypeName += MethodRegEx.SERVICE.getName();
            }
        }

        if (OntConfig.SERVICE_NAME_MAP.keySet().contains(serviceTypeName)) {
            return serviceTypeName;
        } else {
            throw new NotAvailableException("Input string is no state (method) name! " + serviceTypeName);
        }
    }

}
