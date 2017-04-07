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
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.RDFNode;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationDataCollection;
import org.openbase.bco.ontology.lib.system.config.StaticSparqlExpression;
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

    private DateTime dateTimeFrom;
    private DateTime dateTimeUntil;
    private final Interval intervalTimeFrame;

    public DataProviding(final DateTime dateTimeFrom, final DateTime dateTimeUntil) {
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.intervalTimeFrame = new Interval(dateTimeFrom, dateTimeUntil);
    }

    public HashMap<String, Long> getConnectionTimeForEachUnit() {

        final HashMap<String, Long> hashMap = new HashMap<>();

        final OntModel ontModel = OntologyToolkit.loadOntModelFromFile(null, "src/apartment.owl");
        final Query query = QueryFactory.create(StaticSparqlExpression.getAllConnectionPhases);
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final ResultSet resultSet = queryExecution.execSelect();
//        final ResultSet resultSet = SparqlUpdateWeb.sparqlQuerySelectViaRetry(StaticSparqlExpression.getAllConnectionPhases);


        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();

            final String unitId = OntologyToolkit.getLocalName(querySolution.getResource("unit").toString());
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
            }
        }

        queryExecution.close();

        return hashMap;
    }

    public HashMap<String, List<ObservationDataCollection>> getObservationsForEachUnit() {

        final HashMap<String, List<ObservationDataCollection>> hashMap = new HashMap<>();
        final String timestampUntil = addXsdDateTime(dateTimeUntil);

        final OntModel ontModel = OntologyToolkit.loadOntModelFromFile(null, "src/apartment.owl");
        final Query query = QueryFactory.create(StaticSparqlExpression.getAllObservations(timestampUntil));
//        final Query query = QueryFactory.create(StaticSparqlExpression.getRecentObservationsBeforeTimeFrame(timestampFrom));
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
        final ResultSet resultSet = queryExecution.execSelect();
//        final ResultSet resultSet = SparqlUpdateWeb.sparqlQuerySelectViaRetry(StaticSparqlExpression.getAllObservations(timestampFrom, timestampUntil));

//        ResultSetFormatter.out(System.out, resultSet, query);

        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();

            final String timestamp = querySolution.getLiteral("timestamp").getLexicalForm();

            final String unitId = OntologyToolkit.getLocalName(querySolution.getResource("unit").toString());
            final String providerService = OntologyToolkit.getLocalName(querySolution.getResource("providerService").toString());
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

        queryExecution.close();
        return hashMap;
    }

    private String addXsdDateTime(final DateTime dateTime) {
        return "\"" + dateTime.toString() + "\"^^xsd:dateTime";
    }

    private void checkOldObservation() {
        //TODO ask query: after deletion of aggregated data...is there older observation?? => true...error
    }
}
