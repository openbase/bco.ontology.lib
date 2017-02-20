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
package org.openbase.bco.ontology.lib.datapool;

import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.StateObservation;
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.TransactionBuffer;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.dal.AudioSourceDataType.AudioSourceData;
import rst.domotic.unit.dal.BatteryDataType.BatteryData;
import rst.domotic.unit.dal.BrightnessSensorDataType.BrightnessSensorData;
import rst.domotic.unit.dal.ButtonDataType.ButtonData;
import rst.domotic.unit.dal.ColorableLightDataType.ColorableLightData;
import rst.domotic.unit.dal.DimmableLightDataType.DimmableLightData;
import rst.domotic.unit.dal.DimmerDataType.DimmerData;
import rst.domotic.unit.dal.DisplayDataType.DisplayData;
import rst.domotic.unit.dal.HandleDataType.HandleData;
import rst.domotic.unit.dal.LightDataType.LightData;
import rst.domotic.unit.dal.MonitorDataType.MonitorData;
import rst.domotic.unit.dal.MotionDetectorDataType.MotionDetectorData;
import rst.domotic.unit.dal.PowerConsumptionSensorDataType.PowerConsumptionSensorData;
import rst.domotic.unit.dal.PowerSwitchDataType.PowerSwitchData;
import rst.domotic.unit.dal.RFIDDataType.RFIDData;
import rst.domotic.unit.dal.ReedContactDataType.ReedContactData;
import rst.domotic.unit.dal.RollerShutterDataType.RollerShutterData;
import rst.domotic.unit.dal.SmokeDetectorDataType.SmokeDetectorData;
import rst.domotic.unit.dal.SwitchDataType.SwitchData;
import rst.domotic.unit.dal.TamperDetectorDataType.TamperDetectorData;
import rst.domotic.unit.dal.TelevisionDataType.TelevisionData;
import rst.domotic.unit.dal.TemperatureControllerDataType.TemperatureControllerData;
import rst.domotic.unit.dal.TemperatureSensorDataType.TemperatureSensorData;
import rst.domotic.unit.dal.VideoDepthSourceDataType.VideoDepthSourceData;
import rst.domotic.unit.dal.VideoRgbSourceDataType.VideoRgbSourceData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 07.02.17.
 */
