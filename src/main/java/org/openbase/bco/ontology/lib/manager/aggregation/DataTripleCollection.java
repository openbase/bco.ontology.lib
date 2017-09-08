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
package org.openbase.bco.ontology.lib.manager.aggregation;

import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntAggregatedObservation;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntObservation;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChange;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntAggregatedStateChange;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author agatting on 01.04.17.
 */
public class DataTripleCollection extends DataAssignation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataTripleCollection.class);

    private final OffsetDateTime dateTimeFrom;
    private final OffsetDateTime dateTimeUntil;
    private final Stopwatch stopwatch;
    private final DataProviding dataProviding;
    private final Period period;

    public DataTripleCollection(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil, final Period period)
            throws CouldNotPerformException, InterruptedException, ExecutionException {
        super(dateTimeFrom, dateTimeUntil, period);

        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.stopwatch = new Stopwatch();
        this.dataProviding = new DataProviding(dateTimeFrom, dateTimeUntil);
        this.period = period;

        if (period.equals(Period.DAY)) {
            final String sparqlUpdateExpr = SparqlUpdateExpression.getSparqlInsertExpression(collectDataForEachUnit());

            // send aggregated values ...
            SparqlHttp.uploadSparqlRequest(sparqlUpdateExpr, OntConfig.getOntologyDbUrl(), 0);

//            // delete unused connectionPhases (old)
//            SparqlHttp.uploadSparqlRequestViaRetry(StaticSparqlExpression.deleteUnusedConnectionPhases(StringModifier.addXsdDateTime(dateTimeUntil)), OntConfig.ServerService.UPDATE);
//            // delete unused heartBeatPhases (old)
//            SparqlHttp.uploadSparqlRequestViaRetry(StaticSparqlExpression.deleteUnusedHeartBeatPhases(StringModifier.addXsdDateTime(dateTimeUntil)), OntConfig.ServerService.UPDATE);
//            // delete unused observations (old)
//            SparqlHttp.uploadSparqlRequestViaRetry(StaticSparqlExpression.deleteUnusedObservations(StringModifier.addXsdDateTime(dateTimeUntil)), OntConfig.ServerService.UPDATE);

        } else {
            final Period oldPeriod;

            switch (period) {
                case WEEK:
                    oldPeriod = Period.DAY;
                    break;
                case MONTH:
                    oldPeriod = Period.WEEK;
                    break;
                case YEAR:
                    oldPeriod = Period.MONTH;
                    break;
                default:
                    oldPeriod = Period.HOUR; //TODO
                    break;
            }

            final String sparqlUpdateExpr = SparqlUpdateExpression.getSparqlInsertExpression(collectAggDataForEachUnit(oldPeriod));

            // send aggregated aggregations ...
            System.out.println("Send AggData...");
            SparqlHttp.uploadSparqlRequest(sparqlUpdateExpr, OntConfig.getOntologyDbUrl(), 0);

            // delete unused aggregations (old)
            final String dateTimeFromLiteral = StringModifier.convertToLiteral(dateTimeFrom.toString(), XsdType.DATE_TIME);
            final String dateTimeUntilLiteral = StringModifier.convertToLiteral(dateTimeUntil.toString(), XsdType.DATE_TIME);
            final String sparql = StaticSparqlExpression.deleteUnusedAggObs(oldPeriod.toString(), dateTimeFromLiteral, dateTimeUntilLiteral);
            // upload ...
            SparqlHttp.uploadSparqlRequest(sparql, OntConfig.getOntologyDbUrl(), 0);
        }
    }

    /**
     * Method starts the aggregation process of normal observations (not aggregated). Means for each unit the associated observations are collected/sorted in
     * the following (called) methods to calculate and build the ontology triples to insert aggregated observations.
     *
     * @return a list of triples to insert aggregation observations.
     * @throws NotAvailableException is thrown in case the needed information are not available.
     * @throws InterruptedException is thrown in case the application was interrupted.
     * @throws ExecutionException is thrown in case the processing thread throws an unknown exception.
     */
    private List<RdfTriple> collectDataForEachUnit() throws NotAvailableException, InterruptedException, ExecutionException {
        final List<RdfTriple> triples = new ArrayList<>();
        final HashMap<String, Long> unitConnectionMap = dataProviding.getConnectionTimes();
        final HashMap<String, List<OntObservation>> unitObservationMap = dataProviding.getObservations();

        for (final String unitId : unitObservationMap.keySet()) {
            if (unitConnectionMap.containsKey(unitId)) {
                triples.addAll(collectDataForEachService(unitId, unitConnectionMap.get(unitId), unitObservationMap.get(unitId)));
            } else {
                LOGGER.info("The unit with ID >> " + unitId + " << has no state value for aggregation.");
            }
        }

        return triples;
    }

    private List<RdfTriple> collectAggDataForEachUnit(final Period period) throws NotAvailableException, InterruptedException, ExecutionException {
        final List<RdfTriple> triples = new ArrayList<>();
        final HashMap<String, List<OntAggregatedObservation>> unitAggObservationMap = dataProviding.getAggregatedObservations(period);

        for (final String unitId : unitAggObservationMap.keySet()) {
            triples.addAll(collectAggDataForEachService(unitId, unitAggObservationMap.get(unitId)));
        }

        return triples;
    }

    private List<RdfTriple> collectAggDataForEachService(final String unitId, final List<OntAggregatedObservation> ontAggObservations)
            throws InterruptedException {
        final HashMap<String, List<OntAggregatedStateChange>> ontAggStateChanges = new HashMap<>();

        for (final OntAggregatedObservation ontAggObservation : ontAggObservations) {
            final OntAggregatedStateChange ontAggStateChange = new OntAggregatedStateChange(ontAggObservation.getStateValue(),ontAggObservation.getQuantity()
                    , ontAggObservation.getActivityTime(), ontAggObservation.getVariance(), ontAggObservation.getStandardDeviation(), ontAggObservation.getMean(), ontAggObservation.getTimeWeighting());

            if (ontAggStateChanges.containsKey(ontAggObservation.getProviderService())) {
                // there is an entry: add data
                final List<OntAggregatedStateChange> arrayList = ontAggStateChanges.get(ontAggObservation.getProviderService());
                arrayList.add(ontAggStateChange);
                ontAggStateChanges.put(ontAggObservation.getProviderService(), arrayList);
            } else {
                // there is no entry: put data
                ontAggStateChanges.put(ontAggObservation.getProviderService(), new ArrayList<OntAggregatedStateChange>() {{add(ontAggStateChange);}});
            }
        }
        return identifyServiceType(ontAggStateChanges, 0, unitId);
    }

    private List<RdfTriple> collectDataForEachService(final String unitId, final long unitConnectionTimeMilli, final List<OntObservation> ontObservations)
            throws InterruptedException {
        final List<RdfTriple> triples = new ArrayList<>();
        final HashMap<String, List<OntStateChange>> serviceStateChangeMap = new HashMap<>();

        for (final OntObservation ontObservation : ontObservations) {
            final OntStateChange ontStateChange = new OntStateChange(ontObservation.getStateValues(), ontObservation.getTimestamp());

            if (serviceStateChangeMap.containsKey(ontObservation.getProviderService())) {
                // there is an entry: add data
                final List<OntStateChange> arrayList = serviceStateChangeMap.get(ontObservation.getProviderService());
                arrayList.add(ontStateChange);
                serviceStateChangeMap.put(ontObservation.getProviderService(), arrayList);
            } else {
                // there is no entry: put data
                serviceStateChangeMap.put(ontObservation.getProviderService(), new ArrayList<OntStateChange>() {{add(ontStateChange);}});
            }
        }
        triples.addAll(identifyServiceType(serviceStateChangeMap, unitConnectionTimeMilli, unitId));

        return triples;
    }
}
