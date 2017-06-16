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

package org.openbase.bco.ontology.lib.utility.sparql;

import java.time.OffsetDateTime;

/**
 * @author agatting on 16.11.16.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public final class CompetencyQuestions {

    //TODO later: get concrete object of query...
    //TODO make all strings like "home" generic
    //TODO make request of unitType generic
    //TODO add link to providerService up to REQ_16

    // competence questions for ontology validation based on SPARQL 1.1 Query Language
    // Queries based on SELECT to visualize the solutions

    /**
     * Was ist die aktuelle Zeit?
     */
    public static final String REQ_0 =
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?dateTime ?time ?hours WHERE { "
                // function: get current dateTime
                + "BIND (now() AS ?dateTime) . "
                // get hours of current time
                + "BIND (hours(?dateTime) AS ?hours) . "
                // get current time of current dateTime
                + "BIND (xsd:time(?dateTime) AS ?time) . "
            + "}";

    /**
     * Wurde jemals ein Sabotagekontakt ausgelöst und wenn ja, wo?
     */
    public static final String REQ_1 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?unitLabel ?stateValue ?locationLabel WHERE { "
                // get all units tamperDetector
                + "?unit a NAMESPACE:TamperDetector . "
                + "?unit NAMESPACE:hasLabel ?unitLabel . "
                // get all observations of the units
                + "?observation NAMESPACE:hasUnitId ?unit . "
                + "?observation NAMESPACE:hasStateValue NAMESPACE:OPEN, ?stateValue FILTER (?stateValue = NAMESPACE:OPEN) . "
                // get locations of the units
                + "?location NAMESPACE:hasUnit ?unit . "
                + "?location NAMESPACE:hasLabel ?locationLabel . "
                // filter all locations with specific literal
                + "FILTER NOT EXISTS { "
                    + "?location NAMESPACE:hasLabel \"Home\" . "
                + "} . "
            + "} ";

    /**
     * Welche units befinden sich im Wohnzimmer und sind sie erreichbar?
     */
    public static final String REQ_2 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?unitLabel ?isAvailable WHERE { "
                // get units with location "living"
                + "?location NAMESPACE:hasLabel \"Living\" . "
                + "?location NAMESPACE:hasUnit ?unit . "
                + "?unit NAMESPACE:hasLabel ?unitLabel . "
                // get the value of the units
                + "?unit NAMESPACE:isAvailable ?isAvailable . "
            + "}";

    /**
     * Welche Lampe wurden im Apartment bisher am häufigsten verwendet?
     */
    public static final String REQ_3 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?lampLabel (COUNT(?unit) as ?count) ?locationLabel WHERE { "
                // get all lamp units and their labels
                + "{ { ?unit a NAMESPACE:ColorableLight . } "
                    + "UNION "
                + "{ ?unit a NAMESPACE:DimmableLight . } } "
                + "?unit NAMESPACE:hasLabel ?lampLabel . "
                // get the observations with value ON or OFF
                + "?observation NAMESPACE:hasUnitId ?unit . "
                + "{ { ?observation NAMESPACE:hasStateValue NAMESPACE:ON . } "
                    + "UNION "
                + "{ ?observation NAMESPACE:hasStateValue NAMESPACE:OFF . } } "
                // get optional their label and location
                + "OPTIONAL { "
                    + "?location NAMESPACE:hasUnit ?unit . "
                    + "?location NAMESPACE:hasLabel ?locationLabel . "
                    + "FILTER not exists { "
                        + "?location NAMESPACE:hasLabel \"Home\" "
                    + "} . "
                + "} "
            + "} "
            // group all lamps and count them with the popular one in front
            + "GROUP BY ?unit ?lampLabel ?locationLabel "
            + "ORDER BY DESC(?count) LIMIT 1 ";

    /**
     * Sind im Moment Lampen im Wohnzimmer an, alle Rollos unten und ist es zwischen 22:00 - 6:00 Uhr?
     * Welche Lampen sind diese?
     */
    public static final String REQ_4 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            // select lamps only, if conditions of the question apply
            + "SELECT (IF(?isRolUP = false && ?time = true, ?lampLabel, 0) as ?labelLamp) WHERE { "
                // get the current observation of the lamp units
                + "{ SELECT (MAX(?timeLamp) AS ?currentTimeLamp) ?lamp WHERE { "
                    + "{ { ?lamp a NAMESPACE:ColorableLight . } "
                        + "UNION "
                    + "{ ?lamp a NAMESPACE:DimmableLight . } } "
                    + "?location NAMESPACE:hasLabel \"Living\" . "
                    + "?location NAMESPACE:hasUnit ?lamp . "
                    + "?currentObsLamp NAMESPACE:hasUnitId ?lamp . "
                    + "?currentObsLamp NAMESPACE:hasTimeStamp ?timeLamp . "
                + "} GROUP BY ?currentTimeLamp ?lamp } "

                // is there a lamp with current value ON?
                + "?observationLamp NAMESPACE:hasUnitId ?lamp . "
                + "?observationLamp NAMESPACE:hasStateValue NAMESPACE:ON . "
                // get the labels of the lamps
                + "?lamp NAMESPACE:hasLabel ?lampLabel . "

                // get current observation of rollerShutter units
                + "{ SELECT (MAX(?rolTime) AS ?currentRolTime) ?rol WHERE { "
                    + "?rol a NAMESPACE:RollerShutter . "
                    + "?currentObsRol NAMESPACE:hasUnitId ?rol . "
                    + "?currentObsRol NAMESPACE:hasTimeStamp ?rolTime . "
                + "} GROUP BY ?rol ?currentRolTime } "
                // is there a rollerShutter with current value UP or UNKNOWN?
                + "BIND (EXISTS { "
                    + "?observationRol NAMESPACE:hasUnitId ?rol . "
                    + "?observationRol NAMESPACE:hasStateValue ?rolVal . "
                    + "FILTER (?rolVal = NAMESPACE:UP || NAMESPACE:UNKNOWN) } "
                + "AS ?isRolUP ) . "

                // get current dateTime
                + "BIND (xsd:time(now()) AS ?currentTime) . "
                // is the current time in the time frame of the question?
                + "BIND (IF(?currentTime >= \"12:00:00.000+01:00\"^^xsd:time "
                    + "|| ?currentTime <= \"06:00:00.000+01:00\"^^xsd:time, true, false) "
                + "AS ?time ) . "
            + "} "
            // additionally GROUP to reduce duplication... //TODO why?!
            + "GROUP BY ?isRolUP ?time ?lampLabel ?labelLamp ";

    /**
     * Befindet sich momentan mindestens eine Person im Apartment und wenn nicht, sind alle Lampen ausgeschaltet?
     */
    public static final String REQ_5 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT (IF(?isMotion = true, ?motionLabel, 0) AS ?labelMotion) "
                + "(IF(?isMotion = true, ?locMotionLabel, 0) AS ?locationLabelMotion) "
                + "(IF(?isMotion = true, NAMESPACE:MOTION, NAMESPACE:NO_MOTION) AS ?motionValue) "
                + "(IF(?isMotion = false && ?isLampOn = true, ?lampLabel, 0) AS ?labelLamp) "
                + "(IF(?isMotion = false && ?isLampOn = true, ?locLabelOfLamp, 0) AS ?locationLabelLamp) "
                + "(IF(?isMotion = false, NAMESPACE:ON, NAMESPACE:OFF) AS ?lampValue) WHERE { "

                // get current observation of motion units
                + "{ SELECT (MAX(?timeMotion) AS ?currentTimeMotion) ?motionUnit WHERE { "
                    + "?motionUnit a NAMESPACE:MotionDetector . "
                    + "?currentObsMotion NAMESPACE:hasUnitId ?motionUnit . "
                    + "?currentObsMotion NAMESPACE:hasTimeStamp ?timeMotion . "
                + "} GROUP BY ?currentTimeMotion ?motionUnit } "

                // get location- and unit labels of motion unit
                + "OPTIONAL { "
                    + "?motionUnit NAMESPACE:hasLabel ?motionLabel . "
                    + "?locMotion NAMESPACE:hasUnit ?motionUnit . "
                    + "?locMotion NAMESPACE:hasLabel ?locMotionLabel . "
                    + "FILTER not exists { "
                        + "?locMotion NAMESPACE:hasLabel \"Home\" "
                    + "} . "
                + "} "

                // is there a motion unit with current value MOTION?
                + "BIND (EXISTS { "
                    + "?obsMotion NAMESPACE:hasUnitId ?motionUnit . "
                    + "?obsMotion NAMESPACE:hasStateValue NAMESPACE:MOTION } "
                + "AS ?isMotion) . "

                // get current observation of lamp units
                + "{ SELECT (MAX(?timeLamp) AS ?currentTimeLamp) ?lampUnit WHERE { "
                    + "{ { ?lampUnit a NAMESPACE:ColorableLight . } "
                        + "UNION "
                    + "{ ?lampUnit a NAMESPACE:DimmableLight . } } "
                    + "?currentObsLamp NAMESPACE:hasUnitId ?lampUnit . "
                    + "?currentObsLamp NAMESPACE:hasTimeStamp ?timeLamp . "
                + "} GROUP BY ?currentTimeLamp ?lampUnit } "

                // get location- and unit labels of lamp unit
                + "OPTIONAL { "
                    + "?lampUnit NAMESPACE:hasLabel ?lampLabel . "
                    + "?locLamp NAMESPACE:hasUnit ?lampUnit . "
                    + "?locLamp NAMESPACE:hasLabel ?locLabelOfLamp . "
                    + "FILTER not exists { "
                        + "?locLamp NAMESPACE:hasLabel \"Home\" "
                    + "} . "
                + "} "

                // is there a lamp unit with current value ON?
                + "BIND (EXISTS {"
                    + " ?currentObsLamp NAMESPACE:hasStateValue NAMESPACE:ON } "
                + "AS ?isLampOn) . "
            + "} ";

    /**
     * Welcher Raum ist anhand der Häufigkeit der Gerätebenutzung am beliebtesten?
     */
    public static final String REQ_6 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>"
            // count the frequency of all locations by appearance via observations
            + "SELECT ?label (COUNT(?location) as ?count) WHERE { "
                // get all units which are dalUnits
                + "?unitType rdfs:subClassOf NAMESPACE:DalUnit . "
                + "?unit a ?unitType . "
                // get the location and the label via usage of units
                + "?observation NAMESPACE:hasUnitId ?unit . "
                + "?location NAMESPACE:hasUnit ?unit . "
                + "?location NAMESPACE:hasLabel ?label . "
                + "FILTER not exists { "
                    + "?location NAMESPACE:hasLabel \"Home\" "
                + "} . "
            + "} "
            + "GROUP BY ?label ?location "
            + "ORDER BY DESC(?count) LIMIT 1 ";

    /**
     * Welche unitTypen wurden in den letzten 3 Stunden manipuliert (z.B. ein/ausschalten) und wo befinden sich diese?
     */
    public static final String REQ_7 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>"
            + "SELECT ?unitLabel ?unitType (COUNT(?unit) AS ?unitCount) ?locationLabel WHERE { "
                    // get all timestamps of observations, which are not older than 3 hours from now
                    + "?observation NAMESPACE:hasTimeStamp ?time . "
                    + "FILTER (?time > \"" + addTimeToCurrentDateTime(0, -3, 0, 0, 0) + "\"^^xsd:dateTime) . "

                    // get all units, which are dalUnits or Doors or Windows from the observations
                    + "?observation NAMESPACE:hasUnitId ?unit . "
                    + "?unit NAMESPACE:hasLabel ?unitLabel . "
                    + "{ { ?unit a ?unitType . } "
                        + "UNION "
                    + "{ ?unit a NAMESPACE:Door . }"
                        + "UNION "
                    + "{ ?unit a NAMESPACE:Window . } } "
                    + "?unitType rdfs:subClassOf NAMESPACE:DalUnit . "

                    // get the locations of the ?units without "Home"
                    + "?location NAMESPACE:hasUnit ?unit . "
                    + "?location NAMESPACE:hasLabel ?locationLabel . "
                    + "FILTER not exists { "
                        + "?location NAMESPACE:hasLabel \"Home\" "
                    + "} . "
                + "} "
                + "GROUP BY ?unitLabel ?unitType ?unitCount ?locationLabel ";

    /**
     * Wie viel Energie wurde in den letzten 3 Stunden im Apartment und wie viel im Wohnzimmer verbraucht?
     */
    public static final String REQ_8 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>"
            + "SELECT ?locationLabel (SUM(?currentValue - ?oldValue) AS ?consumption) ?physicalType WHERE { "

                // get oldest (within 3 hours) and current timestamp of units with powerConsumption
                + "{ SELECT (MIN(?time) AS ?oldTime) (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?obs NAMESPACE:hasTimeStamp ?time . "
                    + "FILTER (?time > \"" + addTimeToCurrentDateTime(0, -3, 0, 0, 0) + "\"^^xsd:dateTime) . "
                    + "?obs NAMESPACE:hasUnitId ?unit . "
                    + "?unit a NAMESPACE:PowerConsumptionState . "
                + "} "
                + "GROUP BY ?oldTime ?currentTime ?unit } "

                // get units in location "home" or "living" (or ...) only
                + "?location NAMESPACE:hasUnit ?unit . "
                + "?location NAMESPACE:hasLabel \"Living\", ?locationLabel FILTER (?locationLabel = \"Living\") . "

                // get state value of observation with oldest timestamp
                + "?obsOld NAMESPACE:hasUnitId ?unit . "
                + "?obsOld NAMESPACE:hasTimeStamp ?oldTime . "
                + "?obsOld NAMESPACE:hasStateValue ?oldVal . "

                // get state value of observation with current timestamp
                + "?obsCurrent NAMESPACE:hasUnitId ?unit . "
                + "?obsCurrent NAMESPACE:hasTimeStamp ?currentTime . "
                + "?obsCurrent NAMESPACE:hasStateValue ?currentVal . "

                // assign values to operate type (double) and physical type
                + "BIND (xsd:double(?oldVal) AS ?oldValue) . "
                + "BIND (xsd:double(?currentVal) AS ?currentValue) . "
                + "BIND (datatype(?currentVal) AS ?physicalType) . "
            + "} "
            + "GROUP BY ?locationLabel ?consumption ?physicalType ";

    /**
     * Wie ist die Temperaturdifferenz im Badezimmer von jetzt zu vor 3 Stunden?
     */
    public static final String REQ_9 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>"
                + "SELECT ?labelLoc (AVG(?currentValue) - AVG(?oldValue) AS ?temperatureDiff) ?physicalType WHERE { "

                    // get oldest (within 3 hours) and current timestamp of units with temperature
                    + "{ SELECT (MIN(?time) AS ?oldTime) (MAX(?time) AS ?currentTime) ?unit WHERE { "
                        + "?obs NAMESPACE:hasTimeStamp ?time . "
                        + "FILTER (?time > \"" + addTimeToCurrentDateTime(0, -3, 0, 0, 0) + "\"^^xsd:dateTime) . "
                        + "?obs NAMESPACE:hasUnitId ?unit . "
                        + "?unit a NAMESPACE:TemperatureState . "
                    + "} "
                    + "GROUP BY ?oldTime ?currentTime ?unit } "

                    // get units in location "bath"
                    + "?location NAMESPACE:hasUnit ?unit . "
                    + "?location NAMESPACE:hasLabel \"Bath\", ?labelLoc FILTER (?labelLoc = \"Bath\") . "

                    // get state value of observation with oldest timestamp
                    + "?obsOld NAMESPACE:hasUnitId ?unit . "
                    + "?obsOld NAMESPACE:hasTimeStamp ?oldTime . "
                    + "?obsOld NAMESPACE:hasStateValue ?oldVal . "

                    // get state value of observation with current timestamp
                    + "?obsCurrent NAMESPACE:hasUnitId ?unit . "
                    + "?obsCurrent NAMESPACE:hasTimeStamp ?currentTime . "
                    + "?obsCurrent NAMESPACE:hasStateValue ?currentVal . "

                    // assign values to operate type (double) and physical type
                    + "BIND (xsd:double(?oldVal) AS ?oldValue) . "
                    + "BIND (xsd:double(?currentVal) AS ?currentValue) . "
                    + "BIND (datatype(?currentVal) AS ?physicalType) . "
                + "} "
                + "GROUP BY ?temperatureDiff ?labelLoc ?physicalType ";

    /**
     * Welche Batterien haben aktuell mindestens 80 % und welche Batterien unter 20 % Energie?
     */
    // if result should be two lists, the query should be split with different conditions (>= 80 and < 20)
    public static final String REQ_10 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>"
            + "SELECT ?label ?value ?scaleType WHERE { "
                // get battery unit
                + "?unit a NAMESPACE:BatteryState . "
                + "?unit NAMESPACE:hasLabel ?label . "

                // get observations of units
                + "?obs NAMESPACE:hasUnitId ?unit . "
                + "?obs NAMESPACE:hasStateValue ?val . "

                // assign values to operate type (double) and physical type
                + "BIND (xsd:double(?val) AS ?value) . "
                + "BIND (datatype(?val) AS ?scaleType) . "

                // filter to get values based on competence question
                + "FILTER (?value >= \"80\"^^xsd:double || ?value < \"20\"^^xsd:double) "
            + "} ";

    /**
     * Welche Geräte im Wohnzimmer sollen in den nächsten 3 Stunden aktiviert werden?
     */
    //Hint: no information about future events in ontology...
    public static final String REQ_11 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT * WHERE { "
                + " "
            + "} ";

    /**
     * In welchen Räumen gab es in den letzten 3 Stunden Bewegung und gab es in diesen Räumen mehrfache Bewegungen
     * (zeitliche Pausen zw. den Bewegungen)?
     */
    public static final String REQ_12 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?label ?multipleMotion WHERE { "

                // get oldest observation timestamp (within 3 hours) and current timestamp of motion units
                + "{ SELECT (MIN(?time) AS ?oldTime) (MAX(?time) AS ?currentTime) ?label WHERE { "
                    + "?obs NAMESPACE:hasTimeStamp ?time . "
                    + "?obs NAMESPACE:hasUnitId ?unit . "
                    + "?obs NAMESPACE:hasStateValue NAMESPACE:MOTION . "
                    + "FILTER (?time > \"" + addTimeToCurrentDateTime(0, -3, 0, 0, 0) + "\"^^xsd:dateTime) . "

                    // get location with MOTION value
                    + "?unit a NAMESPACE:MotionState . "
                    + "?location NAMESPACE:hasUnit ?unit . "
                    + "?location NAMESPACE:hasLabel ?label . "
                    + "FILTER NOT EXISTS { "
                        + "?location NAMESPACE:hasLabel \"Home\" "
                    + "} . "
                + "} "
                + "GROUP BY ?oldTime ?currentTime ?label } "

                // get duration between the two timestamps. timestamps are bound to common location
                + "BIND (xsd:duration(?currentTime - ?oldTime) AS ?duration) . "
                + "BIND (?duration > \"PT0H01M0.000S\"^^xsd:duration AS ?multipleMotion) . "
            + "} ";

    /**
     * Wurden Türen zwischen 22:00 und 6:00 geöffnet und welche sind diese?
     */
    public static final String REQ_13 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT * WHERE { "
                + "?door a NAMESPACE:Door . "
                + "?door NAMESPACE:hasLabel ?label . "
                + "?observation NAMESPACE:hasUnitId ?door . "
                + "?observation NAMESPACE:hasStateValue NAMESPACE:OPEN ,?stateValue FILTER (?stateValue = NAMESPACE:OPEN) . "
                + "?observation NAMESPACE:hasTimeStamp ?time . "
                + "FILTER ( "
                    + "hours(?time) >= \"22\"^^xsd:double || hours(?time) <= \"06\"^^xsd:double "
                + ") . "
            + "} ";

    /**
     * Wie ist die momentane Temperatur im Badezimmer und sind die Türen zu den Nachbarräumen geschlossen?
     */
    public static final String REQ_14 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT (AVG(?value) AS ?temperature) ?physicalType ?connectionLabel ?stateValue WHERE { "

                // get current timestamp of units with temperature
                + "{ SELECT (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?obs NAMESPACE:hasTimeStamp ?time . "
                    + "?obs NAMESPACE:hasUnitId ?unit . "
                    + "?unit a NAMESPACE:TemperatureState . "
                + "} "
                + "GROUP BY ?currentTime ?unit } "

                // get units with location "Bath"
                + "?location NAMESPACE:hasUnit ?unit . "
                + "?location NAMESPACE:hasLabel \"Bath\" . "

                // get temperature value of units via observations
                + "?observation NAMESPACE:hasUnitId ?unit . "
                + "?observation NAMESPACE:hasStateValue ?val . "

                // assign values to operate type (double) and physical type
                + "BIND (xsd:double(?val) AS ?value) . "
                + "BIND (datatype(?val) AS ?physicalType) . "

                // get connections via location "Bath"
                + "?location NAMESPACE:hasConnection ?connection . "
                + "?connection NAMESPACE:hasLabel ?connectionLabel . "
                + "?connection NAMESPACE:hasCurrentStateValue ?stateValue . "
            + "} "
            + "GROUP BY ?stateValue ?physicalType ?val ?connectionLabel ";

    /**
     * Welche Geräte beziehen seit mindestens 3 Stunden Strom (power consumption) im Wohnzimmer?
     */
    public static final String REQ_15 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT * WHERE { "

                // get units with timestamp within 3 hours via powerConsumptionState
                + "{ SELECT ?unit WHERE { "
                    + "?obs NAMESPACE:hasTimeStamp ?time . "
                    + "FILTER (?time > \"" + addTimeToCurrentDateTime(0, -3, 0, 0, 0) + "\"^^xsd:dateTime) . "
                    + "?obs NAMESPACE:hasUnitId ?unit . "
                    + "?unit a NAMESPACE:PowerConsumptionState . "
                + "} "
                + "GROUP BY ?unit } "

                // get units from living room only. units are dalUnits because of powerConsumptionState (see above)
                + "?location NAMESPACE:hasLabel \"Living\" . "
                + "?location NAMESPACE:hasUnit ?unit . "
                + "?location NAMESPACE:hasLabel ?locationLabel . "
                + "?unit NAMESPACE:hasLabel ?unitLabel . "
            + "} ";

    /**
     * Welche Geräte beziehen aktuell Strom (power consumption) und wie viel jeweils?
     */
    public static final String REQ_16 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?unitLabel ?value ?physicalType WHERE { "

                // get units with current timestamp via powerConsumptionState
                + "{ SELECT (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?obs NAMESPACE:hasTimeStamp ?time . "
                    + "?obs NAMESPACE:hasUnitId ?unit . "
                    + "?obs NAMESPACE:hasProviderService NAMESPACE:POWER_CONSUMPTION_STATE_SERVICE . "
                    + "?unit a NAMESPACE:PowerConsumptionState . "
                + "} "
                + "GROUP BY ?currentTime ?unit } "

                // get the correct and unique observations from the units
                + "?observation NAMESPACE:hasUnitId ?unit . "
                + "?observation NAMESPACE:hasTimeStamp ?currentTime . "
                + "?observation NAMESPACE:hasProviderService NAMESPACE:POWER_CONSUMPTION_STATE_SERVICE . "

                // get label and value of the units with current timestamp
                + "?unit NAMESPACE:hasLabel ?unitLabel . "
                + "?observation NAMESPACE:hasStateValue ?val . "

                // assign values to operate type (double) and physical type
                + "BIND (xsd:double(?val) AS ?value) . "
                + "BIND (datatype(?val) AS ?physicalType) . "
            + "} ";

    /**
     * Welche Orte im Apartment haben aktuell einen Helligkeitswert von mindestens 1000lx?
     */
    // hint: situation true, if one sensor has a value of min. 1000.0lx. alternative is a average over all sensor values
    public static final String REQ_17 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?locationLabel ?unitLabel ?value ?physicalType WHERE { "

                // get units with current timestamp via brightnessState
                + "{ SELECT (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?obs NAMESPACE:hasTimeStamp ?time . "
                    + "?obs NAMESPACE:hasUnitId ?unit . "
                    + "?obs NAMESPACE:hasProviderService NAMESPACE:BRIGHTNESS_STATE_SERVICE . "
                    + "?unit a NAMESPACE:BrightnessState . "
                + "} "
                + "GROUP BY ?currentTime ?unit } "

                // get all units with min. 1000.0lx brightness via observation
                + "?observation NAMESPACE:hasTimeStamp ?currentTime . "
                + "?observation NAMESPACE:hasUnitId ?unit . "
                + "?observation NAMESPACE:hasProviderService NAMESPACE:BRIGHTNESS_STATE_SERVICE . "
                + "?observation NAMESPACE:hasStateValue ?val . "
                + "FILTER (?value >= \"1000.0\"^^xsd:double) . "

                // assign values to operate type (double) and physical type
                + "BIND (xsd:double(?val) AS ?value) . "
                + "BIND (datatype(?val) AS ?physicalType) . "

                // get the location of the positive units (without "home")
                + "?location NAMESPACE:hasUnit ?unit . "
                + "?location NAMESPACE:hasLabel ?locationLabel . "
                + "FILTER NOT EXISTS { "
                    + "?location NAMESPACE:hasLabel \"Home\" "
                + "} . "

                // get unit label
                + "?unit NAMESPACE:hasLabel ?unitLabel . "
            + "} ";

    /**
     * Welche FarbLampen sind aktuell im Flur eingeschaltet und welche Werte haben diese?
     */
    public static final String REQ_18 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?locationLabel ?unitLabel ?valueColor ?valueBrightness WHERE { "

                // get lamps with current timestamp based on powerState
                + "{ SELECT (MAX(?timePower) AS ?currentTimePower) ?unitPower WHERE { "
                    + "?obsPower NAMESPACE:hasTimeStamp ?timePower . "
                    + "?obsPower NAMESPACE:hasUnitId ?unitPower . "
                    + "?obsPower NAMESPACE:hasProviderService NAMESPACE:POWER_STATE_SERVICE . "
                    + "?unitPower a NAMESPACE:ColorableLight . "
                    + "?unitPower a NAMESPACE:PowerState . "
                    + "?obsPower NAMESPACE:hasStateValue NAMESPACE:ON . "
                + "} "
                + "GROUP BY ?currentTimePower ?unitPower } "

                // get lamps with current timestamp based on colorState
                + "{ SELECT (MAX(?timeColor) AS ?currentTimeColor) ?unitColor WHERE { "
                    + "?obsColor NAMESPACE:hasTimeStamp ?timeColor . "
                    + "?obsColor NAMESPACE:hasUnitId ?unitColor . "
                    + "?obsColor NAMESPACE:hasProviderService NAMESPACE:COLOR_STATE_SERVICE . "
                    + "?unitColor a NAMESPACE:ColorableLight . "
                    + "?unitColor a NAMESPACE:ColorState . "
                + "} "
                + "GROUP BY ?currentTimeColor ?unitColor } "

                // get lamps with current timestamp based on brightnessState
                + "{ SELECT (MAX(?timeBrightness) AS ?currentTimeBrightness) ?unitBrightness WHERE { "
                    + "?obsBrightness NAMESPACE:hasTimeStamp ?timeBrightness . "
                    + "?obsBrightness NAMESPACE:hasUnitId ?unitBrightness . "
                    + "?obsBrightness NAMESPACE:hasProviderService NAMESPACE:BRIGHTNESS_STATE_SERVICE . "
                    + "?unitBrightness a NAMESPACE:ColorableLight . "
                    + "?unitBrightness a NAMESPACE:BrightnessState . "
                + "} "
                + "GROUP BY ?currentTimeBrightness ?unitBrightness } "

                // get the common unit with current observations to power, color and brightness
                + "FILTER (?unitColor = ?unitPower && ?unitColor = ?unitBrightness) . "

                // get color value of lamps
                + "?observationColor NAMESPACE:hasTimeStamp ?currentTimeColor . "
                + "?observationColor NAMESPACE:hasUnitId ?unitColor . "
                + "?observationColor NAMESPACE:hasProviderService NAMESPACE:COLOR_STATE_SERVICE . "
                + "?observationColor NAMESPACE:hasStateValue ?valueColor . "

                // get brightness value of lamps
                + "?observationBrightness NAMESPACE:hasTimeStamp ?currentTimeBrightness . "
                + "?observationBrightness NAMESPACE:hasUnitId ?unitBrightness . "
                + "?observationBrightness NAMESPACE:hasProviderService NAMESPACE:BRIGHTNESS_STATE_SERVICE . "
                + "?observationBrightness NAMESPACE:hasStateValue ?valueBrightness . "

                // get labels of lamps and their locations
                + "?unitPower NAMESPACE:hasLabel ?unitLabel . "
                + "?location NAMESPACE:hasUnit ?unitPower . "
                + "?location NAMESPACE:hasLabel ?locationLabel . "
                + "FILTER NOT EXISTS { "
                    + "?location NAMESPACE:hasLabel \"Home\" "
                + "} . "
            + "} ";

    /**
     * Befindet sich aktuell im Wohnzimmer mindestens eine Person und ist dort momentan das Licht eingeschaltet?
     */
    // Hint: query via motion units. alternative query in REQ_20
    public static final String REQ_19 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?motionLabel ?isMotion ?lampLabel ?isLampOn WHERE { "

                // get motion units via current observation
                + "{ SELECT (MAX(?timeMotion) AS ?currentTimeMotion) ?motionUnit WHERE { "
                    + "?motionUnit a NAMESPACE:MotionState . "
                    + "?obsMotion NAMESPACE:hasUnitId ?motionUnit . "
                    + "?obsMotion NAMESPACE:hasTimeStamp ?timeMotion . "
                    + "?obsMotion NAMESPACE:hasProviderService NAMESPACE:MOTION_STATE_SERVICE . "
                + "} GROUP BY ?currentTimeMotion ?motionUnit } "

                // is there a motion unit with current value MOTION?
                + "BIND (EXISTS { "
                    + "?motionObs NAMESPACE:hasUnitId ?motionUnit . "
                    + "?motionObs NAMESPACE:hasTimeStamp ?currentTimeMotion . "
                    + "?motionObs NAMESPACE:hasProviderService NAMESPACE:MOTION_STATE_SERVICE . "
                    + "?motionObs NAMESPACE:hasStateValue NAMESPACE:MOTION } "
                + "AS ?isMotion) . "

                // get lamp units via current observation
                + "{ SELECT (MAX(?timeLamp) AS ?currentTimeLamp) ?lampUnit WHERE { "
                    + "{ { ?lampUnit a NAMESPACE:ColorableLight . } "
                        + "UNION "
                    + "{ ?lampUnit a NAMESPACE:DimmableLight . } } "
                    + "?obsLamp NAMESPACE:hasUnitId ?lampUnit . "
                    + "?obsLamp NAMESPACE:hasTimeStamp ?timeLamp . "
                    + "?obsLamp NAMESPACE:hasProviderService NAMESPACE:POWER_STATE_SERVICE . "
                + "} GROUP BY ?currentTimeLamp ?lampUnit } "

                // get motion and lamp labels of location "living"
                + "?lampUnit NAMESPACE:hasLabel ?lampLabel . "
                + "?motionUnit NAMESPACE:hasLabel ?motionLabel . "
                + "?location NAMESPACE:hasUnit ?lampUnit . "
                + "?location NAMESPACE:hasUnit ?motionUnit . "
                + "?location NAMESPACE:hasLabel \"Living\" . "

                // is there a lamp unit with current value ON?
                + "BIND (EXISTS { "
                    + "?observation NAMESPACE:hasTimeStamp ?currentTimeLamp . "
                    + "?observation NAMESPACE:hasUnitId ?lampUnit . "
                    + "?observation NAMESPACE:hasProviderService ?POWER_STATE_SERVICE . "
                    + "?observation NAMESPACE:hasStateValue NAMESPACE:ON . } "
                + "AS ?isLampOn) . "
            + "} ";

    /**
     * Befinden sich aktuell Personen im Apartment und in welchen Bereichen befinden sie sich?
     */
    // Hint: query via userPresenceState
    public static final String REQ_20 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
                + "SELECT ?unitLabel ?locationLabel WHERE { "

                // get all user units via current observation
                + "{ SELECT (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?unit a NAMESPACE:UserPresenceState . "
                    + "?obs NAMESPACE:hasUnitId ?unit . "
                    + "?obs NAMESPACE:hasTimeStamp ?time . "
                    + "?obs NAMESPACE:hasStateValue NAMESPACE:AT_HOME . "
//                    + "?obs NAMESPACE:hasProviderService NAMESPACE: . " //unknown state_service...
                + "} GROUP BY ?currentTime ?unit } "

                // get labels of units and their locations
                + "?unit NAMESPACE:hasLabel ?unitLabel . "
                + "?location NAMESPACE:hasUnit ?unit . "
                + "?location NAMESPACE:hasLabel ?locationLabel . "
                + "FILTER NOT EXISTS { "
                    + "?location NAMESPACE:hasLabel \"Home\" "
                + "} . "
            + "} ";

    /**
     * Gibt es aktuell eine beliebige Verbindung (Türen offen) zwischen zwei Räumen, die eine Temperaturdifferenz von
     * 10°C aufweisen?
     */
    public static final String REQ_21 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?door ((MAX(?average) - MIN(?average)) AS ?diffTemperature) ?physicalType WHERE { "

                // get all door units via current observation
                + "{ SELECT (MAX(?timeDoor) AS ?currentTimeDoor) ?door WHERE { "
                    + "?door a NAMESPACE:DoorState . "
                    + "?obsDoor NAMESPACE:hasUnitId ?door . "
                    + "?obsDoor NAMESPACE:hasTimeStamp ?timeDoor . "
                    + "?obsDoor NAMESPACE:hasStateValue NAMESPACE:OPEN . "
                    + "?obsDoor NAMESPACE:hasProviderService NAMESPACE:DOOR_STATE_SERVICE . "
                + "} GROUP BY ?currentTimeDoor ?door } "

                // get all locations with the average temperature
                + "{ SELECT (AVG(?val) AS ?average) ?location ?physicalType WHERE { "
                    + "?observation NAMESPACE:hasUnitId ?tempUnit . "
                    + "?observation NAMESPACE:hasTimeStamp ?currentTimeTemp . "
                    + "?observation NAMESPACE:hasProviderService NAMESPACE:TEMPERATURE_STATE_SERVICE . "
                    + "?observation NAMESPACE:hasStateValue ?value . "

                    // assign values to operate type (double) and physical type
                    + "BIND (xsd:double(?value) AS ?val) . "
                    + "BIND (datatype(?value) AS ?physicalType) . "

                    // get locations of temperature units
                    + "?location NAMESPACE:hasUnit ?tempUnit . "
                    + "FILTER NOT EXISTS { "
                        + "?location NAMESPACE:hasLabel \"Home\" "
                    + "} . "

                    // get all temperature units via current observation
                    + "{ SELECT (MAX(?timeTemp) AS ?currentTimeTemp) ?tempUnit WHERE { "
                        + "?tempUnit a NAMESPACE:TemperatureState . "
                        + "?obsTemp NAMESPACE:hasUnitId ?tempUnit . "
                        + "?obsTemp NAMESPACE:hasTimeStamp ?timeTemp . "
                        + "?obsTemp NAMESPACE:hasProviderService NAMESPACE:TEMPERATURE_STATE_SERVICE . "
                    + "} GROUP BY ?currentTimeTemp ?tempUnit } "
                + "} GROUP BY ?location ?average ?physicalType } "

                // get the door connection between two adjacent-rooms
                + "?location NAMESPACE:hasConnection ?door . "
            + "} "
            + "GROUP BY ?door ?diffTemperature ?physicalType ";

    /**
     * Gibt es Geräte in der Küche, die länger als 3 Stunden eingeschaltet sind und welche sind diese?
     */
    public static final String REQ_22 =
            "PREFIX NAMESPACE:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>"
            + "SELECT ?unitLabel WHERE { "

                    // get the observations which have additionally stateValue ON
                    + "?observation NAMESPACE:hasTimeStamp ?currentTime . "
                    + "?observation NAMESPACE:hasUnitId ?unit . "
                    + "?observation NAMESPACE:hasProviderService NAMESPACE:POWER_STATE_SERVICE . "
                    + "?observation NAMESPACE:hasStateValue NAMESPACE:ON . "

                    // get the current time and unit of all unitTypes which have a powerState
                    + "{ SELECT (MAX(?time) AS ?currentTime) ?unit WHERE { "
                        + "?obs NAMESPACE:hasTimeStamp ?time . "
                        + "?obs NAMESPACE:hasUnitId ?unit . "
                        + "?obs NAMESPACE:hasProviderService NAMESPACE:POWER_STATE_SERVICE . "
                        + "?unit a NAMESPACE:PowerState . "
                        + "?unit a ?unitType . "
                        + "?unitType rdfs:subClassOf NAMESPACE:DalUnit . "
                    + "} "
                    + "GROUP BY ?currentTime ?unit } "

                    // check if the latest timeStamp is older than 3hours
                    + "FILTER (?currentTime <= \"" + addTimeToCurrentDateTime(0, -3, 0, 0, 0) + "\"^^xsd:dateTime) . "

                    // get location "kitchen" and labels of the units
                    + "?location NAMESPACE:hasLabel \"Kitchen\" . "
                    + "FILTER NOT EXISTS { "
                        + "?location NAMESPACE:hasLabel \"Home\" "
                    + "} . "
                    + "?location NAMESPACE:hasUnit ?unit . "
                    + "?unit NAMESPACE:hasLabel ?unitLabel . "
            + "} ";

    /**
     * Private Constructor.
     */
    private CompetencyQuestions() {
    }

//    /**
//     * Method returns the current dateTime.
//     * @return String in format yyyy-MM-dd'T'HH:mm:ss.SSSXXX
//     */
//    public static String getCurrentDateTime() {
//        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.ENGLISH);
//        final Date date = new Date();
//        return simpleDateFormat.format(date);
//    }

    /**
     * Method adds/subtracts time from the current dateTime.
     *
     * @param minutes are the minutes.
     * @param hours are the hours.
     * @param days are the days.
     * @param months are the months.
     * @param years are the years.
     * @return the changed dateTime as String.
     */
    public static String addTimeToCurrentDateTime(final int minutes, final int hours, final int days, final int months, final int years) {
        final OffsetDateTime now = OffsetDateTime.now();

        now.plusMinutes(minutes);
        now.plusHours(hours);
        now.plusDays(days);
        now.plusMonths(months);
        now.plusYears(years);

        return now.toString();
    }
}
