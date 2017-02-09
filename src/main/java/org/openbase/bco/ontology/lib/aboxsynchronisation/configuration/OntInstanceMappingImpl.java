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
import org.openbase.bco.dal.lib.layer.service.Service;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType.ServiceConfig;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author agatting on 23.12.16.
 */
public class OntInstanceMappingImpl extends OntInstanceInspection implements OntInstanceMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstanceMappingImpl.class);

    //TODO exception handling
    //TODO add constructor with reusable java instances (e.g. ontClass, ...)?

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingOntTripleOfUnitsAfterInspection(final OntModel ontModel
            , final List<UnitConfig> unitConfigList) {

        // a set of unitConfigs, which are missing in the ontology
        final List<UnitConfig> unitConfigSet = inspectionOfUnits(ontModel, unitConfigList);
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
    public List<TripleArrayList> getMissingOntTripleOfUnits(final OntModel ontModel
            , final List<UnitConfig> unitConfigList) {

        // the ontSuperClass of the ontology to get all unit (sub)classes
        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.UNIT.getName());

        Set<OntClass> ontClassSet = new HashSet<>();
        // the set with all ontology unitType classes
        ontClassSet = listSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        // the triples to insert the missing units into the ontology
        return buildOntTripleOfUnitTypes(ontClassSet, unitConfigList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingOntTripleOfStates(final OntModel ontModel
            , final List<UnitConfig> unitConfigList) {

        // the ontSuperClass of the ontology to get all state (sub)classes
        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.STATE.getName());

        Set<OntClass> ontClassSet = new HashSet<>();
        // the set with all ontology state classes
        ontClassSet = listSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        return buildOntTripleOfStates(ontClassSet, unitConfigList);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getDeleteTripleOfUnitsAndStates(final List<UnitConfig> unitConfigList) {

        final List<TripleArrayList> tripleArrayDeleteLists = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigList) {
            tripleArrayDeleteLists.add(getDeleteTripleOfUnitsAndStates(unitConfig));
        }

        return tripleArrayDeleteLists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TripleArrayList getDeleteTripleOfUnitsAndStates(UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntExpr.A.getName();

        return new TripleArrayList(subject, predicate, null);
    }

    private List<TripleArrayList> buildOntTripleOfUnitTypes(final Set<OntClass> ontClassSet
            , final List<UnitConfig> unitConfigSet) {

        // alternative a list of strings (IDs) as mapValue and an unique key (unitType)
        final Map<String, String> unitTypeUnitIdMap = new HashMap<>();

        // list all unitTypes and their unitIds of the unitConfigSet in a hashMap
        for (final UnitConfig unitConfig : unitConfigSet) {
            String unitType = unitConfig.getType().toString().toLowerCase();
            unitType = unitType.replaceAll(ConfigureSystem.OntExpr.REMOVE.getName(), "");

            // is the current unitType a connection or location? set unitType variable with their type
            if (unitType.equals(ConfigureSystem.OntClass.CONNECTION.getName().toLowerCase())) {
                unitType = unitConfig.getConnectionConfig().getType().toString().toLowerCase();
                unitType = unitType.replaceAll(ConfigureSystem.OntExpr.REMOVE.getName(), "");
            } else if (unitType.equals(ConfigureSystem.OntClass.LOCATION.getName().toLowerCase())) {
                unitType = unitConfig.getLocationConfig().getType().toString().toLowerCase();
                unitType = unitType.replaceAll(ConfigureSystem.OntExpr.REMOVE.getName(), "");
            }

            unitTypeUnitIdMap.put(unitConfig.getId(), unitType);
        }

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        for (final OntClass ontClass : ontClassSet) {
            final String ontClassName = ontClass.getLocalName().toLowerCase();

            for (final Map.Entry<String, String> entry : unitTypeUnitIdMap.entrySet()) {
                if (entry.getValue().equals(ontClassName)) {
                    tripleArrayLists.add(new TripleArrayList(entry.getKey()
                            , ConfigureSystem.OntExpr.A.getName(), ontClass.getLocalName()));
                }
            }
        }
        return tripleArrayLists;
    }

    private List<TripleArrayList> buildOntTripleOfStates(final Set<OntClass> ontClassSet
            , final List<UnitConfig> unitConfigSet) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigSet) {

            if (UnitConfigProcessor.isDalUnit(unitConfig.getType())) {

                final String unitId = unitConfig.getId();

                //TODO check availability (not dal only), compare with ontology
                for (ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                    try {
                        //TODO maybe compare with ontology ontClass State
                        final String serviceState = Service.getServiceStateName(serviceConfig.getServiceTemplate());

                        tripleArrayLists.add(new TripleArrayList(unitId
                                , ConfigureSystem.OntExpr.A.getName(), serviceState));
                    } catch (NotAvailableException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }
                }
            }
        }

        return tripleArrayLists;
    }

    private List<TripleArrayList> buildOntTripleOfProviderServices(final OntClass ontClass
            , final Set<ServiceType> serviceTypeSet) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        if (ontClass != null) {
            // list all serviceTypes in a list
            for (final ServiceType serviceType : serviceTypeSet) {
                tripleArrayLists.add(new TripleArrayList(serviceType.toString()
                        , ConfigureSystem.OntExpr.A.getName(), ontClass.getLocalName()));
            }
        }

        return tripleArrayLists;
    }
}
