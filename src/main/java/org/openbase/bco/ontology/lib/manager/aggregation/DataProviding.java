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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.Observation;
import org.openbase.bco.ontology.lib.utility.time.Interval;
import org.openbase.bco.ontology.lib.utility.OntModelUtility;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationAggDataCollection;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.SparqlVariable;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author agatting on 24.03.17.
 */
public class DataProviding {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviding.class);

    private final OffsetDateTime dateTimeFrom;
    private final OffsetDateTime dateTimeUntil;
    private final Interval interval;

    public DataProviding(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil) throws NotAvailableException {
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.interval = new Interval(dateTimeFrom.toInstant().toEpochMilli(), dateTimeUntil.toInstant().toEpochMilli());
    }

    /**
     * Method returns a map of connection times in milliseconds (values) for each unitId (keys). The connection times are in each case subset of the time
     * frame, which should be aggregated. Means a connection time at most limited to the size of the duration of the time frame.
     *
     * @return a map with connection times in milliseconds (values) for each unitId (keys).
     * @throws NotAvailableException is thrown in case a result parameter of the query is invalid.
     */
    public HashMap<String, Long> getConnectionTimes() throws NotAvailableException {

        final HashMap<String, Long> hashMap = new HashMap<>();
//        final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.getAllConnectionPhases);

        //### Testing ###//
        final OntModel ontModel = OntModelUtility.loadOntModelFromFile(null, "src/normalData.owl");
        final Query query = QueryFactory.create(StaticSparqlExpression.GET_ALL_CONNECTION_PHASES);
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final ResultSet resultSet = queryExecution.execSelect();
        //### Testing ###//

        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();

            final String unitId = StringModifier.getLocalName(querySolution.getResource(SparqlVariable.UNIT.getName()).toString());
            final String startTimestamp = querySolution.getLiteral(SparqlVariable.FIRST_TIMESTAMP.getName()).getLexicalForm();
            final String endTimestamp = querySolution.getLiteral(SparqlVariable.LAST_TIMESTAMP.getName()).getLexicalForm();

            final Interval connectionInterval = new Interval(OffsetDateTime.parse(startTimestamp).toInstant().toEpochMilli()
                    , OffsetDateTime.parse(endTimestamp).toInstant().toEpochMilli());
            final Interval overlapInterval = interval.getOverlap(connectionInterval);

            if (overlapInterval != null) {
                final long durationMillis = overlapInterval.getDurationMillis();

                if (hashMap.containsKey(unitId)) {
                    hashMap.put(unitId, hashMap.get(unitId) + durationMillis);
                } else {
                    hashMap.put(unitId, durationMillis);
                }
            } else if (!hashMap.containsKey(unitId)) {
                hashMap.put(unitId, 0L);
            }
        }
        queryExecution.close();

        return hashMap;
    }

    public HashMap<String, List<Observation>> getObservations() throws NotAvailableException, IOException {

        // key: unitId, value: list of observations
        final HashMap<String, List<Observation>> unitObservationMap = new HashMap<>();

        final String timeFrom = StringModifier.addXsdDateTime(dateTimeFrom.format(OntConfig.DATE_TIME_FORMATTER));
        final String timeUntil = StringModifier.addXsdDateTime(dateTimeUntil.format(OntConfig.DATE_TIME_FORMATTER));
        final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.getAllObservations(timeFrom, timeUntil), OntConfig.getOntologyDbUrl());
