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
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.RdfTriple;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntInst;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntPrefix;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 31.01.17.
 */
public class HeartbeatPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatPhase.class);
    private final Stopwatch stopwatch;
    private Future future;

    public HeartbeatPhase() throws InitializationException {
        this.stopwatch = new Stopwatch();

        try {
            closeOldConnectionPhases();
            setHeartbeatPhase();
            startHeartbeatMonitor();
        } catch (CouldNotPerformException | InterruptedException e) {
            throw new InitializationException(this, e);
        }
    }

    /**
     * Method closes all past connectionPhases onetime, which aren't closed in the past because of an interrupt for example.
     */
    private void closeOldConnectionPhases()  {

        final List<RdfTriple> delete = new ArrayList<>();
        final List<RdfTriple> insert = new ArrayList<>();
        final List<RdfTriple> where = new ArrayList<>();

        delete.add(new RdfTriple(null, OntProp.LAST_CONNECTION.getName(), OntInst.RECENT_HEARTBEAT.getName()));
        insert.add(new RdfTriple(null, OntProp.LAST_CONNECTION.getName(), null));
        where.add(new RdfTriple(null, OntExpr.IS_A.getName(), OntCl.CONNECTION_PHASE.getName()));
        where.add(new RdfTriple(null, OntProp.LAST_CONNECTION.getName(), OntInst.RECENT_HEARTBEAT.getName()));
        where.add(new RdfTriple(OntInst.RECENT_HEARTBEAT.getName(), OntProp.LAST_CONNECTION.getName(), null));

        try {
            SparqlHttp.uploadSparqlRequest(SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, insert, where));
        } catch (NotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    private QuerySolution getQuerySolutionFromOntDB(final String queryExpression) throws IOException, NotAvailableException {
        final ResultSet resultSet = SparqlHttp.sparqlQuery(queryExpression, OntConfig.ONTOLOGY_DB_URL);

        if (resultSet == null || !resultSet.hasNext()) {
            throw new NotAvailableException("Could not get resultSet of query, because query result is invalid! Query wrong?");
        }
        return resultSet.next();
    }

    private void startHeartbeatMonitor() throws NotAvailableException {
        future = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                // get recent heartbeat phase instance name and lastHeartBeat timestamp
                final QuerySolution querySolution = getQuerySolutionFromOntDB(StaticSparqlExpression.getRecentTimestampOfHeartBeat);

                final String heartbeatPhaseInst = StringModifier.getLocalName(querySolution.getResource("heartbeatPhase").toString());
                final String lastTimeStamp = querySolution.getLiteral("lastConnection").getLexicalForm();

                final OffsetDateTime now = OffsetDateTime.now();
                final OffsetDateTime lastHeartbeat = OffsetDateTime.parse(lastTimeStamp).plusSeconds(OntConfig.HEART_BEAT_TOLERANCE);

                if (lastHeartbeat.compareTo(now) >= 0) {
                    // last heartbeat is within the frequency: replace last timestamp of current heartbeatPhase with refreshed timestamp
                    final List<RdfTriple> delete = new ArrayList<>();
                    final List<RdfTriple> insert = new ArrayList<>();
                    final String timestampLiteral = StringModifier.addXsdDateTime(now.toString());

                    delete.add(new RdfTriple(heartbeatPhaseInst, OntProp.LAST_CONNECTION.getName(), null));
                    delete.add(new RdfTriple(OntInst.RECENT_HEARTBEAT.getName(), OntProp.LAST_CONNECTION.getName(), null));

                    insert.add(new RdfTriple(heartbeatPhaseInst, OntProp.LAST_CONNECTION.getName(), timestampLiteral));
                    insert.add(new RdfTriple(OntInst.RECENT_HEARTBEAT.getName(), OntProp.LAST_CONNECTION.getName(), timestampLiteral));

                    SparqlHttp.uploadSparqlRequest(SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, insert, null), OntConfig.ONTOLOGY_DB_URL);
                    LOGGER.info(SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, insert, null));
                } else {
                    // lastHeartBeat timestamp isn't in time. set new heartbeatPhase
                    setHeartbeatPhase();
                }
            } catch (IOException e) {
                ExceptionPrinter.printHistory("Retry...", e, LOGGER, LogLevel.WARN);
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory("Dropped heartbeat update!", e, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException e) {
                future.cancel(true);
            }
        }, 3, OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void setHeartbeatPhase() throws InterruptedException, InitializationException {
        while (true) {
            try {
                final LocalDateTime dateTime = LocalDateTime.now();
                final String timestampLiteral = StringModifier.addXsdDateTime(OffsetDateTime.of(dateTime, OffsetDateTime.now().getOffset()).toString());
                final String heartbeatPhaseInst = OntPrefix.HEARTBEAT.getName() + dateTime.toString();

                final List<RdfTriple> delete = new ArrayList<>();
                final List<RdfTriple> insert = new ArrayList<>();

                delete.add(new RdfTriple(OntInst.RECENT_HEARTBEAT.getName(), OntProp.LAST_CONNECTION.getName(), null));
                insert.add(new RdfTriple(OntInst.RECENT_HEARTBEAT.getName(), OntExpr.IS_A.getName(), OntCl.RECENT_HEARTBEAT.getName()));
                insert.add(new RdfTriple(OntInst.RECENT_HEARTBEAT.getName(), OntProp.LAST_CONNECTION.getName(), timestampLiteral));
                insert.add(new RdfTriple(heartbeatPhaseInst, OntExpr.IS_A.getName(), OntCl.HEARTBEAT_PHASE.getName()));
                insert.add(new RdfTriple(heartbeatPhaseInst, OntProp.FIRST_CONNECTION.getName(), timestampLiteral));
                insert.add(new RdfTriple(heartbeatPhaseInst, OntProp.LAST_CONNECTION.getName(), timestampLiteral));

                SparqlHttp.uploadSparqlRequest(SparqlUpdateExpression.getSeparatedSparqlUpdateExpression(delete, insert, null), OntConfig.ONTOLOGY_DB_URL);
                return;
            } catch (IOException e) {
                ExceptionPrinter.printHistory("Retry...", e, LOGGER, LogLevel.WARN);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            } catch (CouldNotPerformException e) {
                throw new InitializationException("Could not set heartbeatPhase!", e);
            }
        }
    }

}
