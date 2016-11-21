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

    //CHECKSTYLE.OFF: MultipleStringLiterals
    // competence questions for ontology validation
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
            + "ORDER BY DESC(?count)";

    /**
     * Was ist die aktuelle Zeit?
     */
    static final String REQ_4 =
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?currentTime ?currentHours WHERE { "
                + "BIND (now() AS ?currentTime) "
                + "BIND (hours(?currentTime) AS ?currentHours) "
            + "}";

    /**
     * Sind im Moment Lampen im Wohnzimmer an, die Rollos unten und ist es zwischen 22:00 - 6:00 Uhr? Welche sind diese?
     */
    //TODO order important for efficiency? if yes: (here) first ask current time...
    //TODO dateTimeStamp or dateTime?
    static final String REQ_5 =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
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

    //CHECKSTYLE.ON: MultipleStringLiterals
    /**
     * Private Constructor.
     */
    private QueryStrings() {
    }
}
