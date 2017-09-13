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

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntAggregatedObservation;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntObservation;
import org.openbase.bco.ontology.lib.utility.time.Interval;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.bco.ontology.lib.system.config.OntConfig.SparqlVariable;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
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

//        if (dateTimeFrom == null) {
//            this.dateTimeFrom = OffsetDateTime.of(LocalDateTime.parse("2017-01-01T00:00:00.000"), OffsetDateTime.now().getOffset());
//
//        }

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
     * @throws InterruptedException is thrown in case the application is interrupted.
     * @throws ExecutionException is thrown in case the callable thread throws an unknown exception.
     */
    public HashMap<String, Long> getConnectionTimes() throws NotAvailableException, InterruptedException, ExecutionException {

        final HashMap<String, Long> hashMap = new HashMap<>();
        final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.GET_ALL_CONNECTION_PHASES, OntConfig.getOntologyDbUrl(), 0);
        //### Testing ###//
//        final OntModel ontModel = OntModelHandler.loadOntModelFromFile(null, "src/normalData.owl");
//        final Query query = QueryFactory.create(StaticSparqlExpression.GET_ALL_CONNECTION_PHASES);
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
        //### Testing ###//

        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();

            final String unitId = getRDFNodeName(querySolution, SparqlVariable.UNIT.getName());
            final String startTimestamp = getRDFNodeName(querySolution, SparqlVariable.FIRST_TIMESTAMP.getName());
            final String endTimestamp = getRDFNodeName(querySolution, SparqlVariable.LAST_TIMESTAMP.getName());

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

        return hashMap;
    }

    /**
     * Method indicates all observations of the ontology through a SPARQL query. The result is a map with the unitId as key and a list of observations as
     * value is returned.
     *
     * @return a map with unitId as key and a list of related observations.
     * @throws NotAvailableException is thrown in case the dateTimes (from, until) could not be parsed.
     * @throws InterruptedException is thrown in case the application is interrupted.
     * @throws ExecutionException is thrown in case the callable thread throws an unknown exception.
     */
    public HashMap<String, List<OntObservation>> getObservations() throws NotAvailableException, InterruptedException, ExecutionException {
        // key: unitId, value: list of observations
        final HashMap<String, List<OntObservation>> unitObservationMap = new HashMap<>();
        final String timeUntil = StringModifier.convertToLiteral(dateTimeUntil.format(OntConfig.DATE_TIME_FORMATTER), XsdType.DATE_TIME);
        final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.getAllObservations(timeUntil), OntConfig.getOntologyDbUrl(), 0);
