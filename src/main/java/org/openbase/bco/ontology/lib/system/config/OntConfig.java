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
import org.openbase.bco.ontology.lib.commun.web.OntModelWeb;
import org.openbase.bco.ontology.lib.manager.tbox.TBoxVerification;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This java class configures the ontology-system and set different elements like namespace or superclasses of the ontology. Furthermore a method tests the
 * validity of them (e.g. spelling mistakes) to roll an ExceptionHandling part out of the ontology-processing.
 *
 * @author agatting on 14.11.16.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public final class OntConfig {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OntConfig.class);

    /**
     * Namespace of the ontology.
     */
    public static final String NS = "http://www.openbase.org/bco/ontology#";

    /**
     * Namespace of the xsd schema (w3c). Don't modify.
     */
    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    /**
     * An enum with service forms of the fuseki ontology server.
     */
    public enum ServerServiceForm {
        DATA,
        UPDATE,
        SPARQL
    }

    public static final int BACKDATED_BEGINNING_OF_PERIOD = 2;

    public static final Period PERIOD_FOR_AGGREGATION = Period.DAY;

    public enum Period {
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    /**
     * RecentHeartBeat (instance).
     */
    public static final String INSTANCE_RECENT_HEARTBEAT = "recentHeartBeat";

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
         * ConnectionPhase (class).
         */
        CONNECTION_PHASE("ConnectionPhase"),

        /**
         * Observation (class).
         */
        OBSERVATION("Observation"),

        /**
         * StateValue (class).
         */
        STATE_VALUE("StateValue"),

        /**
         * BaseUnit (class).
         */
        BASE_UNIT("BaseUnit"),

        /**
         * HostUnit (class).
         */
        HOST_UNIT("HostUnit"),

        /**
         * DalUnit (class).
         */
        DAL_UNIT("DalUnit"),

        /**
         * RecentHeartBeat (class).
         */
        RECENT_HEARTBEAT("RecentHeartBeat");

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

        /**
         * hasConnectionPhase (object property).
         */
        CONNECTION_PHASE("hasConnectionPhase"),

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
         * hasLastHeartBeat (dataType property).
         */
        LAST_HEARTBEAT("hasLastHeartBeat"),

        /**
         * hasFirstHeartBeat (dataType property).
         */
        FIRST_HEARTBEAT("hasFirstHeartBeat"),

        /**
         * hasFirstConnection (dataType property).
         */
        FIRST_CONNECTION("hasFirstConnection"),

        /**
         * hasLastConnection (dataType property).
         */
        LAST_CONNECTION("hasLastConnection");

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
        REMOVE("[^\\p{Alpha}\\p{Digit}]+");

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
     * A small retry period time in seconds.
     */
    public static final int SMALL_RETRY_PERIOD_SECONDS = 5;

    /**
     * A small retry period time in milliseconds.
     */
    public static final int SMALL_RETRY_PERIOD_MILLISECONDS = 5000;

    /**
     * A big retry period time in seconds.
     */
    public static final int BIG_RETRY_PERIOD_SECONDS = 30;

    /**
     * Absolute zero point of temperature (Celsius).
     */
    public static final double ABSOLUTE_ZERO_POINT_CELSIUS = 273.15;

    /**
     * Freezing point of temperature (Fahrenheit)
     */
    public static final double FREEZING_POINT_FAHRENHEIT = 32.0;

    /**
     * Tolerance of heartbeat communication.
     */
    public static final int HEART_BEAT_TOLERANCE = SMALL_RETRY_PERIOD_SECONDS + 5;

    /**
     * Method tests the ontology elements of the configuration class (valid - e.g. no spelling mistake). Errors are printed via ExceptionPrinter.
     *
     * @throws JPServiceException Exception is thrown, if the uri to the tbox server can't be taken.
     * @throws InterruptedException Exception is thrown, if the stopwatch is interrupted.
     */
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    public void initialTestConfig() throws JPServiceException, InterruptedException {

        MultiException.ExceptionStack exceptionStack = null;
        final OntModel ontModel = OntModelWeb.getTBoxModelViaRetry();

        try {
            // test validity of enum property
            for (final OntProp ontProp : OntProp.values()) {
                try {
                    if (!TBoxVerification.isOntPropertyExisting(ontProp.getName(), ontModel)) {
                        throw new NotAvailableException("Property \"" + ontProp.getName() + "\" doesn't match "
                                + "with ontology property! Wrong String or doesn't exist in ontology!");
                    }
                } catch (IllegalArgumentException | IOException e) {
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                }
            }

            // test validity of enum ontClass
            for (final OntCl ontClass : OntCl.values()) {
                try {
                    if (!TBoxVerification.isOntClassExisting(ontClass.getName(), ontModel)) {
                        throw new NotAvailableException("Ontology class \"" + ontClass.getName()
                                + "\" doesn't match with ontology class! Wrong String or doesn't exist in ontology!");
                    }
                } catch (IllegalArgumentException | IOException e) {
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
            ExceptionPrinter.printHistory("Please check OntConfig - names classes and properties", e, LOGGER, LogLevel.ERROR);
        }
    }
}
