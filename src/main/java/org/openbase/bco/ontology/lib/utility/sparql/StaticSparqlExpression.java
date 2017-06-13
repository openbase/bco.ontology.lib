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

import org.openbase.bco.ontology.lib.system.config.OntConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains static sparql expressions, which are important for the ontology update and ontology query. Please do not modify!
 *
 * @author agatting on 10.03.17.
 */
public class StaticSparqlExpression {

    public static List<RdfTriple> getNullWhereExpression() {
        final List<RdfTriple> where = new ArrayList<>();
        where.add(new RdfTriple(null, null, null));
        return where;
    }

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
     * Select query to get the last timestamp of the recent heartbeatPhase. Please do not modify!
     */
    public final static String getRecentTimestampOfHeartBeat =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?heartbeatPhase ?lastConnection { "
                + "?heartbeatPhase a NS:HeartBeatPhase . "
                + "?heartbeatPhase NS:hasFirstConnection ?firstConnection . "
                + "?heartbeatPhase NS:hasLastConnection ?lastConnection . "
            + "} "
            + "ORDER BY DESC(?lastConnection) LIMIT 1";

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

    public static String getAllObservations(final String timestampUntil) {

        return "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?observation ?unit ?stateValue ?providerService ?timestamp WHERE { "
                    + "?observation a NS:Observation . "
                    + "?observation NS:hasTimeStamp ?timestamp . "
                    + "FILTER (?timestamp < " + timestampUntil + " ) . "
                    + "?observation NS:hasUnitId ?unit . "
                    + "?observation NS:hasStateValue ?stateValue . "
                    + "?observation NS:hasProviderService ?providerService . "
                + "} "
                + "GROUP BY ?observation ?unit ?stateValue ?providerService ?timestamp ";
    }

//    public static String getMinPeriod(final String period, final String timestampFrom, final String timestampUntil) {
//
//        return "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
//                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
//                + "SELECT (MIN(?timestamp) AS ?minTimestamp) WHERE { "
//                    + "?aggObs NS:hasPeriod NS:" + period + " . "
//                    + "?aggObs NS:hasTimeStamp ?timestamp . "
//                + "} "
//                + "GROUP BY ?minTimestamp ";
//    }

    public static String getAllAggObs(final String period, final String dateTimeFrom, final String dateTimeUntil) {

        return "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "SELECT ?aggObs ?unit ?timeWeighting ?service ?stateValue ?quantity ?activityTime ?variance ?standardDeviation ?mean WHERE { "
                    + "?aggObs a NS:AggregationObservation . "
                    + "?aggObs NS:hasPeriod NS:" + period + " . "
                    + "?aggObs NS:hasTimeStamp ?timestamp . "
                    + "FILTER (?timestamp >= " + dateTimeFrom + " && ?timestamp <= " + dateTimeUntil + " ) . "
                    + "?aggObs NS:hasUnitId ?unit . "
                    + "?aggObs NS:hasProviderService ?service . "
                    + "?aggObs NS:hasUnitId ?unit . "
                    + "OPTIONAL {?aggObs NS:hasQuantity ?quantity . } . "
                    + "OPTIONAL {?aggObs NS:hasActivityTime ?activityTime . } . "
                    + "OPTIONAL {?aggObs NS:hasVariance ?variance . } . "
                    + "OPTIONAL {?aggObs NS:hasStandardDeviation ?standardDeviation . } . "
                    + "OPTIONAL {?aggObs NS:hasMean ?mean . } . "
                    + "OPTIONAL {?aggObs NS:hasStateValue ?stateValue . } . "
                    + "OPTIONAL {?aggObs NS:hasTimeWeighting ?timeWeighting . } . "
                + "} "
                + "GROUP BY ?aggObs ?unit ?timeWeighting ?service ?stateValue ?quantity ?activityTime ?variance ?standardDeviation ?mean ";
    }

    public static String deleteUnusedConnectionPhases(final String dateTimeUntil) {
        return "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "DELETE { "
                    + "?connectionPhase ?p ?o . "
                    + "?s NS:hasConnectionPhase ?connectionPhase . "
                + "} WHERE { "
                    + "?connectionPhase ?p ?o . "
                    + "?s NS:hasConnectionPhase ?connectionPhase . "
                    + "?connectionPhase a NS:ConnectionPhase . "
                    + "?connectionPhase NS:hasLastConnection ?timestamp . "
                    + "OPTIONAL { ?timestamp NS:hasLastConnection ?lastHeartBeat . } . "
                    // reduce times to one variable via if condition
                    + "bind(if(isLiteral(?timestamp), ?timestamp, ?lastHeartBeat) as ?lastTimestamp)"
                    + "FILTER (?lastTimestamp < " + dateTimeUntil + " ) . "
                + "}";
    }

    public static String deleteUnusedHeartBeatPhases(final String dateTimeUntil) {
        return "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "DELETE { "
                    + "?heartBeatPhase ?p ?o . "
                + "} WHERE { "
                    + "?heartBeatPhase ?p ?o . "
                    + "?heartBeatPhase a NS:HeartBeatPhase . "
                    + "?heartBeatPhase NS:hasLastConnection ?lastTimestamp . "
                    + "FILTER (?lastTimestamp < " + dateTimeUntil + " ) . "
                + "}";
    }

    public static String deleteUnusedObservations(final String dateTimeUntil) {
        return "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "DELETE { "
                    + "?obs ?p ?o . "
                + "} WHERE { "
                + "{ SELECT ?unit ?providerService (MAX(?timestamp) AS ?maxTimestamp) WHERE { "
                        + "?observation a NS:Observation . "
                        + "?observation NS:hasTimeStamp ?timestamp . "
                        + "FILTER (?timestamp < " + dateTimeUntil + " ) . "
                        + "?observation NS:hasUnitId ?unit . "
                        + "?observation NS:hasStateValue ?stateValue . "
                        + "?observation NS:hasProviderService ?providerService . "
                        + "FILTER NOT EXISTS { "
                            + "?observation NS:hasPeriod ?period . } . "
                        + "} "
                        + "GROUP BY ?unit ?providerService ?maxTimestamp } "

                    + "?obs ?p ?o . "
                    + "?obs a NS:Observation . "
                    + "?obs NS:hasUnitId ?unit . "
                    + "?obs NS:hasProviderService ?providerService . "
                    + "?obs NS:hasTimeStamp ?obsTime . "
                    + "FILTER NOT EXISTS { "
                        + "?obs NS:hasPeriod ?period . "
                    + "} "
                    + "FILTER (?obsTime < ?maxTimestamp ) . "
                + "}";
    }

    public static String deleteUnusedAggObs(String period, final String dateTimeFrom, final String dateTimeUntil) {
        period = period.toLowerCase();
        return "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "DELETE { "
                    + "?aggObs ?p ?o . "
                + "} WHERE { "
                    + "?aggObs ?p ?o . "
                    + "?aggObs a NS:AggregationObservation . "
                    + "?aggObs NS:hasPeriod NS:" + period + " . "
                    + "?aggObs NS:hasTimeStamp ?timestamp . "
                    + "FILTER (?timestamp >= " + dateTimeFrom + " && ?timestamp <= " + dateTimeUntil + " ) . "
                + "}";
    }

    public static final String deleteAllObservationsWithFilter =
            "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
            + "DELETE { "
                + "?observation ?p ?o . "
            + "} WHERE { "
                + "?observation ?p ?o . "
                + "?observation a NS:Observation . "
                + "?observation NS:hasTimeStamp ?timestamp . "
                + "FILTER (?timestamp > \"2017-04-22T00:00:00.000+02:00\"^^xsd:dateTime) . "
            + "}";


    public static String deleteObservationOfTimeFrame(final String dateTimeFrom, final String dateTimeUntil) {
        return "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "DELETE { "
                    + "?obs ?p ?o . "
                + "} WHERE { "
                    + "?obs ?p ?o . "
                    + "?obs a NS:Observation . "
                    + "?obs NS:hasTimeStamp ?timestamp . "
                    + "FILTER (?timestamp < " + dateTimeFrom + " || ?timestamp >= " + dateTimeUntil + " ) . "
                + "}";
    }

    public final static String countAllTriples =
            "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
            + "SELECT (count(*) as ?count) WHERE { "
                + "?s ?p ?o . "
            + "}";

//    public static String test(final String timestampUntil) {
//
//        return "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
//                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
//                + "SELECT ?unit ?providerService (MAX(?timestamp) AS ?maxTimestamp) WHERE { "
//                    + "?observation a NS:Observation . "
//                    + "?observation NS:hasTimeStamp ?timestamp . "
//                    + "FILTER (?timestamp < " + timestampUntil + " ) . "
//                    + "?observation NS:hasUnitId ?unit . "
//                    + "?observation NS:hasStateValue ?stateValue . "
//                    + "?observation NS:hasProviderService ?providerService . "
//                    + "FILTER NOT EXISTS { "
//                        + "?observation NS:hasPeriod ?period . "
//                    + "} . "
//                + "} "
//                + "GROUP BY ?unit ?providerService ?maxTimestamp ";
//    }

//    public static String getRecentObservationsBeforeTimeFrame(final String timestampFrom) {
//
//        return "PREFIX NS: <http://www.openbase.org/bco/ontology#> "
//                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
//                + "SELECT ?observation ?unit ?stateValue ?providerService ?timestamp WHERE { "
//                + "?observation a NS:Observation . "
//                + "?observation NS:hasTimeStamp ?timestamp . "
//                + "FILTER (?timestamp < " + timestampFrom + " ) . "
//                + "?observation NS:hasUnitId ?unit . "
//                + "?observation NS:hasStateValue ?stateValue . "
//                + "?observation NS:hasProviderService ?providerService . "
//                + "} "
//                + "ORDER BY DESC(?observation) DESC(?unit) ?stateValue ?providerService ASC(?timestamp) ";
////                + "GROUP BY ?observation ?unit ?stateValue ?providerService ?timestamp ";
//    }

}
