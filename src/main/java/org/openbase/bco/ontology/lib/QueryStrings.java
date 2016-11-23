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
 * Created by agatting on 16.11.16.
 */
final class QueryStrings {

    //TODO Use webTool/webFrontend for query input
    //TODO later: get concrete object of query...

    //CHECKSTYLE.OFF: MultipleStringLiterals

    // competence questions for ontology validation based on SPARQL 1.1 Query Language

    /**
     * Wurde jemals ein Sabotagekontakt ausgelöst und wenn ja, wo?
     */
    static final String REQ_1 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT * WHERE { "
                + "?unit a NS:TamperDetector . "
                + "?unit NS:hasLabel ?label . "
                + "?observation NS:hasUnitId ?unit . "
                + "?observation NS:hasStateValue NS:OPEN . "
                + "?location NS:hasUnit ?unit . "
            + "} ";

    /**
     * Welche unitTypen befinden sich im Wohnzimmer und welche sind davon eingeschaltet/erreichbar?
     */
    //TODO @Ontology: list all units (disabled enclosed)?
    static final String REQ_2 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT * WHERE { "
                + "?location NS:hasLabel \"Living\" . "
                + "?location NS:hasUnit ?unit . "
                + "?observation NS:hasUnitId ?unit . "
                + "?observation NS:hasStateValue NS:ENABLED . "
                + "?unit a NS:EnablingState . "
            + "}";

    /**
     * Welche Lampe wurden im Apartment bisher am häufigsten verwendet?
     */
    static final String REQ_3 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?unit (COUNT(?unit) as ?count) WHERE { "
                    + "{ { ?unit a NS:ColorableLight . } "
                        + "UNION "
                    + "{ ?unit a NS:DimmableLight . } } "
                    + "?observation NS:hasUnitId ?unit . "
                    + "{ { ?unit a NS:BrightnessState . } "
                        + "UNION "
                    + "{ ?unit a NS:ColorState . } } "
                    + "{ { ?observation NS:hasStateValue NS:ON . } "
                        + "UNION "
                    + "{ ?observation NS:hasStateValue NS:OFF . } } "
            + "} "
            + "GROUP BY ?unit "
            + "ORDER BY DESC(?count) LIMIT 1 ";

    /**
     * ((Sind im Moment Lampen im Wohnzimmer an)), alle Rollos unten und ist es zwischen 22:00 - 6:00 Uhr? Welche sind diese?
     */
    //TODO ask query => true, then select query?
    //TODO order important for efficiency? if yes: (here) first ask current time...
    //TODO dateTimeStamp or dateTime?
    static final String REQ_4_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT * WHERE { "
                + "{ { ?lamp a NS:ColorableLight . } "
                    + "UNION "
                + "{ ?lamp a NS:DimmableLight . } } "
                + "{ { ?lamp a NS:BrightnessState . } "
                    + "UNION "
                + "{ ?lamp a NS:ColorState . } } "
                + "?location NS:hasLabel \"Living\" . "
                + "?location NS:hasUnit ?lamp . "
                + "?observation NS:hasUnitId ?lamp . "
                + "?observation NS:hasStateValue ?stateValue . " //NS:ON
                + "?observation NS:hasTimeStamp ?time . "
            + "} "
            + "ORDER BY DESC(?time) LIMIT 1 ";

    //TODO maybe more efficiency: additional property like 'hasCurrentStateValue', because lots of queries ask
    //TODO the current stateValue of a unit. With the additional amount the solution pool is less...
    /**
     * Sind im Moment Lampen im Wohnzimmer an, ((alle Rollos unten)) und ist es zwischen 22:00 - 6:00 Uhr? Welche sind diese?
     */
    static final String REQ_4_1 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?observation ?unit (max(?time) as ?max) WHERE { "
                + "?unit a NS:RollerShutter . "
                + "?observation NS:hasUnitId ?unit . "
                + "?observation NS:hasStateValue ?stateValue . "
                + "?observation NS:hasTimeStamp ?time . "
                //...
            + "} "
            + "GROUP BY ?observation ?unit ";

    /**
     * Was ist die aktuelle Zeit?
     */
    static final String REQ_4_2 =
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?currentTime ?currentHours WHERE { "
                    + "BIND (now() AS ?currentTime) "
                    + "BIND (hours(?currentTime) AS ?currentHours) "
                + "}";

    /**
     * ((Befindet sich momentan mindestens eine Person im Apartment)) und wenn nicht, sind alle Lampen ausgeschaltet?
     */
    //Hint: used additional property 'hasCurrentStateValue' (see above REQ_4_1)
    static final String REQ_5_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                    + "?motionUnit a NS:MotionDetector . " //NS:MotionState
                    + "?motionUnit NS:hasCurrentStateValue NS:MOTION . "
                + "} ";

    /**
     * Befindet sich momentan mindestens eine Person im Apartment und wenn nicht, ((sind alle Lampen ausgeschaltet?))
     */
    //TODO inverse ASK? Solution is true, if there is a lamp with stateValue ON
    static final String REQ_5_1 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "ASK  { "
                    + "{ { ?lamp a NS:ColorableLight . } "
                        + "UNION "
                    + "{ ?lamp a NS:DimmableLight . } } "
                    + "?lamp NS:hasCurrentStateValue NS:ON . "
                + "} ";

    /**
     * Welcher Raum ist anhand der Häufigkeit der Gerätebenutzung am beliebtesten?
     */
    //TODO get actuators (DAL?!), not all units ...
    static final String REQ_6_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "SELECT ?label ?location (COUNT(?location) as ?count) WHERE { "
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
    static final String REQ_7_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?unitLabel ?locationLabel WHERE { "
                //TODO time: last 3 hours ...
//                    + "BIND (now() AS ?timeHours) . "
//                    + "BIND (?time + "2016-11-23T03:00:00.000+01:00"^^xsd:dateTime AS ?result ) . "
//                    + "FILTER (?time > (now() -  "
//                    + "BIND (hours(?currentTime) AS ?currentHours) "

                    + "?observation NS:hasUnitId ?unit . "
                    + "?unit NS:hasLabel ?unitLabel . "
//                        + "?unit NS:hasCurrentStateValue NS:ENABLED . " //TODO unit -> multiple states?
                    + "?location NS:hasUnit ?unit . "
                    + "?location NS:hasLabel ?locationLabel . "
                    + "FILTER not exists { "
                        + "?location NS:hasLabel \"Home\" "
                    + "} . "
                + "} "
                + "GROUP BY ?unitLabel ?locationLabel ";

    /**
     * Wie viel Energie wurde in den letzten 3 Stunden ((im Apartment)) und wie viel im Wohnzimmer verbraucht?
     */
    //TODO general: stateValue literal based on xsd:string?!
    //Hint: powerConsumptionState only (=> powerConsumptionSensor)
    static final String REQ_8_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "SELECT (CONCAT(STR(SUM(?diffConsumption)), STR(?dataUnit)) AS ?totalConsumption) WHERE { "
                    //TODO time: last 3 hours ...

                    //TODO get oldest observation within 3 hours ...
//                    + "VALUES ?maxTimeBorder { \"2016-11-23T12:24:26.513+01:00\"^^xsd:dateTime } . "
//                    + "FILTER not exists { "
//                        + "?observation NS:hasTimeStamp (?time < ?maxTimeBorder) . "
//                    + "} . "

                    // ?oldConsumption -> consumption three hours in the past
                    + "?consumptionUnit a NS:PowerConsumptionState . "
                    + "?location NS:hasLabel \"Home\" . "
                    + "?location NS:hasUnit ?consumptionUnit . "
                    + "?observation NS:hasUnitId ?consumptionUnit . "
                    + "?observation NS:hasStateValue ?oldConsumption . "
                    + "BIND (xsd:double (strbefore (?oldConsumption, \",\" )) AS ?oldBuf) . "
                    + "?consumptionUnit NS:hasCurrentStateValue ?newConsumption . "
                    + "BIND (xsd:double (strbefore (?newConsumption, \",\" )) AS ?newBuf) . "
                    + "BIND (?newBuf - ?oldBuf AS ?diffConsumption) . " //TODO check negative value (java)
//                    + "BIND (IF (?bla < \"0\"^^xsd:double, MINUS(?bla), ?bla) AS ?ggg"
                    + "BIND (strafter (?oldConsumption, \",\" ) AS ?dataUnit) . " //TODO possibility of one call...?
                + "} "
                + "GROUP BY ?dataUnit";

    /**
     * Wie ist die Temperaturdifferenz im Badezimmer von jetzt zu vor 3 Stunden?
     */
    static final String REQ_9_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                //TODO check negative value (java)
                + "SELECT (CONCAT(STR((SUM(?oldBuf) / COUNT(?oldBuf)) - (SUM(?newBuf) / COUNT(?newBuf)))" +
                    ", STR(?dataUnit)) AS ?diffTemp) WHERE { "
                    //TODO time: last 3 hours ...

                    //TODO get oldest observation within 3 hours ...

                    // ?oldTemp -> temperature three hours in the past
                    + "?temperatureUnit a NS:TemperatureSensor . "
                    + "?location NS:hasLabel \"Bath\" . "
                    + "?location NS:hasUnit ?temperatureUnit . "
                    + "?observation NS:hasUnitId ?temperatureUnit . "
                    + "?observation NS:hasStateValue ?oldTemp . "
                    + "BIND (xsd:double (strbefore (?oldTemp, \",\" )) AS ?oldBuf) . "
                    + "?temperatureUnit NS:hasCurrentStateValue ?newTemp . "
                    + "BIND (xsd:double (strbefore (?newTemp, \",\" )) AS ?newBuf) . "
                    + "BIND (strafter (?oldTemp, \",\" ) AS ?dataUnit) . " //TODO possibility of one call...?
                + "} "
                + "GROUP BY ?dataUnit";

    /**
     * Welche Batterien haben aktuell ((mindestens 80 %)) und welche Batterien unter 20 % Energie?
     */
    static final String REQ_10_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "SELECT * WHERE { "
                    + "?batteryUnit a NS:Battery . "
                    + "?batteryUnit NS:hasLabel ?label . "
                    + "?batteryUnit NS:hasCurrentStateValue ?batteryVal . "
                    + "BIND (xsd:double (strbefore (?batteryVal, \",\" )) AS ?batteryValue) . "
                    + "FILTER ( "
                        + "?batteryValue >= \"0.8\"^^xsd:double "
                    + ") . "
                + "} ";

    /**
     * Welche Batterien haben aktuell mindestens 80 % und welche Batterien ((unter 20 %)) Energie?
     */
    static final String REQ_10_1 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "SELECT * WHERE { "
                    + "?batteryUnit a NS:Battery . "
                    + "?batteryUnit NS:hasLabel ?label . "
                    + "?batteryUnit NS:hasCurrentStateValue ?batteryVal . "
                    + "BIND (xsd:double (strbefore (?batteryVal, \",\" )) AS ?batteryValue) . "
                    + "FILTER ( "
                        + "?batteryValue < \"0.2\"^^xsd:double "
                    + ") . "
                + "} ";

    /**
     * Welche Geräte im Wohnzimmer sollen in den nächsten 3 Stunden aktiviert werden?
     */
    //TODO no information about future events in ontology...
    static final String REQ_11_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "SELECT * WHERE { "
                    + ""
                + "} ";

    /**
     * In welchen Räumen gab es in den letzten 3 Stunden Bewegung und gab es in diesen Räumen mehrfache Bewegungen
     * (zeitliche Pausen zw. den Bewegungen)?
     */
    static final String REQ_12_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "SELECT * WHERE { "
                    + ""
                + "} ";

    /**
     * Wurden Türen zwischen 22:00 und 6:00 geöffnet und welche sind diese?
     */
    static final String REQ_13_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "SELECT * WHERE { "
                    + "?door a NS:Door . "
                    + "?observation NS:hasUnitId ?door . "
                    + "?observation NS:hasStateValue NS:OPEN . "
                    + "?observation NS:hasTimeStamp ?time . "
                    + "FILTER ( "
                        + "hours(?time) >= \"22\"^^xsd:double " //TODO
                    + ") . "
                + "} ";


    //CHECKSTYLE.ON: MultipleStringLiterals
    /**
     * Private Constructor.
     */
    private QueryStrings() {
    }
}
