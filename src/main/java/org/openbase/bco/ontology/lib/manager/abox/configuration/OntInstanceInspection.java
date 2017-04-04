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
package org.openbase.bco.ontology.lib.manager.abox.configuration;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author agatting on 20.12.16.
 */
public class OntInstanceInspection {

    /**
     * Method compares the units of the registry with the actual units in the ontology model. Missing units are listed.
     *
     * @param unitConfigs A list of unitConfigs.
     * @param ontClass The ontClass unit.
     * @return A set of missing unitConfigs (units) in the actual ontology model.
     */
    protected List<UnitConfig> inspectionOfUnits(final List<UnitConfig> unitConfigs, final OntClass ontClass) {

        final List<UnitConfig> missingUnitConfigs = new ArrayList<>();
        Set<String> ontUnitInstances = new HashSet<>();

        ontUnitInstances = listInstancesOfOntClass(ontUnitInstances, ontClass);

        assert unitConfigs != null : "Could not get missing unitConfigs, cause unitConfigs list is null!";
        for (final UnitConfig unitConfig : unitConfigs) {

            if (!ontUnitInstances.contains(unitConfig.getId())) {
                // list all missing units. Means units, which aren't currently in the model
                missingUnitConfigs.add(unitConfig);
            }
        }
        return missingUnitConfigs;
    }

    /**
     * Method compares all registry serviceTypes with the serviceTypes in the ontology. Missing serviceTypes are listed.
     *
     * @param ontClassProviderService ontClassProviderService
     * @return A set of missing serviceTypes in the actual ontology model.
     */
    protected Set<ServiceType> inspectionOfServiceTypes(final OntClass ontClassProviderService) {

        final Set<ServiceType> missingServiceTypeSet = new HashSet<>();
        Set<String> ontServiceTypeInstances = new HashSet<>();

        ontServiceTypeInstances = listInstancesOfOntClass(ontServiceTypeInstances, ontClassProviderService);

        // get all serviceTypes (ProviderService) of the registry
        final ServiceType[] serviceTypes = ServiceType.values();

        for (final ServiceType serviceType : serviceTypes) {
            // list all missing serviceTypes. Means serviceTypes, which aren't currently in the model
            if (!ontServiceTypeInstances.contains(serviceType.name())) {
                missingServiceTypeSet.add(serviceType);
            }
        }
        return missingServiceTypeSet;
    }

    /**
     * Method returns a set of local names of all instances from a superclass via recursion. Differently to listIndividuals() of jena, the method returns all
     * instances (inclusive instances of subclasses). Furthermore the set contains the local name only (without namespace) to aim more efficiency by compare
     * operations (e.g. contains()) later on.
     *
     * @param instanceNameSet The set of names of the individuals. Can be empty. Needed for recursion.
     * @param ontClass The superclass which keeps the individuals.
     *
     * @return A set with local names of the input ont super class.
     */
    private Set<String> listInstancesOfOntClass(Set<String> instanceNameSet, final OntClass ontClass) {

        if (instanceNameSet == null) {
            instanceNameSet = new HashSet<>();
        }

        assert ontClass != null : "Could not list instances, cause ontClass is null!";
        if (ontClass.hasSubClass()) {
            // case: class has subclass and individuals
            final ExtendedIterator instanceExIt = ontClass.listInstances();

            while (instanceExIt.hasNext()) {
                final String instanceName = OntologyToolkit.getLocalName(instanceExIt.next().toString());
                // add local name (substring) of individual only
                instanceNameSet.add(instanceName);
            }
            // goto next (sub-)class
            final ExtendedIterator<OntClass> ontClassExIt = ontClass.listSubClasses();

            while (ontClassExIt.hasNext()) {
                final OntClass ontSubClass = ontClassExIt.next();
                listInstancesOfOntClass(instanceNameSet, ontSubClass);
            }
        } else {
            // class has no subclass(es) anymore. add individuals to list
            final ExtendedIterator instanceExIt = ontClass.listInstances();

            while (instanceExIt.hasNext()) {
                final String instanceName = OntologyToolkit.getLocalName(instanceExIt.next().toString());
                // add local name (substring) of individual only
                instanceNameSet.add(instanceName);
            }
        }
        return instanceNameSet;
    }

}
