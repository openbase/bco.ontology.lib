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

import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.utility.StringUtility;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationAggDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceAggDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceDataCollection;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.schedule.Stopwatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author agatting on 01.04.17.
 */
public class DataTripleCollection extends DataAssignation {

    private final DateTime dateTimeFrom;
    private final DateTime dateTimeUntil;
    private final Stopwatch stopwatch;
    private final DataProviding dataProviding;
    private final Period period;

    public DataTripleCollection(final DateTime dateTimeFrom, final DateTime dateTimeUntil, final Period period) throws CouldNotPerformException, InterruptedException, JPServiceException {
        super(dateTimeFrom, dateTimeUntil, period);
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.stopwatch = new Stopwatch();
        this.dataProviding = new DataProviding(dateTimeFrom, dateTimeUntil);
        this.period = period;

        if (period.equals(Period.DAY)) {
            final String sparqlUpdateExpr = SparqlUpdateExpression.getSparqlUpdateExpression(collectData());

            // send aggregated values ...
            SparqlUpdateWeb.sparqlUpdateToMainOntologyViaRetry(sparqlUpdateExpr, OntConfig.ServerServiceForm.UPDATE);

//            // delete unused connectionPhases (old)
//            SparqlUpdateWeb.sparqlUpdateToMainOntologyViaRetry(StaticSparqlExpression.deleteUnusedConnectionPhases(StringUtility.addXsdDateTime(dateTimeUntil)), OntConfig.ServerServiceForm.UPDATE);
//            // delete unused heartBeatPhases (old)
//            SparqlUpdateWeb.sparqlUpdateToMainOntologyViaRetry(StaticSparqlExpression.deleteUnusedHeartBeatPhases(StringUtility.addXsdDateTime(dateTimeUntil)), OntConfig.ServerServiceForm.UPDATE);
//            // delete unused observations (old)
//            SparqlUpdateWeb.sparqlUpdateToMainOntologyViaRetry(StaticSparqlExpression.deleteUnusedObservations(StringUtility.addXsdDateTime(dateTimeUntil)), OntConfig.ServerServiceForm.UPDATE);

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

            final String sparqlUpdateExpr = SparqlUpdateExpression.getSparqlUpdateExpression(collectAggData(oldPeriod));

            // send aggregated aggregations ...
            System.out.println("Send AggData...");
            SparqlUpdateWeb.sparqlUpdateToMainOntologyViaRetry(sparqlUpdateExpr, OntConfig.ServerServiceForm.UPDATE);

            // delete unused aggregations (old)
            SparqlUpdateWeb.sparqlUpdateToMainOntologyViaRetry(StaticSparqlExpression.deleteUnusedAggObs(oldPeriod.toString(), StringUtility.addXsdDateTime(dateTimeFrom)
                    , StringUtility.addXsdDateTime(dateTimeUntil)), OntConfig.ServerServiceForm.UPDATE);
        }
    }

    private List<RdfTriple> collectData() throws NotAvailableException {
//        final HashMap<String, Long> connTimeEachUnit = dataProviding.getConnectionTimeForEachUnit(); //TODO
        final HashMap<String, List<ObservationDataCollection>> observationsEachUnit = dataProviding.getObservationsForEachUnit();
        final HashMap<String, Long> connTimeEachUnit = dataProviding.getConnectionTimeForEachUnitForTesting(observationsEachUnit.keySet());

        return relateDataForEachUnit(connTimeEachUnit, observationsEachUnit);
    }

    private List<RdfTriple> collectAggData(final Period period) throws JPServiceException, InterruptedException, NotAvailableException {
        final HashMap<String, List<ObservationAggDataCollection>> observationsEachUnit = dataProviding.getAggObsForEachUnit(period);
        return relateAggDataForEachUnit(observationsEachUnit);
    }

