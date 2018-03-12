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
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntObservation;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntProviderServices;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChange;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChangeBuf;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntUnitConnectionTimes;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntUnits;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntAggregatedStateChange;
import org.openbase.bco.ontology.lib.utility.sparql.QueryExpression;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.AggregationTense;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author agatting on 01.04.17.
 */
public class AggregationImpl extends DataAssignation implements Aggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationImpl.class);

    private final DataProviding dataProviding;

    public AggregationImpl(OffsetDateTime dateTimeFrom, OffsetDateTime dateTimeUntil, Period currentPeriod)
            throws CouldNotPerformException {
        super(dateTimeFrom, dateTimeUntil, currentPeriod);

        this.dataProviding = new DataProviding(dateTimeFrom, dateTimeUntil);
    }

    public void startAggregation(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil,
                                 final AggregationTense aggregationTense) {

    }

    public void startAggregation(int currentDays) throws CouldNotPerformException, InterruptedException, ExecutionException {


        final OffsetDateTime dateTimeFromTest = OffsetDateTime.of(LocalDateTime.parse("2017-01-01T00:00:00.000"), OffsetDateTime.now().getOffset());
        final OffsetDateTime dateTimeUntilTest = OffsetDateTime.of(LocalDateTime.parse("2018-01-01T00:00:00.000"), OffsetDateTime.now().getOffset());
        currentDays += 1;

        if (currentDays % 7 == 0) {
            final Period periodTest = Period.WEEK;
            System.out.println("Start aggregation at day: " + currentDays + " : " + periodTest.toString());

            dataTripleCollection(dateTimeFromTest, dateTimeUntilTest, periodTest);
        }
        if (currentDays % 28 == 0) {
            final Period periodTest = Period.MONTH;
            System.out.println("Start aggregation at day: " + currentDays + " : " + periodTest.toString());
            dataTripleCollection(dateTimeFromTest, dateTimeUntilTest, periodTest);
        }
        if (currentDays % 364 == 0) {
            final Period periodTest = Period.YEAR;
            System.out.println("Start aggregation at day: " + currentDays + " : " + periodTest.toString());
            dataTripleCollection(dateTimeFromTest, dateTimeUntilTest, periodTest);
        }

//        final Period periodTest = Period.DAY;
//        new AggregationImpl(dateTimeFrom, dateTimeUntil, period);
//        new AggregationImpl(dateTimeFromTest, dateTimeUntilTest, periodTest);
    }

