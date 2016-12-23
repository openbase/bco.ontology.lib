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
package org.openbase.bco.ontology.lib.ABoxSynchronisation.Configuration;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.ontology.lib.Constants;
import org.openbase.bco.ontology.lib.DataPool;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.EnablingStateType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by agatting on 20.12.16.
 */
public class OntInstancesConfig {
    //TODO handling of units with missing data (e.g. location)
    //TODO swap out strings (unit, providerService)

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstancesConfig.class);

    public OntInstancesConfig(OntModel ontModel) {

        DataPool dataPool = new DataPool();
        UnitRegistry unitRegistry = dataPool.getUnitRegistry();

        inspectionOfUnits(ontModel, unitRegistry);
        inspectionOfServiceTypes(ontModel);
    }

    private void inspectionOfUnits(final OntModel ontModel, final UnitRegistry unitRegistry) {

        List<UnitConfig> missingUnitConfigList = new ArrayList<>();
        List<Individual> ontUnitIndList = new ArrayList<>(); //Ind: individual

        // preparation: get all individuals of the class "Unit" which are currently in the model
        final OntClass ontClassUnit = ontModel.getOntClass(Constants.NS + "Unit");
        ontUnitIndList = getIndOfOntSuperclass(ontUnitIndList, ontClassUnit);

        try {
            // run through all enabled unitConfigs of the registry
            for (final UnitConfig unitConfig : unitRegistry.getUnitConfigs()) {
                if (unitConfig.getEnablingState().getValue().equals(EnablingStateType.EnablingState.State.ENABLED)) {
                    final String unitId = unitConfig.getId();

                    // list all missing units. Means units, which aren't currently in the model
                    if (!ontUnitIndList.toString().contains(unitId)) {
                        missingUnitConfigList.add(unitConfig);
                    }
                }
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("Could not perform unitRegistry in method '"
                    + e.getStackTrace()[0].getMethodName() + "'!", e, LOGGER);
        }
    }

    private void inspectionOfServiceTypes(final OntModel ontModel) {

        List<ServiceType> missingServiceTypeList = new ArrayList<>();
        List<Individual> ontServiceTypeIndList = new ArrayList<>(); //Ind: individual

        // preparation: get all individuals of the class "ProviderService" which are currently in the model
        final OntClass ontClassServiceType = ontModel.getOntClass(Constants.NS + "ProviderService");
        ontServiceTypeIndList = getIndOfOntSuperclass(ontServiceTypeIndList, ontClassServiceType);

        // get all serviceTypes (ProviderService) of the registry
        ServiceType[] serviceTypeArray = ServiceTemplateType.ServiceTemplate.ServiceType.values();

        for (ServiceType serviceTypeElement : serviceTypeArray) {
            final String serviceType = serviceTypeElement.toString();

            // list all missing serviceTypes. Means serviceTypes, which aren't currently in the model
            if (!ontServiceTypeIndList.toString().contains(serviceType)) {
                missingServiceTypeList.add(serviceTypeElement);
            }
        }
    }

    // get a list with all individuals of a superclass via recursion
    private List<Individual> getIndOfOntSuperclass(final List<Individual> individualList, final OntClass ontClass) {
        final ExtendedIterator instanceExIt;

        try {
            if (ontClass.hasSubClass()) {

                // case: class has subclass and individuals
                instanceExIt = ontClass.listInstances();
                while (instanceExIt.hasNext()) {
                    individualList.add((Individual) instanceExIt.next());
                }

                // goto next (sub-)class
                ExtendedIterator<OntClass> ontClassExIt = ontClass.listSubClasses();
                while (ontClassExIt.hasNext()) {
                    OntClass subOntClass = ontClassExIt.next();
                    getIndOfOntSuperclass(individualList, subOntClass);
                }
            } else {

                // class has no subclass(es) anymore. add individuals to list
                instanceExIt = ontClass.listInstances();
                while (instanceExIt.hasNext()) {
                    individualList.add((Individual) instanceExIt.next());
                }
            }
        } catch (NullPointerException e) {
            ExceptionPrinter.printHistory("No existing ontClass in method '"
                    + e.getStackTrace()[0].getMethodName() + "'!", e, LOGGER);
        }
        return individualList;
    }
}