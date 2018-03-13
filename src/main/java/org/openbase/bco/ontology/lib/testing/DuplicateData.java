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
package org.openbase.bco.ontology.lib.testing;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.ontology.lib.commun.web.OntModelHttp;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.utility.sparql.QueryExpression;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @author agatting on 19.04.17.
 */
public class DuplicateData {

//    private final Stopwatch stopwatch;
//    private final DateTimeZone dateTimeZone;
    private final OntModel ontModelApartmentDataSimple;
    private final OntModel ontModelApartmentDataSimpleAggObsDay;

    public DuplicateData() throws InterruptedException {
//        this.stopwatch = new Stopwatch();
//        this.ontModelApartmentDataSimple = StringModifier.loadOntModelFromFile(null, "src/apartmentDataSimple.owl");
//        this.dateTimeZone = DateTimeZone.forOffsetMillis(DateTimeZone.forID("Europe/Berlin").getOffset(DateTime.now()));

        InputStream inputSimpleData = DuplicateData.class.getResourceAsStream("/apartmentDataSimple.owl");
        OntModel ontModelSimpleDate = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModelSimpleDate.read(inputSimpleData, null);
        this.ontModelApartmentDataSimple = ontModelSimpleDate;
        InputStream inputAggObsDay = DuplicateData.class.getResourceAsStream("/apartmentDataSimpleAggObsDay.owl");
        OntModel ontModelAggObsDay = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        ontModelAggObsDay.read(inputAggObsDay, null);
        this.ontModelApartmentDataSimpleAggObsDay = ontModelAggObsDay;
    }

