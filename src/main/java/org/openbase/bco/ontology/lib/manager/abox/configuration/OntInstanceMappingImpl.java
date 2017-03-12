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
import org.openbase.bco.dal.lib.layer.service.Service;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType.ServiceConfig;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author agatting on 23.12.16.
 */
public class OntInstanceMappingImpl extends OntInstanceInspection implements OntInstanceMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntInstanceMappingImpl.class);

    //TODO exception handling
    //TODO add constructor with reusable java instances (e.g. ontClass, ...)?
    //TODO add method, which calls all methods and checks if unitConfigs are initialized
    //TODO adapt serviceType names to noun syntax (global)

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingOntTripleOfUnitsAfterInspection(final OntModel ontModel, final List<UnitConfig> unitConfigList) {
        // a set of unitConfigs, which are missing in the ontology
        final List<UnitConfig> unitConfigSet = inspectionOfUnits(ontModel, unitConfigList);

        // the triples to insert the missing units into the ontology
        return buildOntTripleOfUnitTypes(unitConfigSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingUnitTriples(final List<UnitConfig> unitConfigList) {
        // the triples to insert the missing units into the ontology
        return buildOntTripleOfUnitTypes(unitConfigList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingOntTripleOfStates(final List<UnitConfig> unitConfigList) {

        return buildOntTripleOfStates(unitConfigList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingOntTripleOfProviderServices(final OntModel ontModel) {

        // the set of serviceTypes, which are missing in the ontology
        final Set<ServiceType> serviceTypes = inspectionOfServiceTypes(ontModel);

        return buildServiceTriples(serviceTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingServiceTriples(final List<UnitConfig> unitConfigList) {

        final Set<ServiceType> serviceTypes = new HashSet<>();

        for (final UnitConfig unitConfig : unitConfigList) {
            for (final ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                final ServiceType serviceType = serviceConfig.getServiceTemplate().getType();
//                final String serviceTypeName = OntologyToolkit.convertToNounSyntax(serviceType.name());
                serviceTypes.add(serviceType);
            }
        }
        return buildServiceTriples(serviceTypes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getDeleteTripleOfUnitsAndStates(final List<UnitConfig> unitConfigList) {
        return unitConfigList.stream().map(this::getDeleteTripleOfUnitsAndStates).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TripleArrayList getDeleteTripleOfUnitsAndStates(UnitConfig unitConfig) {
        // s, p, o pattern
        return new TripleArrayList(unitConfig.getId(), OntExpr.A.getName(), null);
    }

    private List<TripleArrayList> buildOntTripleOfUnitTypes(final List<UnitConfig> unitConfigSet) {

        final List<TripleArrayList> triples = new ArrayList<>();

        // list all unitTypes and their unitIds of the unitConfigSet in a hashMap
        for (final UnitConfig unitConfig : unitConfigSet) {
            String unitType = OntologyToolkit.convertToNounSyntax(unitConfig.getType().name());

            // is the current unitType a connection or location? set unitType variable with their type
            if (unitType.equalsIgnoreCase(OntCl.CONNECTION.getName())) {
                unitType = OntologyToolkit.convertToNounSyntax(unitConfig.getConnectionConfig().getType().name());
            } else if (unitType.equalsIgnoreCase(OntCl.LOCATION.getName())) {
                unitType = OntologyToolkit.convertToNounSyntax(unitConfig.getLocationConfig().getType().name());
            }

            triples.add(new TripleArrayList(unitConfig.getId(), OntExpr.A.getName(), unitType));
        }
        return triples;
    }

    private List<TripleArrayList> buildOntTripleOfStates(final List<UnitConfig> unitConfigSet) {

        final List<TripleArrayList> triples = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigSet) {
            final String unitId = unitConfig.getId();

            for (ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                try {
                    String serviceState = Service.getServiceStateName(serviceConfig.getServiceTemplate());
                    serviceState = OntologyToolkit.convertToNounSyntax(serviceState);

                    triples.add(new TripleArrayList(unitId, OntExpr.A.getName(), serviceState));
                } catch (NotAvailableException e) {
                    ExceptionPrinter.printHistory("Could not identify service state name of serviceConfig: " + serviceConfig.toString() + ". Dropped."
                            , e, LOGGER, LogLevel.WARN);
                }
            }
        }
        return triples;
    }

    private List<TripleArrayList> buildServiceTriples(final Set<ServiceType> serviceTypeSet) {

        final List<TripleArrayList> triples = new ArrayList<>();

        // list all serviceTypes in a list
        for (final ServiceType serviceType : serviceTypeSet) {
            triples.add(new TripleArrayList(serviceType.toString(), OntExpr.A.getName(), OntCl.PROVIDER_SERVICE.getName()));
        }
        return triples;
    }
}
