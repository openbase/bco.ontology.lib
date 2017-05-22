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

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.jp.JPOntologyDatabaseURL;
import org.openbase.bco.ontology.lib.utility.OntModelUtility;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationAggDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationDataCollection;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author agatting on 24.03.17.
 */
public class DataProviding {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviding.class);

    private DateTime dateTimeFrom;
    private DateTime dateTimeUntil;
    private final Interval intervalTimeFrame;

    public DataProviding(final DateTime dateTimeFrom, final DateTime dateTimeUntil) {
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.intervalTimeFrame = new Interval(dateTimeFrom, dateTimeUntil);
    }

    public HashMap<String, Long> getConnectionTimeForEachUnit() throws NotAvailableException {

        final HashMap<String, Long> hashMap = new HashMap<>();

        final OntModel ontModel = OntModelUtility.loadOntModelFromFile(null, "src/normalData.owl");
        final Query query = QueryFactory.create(StaticSparqlExpression.getAllConnectionPhases);
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final ResultSet resultSet = queryExecution.execSelect();
//        final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.getAllConnectionPhases);


        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();

            final String unitId = StringModifier.getLocalName(querySolution.getResource("unit").toString());
            final String startTimestamp = querySolution.getLiteral("firstTimestamp").getLexicalForm();
            final String endTimestamp = querySolution.getLiteral("lastTimestamp").getLexicalForm();

            final Interval connectionInterval = new Interval(new DateTime(startTimestamp), new DateTime(endTimestamp));

            final Interval overlapInterval = intervalTimeFrame.overlap(connectionInterval);

            if (overlapInterval != null) {
                final long intervalValue = overlapInterval.getEndMillis() - overlapInterval.getStartMillis();

                if (hashMap.containsKey(unitId)) {
                    hashMap.put(unitId, hashMap.get(unitId) + intervalValue);
                } else {
                    hashMap.put(unitId, intervalValue);
                }
            } else if (!hashMap.containsKey(unitId)) {
                hashMap.put(unitId, 0L);
            }
        }
        queryExecution.close();

        return hashMap;
    }

    public HashMap<String, Long> getConnectionTimeForEachUnitForTesting(final Set<String> unitStrings) {

        final HashMap<String, Long> hashMap = new HashMap<>();

        for (final String unitId : unitStrings) {
            hashMap.put(unitId, 0L);
        }
        return hashMap;
    }


    public HashMap<String, List<ObservationDataCollection>> getObservationsForEachUnit() throws NotAvailableException {

        final HashMap<String, List<ObservationDataCollection>> hashMap = new HashMap<>();
//        final String timestampUntil = StringModifier.addXsdDateTime(dateTimeUntil);

        final InputStream input = DataProviding.class.getResourceAsStream("/apartmentDataSimple.owl");
        final OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModel.read(input, null);
//        final OntModel ontModel = StringModifier.loadOntModelFromFile(null, "/apartmentDataSimple.owl");
        //TODO for each stateValue one observation (from sparql query) in resultSet bad idea: values are inverted.... need alternative
//        final Query query = QueryFactory.create(StaticSparqlExpression.getAllObservations(timestampUntil));
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
//        final ResultSet resultSet = SparqlHttp.sparqlQuery(StaticSparqlExpression.getAllObservations(timestampFrom, timestampUntil));
//        ResultSetFormatter.out(System.out, resultSet, query);

        //TODO jena solution as alternative for testing
        final OntClass observationClass = ontModel.getOntClass(OntConfig.NAMESPACE + OntConfig.OntCl.OBSERVATION.getName());
        final OntProperty hasUnitIdProp = ontModel.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.UNIT_ID.getName());
        final OntProperty hasStateValueProp = ontModel.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.STATE_VALUE.getName());
        final OntProperty hasServiceProp = ontModel.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.PROVIDER_SERVICE.getName());
        final OntProperty hasTimestampProp = ontModel.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.TIME_STAMP.getName());

        final ExtendedIterator observationInstances = observationClass.listInstances();

        while (observationInstances.hasNext()) {
            final Individual individual = (Individual) observationInstances.next();

            final RDFNode unitIdNode = individual.getProperty(hasUnitIdProp).getObject();
            final StmtIterator stateValues = individual.listProperties(hasStateValueProp);
            final RDFNode serviceNode = individual.getProperty(hasServiceProp).getObject();
            final RDFNode timestampNode = individual.getProperty(hasTimestampProp).getObject();

            final String timestamp = timestampNode.asLiteral().getLexicalForm();
            final String unitId = StringModifier.getLocalName(unitIdNode.asResource().toString());
            final String providerService = StringModifier.getLocalName(serviceNode.asResource().toString());

            while (stateValues.hasNext()) {
                final Statement statement = stateValues.next();
                final RDFNode rdfNode = statement.getObject();

                //TODO one very strange observation here...
                if (unitId.equals("70926b9f-916d-4718-9328-5f98bab5e8f2") && providerService.equalsIgnoreCase("ColorStateService") && rdfNode.isResource()) {
                    continue;
                }

                final ObservationDataCollection obsDataColl = new ObservationDataCollection(providerService, rdfNode, timestamp);

                if (hashMap.containsKey(unitId)) {
                    // there is an entry: add data
                    final List<ObservationDataCollection> tripleObsList = hashMap.get(unitId);
                    tripleObsList.add(obsDataColl);
                    hashMap.put(unitId, tripleObsList);
                } else {
                    // there is no entry: put data
                    final List<ObservationDataCollection> tripleObsList = new ArrayList<>();
                    tripleObsList.add(obsDataColl);
                    hashMap.put(unitId, tripleObsList);
                }
            }
        }

