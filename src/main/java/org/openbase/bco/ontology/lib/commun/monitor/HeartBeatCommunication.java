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

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.jp.JPOntologyDatabaseURL;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.RdfTriple;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 31.01.17.
 */
public class HeartBeatCommunication {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatCommunication.class);
    public static final ObservableImpl<Boolean> isInitObservable = new ObservableImpl<>();
//    private final SimpleDateFormat dateFormat;
    private final Stopwatch stopwatch;
    private Future future;
    private final String pred_FirstHeartBeat;
    private final String pred_LastHeartBeat;

    public HeartBeatCommunication() throws InitializationException {

//        this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());
        this.stopwatch = new Stopwatch();
        this.pred_FirstHeartBeat = OntProp.FIRST_CONNECTION.getName();
        this.pred_LastHeartBeat = OntProp.LAST_CONNECTION.getName();

        try {
            // close old connectionPhases. Means set last timestamp of connectionPhases with timestamp pointer to heartbeat pointer
            closeOldConnectionPhases();

            //generate new heartbeat phase
            setNewHeartBeatPhase();

            // init for connectionPhases ready (recentHeartBeat is set)...notify unitRemoteSynchronizer
            isInitObservable.notifyObservers(true);

            startHeartBeatThread();
        } catch (CouldNotPerformException | InterruptedException | JPServiceException e) {
            throw new InitializationException(this, e);
        }
    }

//    private final String queryUpdate =
//            "PREFIX NS: <" + OntConfig.NS + "> "
//            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
//            + "DELETE { "
//                + "?connectionPhase NS:hasLastConnection NS:recentHeartBeat . "
//            + "} INSERT { "
//                + "?connectionPhase NS:hasLastConnection ?time . "
//            + "} WHERE { "
//                + "?connectionPhase NS:hasLastConnection NS:recentHeartBeat . "
//                + "NS:recentHeartBeat NS:hasLastConnection ?time . "
//            + "}";

    private void closeOldConnectionPhases() throws JPServiceException, InterruptedException, NotAvailableException {

        final List<RdfTriple> deleteTriples = new ArrayList<>();
        final List<RdfTriple> insertTriples = new ArrayList<>();
        final List<RdfTriple> whereTriples = new ArrayList<>();

        deleteTriples.add(new RdfTriple(null, OntProp.LAST_CONNECTION.getName(), OntConfig.INSTANCE_RECENT_HEARTBEAT));
        insertTriples.add(new RdfTriple(null, OntProp.LAST_CONNECTION.getName(), null));
        whereTriples.add(new RdfTriple(null, OntProp.LAST_CONNECTION.getName(), OntConfig.INSTANCE_RECENT_HEARTBEAT));
        whereTriples.add(new RdfTriple(OntConfig.INSTANCE_RECENT_HEARTBEAT, OntProp.LAST_CONNECTION.getName(), null));

        final String closeOldConnectionPhases = SparqlUpdateExpression.getSparqlUpdateExpression(deleteTriples, insertTriples, whereTriples);

        while (true) {
            try {
                SparqlHttp.uploadSparqlRequest(closeOldConnectionPhases, JPService.getProperty(JPOntologyDatabaseURL.class).getValue());
                break;
            } catch (IOException e) {
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory("Dropped heartbeat update! Wrong sparql update?!", e, LOGGER, LogLevel.ERROR);
                break;
            }
        }
    }

    private List<RdfTriple> getInitRecentHeartBeat(final String heartBeatTimestamp) {

        final List<RdfTriple> triples = new ArrayList<>();
        final String subj_recentHeartBeat = OntConfig.INSTANCE_RECENT_HEARTBEAT;

        triples.add(new RdfTriple(subj_recentHeartBeat, OntExpr.A.getName(), OntCl.RECENT_HEARTBEAT.getName()));
        triples.add(new RdfTriple(subj_recentHeartBeat, OntProp.LAST_CONNECTION.getName(), heartBeatTimestamp));

        return triples;
    }

    private RdfTriple getDeleteTripleRecentHeartBeat() {
        return new RdfTriple(OntConfig.INSTANCE_RECENT_HEARTBEAT, OntProp.LAST_CONNECTION.getName(), null);
    }

    private RdfTriple getInsertTripleRecentHeartBeat(final String heartBeatTimestamp) {
        return new RdfTriple(OntConfig.INSTANCE_RECENT_HEARTBEAT, OntProp.LAST_CONNECTION.getName(), heartBeatTimestamp);
    }

    private void startHeartBeatThread() throws NotAvailableException {
        //observe current heartbeat now, refresh or start new heartbeat phase
        future = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                // get recent heartbeat phase instance name and lastHeartBeat timestamp
                final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.getLastTimestampOfHeartBeat, JPService.getProperty(JPOntologyDatabaseURL.class).getValue());

                if (resultSet == null || !resultSet.hasNext()) {
                    throw new CouldNotProcessException("Could not process resultSet of heartbeat query, cause query result is invalid! Query wrong?");
                }
                final QuerySolution querySolution = resultSet.next();

                final String subj_HeartBeatPhase = StringModifier.getLocalName(querySolution.getResource("blackout").toString());
                final String lastTimeStamp = querySolution.getLiteral("lastTime").getLexicalForm();
                final DateTime now = new DateTime();