    private List<RdfTriple> relateAggDataForEachUnit(final HashMap<String, List<ObservationAggDataCollection>> obsAggPerUnit) {
        final List<RdfTriple> triples = new ArrayList<>();

        for (final String unitId : obsAggPerUnit.keySet()) {
                final List<ObservationAggDataCollection> obsDataCollList = obsAggPerUnit.get(unitId);

                triples.addAll(relateAggDataForEachServiceOfEachUnit(unitId, obsDataCollList));
        }
        return triples;
    }

    private List<RdfTriple> relateDataForEachUnit(final HashMap<String, Long> connectionTimePerUnit
            , final HashMap<String, List<ObservationDataCollection>> observationsPerUnit) {
        final List<RdfTriple> triples = new ArrayList<>();

        for (final String unitId : observationsPerUnit.keySet()) {
            if (connectionTimePerUnit.containsKey(unitId)) {
                final long connectionTimeMilli = connectionTimePerUnit.get(unitId);
                final List<ObservationDataCollection> obsDataCollList = observationsPerUnit.get(unitId);

                triples.addAll(relateDataForEachServiceOfEachUnit(unitId, connectionTimeMilli, obsDataCollList));
            }
        }
        return triples;
    }

    private List<RdfTriple> relateAggDataForEachServiceOfEachUnit(final String unitId, final List<ObservationAggDataCollection> obsAggDataCollList) {
        final List<RdfTriple> triples = new ArrayList<>();
        final HashMap<String, List<ServiceAggDataCollection>> serviceAggDataCollList = new HashMap<>();

        for (final ObservationAggDataCollection aggDataObs : obsAggDataCollList) {
            final ServiceAggDataCollection serviceAggDataColl = new ServiceAggDataCollection(aggDataObs.getStateValue(),aggDataObs.getQuantity()
                    , aggDataObs.getActivityTime(), aggDataObs.getVariance(), aggDataObs.getStandardDeviation(), aggDataObs.getMean(), aggDataObs.getTimeWeighting());

            if (serviceAggDataCollList.containsKey(aggDataObs.getProviderService())) {
                // there is an entry: add data
                final List<ServiceAggDataCollection> arrayList = serviceAggDataCollList.get(aggDataObs.getProviderService());
                arrayList.add(serviceAggDataColl);
                serviceAggDataCollList.put(aggDataObs.getProviderService(), arrayList);
            } else {
                // there is no entry: put data
                final List<ServiceAggDataCollection> arrayList = new ArrayList<>();
                arrayList.add(serviceAggDataColl);
                serviceAggDataCollList.put(aggDataObs.getProviderService(), arrayList);
            }
        }
        triples.addAll(identifyServiceType(serviceAggDataCollList, 0, unitId));

        return triples;
    }

    private List<RdfTriple> relateDataForEachServiceOfEachUnit(final String unitId, final long connectionTimeMilli
            , final List<ObservationDataCollection> obsDataCollList) {
        final List<RdfTriple> triples = new ArrayList<>();
        final HashMap<String, List<ServiceDataCollection>> serviceDataCollList = new HashMap<>();

        for (final ObservationDataCollection dataObs : obsDataCollList) {
            final ServiceDataCollection serviceDataColl = new ServiceDataCollection(dataObs.getStateValue(), dataObs.getTimestamp());
//            System.out.println(dataObs.getProviderService() + ", " + dataObs.getStateValue() + ", " + unitId);

            if (serviceDataCollList.containsKey(dataObs.getProviderService())) {
                // there is an entry: add data
                final List<ServiceDataCollection> arrayList = serviceDataCollList.get(dataObs.getProviderService());
                arrayList.add(serviceDataColl);
                serviceDataCollList.put(dataObs.getProviderService(), arrayList);
            } else {
                // there is no entry: put data
                final List<ServiceDataCollection> arrayList = new ArrayList<>();
                arrayList.add(serviceDataColl);
                serviceDataCollList.put(dataObs.getProviderService(), arrayList);
            }
        }
        triples.addAll(identifyServiceType(serviceDataCollList, connectionTimeMilli, unitId));

        return triples;
    }
}