//        while (resultSet.hasNext()) {
//            final QuerySolution querySolution = resultSet.nextSolution();
//
//            final String timestamp = querySolution.getLiteral("timestamp").getLexicalForm();
//            final String unitId = StringModifier.getLocalName(querySolution.getResource("unit").toString());
//            final String providerService = StringModifier.getLocalName(querySolution.getResource("providerService").toString());
//            final RDFNode rdfNode = querySolution.get("stateValue");
//
////            if (rdfNode.toString().equals("96.5811996459961^^http://www.openbase.org/bco/ontology#Saturation")) {
////                System.out.println(providerService + ", " + unitId);
////            }
//
//            final ObservationDataCollection obsDataColl = new ObservationDataCollection(providerService, rdfNode, timestamp);
//
//            if (hashMap.containsKey(unitId)) {
//                // there is an entry: add data
//                final List<ObservationDataCollection> tripleObsList = hashMap.get(unitId);
//                tripleObsList.add(obsDataColl);
//                hashMap.put(unitId, tripleObsList);
//            } else {
//                // there is no entry: put data
//                final List<ObservationDataCollection> tripleObsList = new ArrayList<>();
//                tripleObsList.add(obsDataColl);
//                hashMap.put(unitId, tripleObsList);
//            }
//        }
//        queryExecution.close();
        return hashMap;
    }

    public HashMap<String, List<ObservationAggDataCollection>> getAggObsForEachUnit(final OntConfig.Period period) throws JPServiceException
            , InterruptedException, NotAvailableException {
        final HashMap<String, List<ObservationAggDataCollection>> hashMap = new HashMap<>();

        try {

//        final OntModel ontModel = StringModifier.loadOntModelFromFile(null, "src/aggregationExampleFirstStageOfNormalData.owl");
            final String timestampFrom = StringModifier.addXsdDateTime(dateTimeFrom);
            final String timestampUntil = StringModifier.addXsdDateTime(dateTimeUntil);
//        final Query query = QueryFactory.create(StaticSparqlExpression.getAllAggObs(OntConfig.Period.DAY.toString().toLowerCase(), timestampFrom, timestampUntil));
//        final Query query = QueryFactory.create(StaticSparqlExpression.getRecentObservationsBeforeTimeFrame(timestampFrom));
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
            final String query = StaticSparqlExpression.getAllAggObs(period.toString().toLowerCase(), timestampFrom, timestampUntil);
            final ResultSet resultSet = SparqlHttp.sparqlQuery(query, JPService.getProperty(JPOntologyDatabaseURL.class).getValue(), 0);
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