public class UnitRemoteSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRemoteSynchronizer.class);
    private MultiException.ExceptionStack exceptionStack = null;
    private Future taskFuture;

    public UnitRemoteSynchronizer(final TransactionBuffer transactionBuffer) {

        // get classes via reflextion...
//        Reflections reflections = new Reflections("rst.domotic.unit.dal", new SubTypesScanner(false));
//        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        Set<UnitRemote> missingUnitRemoteData = new HashSet<>();

        try {
            taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

                try {
                    List<UnitConfig> unitConfigList = Units.getUnitRegistry().getUnitConfigs();

                    for (UnitConfig unitConfig : unitConfigList) {
                        if (unitConfig.getEnablingState().getValue() == State.ENABLED) {

                            try {
                                final UnitRemote unitRemote = Units.getUnit(unitConfig, false);
                                unitRemote.waitForData(500, TimeUnit.MILLISECONDS);

                                if (unitRemote.isDataAvailable()) {
                                    compareClassNameAndStartObs(unitRemote, unitConfig, transactionBuffer);
                                } else {
                                    // collect unitRemotes with missing data (wait for data timeout)
                                    missingUnitRemoteData.add(unitRemote);
                                }
                            } catch (InterruptedException | CouldNotPerformException e) {
                                // unitRemotes, which could not get(Unit)
                                exceptionStack = MultiException.push(this, e, exceptionStack);
                            }
                        }
                    }
                    taskFuture.cancel(true);

                    MultiException.checkAndThrow("Could not process all unitRemotes!", exceptionStack);
                } catch (CouldNotPerformException e) {
                    // print MultiException
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);

                } catch (InterruptedException e) {
                    // retry via scheduled thread
                    ExceptionPrinter.printHistory("Could not get unitRegistry! Retry in "
                            + ConfigureSystem.SMALL_RETRY_PERIOD + " seconds!", e, LOGGER, LogLevel.WARN);
                }
            }, 0, ConfigureSystem.SMALL_RETRY_PERIOD, TimeUnit.SECONDS);
        } catch (NotAvailableException e) {
            //TODO
        }

        if (!missingUnitRemoteData.isEmpty()) {
            exceptionStack.clear();

            try {
                taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

                    try {
                        for (final UnitRemote unitRemote : missingUnitRemoteData) {
                            unitRemote.waitForData(500, TimeUnit.MILLISECONDS);

                            if (unitRemote.isDataAvailable()) {
                                missingUnitRemoteData.remove(unitRemote);
                            }
                        }
                    } catch (CouldNotPerformException | InterruptedException e) {
                        exceptionStack = MultiException.push(this, e, exceptionStack);
                    }

                    try {
                        MultiException.checkAndThrow("Could not process all unitRemotes!", exceptionStack);
                    } catch (MultiException e) {
                        // print MultiException
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }

                    if (missingUnitRemoteData.isEmpty()) {
                        taskFuture.cancel(true);
                    } else {
                        LOGGER.warn("There are unitRemotes without data. Retry to solve in "
                                + ConfigureSystem.BIG_RETRY_PERIOD + " seconds.");
                    }

                }, 0, ConfigureSystem.BIG_RETRY_PERIOD, TimeUnit.SECONDS);
            } catch (NotAvailableException e) {
                //TODO
            }
        } else {
            LOGGER.info("All unitRemotes loaded successfully.");
        }
    }

    //TODO set generic dataClass?! Following static process not nice...
    private void compareClassNameAndStartObs(final UnitRemote unitRemote, final UnitConfig unitConfig
            , final TransactionBuffer transactionBuffer) {

        final String dataClassName = unitRemote.getDataClass().getSimpleName();

        if (dataClassName.equalsIgnoreCase("audioSourceData")) {
            new StateObservation<AudioSourceData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("batteryData")) {
            new StateObservation<BatteryData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("brightnessSensorData")) {
            new StateObservation<BrightnessSensorData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("buttonData")) {
            new StateObservation<ButtonData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("colorableLightData")) {
            new StateObservation<ColorableLightData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("dimmableLightData")) {
            new StateObservation<DimmableLightData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("dimmerData")) {
            new StateObservation<DimmerData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("displayData")) {
            new StateObservation<DisplayData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("handleData")) {
            new StateObservation<HandleData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("lightData")) {
            new StateObservation<LightData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("monitorData")) {
            new StateObservation<MonitorData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("motionDetectorData")) {
            new StateObservation<MotionDetectorData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("powerConsumptionSensorData")) {
            new StateObservation<PowerConsumptionSensorData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("powerSwitchData")) {
            new StateObservation<PowerSwitchData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("reedContactData")) {
            new StateObservation<ReedContactData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("rfidData")) {
            new StateObservation<RFIDData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("rollerShutterData")) {
            new StateObservation<RollerShutterData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("smokeDetectorData")) {
            new StateObservation<SmokeDetectorData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("switchData")) {
            new StateObservation<SwitchData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("tamperDetectorData")) {
            new StateObservation<TamperDetectorData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("televisionData")) {
            new StateObservation<TelevisionData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("temperatureControllerData")) {
            new StateObservation<TemperatureControllerData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("temperatureSensorData")) {
            new StateObservation<TemperatureSensorData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("videoDepthSourceData")) {
            new StateObservation<VideoDepthSourceData>(unitRemote, unitConfig, transactionBuffer);
        } else if (dataClassName.equalsIgnoreCase("videoRgbSourceData")) {
            new StateObservation<VideoRgbSourceData>(unitRemote, unitConfig, transactionBuffer);
        }
    }

}
