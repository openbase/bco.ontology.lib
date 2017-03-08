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

import org.apache.commons.lang.time.DateUtils;
import org.openbase.bco.ontology.lib.system.config.OntConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The class contains examples of ASK queries, which can be used to generate triggers or rather triggerConfig. They can be used complete or as pattern.
 * All queries be based on SPARQL 1.1 Query Language.
 *
 * @author agatting on 01.03.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class AskQueryExample {

    /**
     * Ist die zuletzt geschaltete Lampe (colorableLight) eingeschaltet?
     */
    public static final String QUERY_0 =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                    // get all colorableLights with their youngest timestamp
                    + "{ SELECT (MAX(?time) AS ?lastTime) ?unit WHERE { "
                        + "?obs a NS:Observation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:ColorableLight . "
                        + "?obs NS:hasProviderService NS:POWER_STATE_SERVICE . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // refer to the observation and check if the state value is on
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasTimeStamp ?lastTime . "
                    + "?obs NS:hasStateValue NS:ON . "
                + "}";

    /**
     * Ist mindestens eine Person im Wohnzimmer anwesend?
     */
    public static final String QUERY_1 =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                    // get all tile units with their youngest timestamp
                    + "{ SELECT (MAX(?time) AS ?lastTime) ?unit WHERE { "
                        + "?obs a NS:Observation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:Tile . "
                        + "?obs NS:hasProviderService NS:PRESENCE_STATE_SERVICE . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // take the units or rather observation and filter...
                    + "?obs NS:hasStateValue NS:PRESENT . "
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
                        + "?obs a NS:Observation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:Television . "
                        + "?obs NS:hasProviderService NS:POWER_STATE_SERVICE . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // take the units or rather observation and filter...
                    + "?obs NS:hasStateValue NS:ON . "
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
                        + "?obs a NS:Observation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:User . "
                        + "?obs NS:hasProviderService NS:PRESENCE_STATE_SERVICE . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // take the units or rather observation and filter...
                    + "?obs NS:hasStateValue NS:ABSENT . "
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
                        + "?obs a NS:Observation . "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:SmokeDetector . "
                        + "?obs NS:hasProviderService NS:SMOKE_STATE_SERVICE . "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    // take the units or rather observation and filter...
                    + "?obs NS:hasStateValue NS:SMOKE . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasTimeStamp ?lastTime . "
                + "}";

    /**
     * Method returns the current dateTime.
     *
     * @return String in format yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static String getCurrentDateTime() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.ENGLISH);
        final Date date = new Date();

        return simpleDateFormat.format(date);
    }

    /**
     * Method adds/subtracts time to the current dateTime.
     *
     * @param minutes The minutes.
     * @param hours The hours.
     * @param days The days.
     * @param months The months.
     * @param years The years.
     * @return The changed dateTime as String.
     */
    public static String addTimeToCurrentDateTime(final int minutes, final int hours, final int days, final int months, final int years) {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.ENGLISH);
        final Date now = new Date();

        Date newDate = DateUtils.addHours(now, hours);
        newDate = DateUtils.addMinutes(newDate, minutes);
        newDate = DateUtils.addDays(newDate, days);
        newDate = DateUtils.addMonths(newDate, months);
        newDate = DateUtils.addYears(newDate, years);

        return simpleDateFormat.format(newDate);
    }

}
