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
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.manager.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.system.config.StaticSparqlExpression;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 31.01.17.
 */
public class HeartBeatCommunication {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartBeatCommunication.class);
    public static final ObservableImpl<Boolean> isInitObservable = new ObservableImpl<>();
    private final SimpleDateFormat dateFormat;
    private final Stopwatch stopwatch;
    private Future future;
    private final String pred_FirstHeartBeat;
    private final String pred_LastHeartBeat;

    public HeartBeatCommunication() throws InitializationException {

        this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());
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
        } catch (NotAvailableException | InterruptedException | JPServiceException e) {
            throw new InitializationException(this, e);
        } catch (MultiException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
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

    private void closeOldConnectionPhases() throws JPServiceException, InterruptedException {

        boolean isHttpSuccess = false;

        final List<TripleArrayList> deleteTriples = new ArrayList<>();
        final List<TripleArrayList> insertTriples = new ArrayList<>();
        final List<TripleArrayList> whereTriples = new ArrayList<>();

        deleteTriples.add(new TripleArrayList(null, OntProp.LAST_CONNECTION.getName(), OntConfig.INSTANCE_RECENT_HEARTBEAT));
        insertTriples.add(new TripleArrayList(null, OntProp.LAST_CONNECTION.getName(), null));
        whereTriples.add(new TripleArrayList(null, OntProp.LAST_CONNECTION.getName(), OntConfig.INSTANCE_RECENT_HEARTBEAT));
        whereTriples.add(new TripleArrayList(OntConfig.INSTANCE_RECENT_HEARTBEAT, OntProp.LAST_CONNECTION.getName(), null));

        final String closeOldConnectionPhases = SparqlUpdateExpression.getSparqlUpdateDeleteAndInsertBundleExpr(deleteTriples, insertTriples, whereTriples);

        while (!isHttpSuccess) {
            try {
                isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToMainOntology(closeOldConnectionPhases, OntConfig.ServerServiceForm.UPDATE);
                if (!isHttpSuccess) {
                    stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory("Update to close old connectionPhases is bad and could not be performed by ontology server." +
                                " Please check implementation.", e, LOGGER
                        , LogLevel.ERROR);
                return;
            }
        }
    }

    private List<TripleArrayList> getInitRecentHeartBeat(final String heartBeatTimestamp) {

        final List<TripleArrayList> triples = new ArrayList<>();
        final String subj_recentHeartBeat = OntConfig.INSTANCE_RECENT_HEARTBEAT;

        triples.add(new TripleArrayList(subj_recentHeartBeat, OntExpr.A.getName(), OntCl.RECENT_HEARTBEAT.getName()));
        triples.add(new TripleArrayList(subj_recentHeartBeat, OntProp.LAST_CONNECTION.getName(), heartBeatTimestamp));

        return triples;
    }

    private TripleArrayList getDeleteTripleRecentHeartBeat() {
        return new TripleArrayList(OntConfig.INSTANCE_RECENT_HEARTBEAT, OntProp.LAST_CONNECTION.getName(), null);
    }

    private TripleArrayList getInsertTripleRecentHeartBeat(final String heartBeatTimestamp) {
        return new TripleArrayList(OntConfig.INSTANCE_RECENT_HEARTBEAT, OntProp.LAST_CONNECTION.getName(), heartBeatTimestamp);
    }

    private void startHeartBeatThread() throws NotAvailableException {
        //observe current heartbeat now, refresh or start new heartbeat phase
        future = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

            try {
                // get recent heartbeat phase instance name and lastHeartBeat timestamp
                final ResultSet resultSet = SparqlUpdateWeb.sparqlQuerySelect(StaticSparqlExpression.getLastTimestampOfHeartBeat);

                if (resultSet == null || !resultSet.hasNext()) {
                    throw new CouldNotPerformException("Could not process resultSet of heartbeat query, cause query result is invalid! Query wrong?");
                }
                final QuerySolution querySolution = resultSet.next();

                final String subj_HeartBeatPhase = OntologyToolkit.getLocalName(querySolution.getResource("blackout").toString());
                final String lastTimeStamp = querySolution.getLiteral("lastTime").getLexicalForm();
                final Date now = new Date();

                Date dateLastTimeStamp = dateFormat.parse(lastTimeStamp);
                dateLastTimeStamp = DateUtils.addSeconds(dateLastTimeStamp, OntConfig.HEART_BEAT_TOLERANCE);

                if (dateLastTimeStamp.compareTo(now) >= 0) {
                    // last heartbeat is within the frequency => replace last timestamp of current blackout with refreshed timestamp
                    final List<TripleArrayList> deleteTriple = new ArrayList<>();
                    final List<TripleArrayList> insertTriple = new ArrayList<>();
                    final String objectDateTimeNow = "\"" + dateFormat.format(now) + "\"^^xsd:dateTime";

                    deleteTriple.add(new TripleArrayList(subj_HeartBeatPhase, pred_LastHeartBeat, null));
                    deleteTriple.add(getDeleteTripleRecentHeartBeat());

                    insertTriple.add(new TripleArrayList(subj_HeartBeatPhase, pred_LastHeartBeat, objectDateTimeNow));
                    insertTriple.add(getInsertTripleRecentHeartBeat(objectDateTimeNow));

                    // sparql update to replace last heartbeat timestamp
                    final String sparqlUpdate = SparqlUpdateExpression.getSparqlUpdateDeleteAndInsertBundleExpr(deleteTriple, insertTriple, null);
                    System.out.println(sparqlUpdate);

                    if (!SparqlUpdateWeb.sparqlUpdateToMainOntology(sparqlUpdate, OntConfig.ServerServiceForm.UPDATE)) {
                        throw new CouldNotProcessException("Dropped heartbeat update. Server offline?");
                    }
                } else {
                    // lastHeartBeat timestamp isn't in time. start with new heartBeat phase
                    setNewHeartBeatPhase();
                }
            } catch (CouldNotProcessException | CouldNotPerformException | IllegalArgumentException | IOException e) {
                ExceptionPrinter.printHistory("Dropped heartbeat update!", e, LOGGER, LogLevel.ERROR);
            } catch (ParseException e) {
                ExceptionPrinter.printHistory("Dropped heartbeat update, cause could not create subject of triple heartbeat!", e, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException | JPServiceException e) {
                future.cancel(true);
            }
        }, 3, OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void setNewHeartBeatPhase() throws InterruptedException, JPServiceException {

        boolean isHttpSuccess = false;

        while (!isHttpSuccess) {
            // both timestamp strings must contain the SAME date
            final Date now = new Date();
            final String dateTime = dateFormat.format(now);

            final String subj_HeartBeatPhase = "heartBeatPhase" + dateTime.substring(0, dateTime.indexOf("+"));
            final String pred_isA = OntExpr.A.getName();

            final String obj_HeartBeat = OntCl.HEARTBEAT_PHASE.getName();
            final String obj_TimeStamp = "\"" + dateFormat.format(now) + "\"^^xsd:dateTime";

            final List<TripleArrayList> insertTriples = new ArrayList<>();

            final String sparqlUpdateDelete = SparqlUpdateExpression.getSparqlUpdateSingleDeleteExpr(getDeleteTripleRecentHeartBeat(), null);
            System.out.println(sparqlUpdateDelete);

            // add initial instance "recentHeartBeat" with initial timestamp
            insertTriples.addAll(getInitRecentHeartBeat(obj_TimeStamp));
            // set initial current heartbeat phase with first and last timestamp (identical)
            insertTriples.add(new TripleArrayList(subj_HeartBeatPhase, pred_isA, obj_HeartBeat));
            insertTriples.add(new TripleArrayList(subj_HeartBeatPhase, pred_FirstHeartBeat, obj_TimeStamp));
            insertTriples.add(new TripleArrayList(subj_HeartBeatPhase, pred_LastHeartBeat, obj_TimeStamp));

//            final String sparqlUpdate = SparqlUpdateExpression.getSparqlUpdateInsertBundleExpr(insertTriples);
            final String sparqlUpdateInsert = SparqlUpdateExpression.getSparqlUpdateInsertBundleExpr(insertTriples);

            try {
                isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToMainOntology(sparqlUpdateDelete, OntConfig.ServerServiceForm.UPDATE);

                if (!isHttpSuccess) {
                    stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                } else {
                    isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToMainOntology(sparqlUpdateInsert, OntConfig.ServerServiceForm.UPDATE);

                    if (!isHttpSuccess) {
                        stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                    }
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory("HeartBeat update is bad and could not be performed by ontology server. Please check implementation.", e, LOGGER
                        , LogLevel.ERROR);
                return;
            }
        }
    }

}