//        ResultSetFormatter.out(System.out, resultSet);

        // identify related state values (e.g. hsb) on the basis of the observation id/name. Because of the SPARQL query they are sorted...
        String observationIdBuf = null;

        try {
            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                final String observationId = getRDFNodeName(querySolution, SparqlVariable.OBSERVATION.getName());
                final String providerService = getRDFNodeName(querySolution, SparqlVariable.PROVIDER_SERVICE.getName());
                final String unitId = getRDFNodeName(querySolution, SparqlVariable.UNIT.getName());
                final RDFNode stateValue = querySolution.get(SparqlVariable.STATE_VALUE.getName());
                final String timestamp = getRDFNodeName(querySolution, SparqlVariable.TIMESTAMP.getName());

                if (stateValue == null || timestamp == null) {
                    throw new CouldNotProcessException("Could not identify at least one observation data element. Check naming-compliance of SPARQL " +
                            "query and querySolution!");
                }

                // sort observations: each unitId has a list of observations. Additionally the state values of same unit sources must be identified and add to
                // the same observation (contains list of state values)
                if (!observationId.equals(observationIdBuf)) {
                    // another ontObservation
                    final OntObservation ontObservation = new OntObservation(providerService, new ArrayList<RDFNode>() {{add(stateValue);}}, timestamp);

                    if (unitObservationMap.containsKey(unitId)) {
                        final List<OntObservation> ontObservations = unitObservationMap.get(unitId);
                        ontObservations.add(ontObservation);
                        unitObservationMap.put(unitId, ontObservations);
                    } else {
                        unitObservationMap.put(unitId, new ArrayList<OntObservation>() {{add(ontObservation);}});
                    }
                } else {
                    // add state value to same source/observation (e.g. color - hsb)
                    unitObservationMap.get(unitId).get(unitObservationMap.get(unitId).size() - 1).addValue(stateValue);
                }

                observationIdBuf = observationId;
            }
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("At least one observation data element is null or the identification is wrong (no resource respectively literal)!"
                    , ex, LOGGER, LogLevel.ERROR);
        }

        return unitObservationMap;
    }

    public HashMap<String, List<OntAggregatedObservation>> getAggregatedObservations(final Period period)
            throws NotAvailableException, InterruptedException, ExecutionException {
        // key: unitId, value: list of aggregated observations
        final HashMap<String, List<OntAggregatedObservation>> unitAggObservationMap = new HashMap<>();

//        final OntModel ontModel = StringModifier.loadOntModelFromFile(null, "src/aggregationExampleFirstStageOfNormalData.owl");
        final String timestampFrom = StringModifier.convertToLiteral(dateTimeFrom.toString(), XsdType.DATE_TIME);
        final String timestampUntil = StringModifier.convertToLiteral(dateTimeUntil.toString(), XsdType.DATE_TIME);
        final String query = StaticSparqlExpression.getAllAggObs(period.toString().toLowerCase(), timestampFrom, timestampUntil);
        final ResultSet resultSet = SparqlHttp.sparqlQuery(query, OntConfig.getOntologyDbUrl(), 0);
//        ResultSetFormatter.out(System.out, resultSet, query);

        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();

            // get all possible aggregated values. If an aggregated observation has not a specific literal in the following than a null is stored
            final String unitId = StringModifier.getLocalName(querySolution.getResource("unit").toString());
            final String providerService = StringModifier.getLocalName(querySolution.getResource("service").toString());
            final String timeWeighting = querySolution.getLiteral("timeWeighting").getLexicalForm();
            final String quantity = getLiteral(querySolution, "quantity");
            final String activityTime = getLiteral(querySolution, "activityTime");
            final String variance = getLiteral(querySolution, "variance");
            final String standardDeviation = getLiteral(querySolution, "standardDeviation");
            final String mean = getLiteral(querySolution, "mean");
            final RDFNode stateValue = querySolution.get("stateValue");

            final OntAggregatedObservation ontAggObs
                    = new OntAggregatedObservation(providerService, stateValue, quantity, activityTime, variance, standardDeviation, mean, timeWeighting);

            if (unitAggObservationMap.containsKey(unitId)) {
                // there is an entry: add data
                final List<OntAggregatedObservation> tripleAggObsList = unitAggObservationMap.get(unitId);
                tripleAggObsList.add(ontAggObs);
                unitAggObservationMap.put(unitId, tripleAggObsList);
            } else {
                // there is no entry: put data
                unitAggObservationMap.put(unitId, new ArrayList<OntAggregatedObservation>() {{add(ontAggObs);}});
            }
        }

        return unitAggObservationMap;
    }

    private String getRDFNodeName(final QuerySolution querySolution, final String name) throws NotAvailableException {
        return (querySolution.get(name).isLiteral()) ? querySolution.getLiteral(name).getLexicalForm()
                : StringModifier.getLocalName(querySolution.getResource(name).toString());
    }

    private String getLiteral(final QuerySolution querySolution, final String propertyName) {
        return (querySolution.getLiteral(propertyName) == null) ? null : querySolution.getLiteral(propertyName).getLexicalForm();
    }

    private void checkOldObservation() {
        //TODO ask query: after deletion of aggregated data...is there older observation?? => true...error
    }
}
