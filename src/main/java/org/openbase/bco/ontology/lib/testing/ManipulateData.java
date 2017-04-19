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
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.StaticSparqlExpression;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.schedule.Stopwatch;

import java.io.IOException;

/**
 * @author agatting on 19.04.17.
 */
public class ManipulateData {

    private final Stopwatch stopwatch;

    public ManipulateData() throws InterruptedException, IOException {
        this.stopwatch = new Stopwatch();

        generateDataFromOneDayToOneYear();
    }

    private void generateDataFromOneDayToOneYear() throws InterruptedException, IOException {

        final OntModel ontModel = OntologyToolkit.loadOntModelFromFile(null, "");

        final OntClass observationClass = ontModel.getOntClass(OntConfig.NS + OntConfig.OntCl.OBSERVATION.getName());
        final OntProperty hasUnitIdProp = ontModel.getOntProperty(OntConfig.NS + OntConfig.OntProp.UNIT_ID.getName());
        final OntProperty hasStateValueProp = ontModel.getOntProperty(OntConfig.NS + OntConfig.OntProp.STATE_VALUE.getName());
        final OntProperty hasServiceProp = ontModel.getOntProperty(OntConfig.NS + OntConfig.OntProp.PROVIDER_SERVICE.getName());
        final OntProperty hasTimestampProp = ontModel.getOntProperty(OntConfig.NS + OntConfig.OntProp.TIME_STAMP.getName());

        final ExtendedIterator observationInstances = observationClass.listInstances();

        while (observationInstances.hasNext()) {
            final Individual individual = (Individual) observationInstances.next();

            final RDFNode unitIdNode = individual.getProperty(hasUnitIdProp).getObject();
            final RDFNode stateValueNode = individual.getProperty(hasStateValueProp).getObject();
            final RDFNode serviceNode = individual.getProperty(hasServiceProp).getObject();
            final RDFNode timestampNode = individual.getProperty(hasTimestampProp).getObject();

            final String timestamp = timestampNode.asLiteral().getLexicalForm();
            final DateTime instanceDateTime = new DateTime(timestamp);
            final DateTime iteratorDateTime = new DateTime(instanceDateTime.getYear(), 1, 1, instanceDateTime.getHourOfDay(), instanceDateTime.getMinuteOfHour(), instanceDateTime.getMillisOfSecond());

            while (!(iteratorDateTime.getYear() == iteratorDateTime.getYear() + 1)) {
                if (iteratorDateTime.getDayOfMonth() != instanceDateTime.getDayOfMonth() && iteratorDateTime.getMonthOfYear() != instanceDateTime.getMonthOfYear()) {

                    stopwatch.waitForStart(1);
                    final String dateTimeNow = new DateTime().toString();
                    final String unitId = OntologyToolkit.getLocalName(unitIdNode.asResource().toString());
                    final String subj_Observation = "obs" + unitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));

                    final String newTimestamp = OntologyToolkit.addXsdDateTime(iteratorDateTime);
                    final Literal timestampLiteral = ontModel.createTypedLiteral(newTimestamp);

                    final Individual newIndividual = ontModel.createIndividual(OntConfig.NS + subj_Observation, observationClass);
                    newIndividual.addProperty(hasUnitIdProp, unitIdNode);
                    newIndividual.addProperty(hasStateValueProp, stateValueNode);
                    newIndividual.addProperty(hasServiceProp, serviceNode);
                    newIndividual.addLiteral(hasTimestampProp, timestampLiteral);

                    iteratorDateTime.plusDays(1);
                }
            }
        }
        OntologyToolkit.saveOntModel(ontModel, "evaluationData");
    }

    private void deleteObservations() throws CouldNotPerformException {
        //TODO
        final String dateTimeFrom = OntologyToolkit.addXsdDateTime(new DateTime(2017, 4, 19, 0, 0, 0, 0));
        final String dateTimeUntil = OntologyToolkit.addXsdDateTime(new DateTime(2017, 4, 20, 0, 0, 0, 0));

        SparqlUpdateWeb.sparqlUpdateToMainOntologyViaRetry(StaticSparqlExpression.deleteObservationOfTimeFrame(dateTimeFrom, dateTimeUntil), OntConfig.ServerServiceForm.UPDATE);
    }
}
