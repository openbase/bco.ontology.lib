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
package org.openbase.bco.ontology.lib.manager.tbox;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.openbase.bco.dal.lib.layer.service.Service;
import org.openbase.bco.ontology.lib.commun.web.OntModelWeb;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType.ServiceConfig;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author agatting on 20.02.17.
 */
public class TBoxSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TBoxSynchronizer.class);

    public OntModel extendTBoxViaServerModel(final List<UnitConfig> unitConfigList) throws InterruptedException, JPServiceException {

        // get tbox from server. if no available: create new from dependency.
        OntModel ontModel = OntModelWeb.getTBoxModelViaRetry();

        // get missing unitTypes and serviceStates
        ontModel = compareMissingUnitsWithModel(unitConfigList, ontModel);
        ontModel = compareMissingStatesWithModel(unitConfigList, ontModel);

        return ontModel;
    }

    public List<TripleArrayList> extendTBoxViaTriples(final List<UnitConfig> unitConfigs) {

        List<TripleArrayList> triples = new ArrayList<>();

        triples = getMissingUnitsAsTriples(unitConfigs, triples);
        triples = getMissingServiceStatesAsTriples(unitConfigs, triples);

        return triples;
    }

    private OntModel compareMissingUnitsWithModel(final List<UnitConfig> unitConfigs, final OntModel ontModel) {

        final OntClass unitOntClass = ontModel.getOntClass(OntologyToolkit.addNamespace(OntConfig.OntCl.UNIT.getName()));
        final Set<UnitType> missingUnitTypes = new HashSet<>();
        Set<OntClass> unitSubOntClasses = new HashSet<>();

        unitSubOntClasses = TBoxVerification.listSubclassesOfOntSuperclass(unitSubOntClasses, unitOntClass, false);

        for (final UnitConfig unitConfig : unitConfigs) {
            if (!isUnitTypePresent(unitConfig, unitSubOntClasses)) {
                missingUnitTypes.add(unitConfig.getType());
            }
        }
        return addMissingUnitsToModel(missingUnitTypes, ontModel);
    }

    private List<TripleArrayList> getMissingUnitsAsTriples(final List<UnitConfig> unitConfigs, final List<TripleArrayList> triples) {

        final Set<UnitType> missingUnitTypes = unitConfigs.stream().map(UnitConfig::getType).collect(Collectors.toSet());

        return addMissingUnitsToTriples(missingUnitTypes, triples);
    }

    private OntModel compareMissingStatesWithModel(final List<UnitConfig> unitConfigs, final OntModel ontModel) {

        final OntClass stateOntClass = ontModel.getOntClass(OntologyToolkit.addNamespace(OntConfig.OntCl.STATE.getName()));
        final Set<String> missingServiceStateTypes = new HashSet<>();
        Set<OntClass> stateSubOntClasses = new HashSet<>();

        stateSubOntClasses = TBoxVerification.listSubclassesOfOntSuperclass(stateSubOntClasses, stateOntClass, false);

        for (final UnitConfig unitConfig : unitConfigs) {
            for (final ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                try {
                    final String serviceStateName = Service.getServiceStateName(serviceConfig.getServiceTemplate());

                    if (!isStateTypePresent(serviceStateName, stateSubOntClasses)) {
                        missingServiceStateTypes.add(serviceStateName);
                    }
                } catch (NotAvailableException e) {
                    ExceptionPrinter.printHistory("Could not identify service state name of serviceConfig: " + serviceConfig.toString() + ". Dropped."
                            , e, LOGGER, LogLevel.WARN);
                }
            }
        }
        return addMissingStatesToModel(missingServiceStateTypes, ontModel);
    }

    private List<TripleArrayList> getMissingServiceStatesAsTriples(final List<UnitConfig> unitConfigs, final List<TripleArrayList> triples) {

        final Set<String> missingServiceStateTypes = new HashSet<>();

        for (final UnitConfig unitConfig : unitConfigs) {
            for (final ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                try {
                    final String serviceStateName = Service.getServiceStateName(serviceConfig.getServiceTemplate());
                    missingServiceStateTypes.add(serviceStateName);
                } catch (NotAvailableException e) {
                    ExceptionPrinter.printHistory("Could not identify service state name of serviceConfig: " + serviceConfig.toString() + ". Dropped."
                            , e, LOGGER, LogLevel.WARN);
                }
            }
        }
        return addMissingStatesToTriples(missingServiceStateTypes, triples);
    }

    private OntModel addMissingUnitsToModel(final Set<UnitType> missingUnitTypes, final OntModel ontModel) {

        final OntClass dalOntClass = ontModel.getOntClass(OntologyToolkit.addNamespace(OntConfig.OntCl.DAL_UNIT.getName()));
        final OntClass baseOntClass = ontModel.getOntClass(OntologyToolkit.addNamespace(OntConfig.OntCl.BASE_UNIT.getName()));
        final OntClass hostOntClass = ontModel.getOntClass(OntologyToolkit.addNamespace(OntConfig.OntCl.HOST_UNIT.getName()));

        for (final UnitType unitType : missingUnitTypes) {
            final String missingUnitType = OntologyToolkit.convertToNounAndAddNS(unitType.name());
            final OntClass newOntClassUnitType = ontModel.createClass(missingUnitType);

            // find correct subclass of unit (e.g. baseUnit, DalUnit)
            if (UnitConfigProcessor.isDalUnit(unitType)) {
                ontModel.getOntClass(dalOntClass.getURI()).addSubClass(newOntClassUnitType);
            } else if (UnitConfigProcessor.isBaseUnit(unitType)) {
                if (UnitConfigProcessor.isHostUnit(unitType)) {
                    ontModel.getOntClass(hostOntClass.getURI()).addSubClass(newOntClassUnitType);
                } else {
                    ontModel.getOntClass(baseOntClass.getURI()).addSubClass(newOntClassUnitType);
                }
            }
        }
        return ontModel;
    }

    private List<TripleArrayList> addMissingUnitsToTriples(final Set<UnitType> missingUnitTypes, final List<TripleArrayList> triples) {

        final String pred_isA = OntConfig.OntExpr.A.getName();

        for (final UnitType unitType : missingUnitTypes) {
            final String subj_UnitType = OntologyToolkit.convertToNounAndAddNS(unitType.name());

            // find correct subclass of unit (e.g. baseUnit, dalUnit)
            if (UnitConfigProcessor.isDalUnit(unitType)) {
                triples.add(new TripleArrayList(subj_UnitType, pred_isA, OntConfig.OntCl.DAL_UNIT.getName()));
            } else if (UnitConfigProcessor.isBaseUnit(unitType)) {
                if (UnitConfigProcessor.isHostUnit(unitType)) {
                    triples.add(new TripleArrayList(subj_UnitType, pred_isA, OntConfig.OntCl.HOST_UNIT.getName()));
                } else {
                    triples.add(new TripleArrayList(subj_UnitType, pred_isA, OntConfig.OntCl.BASE_UNIT.getName()));
                }
            }
        }
        return triples;
    }

    private OntModel addMissingStatesToModel(final Set<String> missingStateTypes, final OntModel ontModel) {

        final OntClass stateOntClass = ontModel.getOntClass(OntologyToolkit.addNamespace(OntConfig.OntCl.STATE.getName()));

        for (final String missingState : missingStateTypes) {

            final String missingStateType = OntologyToolkit.convertToNounAndAddNS(missingState);
            final OntClass newOntClassStateType = ontModel.createClass(missingStateType);

            ontModel.getOntClass(stateOntClass.getURI()).addSubClass(newOntClassStateType);
        }
        return ontModel;
    }

    private List<TripleArrayList> addMissingStatesToTriples(final Set<String> missingStateTypes, final List<TripleArrayList> triples) {

        final String predicate = OntConfig.OntExpr.A.getName();
        final String object = OntConfig.OntCl.STATE.getName();

        for (final String missingState : missingStateTypes) {

            final String missingStateType = OntologyToolkit.convertToNounAndAddNS(missingState);
            triples.add(new TripleArrayList(missingStateType, predicate, object));
        }
        return triples;
    }

    private boolean isUnitTypePresent(final UnitConfig unitConfig, final Set<OntClass> unitSubOntClasses) {

        final String unitTypeName = unitConfig.getType().name();

        for (final OntClass ontClass : unitSubOntClasses) {
            if (ontClass.getLocalName().equalsIgnoreCase(unitTypeName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStateTypePresent(final String serviceStateName, final Set<OntClass> stateSubOntClasses) {

        for (final OntClass ontClass : stateSubOntClasses) {
            if (ontClass.getLocalName().equalsIgnoreCase(serviceStateName)) {
                return true;
            }
        }
        return false;
    }

}