    public void duplicateDataOfAggObs(final int numberOfDays) throws InterruptedException, NotAvailableException {

        final OntModel ontModelDuplicatedData = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        final OntClass aggObsClass = ontModelApartmentDataSimpleAggObsDay.getOntClass(OntConfig.NAMESPACE + OntConfig.OntCl.AGGREGATION_OBSERVATION.getName());
        final OntProperty hasUnitIdProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.UNIT_ID.getName());
        final OntProperty hasPeriodProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.PERIOD.getName());
        final OntProperty hasServiceProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.PROVIDER_SERVICE.getName());
        final OntProperty hasTimestampProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.TIME_STAMP.getName());
        final OntProperty hasStateValueProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.STATE_VALUE.getName());
        final OntProperty hasQuantityProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.QUANTITY.getName());
        final OntProperty hasTimeWeightingProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.TIME_WEIGHTING.getName());
        // discrete values
        final OntProperty hasActivityProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.ACTIVITY_TIME.getName());
        // continuous values
        final OntProperty hasVarianceProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.VARIANCE.getName());
        final OntProperty hasStandardDeviationProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.STANDARD_DEVIATION.getName());
        final OntProperty hasMeanProp = ontModelApartmentDataSimpleAggObsDay.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.MEAN.getName());

        final ExtendedIterator observationInstances = aggObsClass.listInstances();

        while (observationInstances.hasNext()) {
            final Individual individual = (Individual) observationInstances.next();
            final String subj_AggObs = StringModifier.getLocalName(individual.toString()) + numberOfDays;
            final Individual newIndividual = ontModelDuplicatedData.createIndividual(OntConfig.NAMESPACE + subj_AggObs, aggObsClass);

            final RDFNode unitIdNode = individual.getProperty(hasUnitIdProp).getObject();
            final RDFNode periodNode = individual.getProperty(hasPeriodProp).getObject();
            final RDFNode stateValueNode = individual.getProperty(hasStateValueProp).getObject();
            final RDFNode serviceNode = individual.getProperty(hasServiceProp).getObject();
            final RDFNode timestampNode = individual.getProperty(hasTimestampProp).getObject();
            final RDFNode quantityNode = individual.getProperty(hasQuantityProp).getObject();
            final RDFNode timeWeightingNode = individual.getProperty(hasTimeWeightingProp).getObject();

            if (individual.hasProperty(hasActivityProp)) {
                final RDFNode activityNode = individual.getProperty(hasActivityProp).getObject();

                newIndividual.addProperty(hasActivityProp, activityNode);
            } else {
                final RDFNode varianceNode = individual.getProperty(hasVarianceProp).getObject();
                final RDFNode standardDeviationNode = individual.getProperty(hasStandardDeviationProp).getObject();
                final RDFNode meanNode = individual.getProperty(hasMeanProp).getObject();

                newIndividual.addProperty(hasVarianceProp, varianceNode);
                newIndividual.addProperty(hasStandardDeviationProp, standardDeviationNode);
                newIndividual.addProperty(hasMeanProp, meanNode);
            }

            newIndividual.addProperty(hasUnitIdProp, unitIdNode);
            newIndividual.addProperty(hasPeriodProp, periodNode);
            newIndividual.addProperty(hasServiceProp, serviceNode);
            newIndividual.addProperty(hasTimestampProp, timestampNode);
            newIndividual.addProperty(hasStateValueProp, stateValueNode);
            newIndividual.addProperty(hasQuantityProp, quantityNode);
            newIndividual.addProperty(hasTimeWeightingProp, timeWeightingNode);
        }
        System.out.println("send data to fuseki...");
        OntModelHttp.addModelToServer(ontModelDuplicatedData, OntConfig.getOntologyDbUrl(), 0);
    }

    public void duplicateDataOfOneDay(final int numberOfDays) throws InterruptedException, NotAvailableException {

        final OntModel ontModelDuplicatedData = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        final OntClass observationClass = ontModelApartmentDataSimple.getOntClass(OntConfig.NAMESPACE + OntConfig.OntCl.OBSERVATION.getName());
        final OntProperty hasUnitIdProp = ontModelApartmentDataSimple.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.UNIT_ID.getName());
        final OntProperty hasStateValueProp = ontModelApartmentDataSimple.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.STATE_VALUE.getName());
        final OntProperty hasServiceProp = ontModelApartmentDataSimple.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.PROVIDER_SERVICE.getName());
        final OntProperty hasTimestampProp = ontModelApartmentDataSimple.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.TIME_STAMP.getName());

        final ExtendedIterator observationInstances = observationClass.listInstances();

        while (observationInstances.hasNext()) {
//            stopwatch.waitForStart(1);
            final Individual individual = (Individual) observationInstances.next();

            final RDFNode unitIdNode = individual.getProperty(hasUnitIdProp).getObject();
            final StmtIterator stateValues = individual.listProperties(hasStateValueProp);
            final RDFNode serviceNode = individual.getProperty(hasServiceProp).getObject();
            final RDFNode timestampNode = individual.getProperty(hasTimestampProp).getObject();

//            final String timestamp = timestampNode.asLiteral().getLexicalForm();
//            final DateTime instanceDateTime = new DateTime(timestamp);

//            DateTime duplicateDateTime = new DateTime(instanceDateTime.getYear(), 1, 1, instanceDateTime.getHourOfDay(), instanceDateTime.getMinuteOfHour()
//                    , instanceDateTime.getSecondOfMinute(), instanceDateTime.getMillisOfSecond(), dateTimeZone);
//            duplicateDateTime = duplicateDateTime.plusDays(numberOfDays);

//            final String dateTimeNow = new DateTime().toString();
//            final String unitId = StringModifier.getLocalName(unitIdNode.asResource().toString());
//            final String subj_Observation = "obs" + unitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
            final String subj_Observation = StringModifier.getLocalName(individual.toString()) + numberOfDays;

            final Individual newIndividual = ontModelDuplicatedData.createIndividual(OntConfig.NAMESPACE + subj_Observation, observationClass);
            newIndividual.addProperty(hasUnitIdProp, unitIdNode);
            newIndividual.addProperty(hasServiceProp, serviceNode);
            newIndividual.addProperty(hasTimestampProp, timestampNode);
//            newIndividual.addProperty(hasTimestampProp, duplicateDateTime.toString() + "^^xsd:dateTime");

            while (stateValues.hasNext()) {
                final Statement statement = stateValues.next();
                newIndividual.addProperty(hasStateValueProp, statement.getObject());
            }
        }
        System.out.println("send data to fuseki...");
        OntModelHttp.addModelToServer(ontModelDuplicatedData, OntConfig.getOntologyDbUrl(), 0);
    }

    private void generateDataFromOneDayToOneYear() throws InterruptedException, IOException {

//        final OntModel ontModelApartmentDataSimple = StringModifier.loadOntModelFromFile(null, "");
//
//        final OntClass observationClass = ontModelApartmentDataSimple.getOntClass(OntConfig.NAMESPACE + OntConfig.OntCl.OBSERVATION.getName());
//        final OntProperty hasUnitIdProp = ontModelApartmentDataSimple.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.UNIT_ID.getName());
//        final OntProperty hasStateValueProp = ontModelApartmentDataSimple.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.STATE_VALUE.getName());
//        final OntProperty hasServiceProp = ontModelApartmentDataSimple.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.PROVIDER_SERVICE.getName());
//        final OntProperty hasTimestampProp = ontModelApartmentDataSimple.getOntProperty(OntConfig.NAMESPACE + OntConfig.OntProp.TIME_STAMP.getName());
//
//        final ExtendedIterator observationInstances = observationClass.listInstances();
//
//        while (observationInstances.hasNext()) {
//            final Individual individual = (Individual) observationInstances.next();
//
//            final RDFNode unitIdNode = individual.getProperty(hasUnitIdProp).getObject();
//            final RDFNode stateValueNode = individual.getProperty(hasStateValueProp).getObject();
//            final RDFNode serviceNode = individual.getProperty(hasServiceProp).getObject();
//            final RDFNode timestampNode = individual.getProperty(hasTimestampProp).getObject();
//
//            final String timestamp = timestampNode.asLiteral().getLexicalForm();
//            final DateTime instanceDateTime = new DateTime(timestamp);
//            final DateTime iteratorDateTime = new DateTime(instanceDateTime.getYear(), 1, 1, instanceDateTime.getHourOfDay(), instanceDateTime.getMinuteOfHour(), instanceDateTime.getMillisOfSecond());
//
//            while (!(iteratorDateTime.getYear() == iteratorDateTime.getYear() + 1)) {
//                if (iteratorDateTime.getDayOfMonth() != instanceDateTime.getDayOfMonth() && iteratorDateTime.getMonthOfYear() != instanceDateTime.getMonthOfYear()) {
//
//                    stopwatch.waitForStart(1);
//                    final String dateTimeNow = new DateTime().toString();
//                    final String unitId = StringModifier.getLocalName(unitIdNode.asResource().toString());
//                    final String subj_Observation = "obs" + unitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
//
//                    final String newTimestamp = StringModifier.addXsdDateTime(iteratorDateTime);
//                    final Literal timestampLiteral = ontModelApartmentDataSimple.createTypedLiteral(newTimestamp);
//
//                    final Individual newIndividual = ontModelApartmentDataSimple.createIndividual(OntConfig.NAMESPACE + subj_Observation, observationClass);
//                    newIndividual.addProperty(hasUnitIdProp, unitIdNode);
//                    newIndividual.addProperty(hasStateValueProp, stateValueNode);
//                    newIndividual.addProperty(hasServiceProp, serviceNode);
//                    newIndividual.addLiteral(hasTimestampProp, timestampLiteral);
//
//                    iteratorDateTime.plusDays(1);
//                }
//            }
//        }
//        StringModifier.saveOntModel(ontModelApartmentDataSimple, "evaluationData");
    }

    private void deleteObservations() throws CouldNotPerformException, InterruptedException {
        //TODO
        final String dateTimeFrom = StringModifier.convertToLiteral(OffsetDateTime.of(2017, 4, 19, 0, 0, 0, 0, ZoneOffset.UTC).toString(), XsdType.DATE_TIME);
        final String dateTimeUntil = StringModifier.convertToLiteral(OffsetDateTime.of(2017, 4, 20, 0, 0, 0, 0, ZoneOffset.UTC).toString(), XsdType.DATE_TIME);

        SparqlHttp.uploadSparqlRequest(QueryExpression.deleteObservationOfTimeFrame(dateTimeFrom, dateTimeUntil), OntConfig.getOntologyDbUrl(), 0);
    }
}
