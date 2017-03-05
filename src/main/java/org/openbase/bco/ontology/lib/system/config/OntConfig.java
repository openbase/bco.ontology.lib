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

package org.openbase.bco.ontology.lib.system.config;

import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.system.jp.JPOntologyDatabaseUri;
import org.openbase.bco.ontology.lib.system.jp.JPTBoxDatabaseUri;
import org.openbase.bco.ontology.lib.manager.tbox.TBoxVerificationResource;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
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
 *
 * @author agatting on 14.11.16.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public final class OntConfig {

    /**
     * Method returns the uri to the tbox database of the server.
     *
     * @return The tbox database uri.
     */
    public static String getTBoxDatabaseUri() {
        try {
            return JPService.getProperty(JPTBoxDatabaseUri.class).getValue();
        } catch (JPNotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return null;
    }

    /**
     * Method returns the uri to the ontology database of the server.
     *
     * @return The ontology database uri.
     */
    public static String getOntDatabaseUri() {
        try {
            return JPService.getProperty(JPOntologyDatabaseUri.class).getValue() + "data";
        } catch (JPNotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return null;
    }

    /**
     * Method returns the uri to the ontology update of the server.
     *
     * @return The ontology update uri.
     */
    public static String getOntUpdateUri() {
        try {
            return JPService.getProperty(JPOntologyDatabaseUri.class).getValue() + "update";
        } catch (JPNotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return null;
    }

    /**
     * Method returns the uri to the ontology sparql (query) of the server.
     *
     * @return The ontology sparql (query) uri.
     */
    public static String getOntSparqlUri() {
        try {
            return JPService.getProperty(JPOntologyDatabaseUri.class).getValue() + "sparql";
        } catch (JPNotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return null;
    }

    /**
     * Namespace of the ontology.
     */
    public static final String NS = "http://www.openbase.org/bco/ontology#";

    /**
     * Enumeration of ontology classes.
     */
    public enum OntCl {

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
        LOCATION("Location"),

        /**
         * HeartBeatPhase (class).
         */
        HEARTBEAT_PHASE("HeartBeatPhase"),

        /**
         * Observation (class).
         */
        OBSERVATION("Observation"),

        /**
         * StateValue (class).
         */
        STATE_VALUE("StateValue");

        private final String ontClass;

        OntCl(final String ontClass) {
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
         * isEnabled (dataType property).
         */
        IS_ENABLED("isEnabled"),

        /**
         * isConnected (dataType property).
         */
        IS_CONNECTED("isConnected"),

        /**
         * hasLastHeartBeat (dataType property).
         */
        HAS_LAST_HEARTBEAT("hasLastHeartBeat"),

        /**
         * hasFirstHeartBeat (dataType property).
         */
        HAS_FIRST_HEARTBEAT("hasFirstHeartBeat");

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
        REMOVE("[^\\p{Alpha}]"); //TODO "/[^A-Za-z0-9 ]/"?

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
        GET_ID("getId"),

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
        GET_TIMESTAMP("getTimestamp");

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

    /**
     * DateTime format without time zone (for ontology instance naming only!).
     */
    public static final String DATE_TIME_WITHOUT_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /**
     * A small retry period time in seconds.
     */
    public static final int SMALL_RETRY_PERIOD = 5;

    /**
     * A big retry period time in seconds.
     */
    public static final int BIG_RETRY_PERIOD = 30;

    /**
     * Absolute zero point of temperature (Celsius).
     */
    public static final double ABSOLUTE_ZERO_POINT_CELSIUS = 273.15;

    /**
     * Freezing point of temperature (Fahrenheit)
     */
    public static final double FREEZING_POINT_FAHRENHEIT = 32.0;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(OntConfig.class);

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
                    if (!TBoxVerificationResource.isOntPropertyExisting(ontProp.getName(), ontModel)) {
                        throw new NotAvailableException("Property \"" + ontProp.getName() + "\" doesn't match "
                                + "with ontology property! Wrong String or doesn't exist in ontology!");
                    }
                } catch (IllegalArgumentException | CouldNotPerformException e) {
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                }
            }

            // test validity of enum ontClass
            for (final OntCl ontClass : OntCl.values()) {
                try {
                    if (!TBoxVerificationResource.isOntClassExisting(ontClass.getName(), ontModel)) {
                        throw new NotAvailableException("Ontology class \"" + ontClass.getName()
                                + "\" doesn't match with ontology class! Wrong String or doesn't exist in ontology!");
                    }
                } catch (IllegalArgumentException | CouldNotPerformException e) {
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                }
            }

            try {
                // test availability of ontology namespace
                if (!(ontModel.getNsPrefixURI("") + "#").equals(OntConfig.NS)) {
                    throw new NotAvailableException("Namespace \"" + OntConfig.NS
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
