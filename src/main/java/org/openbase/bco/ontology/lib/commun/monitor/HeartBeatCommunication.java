/**
 * ==================================================================
 * <p>
 * This file is part of org.openbase.bco.ontology.lib.
 * <p>
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 * <p>
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.commun.monitor;

import org.apache.commons.lang.time.DateUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.openbase.bco.ontology.lib.commun.web.WebInterface;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 31.01.17.
 */
public class HeartBeatCommunication extends SparqlUpdateExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatCommunication.class);
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.ENGLISH);
    private final SimpleDateFormat simpleDateFormatWithoutTimeZone = new SimpleDateFormat(OntConfig.DATE_TIME_WITHOUT_TIME_ZONE, Locale.ENGLISH);

    private final String predicateFirstHeartBeat;
    private final String predicateLastHeartBeat;

    private final static String queryLastTimeStampOfCurrentHeartBeat =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                    + "SELECT ?blackout ?lastTimeStamp { "
                    + "?blackout a NS:HeartBeatPhase . "
                    + "?blackout NS:hasFirstHeartBeat ?firstTimeStamp . "
                    + "?blackout NS:hasLastHeartBeat ?lastTimeStamp . "
                    + "} "
                    + "ORDER BY DESC(?lastTimeStamp) LIMIT 1";

    public HeartBeatCommunication() throws NotAvailableException {

        this.predicateFirstHeartBeat = OntProp.HAS_FIRST_HEARTBEAT.getName();
        this.predicateLastHeartBeat = OntProp.HAS_LAST_HEARTBEAT.getName();

        //generate new heartbeat phase
        setNewHeartBeatPhase();
        startHeartBeatThread();
    }

    private void startHeartBeatThread() throws NotAvailableException {
        //observe current heartbeat now, refresh or start new heartbeat phase
        GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

            try {
                // get recent heartbeat phase instance name and lastHeartBeat timestamp
                final ResultSet resultSet = WebInterface.sparqlQuerySelect(queryLastTimeStampOfCurrentHeartBeat);

                if (resultSet != null && resultSet.hasNext()) { // in this case, resultSet has one solution only
                    final QuerySolution querySolution = resultSet.next();
                    final Date now = new Date();
                    final String objectDateTimeNow = "\"" + simpleDateFormat.format(now) + "\"^^xsd:dateTime";
                    String lastTimeStamp = "";
                    String heartBeatPhase = "";

                    final Iterator<String> stringIterator = querySolution.varNames();

                    while (stringIterator.hasNext()) {
                        final RDFNode rdfNode = querySolution.get(stringIterator.next());

                        if (rdfNode.isLiteral()) {
                            lastTimeStamp = rdfNode.asLiteral().getLexicalForm();
                        } else {
                            // get substring by own implementation: getLocalName() of jena doesn't work correctly
                            heartBeatPhase = rdfNode.asResource().toString();
                            heartBeatPhase = heartBeatPhase.substring(OntConfig.NS.length(), heartBeatPhase.length());
                        }
                    }

                    Date dateLastTimeStamp = simpleDateFormat.parse(lastTimeStamp + "+01:00");
                    dateLastTimeStamp = DateUtils.addSeconds(dateLastTimeStamp, OntConfig.HEART_BEAT_TOLERANCE);

                    if (dateLastTimeStamp.compareTo(now) >= 0) {
                        // last heartbeat is within the frequency => replace last timestamp of current blackout with refreshed timestamp
                        final List<TripleArrayList> deleteTriple = new ArrayList<>();
                        final List<TripleArrayList> insertTriple = new ArrayList<>();
                        deleteTriple.add(new TripleArrayList(heartBeatPhase, predicateLastHeartBeat, null));
                        insertTriple.add(new TripleArrayList(heartBeatPhase, predicateLastHeartBeat, objectDateTimeNow));

                        // sparql update to replace last heartbeat timestamp
                        final String sparqlUpdate = getSparqlBundleUpdateDeleteAndInsertEx(deleteTriple, insertTriple, null);
//                        System.out.println(sparqlUpdate);

                        sendHeartBeat(sparqlUpdate);
                    } else {
                        // lastHeartBeat timestamp isn't in time. start with new heartBeat phase
                        setNewHeartBeatPhase();
                    }
                }
            } catch (CouldNotProcessException e) {
                ExceptionPrinter.printHistory("Dropped Heartbeat!", e, LOGGER, LogLevel.ERROR);
            } catch (ParseException e) {
                ExceptionPrinter.printHistory("Dropped Heartbeat, cause could not create subject of triple heartbeat!", e, LOGGER, LogLevel.ERROR);
            }
        }, 3, OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void setNewHeartBeatPhase() {
        final Date now = new Date();
        final String timeStampWithoutZoneNow = simpleDateFormatWithoutTimeZone.format(now);
        final String timeStampNow = simpleDateFormat.format(now);

        final String subject = "heartBeatPhase" + timeStampWithoutZoneNow;
        final String predicateA = OntExpr.A.getName();

        final String objectHeartBeat = OntCl.HEARTBEAT_PHASE.getName();
        final String objectTimeStampLiteral = "\"" + timeStampNow + "\"^^xsd:dateTime";

        final List<TripleArrayList> insertTripleArrayLists = new ArrayList<>();
        // set initial current heartbeat phase with first and last timestamp (identical)
        insertTripleArrayLists.add(new TripleArrayList(subject, predicateA, objectHeartBeat));
        insertTripleArrayLists.add(new TripleArrayList(subject, predicateFirstHeartBeat, objectTimeStampLiteral));
        insertTripleArrayLists.add(new TripleArrayList(subject, predicateLastHeartBeat, objectTimeStampLiteral));

        final String sparqlUpdate = getSparqlBundleUpdateInsertEx(insertTripleArrayLists);
//        System.out.println(sparqlUpdate);

        sendHeartBeat(sparqlUpdate);
    }

    private void sendHeartBeat(final String sparqlUpdate) {
        try {
            if (!WebInterface.sparqlUpdateToMainOntology(sparqlUpdate)) {
                LOGGER.warn("Server could not reached! Offline?");
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("Dropped HeartBeat update.", e, LOGGER, LogLevel.ERROR);
        }
    }

}