//                Date dateLastTimeStamp = dateFormat.parse(lastTimeStamp);
                DateTime dateLastTimestamp = new DateTime(lastTimeStamp).plusSeconds(OntConfig.HEART_BEAT_TOLERANCE);
//                dateLastTimeStamp = DateUtils.addSeconds(dateLastTimeStamp, OntConfig.HEART_BEAT_TOLERANCE);

                if (dateLastTimestamp.compareTo(now) >= 0) {
                    // last heartbeat is within the frequency => replace last timestamp of current blackout with refreshed timestamp
                    final List<RdfTriple> deleteTriple = new ArrayList<>();
                    final List<RdfTriple> insertTriple = new ArrayList<>();
                    final String objectDateTimeNow = StringModifier.addXsdDateTime(now);

                    deleteTriple.add(new RdfTriple(subj_HeartBeatPhase, pred_LastHeartBeat, null));
                    deleteTriple.add(getDeleteTripleRecentHeartBeat());

                    insertTriple.add(new RdfTriple(subj_HeartBeatPhase, pred_LastHeartBeat, objectDateTimeNow));
                    insertTriple.add(getInsertTripleRecentHeartBeat(objectDateTimeNow));

                    // sparql update to replace last heartbeat timestamp
                    final String sparqlUpdate = SparqlUpdateExpression.getSparqlUpdateExpression(deleteTriple, insertTriple, null);

                    SparqlHttp.uploadSparqlRequest(sparqlUpdate, JPService.getProperty(JPOntologyDatabaseURL.class).getValue());
                } else {
                    // lastHeartBeat timestamp isn't in time. start with new heartBeat phase
                    setNewHeartBeatPhase();
                }
            } catch (IOException e) {
                LOGGER.warn("IOException: no connection...Retry...");
            } catch (CouldNotPerformException | CouldNotProcessException e) {
                ExceptionPrinter.printHistory("Dropped heartbeat update!", e, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException | JPServiceException e) {
                future.cancel(true);
            }
        }, 3, OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void setNewHeartBeatPhase() throws InterruptedException, JPServiceException {

        while (true) {
            // both timestamp strings must contain the SAME date
//            final Date now = new Date();
            try {
                final String dateTime = new DateTime().toString();

                final String subj_HeartBeatPhase = "heartBeatPhase" + dateTime.substring(0, dateTime.indexOf("+"));
                final String pred_isA = OntExpr.A.getName();

                final String obj_HeartBeat = OntCl.HEARTBEAT_PHASE.getName();
                final String obj_TimeStamp = "\"" + dateTime + "\"^^xsd:dateTime";

                final List<RdfTriple> insertTriples = new ArrayList<>();

                final String sparqlUpdateDelete = SparqlUpdateExpression.getSparqlUpdateExpression(getDeleteTripleRecentHeartBeat(), null);

                // add initial instance "recentHeartBeat" with initial timestamp
                insertTriples.addAll(getInitRecentHeartBeat(obj_TimeStamp));
                // set initial current heartbeat phase with first and last timestamp (identical)
                insertTriples.add(new RdfTriple(subj_HeartBeatPhase, pred_isA, obj_HeartBeat));
                insertTriples.add(new RdfTriple(subj_HeartBeatPhase, pred_FirstHeartBeat, obj_TimeStamp));
                insertTriples.add(new RdfTriple(subj_HeartBeatPhase, pred_LastHeartBeat, obj_TimeStamp));

                final String sparqlUpdateInsert = SparqlUpdateExpression.getSparqlUpdateExpression(insertTriples);

                //TODO one update string
                SparqlHttp.uploadSparqlRequest(sparqlUpdateDelete, JPService.getProperty(JPOntologyDatabaseURL.class).getValue());
                SparqlHttp.uploadSparqlRequest(sparqlUpdateInsert, JPService.getProperty(JPOntologyDatabaseURL.class).getValue());

                return;
            } catch (IOException e) {
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory("Dropped heartbeat update! Wrong sparql update?!", e, LOGGER, LogLevel.ERROR);
                return;
            }
        }
    }

}