//        ResultSetFormatter.out(System.out, resultSet);

        String observationIdBuf = null;

        try {
            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                final String observationId = StringModifier.getLocalName(querySolution.getResource(SparqlVariable.OBSERVATION.getName()).toString());
                final String providerService = StringModifier.getLocalName(querySolution.getResource(SparqlVariable.PROVIDER_SERVICE.getName()).toString());
                final String unitId = StringModifier.getLocalName(querySolution.getResource(SparqlVariable.UNIT.getName()).toString());
                final RDFNode stateValue = querySolution.get(SparqlVariable.STATE_VALUE.getName());
                final String timestamp = querySolution.getLiteral(SparqlVariable.TIMESTAMP.getName()).getLexicalForm();

                if (stateValue == null || timestamp == null) {
                    throw new CouldNotProcessException("Could not identify at least one observation data element. Check naming-compliance of SPARQL " +
                            "query and querySolution!");
                }

                // sort observations: each unitId has a list of observations. Additionally the state values of same unit sources must be identified and add to
                // the same observation (contains list of state values)
                if (!observationId.equals(observationIdBuf)) {
                    // another observation
                    final Observation observation = new Observation(providerService, new ArrayList<RDFNode>() {{add(stateValue);}}, timestamp);

                    if (unitObservationMap.containsKey(unitId)) {
                        final List<Observation> observations = unitObservationMap.get(unitId);
                        observations.add(observation);
                        unitObservationMap.put(unitId, observations);
                    } else {
                        unitObservationMap.put(unitId, new ArrayList<Observation>() {{add(observation);}});
                    }
                } else {
                    // add state value to same source/observation (e.g. color - hsb)
                    unitObservationMap.get(unitId).get(unitObservationMap.get(unitId).size() - 1).addValue(stateValue);
                }

                observationIdBuf = observationId;
            }
        } catch (Exception e) {
            ExceptionPrinter.printHistory("At least one observation data element is null or the identification is wrong (no resource respectively literal)!"
                    , e, LOGGER, LogLevel.ERROR);
        }

        return unitObservationMap;
    }

    public HashMap<String, List<ObservationAggDataCollection>> getAggObsForEachUnit(final OntConfig.Period period) throws InterruptedException, NotAvailableException {
        final HashMap<String, List<ObservationAggDataCollection>> hashMap = new HashMap<>();

        try {

//        final OntModel ontModel = StringModifier.loadOntModelFromFile(null, "src/aggregationExampleFirstStageOfNormalData.owl");
            final String timestampFrom = StringModifier.addXsdDateTime(dateTimeFrom.toString());
            final String timestampUntil = StringModifier.addXsdDateTime(dateTimeUntil.toString());
//        final Query query = QueryFactory.create(StaticSparqlExpression.getAllAggObs(OntConfig.Period.DAY.toString().toLowerCase(), timestampFrom, timestampUntil));
//        final Query query = QueryFactory.create(StaticSparqlExpression.getRecentObservationsBeforeTimeFrame(timestampFrom));
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
            final String query = StaticSparqlExpression.getAllAggObs(period.toString().toLowerCase(), timestampFrom, timestampUntil);
            final ResultSet resultSet = SparqlHttp.sparqlQuery(query, OntConfig.getOntologyDbUrl(), 0);
//        ResultSetFormatter.out(System.out, resultSet, query);

            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                final String unitId = StringModifier.getLocalName(querySolution.getResource("unit").toString());
                final String providerService = StringModifier.getLocalName(querySolution.getResource("service").toString());
                final String timeWeighting = querySolution.getLiteral("timeWeighting").getLexicalForm();
                final String quantity = getLiteral(querySolution, "quantity");
                final String activityTime = getLiteral(querySolution, "activityTime");
                final String variance = getLiteral(querySolution, "variance");
                final String standardDeviation = getLiteral(querySolution, "standardDeviation");
                final String mean = getLiteral(querySolution, "mean");
                final RDFNode rdfNode = querySolution.get("stateValue");
//            final String stateValue = rdfNode.isLiteral() ? rdfNode.asLiteral().getLexicalForm() : rdfNode.asResource().toString();
//            final boolean isLiteral = rdfNode.isLiteral();

                final ObservationAggDataCollection obsAggDataColl = new ObservationAggDataCollection(providerService, rdfNode, quantity, activityTime, variance
                        , standardDeviation, mean, timeWeighting);

                if (hashMap.containsKey(unitId)) {
                    // there is an entry: add data
                    final List<ObservationAggDataCollection> tripleAggObsList = hashMap.get(unitId);
                    tripleAggObsList.add(obsAggDataColl);
                    hashMap.put(unitId, tripleAggObsList);
                } else {
                    // there is no entry: put data
                    final List<ObservationAggDataCollection> tripleAggObsList = new ArrayList<>();
                    tripleAggObsList.add(obsAggDataColl);
                    hashMap.put(unitId, tripleAggObsList);
                }
            }
//          queryExecution.close();
        } catch (ExecutionException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return hashMap;
    }

    private String getLiteral(final QuerySolution querySolution, final String propertyName) {
        return (querySolution.getLiteral(propertyName) == null) ? null : querySolution.getLiteral(propertyName).getLexicalForm();
    }

    private void checkOldObservation() {
        //TODO ask query: after deletion of aggregated data...is there older observation?? => true...error
    }
}
