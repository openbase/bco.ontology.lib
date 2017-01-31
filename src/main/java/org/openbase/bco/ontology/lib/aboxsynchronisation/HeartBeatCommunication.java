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
package org.openbase.bco.ontology.lib.aboxsynchronisation;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author agatting on 31.01.17.
 */
public class HeartBeatCommunication extends SparqlUpdateExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatCommunication.class);

    private final static String queryHasHeartBeat =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "ASK { "
                + "?blackout a NS:NoHeartBeat . "
            + "} ";


    private final static String queryLastHeartBeat =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?lastTimeStamp { "
                + "?blackout a NS:NoHeartBeat . "
                + "?blackout NS:hasLastHeartBeat ?lastTimeStamp . "
                + "FILTER NOT EXISTS { "
                    + "?blackout NS:hasNextHeartBeat ?nextTimeStamp . " //TODO maybe order and filter 1?!
                + "} . "
            + "} ";

//    private final static String updateLastHeartBeat =
//            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
//            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
//            + "INSERT DATA { "
//                + "?blackout a NS:NoHeartBeat . "
//                + "?blackout NS:hasLastHeartBeat ?lastTimeStamp . "
//                + "FILTER (?lastTimeStamp >= \"" + getCurrentDateTime(-35) + "\"^^xsd:dateTime) . "
//            + "} ";

    public HeartBeatCommunication() {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigureSystem.DATE_TIME, Locale.ENGLISH);

        final List<TripleArrayList> deleteTripleArrayLists = new ArrayList<>();
        deleteTripleArrayLists.add(new TripleArrayList(null
                , ConfigureSystem.OntProp.HAS_LAST_HEARTBEAT.getName(), null));
        final List<TripleArrayList> insertTripleArrayLists = new ArrayList<>();
        final String whereExpr =
                "?subject a NS:NoHeartBeat . "
                + "?subject NS:hasLastHeartBeat ?object . "
                + "FILTER NOT EXISTS { "
                    + "?subject NS:hasNextHeartBeat ?nextTimeStamp . "
                + "} . ";

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                Query query = QueryFactory.create(queryLastHeartBeat) ;
                QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:3030/myAppFuseki/sparql", query);
                final ResultSet resultSet = qexec.execSelect();

                final String literalString;

                if (resultSet.hasNext()) {
                    final QuerySolution querySolution = resultSet.next();
                    literalString = querySolution.getLiteral("lastTimeStamp").getLexicalForm(); //2016-11-24T19:00:00.000+01:00

                    try {
                        Date dateLastTimeStamp = simpleDateFormat.parse(literalString);
//                        dateLastTimeStamp = DateUtils.addSeconds(dateLastTimeStamp, 35);
                        final Date now = new Date();
                        System.out.println("-----------");
                        System.out.println(dateLastTimeStamp);
                        System.out.println(now);

//                        if (dateLastTimeStamp.compareTo(now) >= 0) {
                        if (true) {

                            // last heartbeat is within the frequency => replace last timestamp of current blackout
                            final String dateTimeNow = simpleDateFormat.format(now);

                            String test =
                                    "PREFIX NS: <" + ConfigureSystem.NS + "> "
                                            + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
                                            + "DELETE { "
                                            + "?blackout NS:hasLastHeartBeat ?lastTimeStamp . "
                                            + "} INSERT { "
                                            + "?blackout NS:hasLastHeartBeat \"" + dateTimeNow + "\"^^xsd:dateTime"
                                            + "} WHERE { "
                                            + "?blackout a NS:NoHeartBeat . "
                                            + "?blackout NS:hasLastHeartBeat ?lastTimeStamp . "
                                            + "FILTER NOT EXISTS { "
                                            + "?blackout NS:hasNextHeartBeat ?nextTimeStamp . "
                                            + "} . "
                                            + "} ";

                            insertTripleArrayLists.clear();
                            insertTripleArrayLists.add(new TripleArrayList(null
                                    , ConfigureSystem.OntProp.HAS_LAST_HEARTBEAT.getName(), "\"" + dateTimeNow + "\"^^xsd:dateTime"));

                            final String sparqlUpdate = getSparqlBundleUpdateDeleteAndInsertEx(deleteTripleArrayLists
                                    , insertTripleArrayLists, whereExpr);
                            System.out.println(sparqlUpdate);

                            try {
                                sparqlUpdate(sparqlUpdate);
                            } catch (CouldNotPerformException e) {
                                e.printStackTrace();
                            }

                        } else {
                            // blackout => set next timeStamp of current blackout & set last timestamp of new blackout
                        }

                    } catch (ParseException e) {
                        e.printStackTrace(); //TODO
                    }
                }

            }
        }, 0, 5000);
    }


    private static String getCurrentDateTime() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigureSystem.DATE_TIME, Locale.ENGLISH);
        final Date now = new Date();

        return simpleDateFormat.format(now);
    }

}
