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
import org.openbase.bco.ontology.lib.DataPool;
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
public abstract class OntInstanceMapping extends OntInstanceInspection implements ABoxConfiguration {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingOntTripleOfUnitTypes(final OntModel ontModel) {

        // a set of unitConfigs, which are missing in the ontology
        final Set<UnitConfig> unitConfigSet = inspectionOfUnits(ontModel, DataPool.getUnitConfigList());
        // the ontSuperClass of the ontology to get all unit (sub)classes
        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.UNIT.getName());

        Set<OntClass> ontClassSet = new HashSet<>();
        // the set with all ontology unitType classes
        ontClassSet = listSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        // the triples to insert the missing units into the ontology
        return buildOntTripleOfUnitTypes(ontClassSet, unitConfigSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingOntTripleOfStates(final OntModel ontModel) {

        // a set of unitConfigs, which are missing in the ontology
        final Set<UnitConfig> unitConfigSet = inspectionOfUnits(ontModel, DataPool.getUnitConfigList());

        // the ontSuperClass of the ontology to get all state (sub)classes
        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.STATE.getName());

        Set<OntClass> ontClassSet = new HashSet<>();
        // the set with all ontology state classes
        ontClassSet = listSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        return buildOntTripleOfStates(ontClassSet, unitConfigSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingOntTripleOfProviderServices(final OntModel ontModel) {

        // the ontSuperClass of the ontology to get all state (sub)classes
        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS
                + ConfigureSystem.OntClass.PROVIDER_SERVICE.getName());

        // the set of serviceTypes, which are missing in the ontology
        final Set<ServiceType> serviceTypeSet = inspectionOfServiceTypes(ontModel);

        return buildOntTripleOfProviderServices(ontClass, serviceTypeSet);
    }

    private List<TripleArrayList> buildOntTripleOfUnitTypes(final Set<OntClass> ontClassSet,
                                                            final Set<UnitConfig> unitConfigSet) {

        // alternative a list of strings (IDs) as mapValue and an unique key (unitType)
        final Map<String, String> unitTypeUnitIdMap = new HashMap<>();

        // list all unitTypes and their unitIds of the unitConfigSet in a hashMap
        for (final UnitConfig unitConfig : unitConfigSet) {
            String unitType = unitConfig.getType().toString().toLowerCase();
            unitType = unitType.replaceAll(ConfigureSystem.ExprPattern.REMOVE.getName(), "");

            // is the current unitType a connection or location? set unitType variable with their type
            if (unitType.equals(ConfigureSystem.OntClass.CONNECTION.getName())) {
                unitType = unitConfig.getConnectionConfig().getType().toString().toLowerCase();
                unitType = unitType.replaceAll(ConfigureSystem.ExprPattern.REMOVE.getName(), "");
            } else if (unitType.equals(ConfigureSystem.OntClass.LOCATION.getName())) {
                unitType = unitConfig.getLocationConfig().getType().toString().toLowerCase();
                unitType = unitType.replaceAll(ConfigureSystem.ExprPattern.REMOVE.getName(), "");
            }

            unitTypeUnitIdMap.put(unitConfig.getId(), unitType);
        }

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        for (final OntClass ontClass : ontClassSet) {
            final String ontClassName = ontClass.getLocalName().toLowerCase();

            for (final Map.Entry<String, String> entry : unitTypeUnitIdMap.entrySet()) {
                if (entry.getValue().equals(ontClassName)) {
                    tripleArrayLists.add(new TripleArrayList(entry.getKey()
                            , ConfigureSystem.ExprPattern.A.getName(), ontClass.getLocalName()));
                }
            }
        }
        return tripleArrayLists;
    }

    private List<TripleArrayList> buildOntTripleOfStates(final Set<OntClass> ontClassSet,
                                                         final Set<UnitConfig> unitConfigSet) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigSet) {

            if (UnitConfigProcessor.isDalUnit(unitConfig.getType())) {

                //TODO take new method of dal service interface

                final String unitId = unitConfig.getId();
                final UnitRemote unitRemote = DataPool.getUnitRemoteByUnitConfig(unitConfig);
                final Set<Object> objectSet = DataPool.getMethodObjectsByUnitRemote(unitRemote,
                        ConfigureSystem.RegEx.GET_PATTERN_STATE);

                for (final Object object : objectSet) {
                    final String objectStateName = object.getClass().getName().toLowerCase();

                    for (final OntClass ontClass : ontClassSet) {
                        if (objectStateName.contains(ontClass.getLocalName().toLowerCase())) {
                            tripleArrayLists.add(new TripleArrayList(unitId
                                    , ConfigureSystem.ExprPattern.A.getName(), ontClass.getLocalName()));
                        }
                    }
                }
            }
        }

        return tripleArrayLists;
    }

    private List<TripleArrayList> buildOntTripleOfProviderServices(final OntClass ontClass,
                                                                   final Set<ServiceType> serviceTypeSet) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        if (ontClass != null) {
            // list all serviceTypes in a list
            for (final ServiceType serviceType : serviceTypeSet) {
                tripleArrayLists.add(new TripleArrayList(serviceType.toString()
                        , ConfigureSystem.ExprPattern.A.getName(), ontClass.getLocalName()));
            }
        }

        return tripleArrayLists;
    }
}
