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
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChange;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChangeTypes.*;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntUnitConnectionTimes;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntUnits;
import org.openbase.bco.ontology.lib.utility.Preconditions;
import org.openbase.bco.ontology.lib.utility.ontology.OntNodeHandler;
import org.openbase.bco.ontology.lib.utility.time.Interval;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.system.config.OntConfig;

import static org.openbase.bco.ontology.lib.system.config.OntConfig.ObservationType.CONTINUOUS;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import static org.openbase.bco.ontology.lib.system.config.OntConfig.SparqlVariable.*;
import org.openbase.bco.ontology.lib.utility.sparql.QueryExpression;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;

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

    private final String dateTimeFrom;
    private final String dateTimeUntil;
    private final Interval interval;

    public DataProviding(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil)
            throws NotAvailableException {

        final String dateTimeFormatFrom = dateTimeFrom.format(OntConfig.DATE_TIME_FORMATTER);
        final String dateTimeFormatUntil = dateTimeUntil.format(OntConfig.DATE_TIME_FORMATTER);

        this.dateTimeFrom = StringModifier.convertToLiteral(dateTimeFormatFrom, XsdType.DATE_TIME);
        this.dateTimeUntil = StringModifier.convertToLiteral(dateTimeFormatUntil, XsdType.DATE_TIME);
        this.interval = new Interval(dateTimeFrom.toInstant().toEpochMilli(), dateTimeUntil.toInstant().toEpochMilli());
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
            final String query = QueryExpression.SELECT_CONNECTION_PHASES;
            final String url = OntConfig.getOntologyDbUrl();
            final ResultSet resultSet = SparqlHttp.sparqlQuery(query, url, 0);
            final OntUnitConnectionTimes ontConnectionTimes = new OntUnitConnectionTimes();

            //### Testing ###//
//        final OntModel ontModel = OntModelHandler.loadOntModelFromFile(null, "src/normalData.owl");
//        final Query query = QueryFactory.create(QueryExpression.GET_ALL_CONNECTION_PHASES);
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
            //### Testing ###//

            final ExceptionStack exceptionStack = new ExceptionStack();

            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                final String unitId = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, UNIT.getName(), this, exceptionStack);
                final String strtTimestamp = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, FIRST_TIMESTAMP.getName(), this, exceptionStack);
                final String endTimestamp = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, LAST_TIMESTAMP.getName(), this, exceptionStack);

                MultiException.checkAndThrow("Invalid parameters or variables in sparql query.", exceptionStack);

                // cast string to offsetDateTime object
                final OffsetDateTime strtDateTime = Preconditions.Function.apply(OffsetDateTime::parse, strtTimestamp);
                final OffsetDateTime endDateTime = Preconditions.Function.apply(OffsetDateTime::parse, endTimestamp);

                final long startMillis = strtDateTime.toInstant().toEpochMilli();
                final long endMillis = endDateTime.toInstant().toEpochMilli();

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
        } catch (MultiException e) {
            throw new InitializationException("Dissolve data failed.", e);
        }
    }

    /**
     * Method selects all observations (without aggregated) from the BCO ontology by query. The result will be
     * processed and collected into the data structure {@link OntUnits}.
     *
     * @return the data structure {@link OntUnits}.
     * @throws InitializationException is thrown in case the BCO ontology isn't reachable or the processing of the
     * result couldn't be done.
     */
    public OntUnits selectObservations() throws InitializationException {
        try {
            final OntUnits ontUnits = new OntUnits();
            final String query = QueryExpression.selectObservations(dateTimeUntil);
            final String url = OntConfig.getOntologyDbUrl();
            final ResultSet resultSet = SparqlHttp.sparqlQuery(query, url, 0);
//            ResultSetFormatter.out(System.out, resultSet); // Testing

            // thanks to SPARQL mightiness the result entries are sorted by observationId (see SPARQL query). In this
            // case all state values, belonging to the same state change, are listed one after another. Therefore, same
            // id means equal state change (e.g. in case of HSB - three values, one state change). Another id means
            // another state change.
            final ExceptionStack exceptionStack = new ExceptionStack();
            String currentObservationId = "";

            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                final String nextObservationId = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, OBSERVATION.getName(), this, exceptionStack);
                final String unitId = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, UNIT.getName(), this, exceptionStack);
                final String providerService = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, PROVIDER_SERVICE.getName(), this, exceptionStack);

                MultiException.checkAndThrow("Invalid parameters or variables in sparql query.", exceptionStack);

                final RDFNode stateValue = querySolution.get(STATE_VALUE.getName());
                Preconditions.checkNotNull(nextObservationId,"NextObservationId is null.");

                // because of SPARQL query command (ORDER BY) the results are sorted by observationId
                if (nextObservationId.equals(currentObservationId)) {
                    // the same observationId means the identical observation node or rather state value (e.g. HSB)
                    addStateValueToStateChange(ontUnits, unitId, providerService, stateValue);
                } else {
                    // different observationId means a new/another observation node
                    final String timestamp = OntNodeHandler.getRDFLocalName(querySolution, TIMESTAMP.getName());
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

        final List<OntStateChange> stateChanges =
                ontUnits.getOntProviderServices(unitId).getOntStateChanges(providerService);
        final OntStateChange ontStateChange = stateChanges.get(stateChanges.size() - 1);

        if (ontStateChange.getObservationType().equals(CONTINUOUS)) {
            // add state value to last cycle
            ((Continuous) ontUnits.getOntProviderServices(unitId).getOntStateChanges(providerService)
                    .get(stateChanges.size() - 1).getOntStateChange()).addStateValue(stateValue);
        } else {
            throw new CouldNotPerformException("Latest result entry has same observationId but different "
                    + "observation type. Should be continuous.class. Maybe SPARQL query wrong (no SORT BY)?");
        }
    }

    private void createAndAddOntStateChange(final OntUnits ontUnits, final String unitId,
                                            final String providerService, final String timestamp,
                                            final RDFNode stateValue) throws CouldNotPerformException {
        if (stateValue.isLiteral()) {
            final List<RDFNode> stateValues = new ArrayList<RDFNode>() {{add(stateValue);}};
            final OntStateChange ontStateChange = OntStateChange.asContinuous(timestamp, stateValues);

            ontUnits.addOntProviderService(unitId, providerService, ontStateChange);
        } else {
            final OntStateChange ontStateChange = OntStateChange.asDiscrete(timestamp, stateValue);

            ontUnits.addOntProviderService(unitId, providerService, ontStateChange);
        }
    }

    /**
     * Method selects all aggregated observations from the BCO ontology by query. The result will be processed and
     * collected into the data structure {@link OntUnits}.
     *
     * @param period is the defined time frame (e.g. day, week, month...).
     * @return the data structure {@link OntUnits}.
     * @throws InitializationException is thrown in case the BCO ontology isn't reachable or the processing of the
     * result couldn't be done.
     */
    public OntUnits selectAggregatedObservations(final Period period) throws InitializationException {
        try {
            final OntUnits ontUnits = new OntUnits();
            //### Testing ###//
//        final OntModel ontModel = StringModifier.loadOntModelFromFile(null,
//                "src/aggregationExampleFirstStageOfNormalData.owl");
//        ResultSetFormatter.out(System.out, resultSet, query);
            //### Testing ###//

            final String periodName = period.getName();
            final String query = QueryExpression.selectAggregatedObservations(periodName, dateTimeFrom, dateTimeUntil);
            final String url = OntConfig.getOntologyDbUrl();
            final ResultSet resultSet = SparqlHttp.sparqlQuery(query, url, 0);
            ExceptionStack exceptionStack = new ExceptionStack();

            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();

                // get all shared aggregated values
                final String unitId = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, UNIT.getName(), this, exceptionStack);
                final String providerService = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, PROVIDER_SERVICE.getName(), this, exceptionStack);
                final RDFNode stateValue = Preconditions.checkNotNull(querySolution.get(STATE_VALUE.getName()), this,
                        "StateValue isn't present in the query solution", exceptionStack);
                final String timeWeighting = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, TIME_WEIGHTING.getName(), this, exceptionStack);
                final String quantity = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                        querySolution, QUANTITY.getName(), this, exceptionStack);

                // the shared aggregated values shouldn't be null, because they are basic information
                MultiException.checkAndThrow("Invalid parameters or variables in sparql query.", exceptionStack);

                if (stateValue.isLiteral()) {
                    // state value based on (aggregated) continuous. Create and add to data structure ontUnits
                    addAggContinuous(ontUnits, unitId, providerService, stateValue, timeWeighting, quantity,
                            querySolution, exceptionStack);
                } else {
                    // state value based on (aggregated) discrete. Create and add to data structure ontUnits
                    addAggDiscrete(ontUnits, unitId, providerService, stateValue, timeWeighting, quantity,
                            querySolution, exceptionStack);
                }
            }
            return ontUnits;
        } catch (InterruptedException | ExecutionException e) {
            throw new InitializationException("Couldn't get a result from ontology server via SPARQL query.", e);
        } catch (MultiException e) {
            throw new InitializationException("Couldn't dissolve data. At least one parameter is invalid.", e);
        }
    }

    private void addAggContinuous(final OntUnits ontUnits, final String unitId, final String providerService,
                                  final RDFNode stateValue, final String timeWeighting, final String quantity,
                                  final QuerySolution querySolution,
                                  final ExceptionStack exceptionStack) throws MultiException {

        final String variance = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                querySolution, VARIANCE.getName(), this, exceptionStack);
        final String standardDeviation = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                querySolution, STANDARD_DEVIATION.getName(), this, exceptionStack);
        final String mean = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                querySolution, MEAN.getName(), this, exceptionStack);

        // depend on the classification the aggregated values shouldn't be null
        MultiException.checkAndThrow("Invalid parameters or variables in sparql query.", exceptionStack);

        final OntStateChange ontStateChange = OntStateChange.asAggregatedContinuous(mean, variance, standardDeviation,
                timeWeighting, quantity, stateValue);

        // insert collected information
        ontUnits.addOntProviderService(unitId, providerService, ontStateChange);
    }

    private void addAggDiscrete(final OntUnits ontUnits, final String unitId, final String providerService,
                                final RDFNode stateValue, final String timeWeighting, final String quantity,
                                final QuerySolution querySolution,
                                final ExceptionStack exceptionStack) throws MultiException {

        final String activityTime = Preconditions.BiFunction.apply(OntNodeHandler::getRDFLocalName,
                querySolution, ACTIVITY_TIME.getName(), this, exceptionStack);

        // depend on the classification the aggregated values shouldn't be null
        MultiException.checkAndThrow("Invalid parameters or variables in sparql query.", exceptionStack);

        final OntStateChange ontStateChange = OntStateChange.asAggregatedDiscrete(timeWeighting, activityTime, quantity,
                stateValue);

        // insert collected information
        ontUnits.addOntProviderService(unitId, providerService, ontStateChange);
    }

}
