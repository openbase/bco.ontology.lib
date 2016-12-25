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
package org.openbase.bco.ontology.lib.aboxsynchronisation.configuration;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.EnablingStateType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by agatting on 20.12.16.
 */
public class OntInstancesInspection {
    //TODO handling of units with missing data (e.g. location)

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstancesInspection.class);

    /**
     * Constructor for OntInstancesInspection.
     *
     * @param ontModel ontModel is the ontology model.
     */
    public OntInstancesInspection(final OntModel ontModel) {

//        final DataPool dataPool = new DataPool();
//        final UnitRegistry unitRegistry = dataPool.getUnitRegistry();
//
//        final List<UnitConfig> unitConfigList = inspectionOfUnits(ontModel, unitRegistry);
//        final List<ServiceType> serviceTypeList = inspectionOfServiceTypes(ontModel);
    }

    /**
     * Method compares the units of the registry with the actual units in the ontology model. Missing units are listed.
     *
     * @param ontModel The actual ontology model.
     * @param unitRegistry The actual registry.
     *
     * @return A set of missing unitConfigs (units) in the actual ontology model.
     */
    protected Set<UnitConfig> inspectionOfUnits(final OntModel ontModel, final UnitRegistry unitRegistry) {

        final Set<UnitConfig> missingUnitConfigSet = new HashSet<>();
        Set<Individual> ontUnitIndSet = new HashSet<>(); //Ind: individual

        // preparation: get all individuals of the class "Unit" which are currently in the model
        final OntClass ontClassUnit = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.UNIT_SUPERCLASS);
        if (ontClassUnit != null) {
            ontUnitIndSet = getIndOfOntSuperclass(ontUnitIndSet, ontClassUnit);
        }

        try {
            // run through all enabled unitConfigs of the registry
            for (final UnitConfig unitConfig : unitRegistry.getUnitConfigs()) {
                if (unitConfig.getEnablingState().getValue().equals(EnablingStateType.EnablingState.State.ENABLED)) {
                    final String unitId = unitConfig.getId();

                    // list all missing units. Means units, which aren't currently in the model
                    if (!ontUnitIndSet.toString().contains(unitId)) {
                        missingUnitConfigSet.add(unitConfig);
                    }
                }
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("Could not perform unitRegistry in method "
                    + e.getStackTrace()[0].getMethodName(), e, LOGGER);
        }
        return missingUnitConfigSet;
    }

    private Set<ServiceType> inspectionOfServiceTypes(final OntModel ontModel) {

        final Set<ServiceType> missingServiceTypeSet = new HashSet<>();
        Set<Individual> ontServiceTypeIndSet = new HashSet<>(); //Ind: individual

        // preparation: get all individuals of the class "ProviderService" which are currently in the model
        final OntClass ontClassServiceType = ontModel
                .getOntClass(ConfigureSystem.NS + ConfigureSystem.PROVIDER_SERVICES_SUPERCLASS);
        ontServiceTypeIndSet = getIndOfOntSuperclass(ontServiceTypeIndSet, ontClassServiceType);

        // get all serviceTypes (ProviderService) of the registry
        final ServiceType[] serviceTypeArray = ServiceTemplateType.ServiceTemplate.ServiceType.values();

        for (final ServiceType serviceTypeElement : serviceTypeArray) {
            final String serviceType = serviceTypeElement.toString();

            // list all missing serviceTypes. Means serviceTypes, which aren't currently in the model
            if (!ontServiceTypeIndSet.toString().contains(serviceType)) {
                missingServiceTypeSet.add(serviceTypeElement);
            }
        }
        return missingServiceTypeSet;
    }

    // get a list with all individuals of a superclass via recursion
    private Set<Individual> getIndOfOntSuperclass(final Set<Individual> individualSet, final OntClass ontClass) {

        //TODO remove namespace (and prefix) via prefix

        final ExtendedIterator instanceExIt;
        if (ontClass.hasSubClass()) {

            // case: class has subclass and individuals
            instanceExIt = ontClass.listInstances();
            while (instanceExIt.hasNext()) {
                individualSet.add((Individual) instanceExIt.next());
            }

            // goto next (sub-)class
            final ExtendedIterator<OntClass> ontClassExIt = ontClass.listSubClasses();
            while (ontClassExIt.hasNext()) {
                final OntClass ontSubClass = ontClassExIt.next();
                getIndOfOntSuperclass(individualSet, ontSubClass);
            }
        } else {

            // class has no subclass(es) anymore. add individuals to list
            instanceExIt = ontClass.listInstances();
            while (instanceExIt.hasNext()) {
                individualSet.add((Individual) instanceExIt.next());
            }
        }
        return individualSet;
    }

    /**
     * Method delivers all subclasses of the given superclass via recursion.
     *
     * @param ontClassSet The (empty) set to itemize the ontClasses.
     * @param ontSuperClass The superclass.
     * @param inclusiveSuperclass Result list keeps superclass or not.
     *
     * @return The list with ontClasses.
     */
    public Set<OntClass> getAllSubclassesOfOntSuperclass(final Set<OntClass> ontClassSet, final OntClass
            ontSuperClass, final boolean inclusiveSuperclass) {

        // add initial superclass
        if (inclusiveSuperclass) {
            ontClassSet.add(ontSuperClass);
        }

        // get all subclasses of current superclass
        final ExtendedIterator ontClassExIt;
        ontClassExIt = ontSuperClass.listSubClasses();

        // add subclass(es) and if subclass has subclass(es) goto next layer via recursion
        while (ontClassExIt.hasNext()) {
            final OntClass ontClass = (OntClass) ontClassExIt.next();
            ontClassSet.add(ontClass);

            if (ontSuperClass.hasSubClass()) {
                getAllSubclassesOfOntSuperclass(ontClassSet, ontClass, false);
            }
        }
        return ontClassSet;
    }
}