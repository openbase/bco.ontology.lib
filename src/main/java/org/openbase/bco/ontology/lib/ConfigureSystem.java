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
 * @author agatting on 14.11.16.
 */

import org.apache.jena.ontology.OntModel;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This java class configures the ontology-system and set different elements like namespace or superclasses of the
 * ontology. Furthermore a method tests the validity of them to roll an ExceptionHandling part out of the
 * ontology-processing-classes.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public final class ConfigureSystem {

    /**
     * Namespace of the ontology.
     */
    public static final String NS = "http://www.openbase.org/bco/ontology#";

    /**
     * Enumeration of ontology classes.
     */
    public enum OntClass {

        /**
         * Unit (class).
         */
        UNIT("Unit"),

        /**
         * STATE (class).
         */
        STATE("State"),

        /**
         * ProviderService (class).
         */
        PROVIDER_SERVICE("ProviderService"),

        /**
         * Connection (class).
         */
        CONNECTION("Connection"),

        /**
         * Location (class).
         */
        LOCATION("Location");

        private final String ontClass;

        OntClass(final String ontClass) {
            this.ontClass = ontClass;
        }

        /**
         * Method returns the Name of an enum element.
         *
         * @return Name of an enum element as string.
         */
        public String getName() {
            return this.ontClass;
        }
    }

    /**
     * Enumeration of ontology properties.
     */
    public enum OntProp {
        // ### object properties of ontology ###

        /**
         * hasSubLocation (object property).
         */
        SUB_LOCATION("hasSubLocation"),

        /**
         * hasConnection (object property).
         */
        CONNECTION("hasConnection"),

        /**
         * hasProviderService (object property).
         */
        PROVIDER_SERVICE("hasProviderService"),

        /**
         * hasState (object property).
         */
        STATE("hasState"),

        /**
         * hasStateValue (object property / dataType property).
         */
        STATE_VALUE("hasStateValue"), // a dataType property too

        /**
         * hasUnit (object property).
         */
        UNIT("hasUnit"),

        /**
         * hasUnitId (object property).
         */
        UNIT_ID("hasUnitId"),

        // ### dataType properties of ontology ###

        /**
         * hasLabel (dataType property).
         */
        LABEL("hasLabel"),

        /**
         * hasTimeStamp (dataType property).
         */
        TIME_STAMP("hasTimeStamp"),

        /**
         * isAvailable (dataType property).
         */
        IS_AVAILABLE("isAvailable");

        private final String property;

        OntProp(final String property) {
            this.property = property;
        }

        /**
         * Method returns the Name of an enum element.
         *
         * @return Name of an enum element as string.
         */
        public String getName() {
            return this.property;
        }
    }

    /**
     * Enumeration for ontology string pattern.
     */
    public enum OntExpr {

        /**
         * Pattern to insert an individual to a class -> "a".
         */
        A("a"),

        /**
         * Pattern for SPARQL namespace -> "NS:".
         */
        NS("NS:"),

        /**
         * Pattern to remove all special signs in a string.
         */
        REMOVE("[^\\p{Alpha}]");

        private final String pattern;

        OntExpr(final String pattern) {
            this.pattern = pattern;
        }

        /**
         * Method returns the Name of an enum element.
         *
         * @return Name of an enum element as string.
         */
        public String getName() {
            return this.pattern;
        }
    }

    /**
     * Regular expressions for method/object searching.
     */
    public enum MethodRegEx {

        /**
         * Pattern for method name part.
         */
        GET_VALUE("getValue"),

        /**
         * Pattern for method name part.
         */
        GET("get"),

        /**
         * Pattern for method name part.
         */
        STATE("STATE"),

        /**
         * Pattern for method name part.
         */
        GET_ID("getID"),

        /**
         * Pattern for method name part.
         */
        GET_TIMESTAMP("getTimeStamp");

        private final String methodRegEx;

        MethodRegEx(final String methodRegEx) {
            this.methodRegEx = methodRegEx;
        }

        /**
         * Method returns the Name of an enum element.
         *
         * @return Name of an enum element as string.
         */
        public String getName() {
            return this.methodRegEx;
        }
    }

    /**
     * DateTime format.
     */
    public static final String DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

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

    /**
     *
     */
    public static final String GET_PATTERN_STATE = GET + STRING_PATTERN + STATE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureSystem.class);

    /**
     * Method tests configurations.
     *
     * @param ontModel The ontology model.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    public void initialTestConfig(final OntModel ontModel) throws CouldNotPerformException {

        //TODO if null -> list all classes

        MultiException.ExceptionStack exceptionStack = null;

        try {
            // test validity of enum property
            for (final OntProp ontProp : OntProp.values()) {
                try {
                    if (ontModel.getOntProperty(NS + ontProp.getName()) == null) {
                        throw new NotAvailableException("Property \"" + ontProp.getName() + "\" doesn't match "
                                + "with ontology property! Wrong String or doesn't exist in ontology!");
                    }
                } catch (NotAvailableException e) {
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                }
            }

            // test validity of enum ontClass
            for (final OntClass ontClass : OntClass.values()) {
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
            throw new CouldNotPerformException("Cannot perform reflection!", e);
        }

    }
}
