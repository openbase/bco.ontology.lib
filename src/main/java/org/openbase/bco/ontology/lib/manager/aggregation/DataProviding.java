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
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ConnectionTimeRatio;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationDataCollection;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.StaticSparqlExpression;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author agatting on 24.03.17.
 */
public class DataProviding {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviding.class);
    private final DateTime dateTimeFrom;
    private final DateTime dateTimeUntil;


    public DataProviding() {
        final DateTime now = new DateTime();

        this.dateTimeFrom = getAdaptedDateTime(now);
        this.dateTimeUntil = getAdaptedDateTime(now);

        getAllObservationOfPeriod();
    }

    private HashMap<String, Long> getConnectionTimeForEachUnit() {

        final HashMap<String, Long> hashMap = new HashMap<>();
        final Interval intervalWholeDay = new Interval(dateTimeFrom, dateTimeUntil);


        final OntModel ontModel = OntologyToolkit.loadOntModelFromFile(null, "src/Ontology3.owl");
        final Query query = QueryFactory.create(StaticSparqlExpression.getAllConnectionPhases);
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final ResultSet resultSet = queryExecution.execSelect();
//        final ResultSet resultSet = SparqlUpdateWeb.sparqlQuerySelectViaRetry(StaticSparqlExpression.getAllConnectionPhases);


        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();

            final String unitId = OntologyToolkit.getLocalName(querySolution.getResource("unit").toString());
            final String startTimestamp = querySolution.getLiteral("firstTimestamp").getLexicalForm();
            final String endTimestamp = querySolution.getLiteral("lastTimestamp").getLexicalForm();

            final Interval ontInterval = new Interval(new DateTime(startTimestamp), new DateTime(endTimestamp));

            final Interval overlapInterval = intervalWholeDay.overlap(ontInterval);

            if (overlapInterval != null) {
                final long intervalValue = overlapInterval.getEndMillis() - overlapInterval.getStartMillis();

                if (hashMap.containsKey(unitId)) {
                    hashMap.put(unitId, hashMap.get(unitId) + intervalValue);
                } else {
                    hashMap.put(unitId, intervalValue);
                }
            }
        }

        queryExecution.close();
        return hashMap;
    }

//    private HashMap<String, ConnectionTimeRatio> blub(final HashMap<String, Long> unitIdAndTime) {
//
//    }

    private HashMap<String, List<ObservationDataCollection>> getAllObservationOfPeriod() {

        final HashMap<String, List<ObservationDataCollection>> hashMap = new HashMap<>();
        final String timestampFrom = addXsdDateTime(dateTimeFrom);
        final String timestampUntil = addXsdDateTime(dateTimeUntil);


        final OntModel ontModel = OntologyToolkit.loadOntModelFromFile(null, "src/Ontology3.owl");
        final Query query = QueryFactory.create(StaticSparqlExpression.getAllObservations(timestampFrom, timestampUntil));
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final ResultSet resultSet = queryExecution.execSelect();
//        final ResultSet resultSet = SparqlUpdateWeb.sparqlQuerySelectViaRetry(StaticSparqlExpression.getAllObservations(timestampFrom, timestampUntil));

//        ResultSetFormatter.out(System.out, resultSet, query);

        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();

            final String unitId = OntologyToolkit.getLocalName(querySolution.getResource("unit").toString());
            final String providerService = OntologyToolkit.getLocalName(querySolution.getResource("providerService").toString());
            final String timestamp = querySolution.getLiteral("timestamp").getLexicalForm();
            final RDFNode rdfNode = querySolution.get("stateValue");
            String dataType = null;
            String stateValue;

            if (rdfNode.isLiteral()) {
                stateValue = rdfNode.asLiteral().getLexicalForm();
                dataType = OntologyToolkit.getLocalName(rdfNode.asLiteral().getDatatypeURI());

                if (dataType == null) {
                    LOGGER.error("Could not identify dataType of " + rdfNode.asLiteral().getDatatypeURI());
                }
            } else {
                stateValue = OntologyToolkit.getLocalName(rdfNode.asResource().toString());
            }

            final ObservationDataCollection obsDataColl = new ObservationDataCollection(providerService, stateValue, dataType, timestamp);

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

//        for (String s : hashMap.keySet()) {
//            for (ObservationDataCollection obsDataColl : hashMap.get(s)) {
//                System.out.println(s + ", " + obsDataColl.getProviderService() + ", " + obsDataColl.getStateValue()
//                        + ", " + obsDataColl.getDataType() + ", " + obsDataColl.getTimestamp());
//            }
//        }

        queryExecution.close();
        return hashMap;
    }

    private DateTime getAdaptedDateTime(DateTime dateTime) {

        switch (OntConfig.PERIOD_FOR_AGGREGATION) {
            case HOUR:
                dateTime = dateTime.minusHours(OntConfig.BACKDATED_BEGINNING_OF_PERIOD);
                break;
            case DAY:
                dateTime = dateTime.minusDays(OntConfig.BACKDATED_BEGINNING_OF_PERIOD);
                break;
            case WEEK:
                dateTime = dateTime.minusWeeks(OntConfig.BACKDATED_BEGINNING_OF_PERIOD);
                break;
            case MONTH:
                dateTime = dateTime.minusMonths(OntConfig.BACKDATED_BEGINNING_OF_PERIOD);
                break;
            case YEAR:
                dateTime = dateTime.minusYears(OntConfig.BACKDATED_BEGINNING_OF_PERIOD);
                break;
            default:
                try {
                    throw new NotAvailableException("Could not perform adaption of dateTime for aggregation. Cause period time "
                            + OntConfig.PERIOD_FOR_AGGREGATION + " could not be identified!");
                } catch (NotAvailableException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
        }

        return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), 0, 0);
    }

    private String addXsdDateTime(final DateTime dateTime) {
        return "\"" + dateTime.toString() + "\"^^xsd:dateTime";
    }


    private void checkOldObservation() {
        //TODO ask query: after deletion of aggregated data...is there older observation?? => true...error
    }
}
