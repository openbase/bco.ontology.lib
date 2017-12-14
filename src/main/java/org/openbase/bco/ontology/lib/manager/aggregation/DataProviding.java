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
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChange;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChange.*;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntUnitConnectionTimes;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntObservation;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntUnits;
import org.openbase.bco.ontology.lib.utility.ontology.OntNodeHandler;
import org.openbase.bco.ontology.lib.utility.time.Interval;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import static org.openbase.bco.ontology.lib.system.config.OntConfig.ObservationType.CONTINUOUS;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import static org.openbase.bco.ontology.lib.system.config.OntConfig.SparqlVariable.*;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This class is used as information interface for the aggregation process. The information will be asked from the BCO
 * ontology via SPARQL over HTTP (SOH). Providing data are e.g. unit-connection times, unit observations (state values).
 *
 * @author agatting on 24.03.17.
 */
public class DataProviding {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviding.class);

    private final OffsetDateTime dateTimeFrom;
    private final OffsetDateTime dateTimeUntil;
    private final Interval interval;

    public DataProviding(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil)
            throws NotAvailableException {

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

        final OntUnitConnectionTimes ontUnitConnectionTimes = new OntUnitConnectionTimes();
//        final HashMap<String, Long> hashMap = new HashMap<>();
        final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.SELECT_CONNECTION_PHASES, OntConfig.getOntologyDbUrl(), 0);
        //### Testing ###//
//        final OntModel ontModel = OntModelHandler.loadOntModelFromFile(null, "src/normalData.owl");
//        final Query query = QueryFactory.create(StaticSparqlExpression.GET_ALL_CONNECTION_PHASES);
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
        //### Testing ###//

