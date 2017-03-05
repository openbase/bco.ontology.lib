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
        final OntModel ontModel = getTBox();

        // add missing unitTypes to ontModel
        compareUnitsWithOntClasses(unitConfigList, ontModel);
    }

    private void compareUnitsWithOntClasses(final List<UnitConfig> unitConfigList, final OntModel ontModel) {

        final OntClass unitOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespaceToOntElement(OntConfig.OntCl.UNIT.getName()));
        final Set<UnitType> missingUnitTypes = new HashSet<>();
        Set<OntClass> ontClasses = new HashSet<>();

        ontClasses = TBoxVerificationResource.listSubclassesOfOntSuperclass(ontClasses, unitOntClass, false);

        for (final UnitConfig unitConfig : unitConfigList) {
            if (!isUnitTypePresent(unitConfig, ontClasses)) {
                missingUnitTypes.add(unitConfig.getType());
            }
        }

        addMissingOntUnit(ontModel, missingUnitTypes);
    }

    private void addMissingOntUnit(final OntModel ontModel, final Set<UnitType> missingUnitTypes) {

        final OntClass dalOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespaceToOntElement(OntConfig.OntCl.DAL_UNIT.getName()));
        final OntClass baseOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespaceToOntElement(OntConfig.OntCl.BASE_UNIT.getName()));
        final OntClass hostOntClass = ontModel.getOntClass(OntologyEditCommands.addNamespaceToOntElement(OntConfig.OntCl.HOST_UNIT.getName()));

        for (final UnitType unitType : missingUnitTypes) {
            String newOntClassUnitType = OntologyEditCommands.convertWordToNounSyntax(unitType.name());
            newOntClassUnitType = OntologyEditCommands.addNamespaceToOntElement(newOntClassUnitType);
            final OntClass missingUnitType = ontModel.createClass(newOntClassUnitType);

            // find correct subclass of unit (e.g. baseUnit, DalUnit)
            if (UnitConfigProcessor.isDalUnit(unitType)) {
                ontModel.getOntClass(dalOntClass.getURI()).addSubClass(missingUnitType);
            } else if (UnitConfigProcessor.isBaseUnit(unitType)) {
                if (UnitConfigProcessor.isHostUnit(unitType)) {
                    ontModel.getOntClass(hostOntClass.getURI()).addSubClass(missingUnitType);
                } else {
                    ontModel.getOntClass(baseOntClass.getURI()).addSubClass(missingUnitType);
                }
            }
        }
    }

    private boolean isUnitTypePresent(final UnitConfig unitConfig, final Set<OntClass> ontClasses) {

        final String unitTypeName = unitConfig.getType().name();

        for (final OntClass ontClass : ontClasses) {
            if (ontClass.getLocalName().equalsIgnoreCase(unitTypeName)) {
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

    private void initTBox() throws NotAvailableException {
        scheduledFutureTask = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

            try {
                if (!ServerOntologyModel.isOntModelOnServer(OntConfig.getTBoxDatabaseUri())) {
                    // server is empty - load and put ontology model (TBox) to first and second dataSets
                    final OntModel ontModel = TBoxLoader.loadOntModelFromFile(null);
                    ServerOntologyModel.addOntologyModel(ontModel, OntConfig.getTBoxDatabaseUri());
                    ServerOntologyModel.addOntologyModel(ontModel, OntConfig.getOntDatabaseUri());
                }

                if (ServerOntologyModel.isOntModelOnServer(OntConfig.getTBoxDatabaseUri())
                        && ServerOntologyModel.isOntModelOnServer(OntConfig.getOntDatabaseUri())) {
                    // tbox upload was successful
                    scheduledFutureTask.cancel(true);
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            }
        }, 0, OntConfig.BIG_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

}
