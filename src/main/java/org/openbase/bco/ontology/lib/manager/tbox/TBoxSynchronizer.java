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
import org.openbase.bco.ontology.lib.manager.OntologyEditCommands;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.commun.web.ServerOntologyModel;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType.ServiceConfig;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 20.02.17.
 */
public class TBoxSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TBoxSynchronizer.class);
    private ScheduledFuture scheduledFutureTask;
    private final Stopwatch stopwatch;

    public TBoxSynchronizer(final List<UnitConfig> unitConfigList) throws InterruptedException {

        stopwatch = new Stopwatch();

        // ### Init ###
        OntModel ontModel = getTBox();

        // add missing unitTypes to ontModel
        ontModel = compareUnitsWithOntClasses(unitConfigList, ontModel);
        // add missing stateTypes to ontModel
        ontModel = compareStatesWithOntology(unitConfigList, ontModel);

    }

    private OntModel compareUnitsWithOntClasses(final List<UnitConfig> unitConfigList, final OntModel ontModel) {

        final OntClass unitOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespace(OntConfig.OntCl.UNIT.getName()));
        final Set<UnitType> missingUnitTypes = new HashSet<>();
        Set<OntClass> unitSubOntClasses = new HashSet<>();

        unitSubOntClasses = TBoxVerificationResource.listSubclassesOfOntSuperclass(unitSubOntClasses, unitOntClass, false);

        for (final UnitConfig unitConfig : unitConfigList) {
            if (!isUnitTypePresent(unitConfig, unitSubOntClasses)) {
                missingUnitTypes.add(unitConfig.getType());
            }
        }

        addMissingOntUnits(ontModel, missingUnitTypes);

        return ontModel;
    }

    private OntModel compareStatesWithOntology(final List<UnitConfig> unitConfigList, final OntModel ontModel) {

        final OntClass stateOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespace(OntConfig.OntCl.STATE.getName()));
        final Set<String> missingServiceStateTypes = new HashSet<>();
        Set<OntClass> stateSubOntClasses = new HashSet<>();

        stateSubOntClasses = TBoxVerificationResource.listSubclassesOfOntSuperclass(stateSubOntClasses, stateOntClass, false);

        for (final UnitConfig unitConfig : unitConfigList) {
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
        addMissingOntStates(ontModel, missingServiceStateTypes);

        return ontModel;
    }

    private OntModel addMissingOntUnits(final OntModel ontModel, final Set<UnitType> missingUnitTypes) {

        final OntClass dalOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespace(OntConfig.OntCl.DAL_UNIT.getName()));
        final OntClass baseOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespace(OntConfig.OntCl.BASE_UNIT.getName()));
        final OntClass hostOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespace(OntConfig.OntCl.HOST_UNIT.getName()));

        for (final UnitType unitType : missingUnitTypes) {

            final String missingUnitType = OntologyEditCommands.convertToNounAndAddNS(unitType.name());
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

    private OntModel addMissingOntStates(final OntModel ontModel, final Set<String> missingStateTypes) {

        final OntClass stateOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespace(OntConfig.OntCl.STATE.getName()));

        for (final String missingState : missingStateTypes) {

            final String missingStateType = OntologyEditCommands.convertToNounAndAddNS(missingState);
            final OntClass newOntClassStateType = ontModel.createClass(missingStateType);

            ontModel.getOntClass(stateOntClass.getURI()).addSubClass(newOntClassStateType);
        }
        return ontModel;
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

    private OntModel getTBox() throws InterruptedException {

        OntModel ontModel = null;

        while (ontModel == null) {
            try {
                ontModel = ServerOntologyModel.getOntologyModelFromServer(OntConfig.getTBoxDatabaseUri());

                if (ontModel.isEmpty()) {
                    ontModel = TBoxLoader.loadOntModelFromFile(null);
                }

            } catch (IOException e) {
                //retry
                ExceptionPrinter.printHistory("No connection...retry. ", e, LOGGER, LogLevel.WARN);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
        return ontModel;
    }

    private void uploadTBox(final OntModel ontModel) {

        try {
            ServerOntologyModel.addOntologyModel(ontModel, OntConfig.getOntDatabaseUri(), OntConfig.getTBoxDatabaseUri());
        } catch (CouldNotPerformException e) {

        }
    }

    private void initTBox() throws NotAvailableException {
        scheduledFutureTask = GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {

            try {
                if (!ServerOntologyModel.isOntModelOnServer(OntConfig.getTBoxDatabaseUri())) {
                    // server is empty - load and put ontology model (TBox) to first and second dataSets
                    final OntModel ontModel = TBoxLoader.loadOntModelFromFile(null);
                    ServerOntologyModel.addOntologyModel(ontModel, OntConfig.getOntDatabaseUri(), OntConfig.getTBoxDatabaseUri());
                }

                if (ServerOntologyModel.isOntModelOnServer(OntConfig.getTBoxDatabaseUri())
                        && ServerOntologyModel.isOntModelOnServer(OntConfig.getOntDatabaseUri())) {
                    // tbox upload was successful
                    scheduledFutureTask.cancel(true);
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            }
        }, 0, OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

}
