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
import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.TripleArrayList;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
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
        super();

        final Set<UnitConfig> unitConfigSet = inspectionOfUnits(ontModel, getUnitConfigList());
        Set<OntClass> ontClassSet = new HashSet<>();
        OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.UNIT.getName());
        ontClassSet = getAllSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        List<TripleArrayList> tripleArrayLists1 = getOntTripleOfUnitTypes(ontClassSet, unitConfigSet);

        ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.STATE.getName());
        ontClassSet.clear();
        ontClassSet = getAllSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        List<TripleArrayList> tripleArrayLists2 = getOntTripleOfStates(ontClassSet, unitConfigSet);

        ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.PROVIDER_SERVICE.getName());
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
            if (unitType.equals(ConfigureSystem.OntClass.CONNECTION.getName())) {
                unitType = unitConfig.getConnectionConfig().getType().toString().toLowerCase();
                unitType = unitType.replaceAll(ConfigureSystem.REMOVE_PATTERN, "");
            } else if (unitType.equals(ConfigureSystem.OntClass.LOCATION.getName())) {
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

        for (final UnitConfig unitConfig : unitConfigSet) {

            if (UnitConfigProcessor.isDalUnit(unitConfig.getType())) {

                final String unitId = unitConfig.getId();
                final UnitRemote unitRemote = getUnitRemoteByUnitConfig(unitConfig);
                final Set<Object> objectSet = getMethodObjectsByUnitRemote(unitRemote,
                        ConfigureSystem.RegEx.GET_PATTERN_STATE);

                for (final Object object : objectSet) {
                    final String objectStateName = object.getClass().getName().toLowerCase();

                    for (final OntClass ontClass : ontClassSet) {
                        if (objectStateName.contains(ontClass.getLocalName().toLowerCase())) {
                            tripleArrayLists.add(new TripleArrayList(unitId, "a", ontClass.getLocalName()));
                        }
                    }
                }
            }
        }

        return tripleArrayLists;
    }

    private List<TripleArrayList> getOntTripleOfProviderServices(final OntClass ontClass,
                                                                 final Set<ServiceType> serviceTypeSet) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        if (ontClass != null) {
            // list all serviceTypes in a list
            for (final ServiceType serviceType : serviceTypeSet) {
                tripleArrayLists.add(new TripleArrayList(serviceType.toString(), "a", ontClass.getLocalName()));
            }
        }

        return tripleArrayLists;
    }
}
