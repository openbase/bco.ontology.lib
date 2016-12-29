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

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.DataPool;
import org.openbase.bco.ontology.lib.TripleArrayList;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by agatting on 23.12.16.
 */
public class OntInstanceMapping extends OntInstanceInspection {

    /**
     * Constructor for OntInstanceInspection.
     *
     * @param ontModel The actual ontology model.
     */
    public OntInstanceMapping(final OntModel ontModel) {
        super(ontModel);

        final DataPool dataPool = new DataPool();
        final UnitRegistry unitRegistry = dataPool.getUnitRegistry();

        final Set<UnitConfig> unitConfigSet = inspectionOfUnits(ontModel, unitRegistry);
        Set<OntClass> ontClassSet = new HashSet<>();
        OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.UNIT_SUPERCLASS);
        ontClassSet = getAllSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        List<TripleArrayList> tripleArrayLists1 = getOntTripleOfUnitTypes(ontClassSet, unitConfigSet);

        ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.STATE_SUPERCLASS);
        ontClassSet.clear();
        ontClassSet = getAllSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        List<TripleArrayList> tripleArrayLists2 = getOntTripleOfStates(ontClassSet, unitConfigSet);


        ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.PROVIDER_SERVICE_SUPERCLASS);
        final Set<ServiceType> serviceTypeSet = inspectionOfServiceTypes(ontModel);

        List<TripleArrayList> tripleArrayLists3 = getOntTripleOfProviderServices(ontClass, serviceTypeSet);
    }

    private List<TripleArrayList> getOntTripleOfUnitTypes(final Set<OntClass> ontClassSet,
                                                          final Set<UnitConfig> unitConfigSet) {

        // alternative a list of strings (IDs) as mapValue and an unique key (unitType)
        final Map<String, String> unitTypeUnitIdMap = new HashMap<>();

        // list all unitTypes and their unitIds of the unitConfigSet in a hashMap
        for (final UnitConfig unitConfig : unitConfigSet) {
            String unitType = unitConfig.getType().toString().toLowerCase();
            unitType = unitType.replaceAll(ConfigureSystem.REMOVE_PATTERN, "");

            // is the current unitType a connection or location? set unitType variable with their type
            if (unitType.equals(ConfigureSystem.UNIT_TYPE_CONNECTION)) {
                unitType = unitConfig.getConnectionConfig().getType().toString().toLowerCase();
                unitType = unitType.replaceAll(ConfigureSystem.REMOVE_PATTERN, "");
            } else if (unitType.equals(ConfigureSystem.UNIT_TYPE_LOCATION)) {
                unitType = unitConfig.getLocationConfig().getType().toString().toLowerCase();
                unitType = unitType.replaceAll(ConfigureSystem.REMOVE_PATTERN, "");
            }

            unitTypeUnitIdMap.put(unitConfig.getId(), unitType);
        }

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        for (final OntClass ontClass : ontClassSet) {
            final String ontClassName = ontClass.getLocalName().toLowerCase();

            for (final Map.Entry<String, String> entry : unitTypeUnitIdMap.entrySet()) {
                if (entry.getValue().equals(ontClassName)) {
                    tripleArrayLists.add(new TripleArrayList(entry.getKey(), "a", ontClass.getLocalName()));
                }
            }
        }
        return tripleArrayLists;
    }

    private List<TripleArrayList> getOntTripleOfStates(final Set<OntClass> ontClassSet,
                                                       final Set<UnitConfig> unitConfigSet) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();
        // alternative a list of strings (IDs) as mapValue and an unique key (state)
//        final Map<String, String> stateUnitIdMap = new HashMap<>();

        // list all states and their unitIds of the unitConfigSet in a hashMap
        for (final UnitConfig unitConfig : unitConfigSet) {
            //TODO
        }



        return tripleArrayLists;
    }

    private List<TripleArrayList> getOntTripleOfProviderServices(final OntClass ontClass,
                                                                 final Set<ServiceType> serviceTypeSet) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        // list all serviceTypes in a list
        for (final ServiceType serviceType : serviceTypeSet) {
            tripleArrayLists.add(new TripleArrayList(serviceType.toString(), "a", ontClass.getLocalName()));
        }

        return tripleArrayLists;
    }
}