//        while (resultSet.hasNext()) {
//            final QuerySolution querySolution = resultSet.nextSolution();
//
//            final String unitId = OntNodeHandler.getRDFNodeName(querySolution, SparqlVariable.UNIT.getName());
//            final String startTimestamp = OntNodeHandler.getRDFNodeName(querySolution, SparqlVariable.FIRST_TIMESTAMP.getName());
//            final String endTimestamp = OntNodeHandler.getRDFNodeName(querySolution, SparqlVariable.LAST_TIMESTAMP.getName());
//
//            final long startMilliS = OffsetDateTime.parse(startTimestamp).toInstant().toEpochMilli();
//            final long endMilliS = OffsetDateTime.parse(endTimestamp).toInstant().toEpochMilli();
//            final Interval connectionInterval = new Interval(startMilliS, endMilliS);
//            final Interval overlapInterval = interval.getOverlap(connectionInterval);
//
//            if (overlapInterval != null) {
//                final long durationMillis = overlapInterval.getDurationMillis();
//
//                if (hashMap.containsKey(unitId)) {
//                    hashMap.put(unitId, hashMap.get(unitId) + durationMillis);
//                } else {
//                    hashMap.put(unitId, durationMillis);
//                }
//            } else if (!hashMap.containsKey(unitId)) {
//                hashMap.put(unitId, 0L);
//            }
//        }

        return new HashMap<>();
    }

    /**
     * Method executes a SPARQL query via http to get all connection phases of the units from the BCO ontology. Based
     * on the result the connection times for each unit are extracted. The data (units and their times in milliseconds)
     * are mapped in the data type {@link OntUnitConnectionTimes}. A connection time is at most limited to the size of
     * the duration of the time frame {@link Interval}.
     *
     * @return {@link OntUnitConnectionTimes} - a mapping of units and connection times.
     * @throws InitializationException is thrown in case the data couldn't be get or mapped.
     */
    public OntUnitConnectionTimes selectConnectionPhases() throws InitializationException {
        try {
            final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.SELECT_CONNECTION_PHASES,
                    OntConfig.getOntologyDbUrl(), 0);
            final OntUnitConnectionTimes ontConnectionTimes = new OntUnitConnectionTimes();

            //### Testing ###//
//        final OntModel ontModel = OntModelHandler.loadOntModelFromFile(null, "src/normalData.owl");
//        final Query query = QueryFactory.create(StaticSparqlExpression.GET_ALL_CONNECTION_PHASES);
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
            //### Testing ###//

            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                final String unitId = OntNodeHandler.getRDFNodeName(querySolution, UNIT.getName());
                final String startTimestamp = OntNodeHandler.getRDFNodeName(querySolution, FIRST_TIMESTAMP.getName());
                final String endTimestamp = OntNodeHandler.getRDFNodeName(querySolution, LAST_TIMESTAMP.getName());

                final long startMillis = OffsetDateTime.parse(startTimestamp).toInstant().toEpochMilli();
                final long endMillis = OffsetDateTime.parse(endTimestamp).toInstant().toEpochMilli();

                final Interval connectionInterval = new Interval(startMillis, endMillis);
                // get the connection time, which is inside the aggregation time frame
                final Interval overlapInterval = interval.getOverlap(connectionInterval);
                final long connectionTimeMillis = (overlapInterval == null) ? 0L : overlapInterval.getDurationMillis();

                ontConnectionTimes.addUnitConnectionTime(unitId, connectionTimeMillis);
            }
            return ontConnectionTimes;

        } catch (InterruptedException | ExecutionException e) {
            throw new InitializationException("Couldn't get a result from ontology server via SPARQL query.", e);
        } catch (NotAvailableException e) {
            throw new InitializationException("Couldn't dissolve data. At least one parameter is invalid.", e);
        }
    }

    /**
     * Method selects all observations (NOT aggregated only!) from the BCO ontology by query. The result will be
     * processed and collected into the data structure {@link OntUnits}.
     *
     * @return the data structure {@link OntUnits}.
     * @throws InitializationException is thrown in case the BCO ontology isn't reachable or the processing of the
     * result couldn't be done.
     */
    public OntUnits selectObservations() throws InitializationException {
        try {
            final String timeUntilFormat = dateTimeUntil.format(OntConfig.DATE_TIME_FORMATTER);
            final String timeUntilLiteral = StringModifier.convertToLiteral(timeUntilFormat, XsdType.DATE_TIME);
            final OntUnits ontUnits = new OntUnits();

            final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression
                    .selectObservations(timeUntilLiteral), OntConfig.getOntologyDbUrl(), 0);
            //### Testing ###//
//            ResultSetFormatter.out(System.out, resultSet);
            //### Testing ###//

            // thanks to SPARQL mightiness the result entries are sorted by observationId (see SPARQL query). In this
            // case all state values, belonging to the same state change, are listed one after another. Therefore, same
            // id means equal state change (e.g. in case of HSB - three values, one state change). Another id means
            // another state change.
            String currentObservationId = null;

            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                final String nextObservationId = OntNodeHandler.getRDFNodeName(querySolution, OBSERVATION.getName());
                final String unitId = OntNodeHandler.getRDFNodeName(querySolution, UNIT.getName());
                final String providerService = OntNodeHandler.getRDFNodeName(querySolution, PROVIDER_SERVICE.getName());
                final RDFNode stateValue = querySolution.get(STATE_VALUE.getName());

                // because of SPARQL query command (ORDER BY) the results are sorted by observationId
                if (nextObservationId.equals(currentObservationId)) {
                    // the same observationId means the identical observation node or rather state value (e.g. HSB)
                    addStateValueToStateChange(ontUnits, unitId, providerService, stateValue);
                } else {
                    // different observationId means a new/another observation node
                    final String timestamp = OntNodeHandler.getRDFNodeName(querySolution, TIMESTAMP.getName());

                    createAndAddOntStateChange(ontUnits, unitId, providerService, timestamp, stateValue);
                }
                currentObservationId = nextObservationId;
            }
            return ontUnits;
        } catch (InterruptedException | ExecutionException e) {
            throw new InitializationException("Couldn't get a result from ontology server via SPARQL query.", e);
        } catch (CouldNotPerformException e) {
            throw new InitializationException("Couldn't dissolve data. At least one parameter is invalid.", e);
        }
    }

    private void addStateValueToStateChange(final OntUnits ontUnits, final String unitId, final String providerService,
                                            final RDFNode stateValue) throws CouldNotPerformException {
        try {
            final List<OntStateChange> stateChanges =
                    ontUnits.getOntProviderServices(unitId).getOntStateChanges(providerService);
            final OntStateChange ontStateChange = stateChanges.get(stateChanges.size() - 1);

            if (ontStateChange.getObservationType().equals(CONTINUOUS)) {
                // add state value to last cycle
                ((Continuous) ontUnits.getOntProviderServices(unitId).getOntStateChanges(providerService)
                        .get(stateChanges.size() - 1).getType()).addStateValue(stateValue);
            } else {
                throw new CouldNotPerformException("Latest result entry has same observationId but different "
                        + "observation type. Should be continuous.class. Maybe SPARQL query wrong (no SORT BY). "
                        + "State value is > " + stateValue.toString() + " <");
            }
        } catch (NotAvailableException e) {
            throw new CouldNotPerformException(e);
        }
    }

    private void createAndAddOntStateChange(final OntUnits ontUnits, final String unitId,
                                            final String providerService, final String timestamp,
                                            final RDFNode stateValue) throws CouldNotPerformException {
        try {
            final OntStateChange ontStateChange;

            if (stateValue.isLiteral()) {
                final List<RDFNode> stateValues = new ArrayList<RDFNode>() {{add(stateValue);}};
                ontStateChange = new OntStateChange<>(new Continuous(timestamp, stateValues));
            } else {
                ontStateChange = new OntStateChange<>(new Discrete(timestamp, stateValue));
            }

            ontUnits.addOntProviderService(unitId, providerService, ontStateChange);
        } catch (MultiException | InvalidStateException e) {
            throw new CouldNotPerformException(e);
        }
    }

    /**
     * Method indicates all observations of the ontology through a SPARQL query. The result is a map with the unitId as key and a list of observations as
     * value is returned.
     *
     * @return a map with unitId as key and a list of related observations.
     * @throws MultiException is thrown in case the dateTimes (from, until) could not be parsed.
     * @throws InterruptedException is thrown in case the application is interrupted.
     * @throws ExecutionException is thrown in case the callable thread throws an unknown exception.
     */
    public HashMap<String, List<OntObservation>> getObservations() throws MultiException, InterruptedException, ExecutionException {

        final OntUnits ontUnits = new OntUnits();

        // key: unitId, value: list of observations
        final HashMap<String, List<OntObservation>> unitObservationMap = new HashMap<>();
        String timeUntil = null;
        try {
            timeUntil = StringModifier.convertToLiteral(dateTimeUntil.format(OntConfig.DATE_TIME_FORMATTER), XsdType.DATE_TIME);
        } catch (NotAvailableException e) {
            e.printStackTrace();
        }
        final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.selectObservations(timeUntil), OntConfig.getOntologyDbUrl(), 0);
//        ResultSetFormatter.out(System.out, resultSet);

        // identify related state values (e.g. hsb) on the basis of the observation id/name. Because of the SPARQL query they are sorted...
        String observationIdBuf = null;

        try {
            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                final String observationId = OntNodeHandler.getRDFNodeName(querySolution, OBSERVATION.getName());
                final String providerService = OntNodeHandler.getRDFNodeName(querySolution, PROVIDER_SERVICE.getName());
                final String unitId = OntNodeHandler.getRDFNodeName(querySolution, UNIT.getName());
                final RDFNode stateValue = querySolution.get(STATE_VALUE.getName());
                final String timestamp = OntNodeHandler.getRDFNodeName(querySolution, TIMESTAMP.getName());

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
            throws MultiException, NotAvailableException, InterruptedException, ExecutionException {
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



    private String getLiteral(final QuerySolution querySolution, final String propertyName) {
        return (querySolution.getLiteral(propertyName) == null) ? null : querySolution.getLiteral(propertyName).getLexicalForm();
    }

    private void checkOldObservation() {
        //TODO ask query: after deletion of aggregated data...is there older observation?? => true...error
    }
}
