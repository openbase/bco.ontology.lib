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

import javafx.util.Pair;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.openbase.bco.dal.lib.layer.service.Service;
import org.openbase.bco.ontology.lib.manager.OntologyEditCommands;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.commun.web.ServerOntologyModel;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType.ServiceConfig;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author agatting on 20.02.17.
 */
public class TBoxSynchronizer {

    //TODO split tbox synch ontModel and sparql update (triples)

    private static final Logger LOGGER = LoggerFactory.getLogger(TBoxSynchronizer.class);
    private final Stopwatch stopwatch;

    public TBoxSynchronizer() {
        this.stopwatch = new Stopwatch();
    }

    public Pair<OntModel, List<TripleArrayList>> extendTBox(final List<UnitConfig> unitConfigList) throws InterruptedException {

        // get tbox from server. if no available: create new from dependency.
        final OntModel ontModel = getTBox();

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();
        Pair<OntModel, List<TripleArrayList>> ontModelTriplePair = new Pair<>(ontModel, tripleArrayLists);

        // get missing unitTypes
        ontModelTriplePair = compareUnitsWithOntClasses(unitConfigList, ontModelTriplePair);
        // get missing stateTypes
        return compareStatesWithOntology(unitConfigList, ontModelTriplePair);
    }

    public void uploadOntModel(final OntModel ontModel) throws InterruptedException, CouldNotPerformException {

        boolean isUploaded = false;

        while (!isUploaded) {
            try {
                ServerOntologyModel.addOntologyModel(ontModel, OntConfig.getOntDatabaseUri(), OntConfig.getTBoxDatabaseUri());
                isUploaded = true;
            } catch (IOException e) {
                //retry
                ExceptionPrinter.printHistory("No connection to upload ontModel to databases. Retry...", e, LOGGER, LogLevel.WARN);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
    }

    private Pair<OntModel, List<TripleArrayList>> compareUnitsWithOntClasses(final List<UnitConfig> unitConfigList
            , final Pair<OntModel, List<TripleArrayList>> ontModelTriplePair) {

        final OntClass unitOntClass = ontModelTriplePair.getKey().getOntClass(OntologyEditCommands.addNamespace(OntConfig.OntCl.UNIT.getName()));
        final Set<UnitType> missingUnitTypes = new HashSet<>();
        Set<OntClass> unitSubOntClasses = new HashSet<>();

        unitSubOntClasses = TBoxVerificationResource.listSubclassesOfOntSuperclass(unitSubOntClasses, unitOntClass, false);

        for (final UnitConfig unitConfig : unitConfigList) {
            if (!isUnitTypePresent(unitConfig, unitSubOntClasses)) {
                missingUnitTypes.add(unitConfig.getType());
            }
        }

        return addMissingOntUnits(missingUnitTypes, ontModelTriplePair);
    }

    private Pair<OntModel, List<TripleArrayList>> compareStatesWithOntology(final List<UnitConfig> unitConfigList
            , final Pair<OntModel, List<TripleArrayList>> ontModelTriplePair) {

        final OntClass stateOntClass = ontModelTriplePair.getKey().getOntClass(OntologyEditCommands.addNamespace(OntConfig.OntCl.STATE.getName()));
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
        return addMissingOntStates(missingServiceStateTypes, ontModelTriplePair);
    }

    private Pair<OntModel, List<TripleArrayList>> addMissingOntUnits(final Set<UnitType> missingUnitTypes
            , final Pair<OntModel, List<TripleArrayList>> ontModelTriplePair) {

        final String predicate = OntConfig.OntExpr.A.getName();
        final String dalObject = OntConfig.OntCl.DAL_UNIT.getName();
        final String baseObject = OntConfig.OntCl.HOST_UNIT.getName();
        final String hostObject = OntConfig.OntCl.BASE_UNIT.getName();

        final OntClass dalOntClass = ontModelTriplePair.getKey().getOntClass(OntologyEditCommands.addNamespace(dalObject));
        final OntClass baseOntClass = ontModelTriplePair.getKey().getOntClass(OntologyEditCommands.addNamespace(baseObject));
        final OntClass hostOntClass = ontModelTriplePair.getKey().getOntClass(OntologyEditCommands.addNamespace(hostObject));

        for (final UnitType unitType : missingUnitTypes) {

            final String missingUnitType = OntologyEditCommands.convertToNounAndAddNS(unitType.name());
            final OntClass newOntClassUnitType = ontModelTriplePair.getKey().createClass(missingUnitType);

            // find correct subclass of unit (e.g. baseUnit, DalUnit)
            if (UnitConfigProcessor.isDalUnit(unitType)) {
                ontModelTriplePair.getKey().getOntClass(dalOntClass.getURI()).addSubClass(newOntClassUnitType);
                ontModelTriplePair.getValue().add(new TripleArrayList(missingUnitType, predicate, dalObject));
            } else if (UnitConfigProcessor.isBaseUnit(unitType)) {
                if (UnitConfigProcessor.isHostUnit(unitType)) {
                    ontModelTriplePair.getKey().getOntClass(hostOntClass.getURI()).addSubClass(newOntClassUnitType);
                    ontModelTriplePair.getValue().add(new TripleArrayList(missingUnitType, predicate, hostObject));
                } else {
                    ontModelTriplePair.getKey().getOntClass(baseOntClass.getURI()).addSubClass(newOntClassUnitType);
                    ontModelTriplePair.getValue().add(new TripleArrayList(missingUnitType, predicate, baseObject));
                }
            }
        }

        return ontModelTriplePair;
    }

    private Pair<OntModel, List<TripleArrayList>> addMissingOntStates(final Set<String> missingStateTypes
            , final Pair<OntModel, List<TripleArrayList>> ontModelTriplePair) {

        final String predicate = OntConfig.OntExpr.A.getName();
        final String object = OntConfig.OntCl.STATE.getName();

        final OntClass stateOntClass = ontModelTriplePair.getKey().getOntClass(OntologyEditCommands.addNamespace(object));

        for (final String missingState : missingStateTypes) {

            final String missingStateType = OntologyEditCommands.convertToNounAndAddNS(missingState);
            final OntClass newOntClassStateType = ontModelTriplePair.getKey().createClass(missingStateType);

            ontModelTriplePair.getKey().getOntClass(stateOntClass.getURI()).addSubClass(newOntClassStateType);
            ontModelTriplePair.getValue().add(new TripleArrayList(missingStateType, predicate, object));
        }

        return ontModelTriplePair;
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
                ExceptionPrinter.printHistory("No connection to get tbox ontModel from server. Retry...", e, LOGGER, LogLevel.WARN);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
        return ontModel;
    }

}
