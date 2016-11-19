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

    //CHECKSTYLE.ON: MultipleStringLiterals
    /**
     * Private Constructor.
     */
    private QueryStrings() {
    }
}
