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
 * Created by agatting on 14.11.16.
 */

import org.apache.jena.ontology.OntModel;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This java class configures the ontology-system and set different elements like namespace or superclasses of the
 * ontology. Furthermore a method tests the validity of them to roll an ExceptionHandling part out of the
 * ontology-processing-classes.
 */
public final class ConfigureSystem {

    /**
     * Namespace of the ontology.
     */
    public static final String NS = "http://www.openbase.org/bco/ontology#";

    /**
     * Enumeration of ontology classes.
     */
    public enum OntClass {

        UNIT("Unit"),
        STATE("State"),
        PROVIDER_SERVICE("ProviderService"),
        CONNECTION("Connection"),
        LOCATION("Location");

        final private String ontClass;

        OntClass(final String ontClass) {
            this.ontClass = ontClass;
        }

        final public String getName() {
            return this.ontClass;
        }
    }

    /**
     * Enumeration of ontology properties.
     */
    public enum OntProp {

        // object properties of ontology
        SUB_LOCATION("hasSubLocation"),
        CONNECTION("hasConnection"),
        PROVIDER_SERVICE("hasState"),
        STATE("hasState"),
        STATE_VALUE("hasStateValue"), // a dataType property too
        UNIT("hasUnit"),
        UNIT_ID("hasUnitId"),

        // dataType properties of ontology
        LABEL("hasLabel"),
        TIME_STAMP("hasTimeStamp"),
        IS_AVAILABLE("isAvailable");

        final private String property;

        OntProp(final String property) {
            this.property = property;
        }

        final public String getName() {
            return this.property;
        }
    }

    /**
     * DateTime format.
     */
    public static final String DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * Pattern to remove all special signs in a string.
     */
    public static final String REMOVE_PATTERN = "[^\\p{Alpha}]";

    // -------------------------------

    /**
     * General string pattern (lower case only).
     */
    public static final String STRING_PATTERN = "[a-z]*";

    /**
     * Dollar sign.
     */
    public static final char DOLLAR_SIGN = '$';

    /**
     * state String.
     */
    public static final String STATE = "state";

    /**
     * get String.
     */
    public static final String GET = "get";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureSystem.class);

    /**
     * Regular expressions for method searching.
     */
    //TODO interface not nice...
    public interface RegEx {
        /**
         * Regular expression "getid".
         */
        String GET_ID = "getId";
        /**
         * Regular expression "getValue".
         */
        String GET_VALUE = "getValue";
        /**
         * Regular expression "get" + Pattern + "dataUnit".
         */
        String GET_PATTERN_DATA_UNIT = GET + STRING_PATTERN + "dataUnit";
        /**
         * Regular expression "get" + Pattern + "State".
         */
        String GET_PATTERN_STATE = GET + STRING_PATTERN + STATE;
        /**
         * Regular expression "getTimeStamp".
         */
        String GET_TIME_STAMP = "getTimeStamp";

        /**
         * Empty method for pmd.
         * @return -
         */
        String emptyMethod();
    }

    /**
     * Private Constructor.
     */
    public ConfigureSystem() {
    }

    /**
     * Method tests configurations.
     *
     * @param ontModel The ontology model.
     */
    public void initialTestConfig(final OntModel ontModel) {

        //TODO if null -> list all classes
        //CHECKSTYLE.OFF: MultipleStringLiterals

        MultiException.ExceptionStack exceptionStack = null;

        try {
            // test validity of enum property
            for (OntProp ontProp : OntProp.values()) {
                try {
                    if (ontModel.getOntProperty(NS + ontProp.getName()) == null) {
                        throw new NotAvailableException("Property \"" + ontProp.getName()
                                + "\" doesn't match with ontology property! Wrong String or doesn't exist in ontology!");
                    }
                } catch (NotAvailableException e) {
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                }
            }

            // test validity of enum ontClass
            for (OntClass ontClass : OntClass.values()) {
                try {
                    if (ontModel.getOntClass(NS + ontClass.getName()) == null) {
                        throw new NotAvailableException("OntClass \"" + ontClass.getName()
                                + "\" doesn't match with ontology class! Wrong String or doesn't exist in ontology!");
                    }
                } catch (NotAvailableException e) {
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                }
            }


            try {
                // test availability of ontology namespace
                if (!(ontModel.getNsPrefixURI("") + "#").equals(ConfigureSystem.NS)) {
                    throw new NotAvailableException("Namespace \"" + ConfigureSystem.NS
                            + "\" doesn't match with ontology namespace! Wrong String or ontology!");
                }
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(this, e, exceptionStack);
            }

            MultiException.checkAndThrow("Could not process all ontology participants correctly!", exceptionStack);
        }  catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        //CHECKSTYLE.ON: MultipleStringLiterals
    }
}
