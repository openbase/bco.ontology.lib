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

import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by agatting on 16.11.16.
 */
public final class QueryStrings {

    //TODO Use webTool/webFrontend for query input
    //TODO later: get concrete object of query...
    //TODO make all strings like "home" generic
    //TODO make request of unitType generic

    //TODO add link to providerService up to REQ_16

    //CHECKSTYLE.OFF: MultipleStringLiterals

    // competence questions for ontology validation based on SPARQL 1.1 Query Language
    //TODO readability -> get Labels and question

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
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?unitLabel ?stateValue ?locationLabel WHERE { "
                // get all units tamperDetector
                + "?unit a NS:TamperDetector . "
                + "?unit NS:hasLabel ?unitLabel . "
                // get all observations of the units
                + "?observation NS:hasUnitId ?unit . "
                + "?observation NS:hasStateValue NS:OPEN, ?stateValue FILTER (?stateValue = NS:OPEN) . "
                // get locations of the units
                + "?location NS:hasUnit ?unit . "
                + "?location NS:hasLabel ?locationLabel . "
                // filter all locations with specific literal
                + "FILTER NOT EXISTS { "
                    + "?location NS:hasLabel \"Home\" . "
                + "} . "
            + "} ";

//    /**
//     * Welche units befinden sich im Wohnzimmer und welche sind davon eingeschaltet/erreichbar?
//     */
//    //TODO @Ontology: list all units (disabled enclosed)?
//    public static final String REQ_2 =
//            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
//            + "SELECT ?unitLabel ?stateValue WHERE { "
//                + "?location NS:hasLabel \"Living\" . "
//                + "?location NS:hasUnit ?unit . "
//                + "?unit NS:hasLabel ?unitLabel . "
//                + "?observation NS:hasUnitId ?unit . "
//                + "?observation NS:hasStateValue NS:ENABLED, ?stateValue FILTER (?stateValue = NS:ENABLED) . "
//                + "?unit a NS:EnablingState . "
//            + "}";

    /**
     * Welche Lampe wurden im Apartment bisher am häufigsten verwendet?
     */
    public static final String REQ_3 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?lampLabel (COUNT(?unit) as ?count) ?locationLabel WHERE { "
                // get all lamp units and their labels
                + "{ { ?unit a NS:ColorableLight . } "
                    + "UNION "
                + "{ ?unit a NS:DimmableLight . } } "
                + "?unit NS:hasLabel ?lampLabel . "
                // get the observations with value ON or OFF
                + "?observation NS:hasUnitId ?unit . "
                + "{ { ?observation NS:hasStateValue NS:ON . } "
                    + "UNION "
                + "{ ?observation NS:hasStateValue NS:OFF . } } "
                // get optional their label and location
                + "OPTIONAL { "
                    + "?location NS:hasUnit ?unit . "
                    + "?location NS:hasLabel ?locationLabel . "
                    + "FILTER not exists { "
                        + "?location NS:hasLabel \"Home\" "
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
    //TODO maybe check all units (here rollerShutter), if they have min. one observation with a stateValue ...
    public static final String REQ_4 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            //TODO better solution of SELECT [...]
            // select lamps only, if conditions of the question apply
            + "SELECT (IF(?isRolUP = false && ?time = true, ?lampLabel, 0) as ?labelLamp) WHERE { "
                // get the current observation of the lamp units
                + "{ SELECT (MAX(?timeLamp) AS ?currentTimeLamp) ?lamp WHERE { "
                    + "{ { ?lamp a NS:ColorableLight . } "
                        + "UNION "
                    + "{ ?lamp a NS:DimmableLight . } } "
                    + "?location NS:hasLabel \"Living\" . "
                    + "?location NS:hasUnit ?lamp . "
                    + "?currentObsLamp NS:hasUnitId ?lamp . "
                    + "?currentObsLamp NS:hasTimeStamp ?timeLamp . "
                + "} GROUP BY ?currentTimeLamp ?lamp } "

                // is there a lamp with current value ON?
                + "?observationLamp NS:hasUnitId ?lamp . "
                + "?observationLamp NS:hasStateValue NS:ON . "
                // get the labels of the lamps
                + "?lamp NS:hasLabel ?lampLabel . "

                // get current observation of rollerShutter units
                + "{ SELECT (MAX(?rolTime) AS ?currentRolTime) ?rol WHERE { "
                    + "?rol a NS:RollerShutter . "
                    + "?currentObsRol NS:hasUnitId ?rol . "
                    + "?currentObsRol NS:hasTimeStamp ?rolTime . "
                + "} GROUP BY ?rol ?currentRolTime } "
                // is there a rollerShutter with current value UP or UNKNOWN?
                + "BIND (EXISTS { "
                    + "?observationRol NS:hasUnitId ?rol . "
                    + "?observationRol NS:hasStateValue ?rolVal . "
                    + "FILTER (?rolVal = NS:UP || NS:UNKNOWN) } "
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
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            //TODO better solution of SELECT [...]
            + "SELECT (IF(?isMotion = true, ?motionLabel, 0) AS ?labelMotion) "
                + "(IF(?isMotion = true, ?locMotionLabel, 0) AS ?locationLabelMotion) "
                + "(IF(?isMotion = true, NS:MOTION, NS:NO_MOTION) AS ?motionValue) "
                + "(IF(?isMotion = false && ?isLampOn = true, ?lampLabel, 0) AS ?labelLamp) "
                + "(IF(?isMotion = false && ?isLampOn = true, ?locLabelOfLamp, 0) AS ?locationLabelLamp) "
                + "(IF(?isMotion = false, NS:ON, NS:OFF) AS ?lampValue) WHERE { "

                // get current observation of motion units
                + "{ SELECT (MAX(?timeMotion) AS ?currentTimeMotion) ?motionUnit WHERE { "
                    + "?motionUnit a NS:MotionDetector . "
                    + "?currentObsMotion NS:hasUnitId ?motionUnit . "
                    + "?currentObsMotion NS:hasTimeStamp ?timeMotion . "
                + "} GROUP BY ?currentTimeMotion ?motionUnit } "

                // get location- and unit labels of motion unit
                + "OPTIONAL { "
                    + "?motionUnit NS:hasLabel ?motionLabel . "
                    + "?locMotion NS:hasUnit ?motionUnit . "
                    + "?locMotion NS:hasLabel ?locMotionLabel . "
                    + "FILTER not exists { "
                        + "?locMotion NS:hasLabel \"Home\" "
                    + "} . "
                + "} "

                // is there a motion unit with current value MOTION?
                + "BIND (EXISTS { "
                    + "?obsMotion NS:hasUnitId ?motionUnit . "
                    + "?obsMotion NS:hasStateValue NS:MOTION } "
                + "AS ?isMotion) . "

                // get current observation of lamp units
                + "{ SELECT (MAX(?timeLamp) AS ?currentTimeLamp) ?lampUnit WHERE { "
                    + "{ { ?lampUnit a NS:ColorableLight . } "
                        + "UNION "
                    + "{ ?lampUnit a NS:DimmableLight . } } "
                    + "?currentObsLamp NS:hasUnitId ?lampUnit . "
                    + "?currentObsLamp NS:hasTimeStamp ?timeLamp . "
                + "} GROUP BY ?currentTimeLamp ?lampUnit } "

                // get location- and unit labels of lamp unit
                + "OPTIONAL { "
                    + "?lampUnit NS:hasLabel ?lampLabel . "
                    + "?locLamp NS:hasUnit ?lampUnit . "
                    + "?locLamp NS:hasLabel ?locLabelOfLamp . "
                    + "FILTER not exists { "
                        + "?locLamp NS:hasLabel \"Home\" "
                    + "} . "
                + "} "

                // is there a lamp unit with current value ON?
                + "BIND (EXISTS {"
                    + " ?currentObsLamp NS:hasStateValue NS:ON } "
                + "AS ?isLampOn) . "
            + "} ";

    /**
     * Welcher Raum ist anhand der Häufigkeit der Gerätebenutzung am beliebtesten?
     */
    public static final String REQ_6 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>"
            // count the frequency of all locations by appearance via observations
            + "SELECT ?label (COUNT(?location) as ?count) WHERE { "
                // get all units which are dalUnits
                + "?unitType rdfs:subClassOf NS:DalUnit . "
                + "?unit a ?unitType . "
                // get the location and the label via usage of units
                + "?observation NS:hasUnitId ?unit . "
                + "?location NS:hasUnit ?unit . "
                + "?location NS:hasLabel ?label . "
                + "FILTER not exists { "
                    + "?location NS:hasLabel \"Home\" "
                + "} . "
            + "} "
            + "GROUP BY ?label ?location "
            + "ORDER BY DESC(?count) LIMIT 1 ";

    /**
     * Welche unitTypen wurden in den letzten 3 Stunden manipuliert (z.B. ein/ausschalten) und wo befinden sich diese?
     */
    public static final String REQ_7 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>"
            + "SELECT ?unitLabel ?unitType (COUNT(?unit) AS ?unitCount) ?locationLabel WHERE { "
                    // get all timestamps of observations, which are not older than 3 hours from now
                    + "?observation NS:hasTimeStamp ?time . "
                    + "FILTER (?time > \"" + addTimeToCurrentDateTime(-3, 0) + "\"^^xsd:dateTime) . "

                    // get all units, which are dalUnits or Doors or Windows from the observations
                    + "?observation NS:hasUnitId ?unit . "
                    + "?unit NS:hasLabel ?unitLabel . "
                    + "{ { ?unit a ?unitType . } "
                        + "UNION "
                    + "{ ?unit a NS:Door . }"
                        + "UNION "
                    + "{ ?unit a NS:Window . } } "
                    + "?unitType rdfs:subClassOf NS:DalUnit . "

                    // get the locations of the ?units without "Home"
                    + "?location NS:hasUnit ?unit . "
                    + "?location NS:hasLabel ?locationLabel . "
                    + "FILTER not exists { "
                        + "?location NS:hasLabel \"Home\" "
                    + "} . "
                + "} "
                + "GROUP BY ?unitLabel ?unitType ?unitCount ?locationLabel ";

    /**
     * Wie viel Energie wurde in den letzten 3 Stunden im Apartment und wie viel im Wohnzimmer verbraucht?
     */
    //TODO howto create new datatype which based on double...
    public static final String REQ_8 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>"
            + "SELECT (SUM(?currentValue - ?oldValue) AS ?consumption) ?labelLoc  WHERE { "

                // get oldest (within 3 hours) and current timestamp of units with powerConsumption
                + "{ SELECT (MIN(?time) AS ?oldTime) (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?obs NS:hasTimeStamp ?time . "
                    + "FILTER (?time > \"" + addTimeToCurrentDateTime(-3, 0) + "\"^^xsd:dateTime) . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?unit a NS:PowerConsumptionState . "
                + "} "
                + "GROUP BY ?oldTime ?currentTime ?unit } "

                // get units in location "home" or "living" (or ...) only
                + "?location NS:hasUnit ?unit . "
                + "?location NS:hasLabel \"Living\", ?labelLoc FILTER (?labelLoc = \"Living\") . "

                // get state value of observation with oldest timestamp
                + "?obsOld NS:hasUnitId ?unit . "
                + "?obsOld NS:hasTimeStamp ?oldTime . "
                + "?obsOld NS:hasStateValue ?oldValue . "

                // get state value of observation with current timestamp
                + "?obsCurrent NS:hasUnitId ?unit . "
                + "?obsCurrent NS:hasTimeStamp ?currentTime . "
                + "?obsCurrent NS:hasStateValue ?currentValue . "
            + "} "
            + "GROUP BY ?labelLoc ?consumption ";

    /**
     * Wie ist die Temperaturdifferenz im Badezimmer von jetzt zu vor 3 Stunden?
     */
    //TODO datatype physical unit
    public static final String REQ_9 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>"
                + "SELECT ?labelLoc ((SUM(?currentValue - ?oldValue) / COUNT(?unit)) AS ?temperatureDiff) WHERE { "

                    // get oldest (within 3 hours) and current timestamp of units with temperature
                    + "{ SELECT (MIN(?time) AS ?oldTime) (MAX(?time) AS ?currentTime) ?unit WHERE { "
                        + "?obs NS:hasTimeStamp ?time . "
                        + "FILTER (?time > \"" + addTimeToCurrentDateTime(-3, 0) + "\"^^xsd:dateTime) . "
                        + "?obs NS:hasUnitId ?unit . "
                        + "?unit a NS:TemperatureState . "
                    + "} "
                    + "GROUP BY ?oldTime ?currentTime ?unit } "

                    // get units in location "bath"
                    + "?location NS:hasUnit ?unit . "
                    + "?location NS:hasLabel \"Bath\", ?labelLoc FILTER (?labelLoc = \"Bath\") . "

                    // get state value of observation with oldest timestamp
                    + "?obsOld NS:hasUnitId ?unit . "
                    + "?obsOld NS:hasTimeStamp ?oldTime . "
                    + "?obsOld NS:hasStateValue ?oldValue . "

                    // get state value of observation with current timestamp
                    + "?obsCurrent NS:hasUnitId ?unit . "
                    + "?obsCurrent NS:hasTimeStamp ?currentTime . "
                    + "?obsCurrent NS:hasStateValue ?currentValue . "
                + "} "
                + "GROUP BY ?temperatureDiff ?labelLoc ";

    /**
     * Welche Batterien haben aktuell mindestens 80 % und welche Batterien unter 20 % Energie?
     */
    // if result should be two lists, the query should be split with different conditions (>= 0.8 and < 0.2)
    //TODO datatype percentage
    public static final String REQ_10 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>"
            + "SELECT ?label ?value WHERE { "
                // get battery unit
                + "?unit a NS:BatteryState . "
                + "?unit NS:hasLabel ?label . "

                // get observations of units
                + "?obs NS:hasUnitId ?unit . "
                + "?obs NS:hasStateValue ?value . "

                // filter to get values based on competence question
                + "FILTER (?value >= \"0.8\"^^xsd:double || ?value < \"0.2\"^^xsd:double) "
            + "} ";

    /**
     * Welche Geräte im Wohnzimmer sollen in den nächsten 3 Stunden aktiviert werden?
     */
    //TODO no information about future events in ontology...
    public static final String REQ_11 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT * WHERE { "
                + " "
            + "} ";

    /**
     * In welchen Räumen gab es in den letzten 3 Stunden Bewegung und gab es in diesen Räumen mehrfache Bewegungen
     * (zeitliche Pausen zw. den Bewegungen)?
     */
    public static final String REQ_12 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?label ?multipleMotion WHERE { "

                // get oldest observation timestamp (within 3 hours) and current timestamp of motion units
                + "{ SELECT (MIN(?time) AS ?oldTime) (MAX(?time) AS ?currentTime) ?label WHERE { "
                    + "?obs NS:hasTimeStamp ?time . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasStateValue NS:MOTION . "
                    + "FILTER (?time > \"" + addTimeToCurrentDateTime(-3, 0) + "\"^^xsd:dateTime) . "

                    // get location with MOTION value
                    + "?unit a NS:MotionState . "
                    + "?location NS:hasUnit ?unit . "
                    + "?location NS:hasLabel ?label . "
                    + "FILTER NOT EXISTS { "
                        + "?location NS:hasLabel \"Home\" "
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
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT * WHERE { "
                + "?door a NS:Door . "
                + "?door NS:hasLabel ?label . "
                + "?observation NS:hasUnitId ?door . "
                + "?observation NS:hasStateValue NS:OPEN ,?stateValue FILTER (?stateValue = NS:OPEN) . "
                + "?observation NS:hasTimeStamp ?time . "
                + "FILTER ( "
                    + "hours(?time) >= \"22\"^^xsd:double || hours(?time) <= \"06\"^^xsd:double "
                + ") . "
            + "} ";

    /**
     * Wie ist die momentane Temperatur im Badezimmer und sind die Türen zu den Nachbarräumen geschlossen?
     */
    public static final String REQ_14 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT (SUM(?value) / COUNT(?value) AS ?temperature) ?connectionLabel ?stateValue WHERE { "

                // get current timestamp of units with temperature
                + "{ SELECT (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?obs NS:hasTimeStamp ?time . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?unit a NS:TemperatureState . "
                + "} "
                + "GROUP BY ?currentTime ?unit } "

                // get units with location "Bath"
                + "?location NS:hasUnit ?unit . "
                + "?location NS:hasLabel \"Bath\" . "

                // get temperature value of units via observations
                + "?observation NS:hasUnitId ?unit . "
                + "?observation NS:hasStateValue ?value . "

                // get connections via location "Bath"
                + "?location NS:hasConnection ?connection . "
                + "?connection NS:hasLabel ?connectionLabel . "
                + "?connection NS:hasCurrentStateValue ?stateValue . "
            + "} "
            + "GROUP BY ?stateValue ?temperature ?connectionLabel ";

    /**
     * Welche Geräte beziehen seit mindestens 3 Stunden Strom (power consumption) im Wohnzimmer?
     */
    public static final String REQ_15 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT * WHERE { "

                // get units with timestamp within 3 hours via powerConsumptionState
                + "{ SELECT ?unit WHERE { "
                    + "?obs NS:hasTimeStamp ?time . "
                    + "FILTER (?time > \"" + addTimeToCurrentDateTime(-3, 0) + "\"^^xsd:dateTime) . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?unit a NS:PowerConsumptionState . "
                + "} "
                + "GROUP BY ?unit } "

                // get units from living room only. units are dalUnits because of powerConsumptionState (see above)
                + "?location NS:hasLabel \"Living\" . "
                + "?location NS:hasUnit ?unit . "
                + "?location NS:hasLabel ?locationLabel . "
                + "?unit NS:hasLabel ?unitLabel . "
            + "} ";

    /**
     * Welche Geräte beziehen aktuell Strom (power consumption) und wie viel jeweils?
     */
    //TODO dataType physical unit
    public static final String REQ_16 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?unitLabel ?value WHERE { "

                // get units with current timestamp via powerConsumptionState
                + "{ SELECT (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?obs NS:hasTimeStamp ?time . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasProviderService NS:POWER_CONSUMPTION_STATE_SERVICE . "
                    + "?unit a NS:PowerConsumptionState . "
                + "} "
                + "GROUP BY ?currentTime ?unit } "

                // get the correct and unique observations from the units
                + "?observation NS:hasUnitId ?unit . "
                + "?observation NS:hasTimeStamp ?currentTime . "
                + "?observation NS:hasProviderService NS:POWER_CONSUMPTION_STATE_SERVICE . "

                // get label and value of the units with current timestamp
                + "?unit NS:hasLabel ?unitLabel . "
                + "?observation NS:hasStateValue ?value . "
            + "} ";

    /**
     * Welche Orte im Apartment haben aktuell einen Helligkeitswert von mindestens 1000lx?
     */
    // hint: situation true, if one sensor has a value of min. 1000.0lx. alternative is a average over all sensor values
    //TODO dataType physical unit
    public static final String REQ_17 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?locationLabel ?unitLabel ?value WHERE { "

                // get units with current timestamp via brightnessState
                + "{ SELECT (MAX(?time) AS ?currentTime) ?unit WHERE { "
                    + "?obs NS:hasTimeStamp ?time . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasProviderService NS:BRIGHTNESS_STATE_SERVICE . "
                    + "?unit a NS:BrightnessState . "
                + "} "
                + "GROUP BY ?currentTime ?unit } "

                // get all units with min. 1000.0lx brightness via observation
                + "?observation NS:hasTimeStamp ?currentTime . "
                + "?observation NS:hasUnitId ?unit . "
                + "?observation NS:hasProviderService NS:BRIGHTNESS_STATE_SERVICE . "
                + "?observation NS:hasStateValue ?value . "
                + "FILTER (?value >= \"1000.0\"^^xsd:double) . "

                // get the location of the positive units (without "home")
                + "?location NS:hasUnit ?unit . "
                + "?location NS:hasLabel ?locationLabel . "
                + "FILTER NOT EXISTS { "
                    + "?location NS:hasLabel \"Home\" "
                + "} . "

                // get unit label
                + "?unit NS:hasLabel ?unitLabel . "
            + "} ";

    /**
     * Welche Lampen sind aktuell im Flur eingeschaltet und welche Werte haben diese?
     */
    public static final String REQ_18 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?locationLabel ?unitLabel ?valueB ?valueC WHERE { "

                // get lamps with current timestamp based on powerState
                + "{ SELECT (MAX(?timeA) AS ?currentTimeA) ?unitA WHERE { "
                    + "?obsA NS:hasTimeStamp ?timeA . "
                    + "?obsA NS:hasUnitId ?unitA . "
                    + "?obsA NS:hasProviderService NS:POWER_STATE_SERVICE . "
                    + "{ { ?unitA a NS:ColorableLight . } "
                        + "UNION "
                    + "{ ?unitA a NS:DimmableLight . } } "
                    + "?unitA a NS:PowerState . "
                    + "?obsA NS:hasStateValue NS:ON . "
                + "} "
                + "GROUP BY ?currentTimeA ?unitA } "

                    // get lamps with current timestamp based on colorState
                + "{ SELECT (MAX(?timeB) AS ?currentTimeB) ?unitB WHERE { "
                    + "?obsB NS:hasTimeStamp ?timeB . "
                    + "?obsB NS:hasUnitId ?unitB . "
                    + "?obsB NS:hasProviderService NS:COLOR_STATE_SERVICE . "
                    + "{ { ?unitB a NS:ColorableLight . } "
                        + "UNION "
                    + "{ ?unitB a NS:DimmableLight . } } "
                    + "?unitB a NS:ColorState . "
                + "} "
                + "GROUP BY ?currentTimeB ?unitB } "

                    // get lamps with current timestamp based on brightnessState
                + "{ SELECT (MAX(?timeC) AS ?currentTimeC) ?unitC WHERE { "
                    + "?obsC NS:hasTimeStamp ?timeC . "
                    + "?obsC NS:hasUnitId ?unitC . "
                    + "?obsC NS:hasProviderService NS:BRIGHTNESS_STATE_SERVICE . "
                    + "{ { ?unitC a NS:ColorableLight . } "
                        + "UNION "
                    + "{ ?unitC a NS:DimmableLight . } } "
                    + "?unitC a NS:BrightnessState . "
                + "} "
                + "GROUP BY ?currentTimeC ?unitC } "

                //TODO here: colorLamps only -> differentiate between colorLamp, dimmerLamp and normalLamp
                + "FILTER (?unitB = ?unitA && ?unitB = ?unitC) . "

                + "?observationB NS:hasTimeStamp ?currentTimeB . "
                + "?observationB NS:hasUnitId ?unitB . "
                + "?observationB NS:hasProviderService NS:COLOR_STATE_SERVICE . "
                + "?observationB NS:hasStateValue ?valueB . "

                + "?observationC NS:hasTimeStamp ?currentTimeC . "
                + "?observationC NS:hasUnitId ?unitC . "
                + "?observationC NS:hasProviderService NS:BRIGHTNESS_STATE_SERVICE . "
                + "?observationC NS:hasStateValue ?valueC . "

                + "?unitA NS:hasLabel ?unitLabel . "
                + "?location NS:hasUnit ?unitA . "
                + "?location NS:hasLabel ?locationLabel . "
                + "FILTER NOT EXISTS { "
                    + "?location NS:hasLabel \"Home\" "
                + "} . "
            + "} ";

    //CHECKSTYLE.ON: MultipleStringLiterals
    /**
     * Private Constructor.
     */
    private QueryStrings() {
    }

    /**
     * Method returns the current dateTime.
     * @return String in format yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static String getCurrentDateTime() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_TIME, Locale.ENGLISH);
        final Date date = new Date();
        return simpleDateFormat.format(date);
    }

    /**
     * Method adds/subtracts time from the current dateTime.
     * @param hours The hours.
     * @param minutes The minutes.
     * @return The changed dateTime as String.
     */
    //TODO expand method with years, days....
    public static String addTimeToCurrentDateTime(final int hours, final int minutes) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_TIME, Locale.ENGLISH);
        final Date now = new Date();
        Date newDate = DateUtils.addHours(now, hours);
        newDate = DateUtils.addMinutes(newDate, minutes);

        return simpleDateFormat.format(newDate);
    }
}
