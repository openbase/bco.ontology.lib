/**
 * ==================================================================
 * <p>
 * This file is part of org.openbase.bco.ontology.lib.
 * <p>
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 * <p>
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.manager.datapool;

import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.ontology.lib.OntologyManagerController;
import org.openbase.bco.ontology.lib.commun.monitor.HeartBeatCommunication;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.abox.observation.StateObservation;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author agatting on 07.02.17.
 */
public class UnitRemoteSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRemoteSynchronizer.class);
    private Future task;
    private Future taskRemaining;

    //TODO incomplete units + observer, diff

    public UnitRemoteSynchronizer() throws InstantiationException, InitializationException {

        final Observer<Boolean> activationObserver = (source, data) -> loadUnitRemotes(null); //TODO
        final Observer<List<UnitConfig>> newUnitConfigObserver = (source, unitConfigs) -> loadUnitRemotes(unitConfigs);

        OntologyManagerController.newUnitConfigObservable.addObserver(newUnitConfigObserver);
        HeartBeatCommunication.isInitObservable.addObserver(activationObserver);
    }

    /**
     * Method loads the unitRemotes from the given unitConfigs and starts the state observation.
     *
     * @param unitConfigs The unitConfigs. Can be set on {@code null} then all unitConfigs from the unitRegistry is taken. Otherwise the external unitConfigs
     *                    is taken.
     * @throws NotAvailableException Exception is thrown, if the threads are not available.
     */
    private void loadUnitRemotes(final List<UnitConfig> unitConfigs) throws NotAvailableException {

        task = GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                final Set<UnitConfig> missingUnitConfigs = new HashSet<>();
                final List<UnitConfig> unitConfigsBuf;

                if (unitConfigs == null) {
                    unitConfigsBuf = Units.getUnitRegistry().getUnitConfigs();
                } else {
                    unitConfigsBuf = unitConfigs;
                }

                for (final UnitConfig unitConfig : unitConfigsBuf) {

                    //filter device units
                    if(unitConfig.getType() == UnitType.DEVICE) {
                        continue;
                    }

                    if (unitConfig.getEnablingState().getValue() == State.ENABLED) {
                        UnitRemote unitRemote = null;
                        try {
                            unitRemote = Units.getFutureUnit(unitConfig, false).get(3, TimeUnit.SECONDS);
                            if (unitRemote.isDataAvailable()) {
                                // unitRemote is ready. add stateObservation
                                identifyUnitRemote(unitRemote);
                                LOGGER.info(unitRemote.getLabel() + " is loaded...state observation activated.");
                            } else {
                                missingUnitConfigs.add(unitConfig);
                            }
                        } catch (TimeoutException e) {
                            // collect unitRemotes with missing data (wait for data timeout)
                            missingUnitConfigs.add(unitConfig);
                        } catch (ExecutionException | NotAvailableException e) {
                            LOGGER.warn("Could not get unitRemote of " + unitConfig.getType());
                        }
                    }
                }
                if (missingUnitConfigs.isEmpty()) {
                    LOGGER.info("All unitRemotes loaded successfully.");
                } else {
                    processRemainingUnitRemotes(missingUnitConfigs);
                    LOGGER.info("There are " + missingUnitConfigs.size() + " unloaded unitRemotes... retry in " + OntConfig.BIG_RETRY_PERIOD_SECONDS + " seconds...");
                }
                task.cancel(true);
            } catch (CouldNotPerformException e) {
                // retry via scheduled thread
                ExceptionPrinter.printHistory("Could not get unitRegistry! Retry in " + OntConfig.BIG_RETRY_PERIOD_SECONDS + " seconds!", e, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException e) {
                task.cancel(true);
            }
        }, 0, OntConfig.BIG_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private void processRemainingUnitRemotes(final Set<UnitConfig> unitConfigs) throws NotAvailableException {

        taskRemaining = GlobalCachedExecutorService.submit(() -> {
            try {

                final Set<UnitConfig> missingUnitConfigs = new HashSet<>();
                final Stopwatch stopwatch = new Stopwatch();

                while (!unitConfigs.isEmpty()) {

                    for (final UnitConfig unitConfig : unitConfigs) {
                        UnitRemote unitRemote = null;
                        try {
                             unitRemote = Units.getFutureUnit(unitConfig, false).get(3, TimeUnit.SECONDS);

                            if (unitRemote.isDataAvailable()) {
                                // unitRemote is ready. add stateObservation
                                identifyUnitRemote(unitRemote);
                                LOGGER.info(unitRemote.getLabel() + " is loaded...state observation activated.");
                            } else {
                                missingUnitConfigs.add(unitConfig);
                            }
                        } catch (TimeoutException e) {
                            // collect unitRemotes with missing data (wait for data timeout)
                            missingUnitConfigs.add(unitConfig);
                        } catch (NotAvailableException | ExecutionException e) {
                            LOGGER.warn("Could not get unitRemote of " + unitConfig.getType());
                        } catch (InstantiationException e) {
                            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                        }
                    }

                    if (!missingUnitConfigs.isEmpty()) {
                        unitConfigs.removeAll(missingUnitConfigs);
                        missingUnitConfigs.clear();
                        LOGGER.info("There are " + unitConfigs.size() + " unloaded unitRemotes... retry in " + OntConfig.BIG_RETRY_PERIOD_SECONDS + " seconds...");

                        stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                    } else {
                        unitConfigs.clear();
                    }
                }
            } catch (InterruptedException e) {
                taskRemaining.cancel(true);
            }
        });

        LOGGER.info("All unitRemotes loaded successfully.");
    }

    private boolean identifyUnitRemote(final UnitRemote unitRemote) throws InstantiationException, NotAvailableException {

        final UnitType unitType = unitRemote.getType();

        // currently problematic unitTypes...fix in future
        switch (unitType) {
            case AUDIO_SINK:
//                new StateObservation(unitRemote, .class);
                return false;
            case AUDIO_SOURCE:
//                new StateObservation(unitRemote, .class);
                return false;
            case CONNECTION:
//                new StateObservation(unitRemote, ConnectionData.class);
                return false;
            case DEVICE:
//                new StateObservation(unitRemote, DeviceData.class);
                return false;
            case LOCATION:
//                new StateObservation(unitRemote, LocationData.class);
                return false;
        }

        new StateObservation(unitRemote, unitRemote.getDataClass());
        return true;
    }
}
