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
import org.openbase.bco.ontology.lib.jp.JPOntologyDBURL;
import org.openbase.bco.ontology.lib.jp.JPOntologyMode;
import org.openbase.bco.ontology.lib.jp.JPOntologyPingURL;
import org.openbase.bco.ontology.lib.jp.JPOntologyRSBScope;
import org.openbase.bco.ontology.lib.utility.OntModelUtility;
import org.openbase.bco.ontology.lib.manager.tbox.TBoxVerification;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

/**
 * This java class configures the ontology-system and set different elements like namespace or superclasses of the ontology. Furthermore a method tests the
 * validity of them (e.g. spelling mistakes) to roll an ExceptionHandling part out of the ontology-processing.
 *
 * @author agatting on 14.11.16.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public final class OntConfig {

    /**
     * Namespace of the ontology.
     */
    public static final String NAMESPACE = "http://www.openbase.org/bco/ontology#";

    /**
     * Namespace of the xsd schema (w3c). Don't modify.
     */
    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    /**
     * Map contains the service types with appropriate name in camel case (and first char in lower case). E.g. POWER_STATE_SERVICE - powerStateService
     */
    public static final Map<String, ServiceType> SERVICE_NAME_MAP = new HashMap<>();

    /**
     * Map contains the unit types with appropriate name in camel case (and first char in lower case). E.g. COLORABLE_LIGHT - colorableLight
     */
    public static final Map<String, UnitType> UNIT_NAME_MAP = new HashMap<>();

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
     * Absolute zero point of temperature (Celsius).
     */
    public static final double ABSOLUTE_ZERO_POINT_CELSIUS = 273.15;

    /**
     * Freezing point of temperature (Fahrenheit).
     */
    public static final double FREEZING_POINT_FAHRENHEIT = 32.0;

    /**
     * Divisor for temperature (Celsius to Fahrenheit).
     */
    public static final double FAHRENHEIT_DIVISOR = 1.8;

    /**
     * Tolerance of heartbeat communication.
     */
    public static final int HEART_BEAT_TOLERANCE = SMALL_RETRY_PERIOD_SECONDS * 3;

    /**
     * The size of the transaction buffer.
     */
    public static final int TRANSACTION_BUFFER_SIZE = 10000000;

    /**
     * All listed location types, which are subsets of the class Location.
     */
    public static final String[] LOCATION_CATEGORIES = new String[] {"Region", "Tile", "Zone"};

    /**
     * All listed connection types, which are subsets of the class Connection.
     */
    public static final String[] CONNECTION_CATEGORIES = new String[] {"Door", "Passage", "Window"};

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OntConfig.class);

    /**
     * The ontology database URL.
     */
    private static String ontologyDbUrl;

    /**
     * The ontology ping URL.
     */
    private static String ontologyPingUrl;

    /**
     * The ontology scope (RSB).
     */
    private static String ontologyRsbScope;

    /**
     * The ontology mode.
     */
    private static boolean ontologyModeHistoricData;

    static {
        for (final ServiceType serviceType : ServiceType.values()) {
            try {
                if (serviceType != null) {
                    SERVICE_NAME_MAP.put(StringModifier.firstCharToLowerCase(StringModifier.getServiceTypeName(serviceType)), serviceType);
                }
            } catch (NotAvailableException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }
        }

        for (final UnitType unitType : UnitType.values()) {
            try {
                if (unitType != null) {
                    UNIT_NAME_MAP.put(StringModifier.getUnitTypeName(unitType), unitType);
                }
            } catch (NotAvailableException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }
        }

        try {
            ontologyDbUrl = JPService.getProperty(JPOntologyDBURL.class).getValue();
            ontologyPingUrl = JPService.getProperty(JPOntologyPingURL.class).getValue();
            ontologyRsbScope = JPService.getProperty(JPOntologyRSBScope.class).getValue();
            ontologyModeHistoricData = JPService.getProperty(JPOntologyMode.class).getValue();
        } catch (JPNotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * Getter for ontology database url.
     *
     * @return the ontology database url.
     */
    public static String getOntologyDbUrl() {
        return ontologyDbUrl;
    }

    /**
     * Getter for ontology ping url.
     *
     * @return the ontology ping url.
     */
    public static String getOntologyPingUrl() {
        return ontologyPingUrl;
    }

    /**
     * Getter for ontology rsb scope.
     *
     * @return the ontology rsb scope.
     */
    public static String getOntologyRsbScope() {
        return ontologyRsbScope;
    }

    /**
     * Getter for ontology mode to save historic data or current state values only.
     *
     * @return the ontology mode.
     */
    public static boolean getOntologyModeHistoricData() {
        return ontologyModeHistoricData;
    }

    /**
     * Enum contains the server services of the fuseki server. They are components of the url (suffix).
     */
    public enum ServerService {

        /**
         * Element addresses the graph store protocol service. Up- and download of the ontology data (e.g. ontModel).
         */
        DATA("data"),

        /**
         * Element addresses the SPARQL update service. Send updates (manipulate) the ontology model (e.g. insert, delete, where, ... triple).
         */
        UPDATE("update"),

        /**
         * Element addresses the SPARQL query service. Provide a query request (e.g. ASK, SELECT, ...)
         */
        SPARQL("sparql"),

        /**
         * Element addresses the SPARQL query service. Provide a query request (e.g. ASK, SELECT, ...)
         */
        QUERY("query");

        private final String serverService;

        ServerService(final String serverService) {
            this.serverService = serverService;
        }

        /**
         * Method returns the name of an enum element.
         *
         * @return the name of an enum element as string.
         */
        public String getName() {
            return this.serverService;
        }
    }

    /**
     * Enum contains the different period types.
     */
    public enum Period {

        /**
         * Period hour.
         */
        HOUR,
        /**
         * Period day.
         */
        DAY,
        /**
         * Period week.
         */
        WEEK,
        /**
         * Period month.
         */
        MONTH,
        /**
         * Period year.
         */
        YEAR
    }

    /**
     * Method returns the correct decimal format for timestamp transformation.
     *
     * @return the decimal format.
     */
    public static DecimalFormat decimalFormat() {
        final DecimalFormat decimalFormat = new DecimalFormat("#.###");
        final DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();

        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        return decimalFormat;
    }

    /**
     * Enumeration of ontology instances.
     */
    public enum OntInst {

        /**
         * recentHeartBeat (instance).
         */
        RECENT_HEARTBEAT("recentHeartBeat");

        private final String ontInst;

        OntInst(final String ontInst) {
            this.ontInst = ontInst;
        }

        /**
         * Method returns the Name of an enum element.
         *
         * @return Name of an enum element as string.
         */
        public String getName() {
            return this.ontInst;
        }
    }

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
         * Period (class).
         */
        PERIOD("Period"),

        /**
         * AggregationObservation (class).
         */
        AGGREGATION_OBSERVATION("AggregationObservation"),

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

        /**
         * hasPeriod (object property).
         */
        PERIOD("hasPeriod"),

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
         * hasFirstConnection (dataType property).
         */
        FIRST_CONNECTION("hasFirstConnection"),

        /**
         * hasTimeWeighting (dataType property).
         */
        TIME_WEIGHTING("hasTimeWeighting"),

        /**
         * hasQuantity (dataType property).
         */
        QUANTITY("hasQuantity"),

        /**
         * hasActivityTime (dataType property).
         */
        ACTIVITY_TIME("hasActivityTime"),

        /**
         * hasMean (dataType property).
         */
        MEAN("hasMean"),

        /**
         * hasStandardDeviation (dataType property).
         */
        STANDARD_DEVIATION("hasStandardDeviation"),

        /**
         * hasVariance (dataType property).
         */
        VARIANCE("hasVariance"),

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
         * Keyword to insert a subject as subclass of the named object.
         */
        SUB_CLASS_OF("rdfs:subClassOf"),

        /**
         * Pattern to insert an individual to a class: "is a" - scheme.
         */
        IS_A("a"),

        /**
         * Pattern for SPARQL namespace: "NS:".
         */
        NS("NS:");

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
     * Enumeration for ontology instance prefixes.
     */
    public enum OntPrefix {

        /**
         * Prefix of observation instance.
         */
        OBSERVATION("observation_"),

        /**
         * Prefix of heartbeatPhase instance.
         */
        HEARTBEAT("heartbeatPhase_"),

        /**
         * Prefix of connectionPhase instance.
         */
        CONNECTION_PHASE("connectionPhase_");

        private final String prefix;

        OntPrefix(final String prefix) {
            this.prefix = prefix;
        }

        /**
         * Method returns the name of an enum element.
         *
         * @return name of an enum element as string.
         */
        public String getName() {
            return this.prefix;
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
        STATE("State"),

        /**
         * Pattern for method name part.
         */
        SERVICE("Service"),

        /**
         * Pattern for method name part.
         */
        GET_TIMESTAMP("^getTimestamp$"),

        /**
         * Pattern for state method names.
         */
        STATE_METHOD("(?s)^get.*state$");

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
     * Method tests the ontology elements of the configuration class (valid - e.g. no spelling mistake). Errors are printed via ExceptionPrinter.
     *
     * @throws InterruptedException Exception is thrown, if the stopwatch is interrupted.
     * @throws NotAvailableException NotAvailableException
     */
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    public void initialTestConfig() throws InterruptedException, NotAvailableException {

        MultiException.ExceptionStack exceptionStack = null;
        final OntModel ontModel = OntModelUtility.loadOntModelFromFile(null, null);


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
                if (!(ontModel.getNsPrefixURI("") + "#").equals(OntConfig.NAMESPACE)) {
                    throw new NotAvailableException("Namespace \"" + OntConfig.NAMESPACE
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
