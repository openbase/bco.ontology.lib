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
import org.apache.jena.ontology.OntModel;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
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
    //TODO handling of units with missing data (e.g. location)

    /**
     * Method compares the units of the registry with the actual units in the ontology model. Missing units are listed.
     *
     * @param ontModel The actual ontology model.
     * @param unitConfigList A list of unitConfigs.
     *
     * @return A set of missing unitConfigs (units) in the actual ontology model.
     */
    protected List<UnitConfig> inspectionOfUnits(final OntModel ontModel, final List<UnitConfig> unitConfigList) {

        final List<UnitConfig> missingUnitConfigSet = new ArrayList<>();
        Set<String> ontUnitIndNameSet = new HashSet<>(); //Ind: individual

        // preparation: get all individuals of the class "Unit" which are currently in the model
        final OntClass ontClassUnit = ontModel.getOntClass(OntConfig.NS + OntCl.UNIT.getName());

        if (ontClassUnit == null) {
            //TODO
        } else {
            ontUnitIndNameSet = listIndOfOntClass(ontUnitIndNameSet, ontClassUnit);
        }

        for (final UnitConfig unitConfig : unitConfigList) {
            final String unitId = unitConfig.getId();

            if (!ontUnitIndNameSet.contains(unitId)) {
                // list all missing units. Means units, which aren't currently in the model
                missingUnitConfigSet.add(unitConfig);
            }
        }

        return missingUnitConfigSet;
    }

    /**
     * Method compares all registry serviceTypes with the serviceTypes in the ontology. Missing serviceTypes are listed.
     *
     * @param ontModel The actual ontology model.
     *
     * @return A set of missing serviceTypes in the actual ontology model.
     */
    protected Set<ServiceType> inspectionOfServiceTypes(final OntModel ontModel) {

        final Set<ServiceType> missingServiceTypeSet = new HashSet<>();
        Set<String> ontServiceTypeIndNameSet = new HashSet<>(); //Ind: individual

        // preparation: get all individuals of the class "ProviderService" which are currently in the model
        final OntClass ontClassServiceType = ontModel
                .getOntClass(OntConfig.NS + OntCl.PROVIDER_SERVICE.getName());
        ontServiceTypeIndNameSet = listIndOfOntClass(ontServiceTypeIndNameSet, ontClassServiceType);

        // get all serviceTypes (ProviderService) of the registry
        final ServiceType[] serviceTypeArray = ServiceType.values();

        for (final ServiceType serviceTypeElement : serviceTypeArray) {
            final String serviceType = serviceTypeElement.toString();

            // list all missing serviceTypes. Means serviceTypes, which aren't currently in the model
            if (!ontServiceTypeIndNameSet.contains(serviceType)) {
                missingServiceTypeSet.add(serviceTypeElement);
            }
        }
        return missingServiceTypeSet;
    }

    /**
     * Method returns a set of names of individuals from a superclass via recursion. Differently to listIndividuals()
     * of jena, the method returns all individuals (inclusive individuals of subclasses). Furthermore the set contains
     * the local name only (without namespace) to aim more efficiency by compare operations (e.g. contains()) later on.
     *
     * @param individualNameSet The set of names of the individuals.
     * @param ontClass The superclass which keeps the individuals.
     *
     * @return A set of strings with individual localNames.
     */
    private Set<String> listIndOfOntClass(final Set<String> individualNameSet, final OntClass ontClass) {

        final ExtendedIterator instanceExIt;
        if (ontClass.hasSubClass()) {

            // case: class has subclass and individuals
            instanceExIt = ontClass.listInstances();
            while (instanceExIt.hasNext()) {
                // add local name (substring) of individual only
                String indName = instanceExIt.next().toString();
                indName = indName.substring(OntConfig.NS.length(), indName.length());
                individualNameSet.add(indName);
            }

            // goto next (sub-)class
            final ExtendedIterator<OntClass> ontClassExIt = ontClass.listSubClasses();
            while (ontClassExIt.hasNext()) {
                final OntClass ontSubClass = ontClassExIt.next();
                listIndOfOntClass(individualNameSet, ontSubClass);
            }
        } else {

            // class has no subclass(es) anymore. add individuals to list
            instanceExIt = ontClass.listInstances();
            while (instanceExIt.hasNext()) {
                // add local name (substring) of individual only
                String indName = instanceExIt.next().toString();
                indName = indName.substring(OntConfig.NS.length(), indName.length());
                individualNameSet.add(indName);
            }
        }
        return individualNameSet;
    }

    /**
     * Method inspects the availability of an individual in the ontology.
     *
     * @param ontClass The ontClass, which contains the individual. Limited the search area.
     * @param individualName The name of the individual (with or without namespace).
     *
     * @return Boolean with result true, if available and result false, if not available.
     */
    @SuppressWarnings("PMD.LocalVariableCouldBeFinal")
    protected boolean existIndInOnt(final OntClass ontClass, final String individualName) {

        String bufName = individualName;

        if (individualName.startsWith(OntConfig.NS)) {
            bufName = individualName.substring(OntConfig.NS.length(), individualName.length());
        }

        Set<String> stringSet = new HashSet<>();
        stringSet = listIndOfOntClass(stringSet, ontClass);

        for (String ontIndividual : stringSet) {
             if (ontIndividual.equals(bufName)) {
                 return true;
             }
        }

        return false;
    }
}
