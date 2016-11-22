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

import org.apache.jena.sparql.engine.QueryEngineBase;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;

/**
 * Created by agatting on 16.11.16.
 */
final class QueryStrings {

    //TODO Use webTool/webFrontend for query input

    //CHECKSTYLE.OFF: MultipleStringLiterals
    // competence questions for ontology validation

    //TODO general: get concrete object...
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
    static final String REQ_6_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "SELECT ?label ?location (COUNT(?location) as ?count) WHERE { "
                    + "?observation NS:hasUnitId ?unit . "
                    + "?location NS:hasUnit ?unit . "
                    + "?location NS:hasLabel ?label . "
                    + "FILTER regex(?label, \"Home\" ) . " // TODO FILTER -> without specific literal
                + "} "
                + "GROUP BY ?label ?location "
                + "ORDER BY DESC(?count) LIMIT 1 ";

    /**
     * Welche Geräte wurden in den letzten 3 Stunden manipuliert (z.B. ein/ausschalten) und wo befinden sich diese?
     */
    static final String REQ_7_0 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "SELECT * WHERE { "
                    + "BIND (now() - hours(?h = 3) AS ?test ) . "
//                    + "BIND (now() AS ?currentTime) . "
//                    + "?observation NS:hasTimeStamp ?time . "
//                    + "FILTER (?time > (now() -  "
//                    + "BIND (hours(?currentTime) AS ?currentHours) "
                + "} ";


    //CHECKSTYLE.ON: MultipleStringLiterals
    /**
     * Private Constructor.
     */
    private QueryStrings() {
    }
}