//    private void initParameters(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil) throws InterruptedException, ExecutionException,
//            NotAvailableException {
//
//        if (dateTimeFrom == null) {
//            final ResultSet resultSet = SparqlHttp.sparqlQuery(QueryExpression.GET_AGG_DATE_TIME_FROM, OntConfig.getOntologyDbUrl(), 0);
//
//            if (resultSet.hasNext()) {
//                final QuerySolution querySolution = resultSet.nextSolution();
//                final String timestampFrom = OntNodeHandler.getRDFLocalName(querySolution, OntConfig.SparqlVariable.TIMESTAMP.getName());
//                this.dateTimeFrom = OffsetDateTime.parse(timestampFrom);
//            } else {
//                // there was no aggregation so far. set new timestamp
//
//                //TODO set timestamp...
//                this.dateTimeFrom = OffsetDateTime.of(LocalDateTime.parse("2017-01-01T00:00:00.000"), OffsetDateTime.now().getOffset());
//            }
//        } else {
//            this.dateTimeFrom = dateTimeFrom;
//        }
//
//    }

    private void dataTripleCollection(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil, final Period period)
            throws CouldNotPerformException, InterruptedException, ExecutionException {

        if (period.equals(Period.DAY)) {
            final String sparqlUpdateExpr = SparqlUpdateExpression.getSparqlInsertExpression(collectDataForEachUnit());

            // send aggregated values ...
            SparqlHttp.uploadSparqlRequest(sparqlUpdateExpr, OntConfig.getOntologyDbUrl(), 0);

//            // delete unused connectionPhases (old)
//            SparqlHttp.uploadSparqlRequestViaRetry(QueryExpression.deleteUnusedConnectionPhases(StringModifier.addXsdDateTime(dateTimeUntil)), OntConfig.ServerService.UPDATE);
//            // delete unused heartBeatPhases (old)
//            SparqlHttp.uploadSparqlRequestViaRetry(QueryExpression.deleteUnusedHeartBeatPhases(StringModifier.addXsdDateTime(dateTimeUntil)), OntConfig.ServerService.UPDATE);
//            // delete unused observations (old)
//            SparqlHttp.uploadSparqlRequestViaRetry(QueryExpression.deleteUnusedObservations(StringModifier.addXsdDateTime(dateTimeUntil)), OntConfig.ServerService.UPDATE);

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
            final String sparql = QueryExpression.deleteUnusedAggObs(oldPeriod.toString(), dateTimeFromLiteral, dateTimeUntilLiteral);
            // upload ...
            SparqlHttp.uploadSparqlRequest(sparql, OntConfig.getOntologyDbUrl(), 0);
        }
    }

    /**
     * Method starts the aggregation process of normal observations (not aggregated). Means for each unit the associated observations are collected/sorted in
     * the following (called) methods to calculate and build the ontology triples to insert aggregated observations.
     *
     * @return a list of triples to insert aggregation observations.
     * @throws InitializationException is thrown in case the needed information are not available.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> collectDataForEachUnit() throws InitializationException, InterruptedException {
        final List<RdfTriple> triples = new ArrayList<>();
        final OntUnitConnectionTimes ontUnitConnectionTimes = dataProviding.selectConnectionPhases();
        final OntUnits ontUnits = dataProviding.selectObservations();

        final Set<String> unitIds = ontUnits.getOntUnits().keySet();
        final HashMap<String, Long> ontConnectionTimesMillis = ontUnitConnectionTimes.getOntConnectionTimesMilli();

        for (final String unitId : unitIds) {
            if (ontConnectionTimesMillis.containsKey(unitId)) {
                final OntProviderServices ontProviderServices = ontUnits.getOntProviderServices(unitId);
                final long unitConnectionTimeMillis = ontConnectionTimesMillis.get(unitId);

                if (ontProviderServices != null) {
                    triples.addAll(collectDataForEachService(unitId, unitConnectionTimeMillis, ontProviderServices));
                } else {
                    //TODO
                }
            } else {
                //TODO
                LOGGER.info("The unit with ID >> " + unitId + " << has no state value for aggregation.");
            }
        }

        return triples;
    }

    private List<RdfTriple> collectAggDataForEachUnit(final Period period) throws InitializationException, InterruptedException
            , ExecutionException {
        final List<RdfTriple> triples = new ArrayList<>();
        final OntUnits ontUnits = dataProviding.selectAggregatedObservations(period);

        final Set<String> unitIds = ontUnits.getOntUnits().keySet();

        for (final String unitId : unitIds) {
            final OntProviderServices ontProviderServices = ontUnits.getOntProviderServices(unitId);
            triples.addAll(collectAggDataForEachService(unitId, ontProviderServices));
        }

        return triples;
    }

    private List<RdfTriple> collectAggDataForEachService(final String unitId, final OntProviderServices ontProviderServices)
            throws InterruptedException {
        final HashMap<String, List<OntAggregatedStateChange>> ontAggStateChanges = new HashMap<>();

        final Set<String> providerServices = ontProviderServices.getOntProviderServices().keySet();
        final List<RdfTriple> triples = new ArrayList<>();

        for (final String providerService : providerServices) {
            final List<OntStateChange> ontStateChanges = ontProviderServices.getOntStateChanges(providerService);

            triples.addAll(identifyServiceType(providerService, ontStateChanges, 0, unitId));
        }
        return triples;

//        for (final OntAggregatedObservation ontAggObservation : ontAggObservations) {
//            final OntAggregatedStateChange ontAggStateChange = new OntAggregatedStateChange(ontAggObservation.getStateValue(),ontAggObservation.getQuantity()
//                    , ontAggObservation.getActivityTime(), ontAggObservation.getVariance(), ontAggObservation.getStandardDeviation(), ontAggObservation.getMean(), ontAggObservation.getTimeWeighting());
//
//            if (ontAggStateChanges.containsKey(ontAggObservation.getProviderService())) {
//                // there is an entry: add data
//                final List<OntAggregatedStateChange> arrayList = ontAggStateChanges.get(ontAggObservation.getProviderService());
//                arrayList.add(ontAggStateChange);
//                ontAggStateChanges.put(ontAggObservation.getProviderService(), arrayList);
//            } else {
//                // there is no entry: put data
//                ontAggStateChanges.put(ontAggObservation.getProviderService(), new ArrayList<OntAggregatedStateChange>() {{add(ontAggStateChange);}});
//            }
//        }
//        return identifyServiceType(ontAggStateChanges, 0, unitId);
    }

    private List<RdfTriple> collectDataForEachService(final String unitId, final long unitConnectionTimeMilli,
                                                      final OntProviderServices ontProviderServices) throws InterruptedException {
        final List<RdfTriple> triples = new ArrayList<>();
        final Set<String> providerServices = ontProviderServices.getOntProviderServices().keySet();

        for (final String providerService : providerServices) {
            final List<OntStateChange> ontStateChanges = ontProviderServices.getOntStateChanges(providerService);

            triples.addAll(identifyServiceType(providerService, ontStateChanges, unitConnectionTimeMilli, unitId));
        }

//        final HashMap<String, List<OntStateChangeBuf>> serviceStateChangeMap = new HashMap<>();
//
//
//        for (final OntObservation ontObservation : ontObservations) {
//            final OntStateChangeBuf ontStateChangeBuf = new OntStateChangeBuf(ontObservation.getStateValues(), ontObservation.getTimestamp());
//
//            if (serviceStateChangeMap.containsKey(ontObservation.getProviderService())) {
//                // there is an entry: add data
//                final List<OntStateChangeBuf> arrayList = serviceStateChangeMap.get(ontObservation.getProviderService());
//                arrayList.add(ontStateChangeBuf);
//                serviceStateChangeMap.put(ontObservation.getProviderService(), arrayList);
//            } else {
//                // there is no entry: put data
//                serviceStateChangeMap.put(ontObservation.getProviderService(), new ArrayList<OntStateChangeBuf>() {{add(ontStateChangeBuf);}});
//            }
//        }
//        triples.addAll(identifyServiceType(serviceStateChangeMap, unitConnectionTimeMilli, unitId));

        return triples;
    }

    ////// Test area /////

//    public AggregationImpl() throws CouldNotPerformException, InterruptedException {
//
//        this.period = OntConfig.PERIOD_FOR_AGGREGATION;
//        this.backDatedQuantity = OntConfig.BACKDATED_BEGINNING_OF_PERIOD;
//
//        DateTime now = new DateTime();
//
//        this.dateTimeFrom = getAdaptedDateTime(now, backDatedQuantity);
//        this.dateTimeUntil = getAdaptedDateTime(now, backDatedQuantity - 1);

//        final OntModel ontModel = StringModifier.loadOntModelFromFile(null, "src/aggregationExampleFirstStageOfNormalData.owl");
//        final String timestampFrom = StringModifier.addXsdDateTime(dateTimeFrom);
//        final String timestampUntil = StringModifier.addXsdDateTime(dateTimeUntil);
//        final Query query = QueryFactory.create(QueryExpression.getAllAggObs(Period.DAY.toString().toLowerCase(), timestampFrom, timestampUntil));
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
//
//        ResultSetFormatter.out(System.out, resultSet, query);

//        startAggregation();
//    }

//    /**
//     * @param period The time frame, which should be aggregated (hour, day, week, ...).
////     * @param quantityPeriod The number of period (one day, two days, ten days, ...). Must be less than backDatedQuantity!
//     * @param backDatedQuantity The back-dated beginning of the aggregation (before two days, before 20 days, ...). Must be bigger than quantityPeriod!
//     * @throws CouldNotPerformException CouldNotPerformException
//     */
//    public void setTimeFrame(final Period period, final int backDatedQuantity) throws CouldNotPerformException {
//
//
//        this.period = period;
//        this.backDatedQuantity = backDatedQuantity;
//
//        final DateTime now = new DateTime();
//
//        this.dateTimeFrom = getAdaptedDateTime(now, backDatedQuantity);
//        this.dateTimeUntil = getAdaptedDateTime(now, backDatedQuantity);
//    }

    //    private OffsetDateTime getAdaptedDateTime(OffsetDateTime dateTime, final int timeToReduce) throws NotAvailableException {
//
//        switch (period) {
//            case HOUR:
//                dateTime = dateTime.minusHours(timeToReduce);
//                return OffsetDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), 0, 0, 0, ZoneOffset.UTC);
//            case DAY:
//                dateTime = dateTime.minusDays(timeToReduce);
//                return OffsetDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), 0, 0, 0, 0, ZoneOffset.UTC);
////            case WEEK:
////                dateTime = dateTime.minusWeeks(timeToReduce);
////                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), 0, 0);
//            case MONTH:
//                dateTime = dateTime.minusMonths(timeToReduce);
//                return OffsetDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), 1, 0, 0, 0, 0, ZoneOffset.UTC);
//            case YEAR:
//                dateTime = dateTime.minusYears(timeToReduce);
//                return OffsetDateTime.of(dateTime.getYear(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
//            default:
//                throw new NotAvailableException("Could not perform adaption of dateTime for aggregation. Cause period time "
//                        + period.toString() + " could not be identified!");
//        }
//    }
}
