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
package org.openbase.bco.ontology.lib.trigger.sparql;

import org.openbase.bco.ontology.lib.trigger.Trigger;
import org.openbase.bco.ontology.lib.trigger.TriggerFactory;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.pattern.Observable;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.ontology.TriggerConfigType.TriggerConfig;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.time.OffsetDateTime;

/**
 * The class contains examples of ASK queries, which can be used to generate triggers or rather triggerConfig. They can be used complete or as pattern.
 * All queries be based on SPARQL 1.1 Query Language.
 *
 * @author agatting on 01.03.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class AskQueryExample {

    public static final String QUERY_TEST =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                    + "ASK { "
                    + "?unit NS:hasProviderService NS:powerStateService . "
                    + "} ";

    /**
     * Ist die zuletzt geschaltete Lampe (colorableLight) eingeschaltet?
     */
    public static final String QUERY_0 =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                    // get all colorableLights with their youngest timestamp
                    + "{ SELECT (MAX(?time) AS ?lastTime) ?unit WHERE { "
                        + "?obs a NS:OntObservation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:PowerSwitch . "
                        + "?obs NS:hasProviderService NS:powerStateService . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // refer to the observation and check if the state value is on
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasTimeStamp ?lastTime . "
                    + "?obs NS:hasStateValue NS:on . "
                + "}";

    /**
     * Ist mindestens eine Person im Wohnzimmer anwesend?
     */
    public static final String QUERY_1 =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                    // get all tile units with their youngest timestamp
                    + "{ SELECT (MAX(?time) AS ?lastTime) ?unit WHERE { "
                        + "?obs a NS:OntObservation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:Tile . "
                        + "?obs NS:hasProviderService NS:presenceStateService . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // take the units or rather observation and filter...
                    + "?obs NS:hasStateValue NS:present . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasTimeStamp ?lastTime . "
                    + "?unit NS:hasLabel \"Living\" . "
                + "}";

    /**
     * Ist ein Fernseher im Apartment (außer im Badezimmer) eingeschaltet?
     */
    public static final String QUERY_2 =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                    // get all television units with their youngest timestamp
                    + "{ SELECT (MAX(?time) AS ?lastTime) ?unit WHERE { "
                        + "?obs a NS:OntObservation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:Television . "
                        + "?obs NS:hasProviderService NS:powerStateService . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // take the units or rather observation and filter...
                    + "?obs NS:hasStateValue NS:on . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasTimeStamp ?lastTime . "
                    + "?location NS:hasUnit ?unit . "
                    + "FILTER NOT EXISTS { "
                        + "?location NS:hasLabel \"Bath\" "
                    + "} . "
            + "}";

    /**
     * Ist der Einwohner abwesend?
     */
    public static final String QUERY_3 =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                    // get all user units with their youngest timestamp
                    + "{ SELECT (MAX(?time) AS ?lastTime) ?unit WHERE { "
                        + "?obs a NS:OntObservation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:User . "
                        + "?obs NS:hasProviderService NS:presenceStateService . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // take the units or rather observation and filter...
                    + "?obs NS:hasStateValue NS:absent . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasTimeStamp ?lastTime . "
                + "}";

    /**
     * Meldet der Rauchmelder Rauch??
     */
    public static final String QUERY_4 =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                    // get all smokeDetector units with their youngest timestamp
                    + "{ SELECT (MAX(?time) AS ?lastTime) ?unit WHERE { "
                        + "?obs a NS:OntObservation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:SmokeDetector . "
                        + "?obs NS:hasProviderService NS:smokeStateService . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // take the units or rather observation and filter...
                    + "?obs NS:hasStateValue NS:smoke . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasTimeStamp ?lastTime . "
                + "}";

    /**
     * Sparql query to select the last timestamp of connection phase. The query considers the form of the last timestamp (normal literal or pointer across
     * recentHeartBeat)
     */
    public static final String QUERY_LAST_CONNECTION_PHASE_TIMESTAMP =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT * WHERE { "
                    + "?conn a NS:ConnectionPhase . "
                    // get times, which are stored in different cases
                    + "?conn NS:hasLastConnection ?time . "
                    + "OPTIONAL { ?time NS:hasLastConnection ?lastHeartBeat . } . "
                    // reduce times to one variable via if condition
                    + "bind(if(isLiteral(?time), ?time, ?lastHeartBeat) as ?resultTime)"
                + "} ";

    public enum QueryExample {

        QUERY_TEST_EXAMPLE(QUERY_TEST, "Test: Unit hasProviderService powerStateService"),

        QUERY_LAST_LAMP_ON(QUERY_0, "Is the last switched lamp on?"),

        QUERY_PERSON_IN_LIVING_ROOM(QUERY_1, "Is there a movement in the living room?"),

        QUERY_TV_ON(QUERY_2, "Is there at least one TV on?"),

        QUERY_PERSON_ABSENT(QUERY_3, "Is the inhabitant present?"),

        QUERY_SMOKE(QUERY_4, "Is there smoke in the apartment?");

        private final String query, description;

        QueryExample(final String query, final String description) {
            this.query = query;
            this.description = description;
        }

        public String getQuery() {
            return this.query;
        }

        /**
         * Method returns the name of an enum element.
         *
         * @return the name of an enum element as string.
         */
        public String getDescription() {
            return this.description;
        }
    }

    /**
     * Method returns the current dateTime.
     *
     * @return String in format yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static String getCurrentDateTime() {
//        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.ENGLISH);
//        final Date date = new Date();

        return OffsetDateTime.now().toString();
    }

//    /**
//     * Method adds/subtracts time to the current dateTime.
//     *
//     * @param minutes The minutes.
//     * @param hours The hours.
//     * @param days The days.
//     * @param months The months.
//     * @param years The years.
//     * @return The changed dateTime as String.
//     */
//    public static String addTimeToCurrentDateTime(final int minutes, final int hours, final int days, final int months, final int years) {
//
//        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.ENGLISH);
//        final Date now = new Date();
//
//        Date newDate = DateUtils.addHours(now, hours);
//        newDate = DateUtils.addMinutes(newDate, minutes);
//        newDate = DateUtils.addDays(newDate, days);
//        newDate = DateUtils.addMonths(newDate, months);
//        newDate = DateUtils.addYears(newDate, years);
//
//        return simpleDateFormat.format(newDate);
//    }
    //TODO adapt method with joda time...

    /**
     * Just an example trigger. Do not use method. Take the example code and modify.
     *
     * @throws CouldNotPerformException Exception is thrown, if the TriggerFactory could not be buil.
     * @throws InterruptedException Exception is thrown, if the application is interrupted.
     */
    public void exampleTrigger() throws CouldNotPerformException, InterruptedException {

        final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(OntologyChange.Category.UNKNOWN)
                .addUnitType(UnitType.COLORABLE_LIGHT).addServiceType(ServiceType.POWER_STATE_SERVICE).build();
        final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger0").setQuery(AskQueryExample.QUERY_0)
                .setDependingOntologyChange(ontologyChange).build();

        final TriggerFactory triggerFactory = new TriggerFactory();
        final Trigger trigger = triggerFactory.newInstance(triggerConfig);

        trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
            System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
            // do useful stuff
        });
    }

}
