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

/**
 * This class contains static sparql expressions, which are important for the ontology update and ontology query. Please do not modify!
 *
 * @author agatting on 10.03.17.
 */
public class StaticSparqlExpression {

    /**
     * Method returns a sparql update string, which identifies and fill the latest connectionPhase instance of each unit. If a connectionPhase is incomplete,
     * means the property 'hasLastConnection' is missing, the sparql update extends the connectionPhase with the 'hasLastConnection' and the given last
     * heartBeat timestamp. Please do not modify!
     *
     * @param lastHeartBeatTimestamp The latest heartBeat timestamp.
     * @return A sparql update string to repair incomplete connectionPhases.
     */
    public static String getConnectionPhaseUpdateExpr(final String lastHeartBeatTimestamp) {

         return "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
                + "INSERT { "
                    + "?connectionPhase NS:hasLastConnection " + lastHeartBeatTimestamp + " . "
                + "} WHERE { "
                    + "{ SELECT (MAX(?time) AS ?lastTime) ?unit WHERE { "
                        + "?unit NS:hasConnectionPhase ?connectionPhase . "
                        + "?connectionPhase a NS:ConnectionPhase . "
                        + "?connectionPhase NS:hasFirstConnection ?time . "
                        + "FILTER NOT EXISTS { "
                        + "?connectionPhase NS:hasLastConnection ?timeBuf . } "
                    + "} "
                    + "GROUP BY ?lastTime ?unit } "
                    + "?unit NS:hasConnectionPhase ?connectionPhase . "
                    + "?connectionPhase a NS:ConnectionPhase . "
                    + "?connectionPhase NS:hasFirstConnection ?lastTime . "
                    + "FILTER NOT EXISTS { "
                        + "?connectionPhase NS:hasLastConnection ?timeVal . "
                    + "} "
                + "} ";
    }

    /**
     * Select query to get the last timestamp of the latest heartbeat phase. Please do not modify!
     */
    public final static String getLastTimestampOfHeartBeat =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?blackout ?lastTime { "
                + "?blackout a NS:HeartBeatPhase . "
                + "?blackout NS:hasFirstHeartBeat ?firstTime . "
                + "?blackout NS:hasLastHeartBeat ?lastTime . "
            + "} "
            + "ORDER BY DESC(?lastTime) LIMIT 1";

    public final static String getAllConnectionPhases =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT ?connectionPhase ?unit ?firstTimestamp ?lastTimestamp WHERE { "
                // get all connectionPhases
                + "?connectionPhase a NS:ConnectionPhase . "
                + "?unit NS:hasConnectionPhase ?connectionPhase . "
                + "?connectionPhase NS:hasFirstConnection ?firstTimestamp . "

                + "?connectionPhase NS:hasLastConnection ?timestamp . "
                + "OPTIONAL { ?timestamp NS:hasLastConnection ?lastHeartBeat . } . "
                // reduce times to one variable via if condition
                + "bind(if(isLiteral(?timestamp), ?timestamp, ?lastHeartBeat) as ?lastTimestamp)"
            + "} "
            + "GROUP BY ?connectionPhase ?unit ?firstTimestamp ?lastTimestamp ";

    public static String getAllObservations(final String timestampFrom, final String timestampUntil) {

        return "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?observation ?unit ?stateValue ?providerService ?timestamp WHERE { "
                    + "?observation a NS:Observation . "
                    + "?observation NS:hasTimeStamp ?timestamp . "
//                    + "FILTER (?timestamp >= " + timestampFrom + " && ?timestamp < " + timestampUntil + " ) . "
                    + "?observation NS:hasUnitId ?unit . "
                    + "?observation NS:hasStateValue ?stateValue . "
                    + "?observation NS:hasProviderService ?providerService . "
                + "} "
                + "GROUP BY ?observation ?unit ?stateValue ?providerService ?timestamp ";
    }

    //TODO get multiple observations per unit (for every different state...)
    public static String getLastObservationsOfBeforePeriod(final String timestampFrom) {

        return "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?observation ?unit ?stateValue ?providerService ?timestamp WHERE { "
                    + "?observation a NS:Observation . "
                    + "?observation NS:hasTimeStamp ?timestamp . "
                    + "FILTER (?timestamp < " + timestampFrom + " ) . "
                    + "?observation NS:hasUnitId ?unit . "
                    + "?observation NS:hasStateValue ?stateValue . "
                    + "?observation NS:hasProviderService ?providerService . "
                + "} "
                + "GROUP BY ?observation ?unit ?stateValue ?providerService ?timestamp "
                + "ORDER BY DESC(?timestamp) LIMIT 1 ";
    }

}
