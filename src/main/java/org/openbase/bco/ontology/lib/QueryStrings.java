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
     * Private Constructor.
     */
    private QueryStrings() {
    }
}
