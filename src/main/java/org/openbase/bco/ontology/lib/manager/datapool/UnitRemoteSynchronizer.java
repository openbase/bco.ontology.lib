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
import org.openbase.bco.ontology.lib.commun.monitor.HeartBeatCommunication;
import org.openbase.bco.ontology.lib.system.config.BCOConfig.UnitDataClass;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.abox.observation.StateObservation;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
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
import rst.domotic.unit.dal.LightSensorDataType.LightSensorData;
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
import rst.domotic.unit.device.DeviceDataType.DeviceData;
import rst.domotic.unit.app.AppDataType.AppData;
import rst.domotic.unit.authorizationgroup.AuthorizationGroupDataType.AuthorizationGroupData;
import rst.domotic.unit.scene.SceneDataType.SceneData;
import rst.domotic.unit.unitgroup.UnitGroupDataType.UnitGroupData;
import rst.domotic.unit.user.UserDataType.UserData;
import rst.domotic.unit.agent.AgentDataType.AgentData;

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
    private Future task;
    private Future taskRemaining;

    private final TransactionBuffer transactionBuffer;
    private final RSBInformer<OntologyChange> rsbInformer;

    public UnitRemoteSynchronizer(final TransactionBuffer transactionBuffer, final RSBInformer<OntologyChange> rsbInformer)
            throws InstantiationException, InitializationException {

        this.transactionBuffer = transactionBuffer;
        this.rsbInformer = rsbInformer;

        final Observer<Boolean> activationObserver = (source, data) -> loadUnitRemotes(null);
        final Observer<List<UnitConfig>> newUnitConfigObserver = (source, unitConfigs) -> loadUnitRemotes(unitConfigs);

        UnitRegistrySynchronizer.newUnitConfigObservable.addObserver(newUnitConfigObserver);
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
                final Set<UnitRemote> missingUnitRemotes = new HashSet<>();
                final List<UnitConfig> unitConfigsBuf;

                if (unitConfigs == null) {
                    unitConfigsBuf = Units.getUnitRegistry().getUnitConfigs();
                } else {
                    unitConfigsBuf = unitConfigs;
                }

                for (final UnitConfig unitConfig : unitConfigsBuf) {
                    if (unitConfig.getEnablingState().getValue() == State.ENABLED) {

                        final UnitRemote unitRemote = Units.getUnit(unitConfig, false);
                        try {
                            unitRemote.waitForData(1, TimeUnit.SECONDS);

                            if (unitRemote.isDataAvailable()) {
                                // unitRemote is ready. add stateObservation
                                identifyUnitRemote(unitRemote);
                                LOGGER.info(unitRemote.getLabel() + " is loaded...state observation activated.");
                            } else {
                                missingUnitRemotes.add(unitRemote);
                            }
                        } catch (CouldNotPerformException e) {
                            // collect unitRemotes with missing data (wait for data timeout)
                            missingUnitRemotes.add(unitRemote);
                        }
                    }
                }
                if (missingUnitRemotes.isEmpty()) {
                    LOGGER.info("All unitRemotes loaded successfully.");
                } else {
                    processRemainingUnitRemotes(missingUnitRemotes);
                    LOGGER.info("There are " + missingUnitRemotes.size() + " unloaded unitRemotes... retry in " + OntConfig.BIG_RETRY_PERIOD_SECONDS + " seconds...");
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

    private void processRemainingUnitRemotes(final Set<UnitRemote> unitRemotes) throws NotAvailableException {

        taskRemaining = GlobalCachedExecutorService.submit(() -> {
            try {

                final Set<UnitRemote> missingUnitRemotes = new HashSet<>();
                final Stopwatch stopwatch = new Stopwatch();

                while (!unitRemotes.isEmpty()) {

                    for (final UnitRemote unitRemote : unitRemotes) {
                        try {
                            unitRemote.waitForData(2, TimeUnit.SECONDS);

                            if (unitRemote.isDataAvailable()) {
                                // unitRemote is ready. add stateObservation
                                identifyUnitRemote(unitRemote);
                                LOGGER.info(unitRemote.getLabel() + " is loaded...state observation activated.");
                            } else {
                                missingUnitRemotes.add(unitRemote);
                            }
                        } catch (CouldNotPerformException e) {
                            // collect unitRemotes with missing data (wait for data timeout)
                            missingUnitRemotes.add(unitRemote);
                        }
                    }

                    if (!missingUnitRemotes.isEmpty()) {
                        unitRemotes.removeAll(missingUnitRemotes);
                        missingUnitRemotes.clear();
                        LOGGER.info("There are " + unitRemotes.size() + " unloaded unitRemotes... retry in " + OntConfig.BIG_RETRY_PERIOD_SECONDS + " seconds...");

                        stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
                    } else {
                        unitRemotes.clear();
                    }
                }
            } catch (InterruptedException e) {
                taskRemaining.cancel(true);
            }
        });

        LOGGER.info("All unitRemotes loaded successfully.");
    }

    //TODO set generic dataClass?! Following static process not nice...
    private void identifyUnitRemote(final UnitRemote unitRemote) throws InstantiationException {

        final String dataClassName = unitRemote.getDataClass().getSimpleName().toLowerCase();

        switch (dataClassName) {
            case UnitDataClass.APP:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, AppData.class);
                break;
            case UnitDataClass.AGENT:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, AgentData.class);
                break;
            case UnitDataClass.AUDIO_SOURCE:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, AudioSourceData.class);
                break;
            case UnitDataClass.AUTHORIZATION_GROUP:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, AuthorizationGroupData.class);
                break;
            case UnitDataClass.BATTERY:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, BatteryData.class);
                break;
            case UnitDataClass.BRIGHTNESS_SENSOR:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, BrightnessSensorData.class);
                break;
            case UnitDataClass.BUTTON:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, ButtonData.class);
                break;
            case UnitDataClass.COLORABLE_LIGHT:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, ColorableLightData.class);
                break;
//            case UnitDataClass.CONNECTION:
//                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, ConnectionData.class);
//                break;
            case UnitDataClass.DEVICE:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, DeviceData.class);
                break;
            case UnitDataClass.DIMMABLE_LIGHT:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, DimmableLightData.class);
                break;
            case UnitDataClass.DIMMER:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, DimmerData.class);
                break;
            case UnitDataClass.DISPLAY:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, DisplayData.class);
                break;
            case UnitDataClass.HANDLE:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, HandleData.class);
                break;
            case UnitDataClass.LIGHT:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, LightData.class);
                break;
            case UnitDataClass.LIGHT_SENSOR:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, LightSensorData.class);
                break;
//            case UnitDataClass.LOCATION:
//                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, LocationData.class);
//                break;
            case UnitDataClass.MONITOR:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, MonitorData.class);
                break;
            case UnitDataClass.MOTION_DETECTOR:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, MotionDetectorData.class);
                break;
            case UnitDataClass.POWER_CONSUMPTION_SENSOR:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, PowerConsumptionSensorData.class);
                break;
            case UnitDataClass.POWER_SWITCH:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, PowerSwitchData.class);
                break;
            case UnitDataClass.REED_CONTACT:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, ReedContactData.class);
                break;
            case UnitDataClass.RFID:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, RFIDData.class);
                break;
            case UnitDataClass.ROLLER_SHUTTER:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, RollerShutterData.class);
                break;
            case UnitDataClass.SCENE:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, SceneData.class);
                break;
            case UnitDataClass.SMOKE_DETECTOR:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, SmokeDetectorData.class);
                break;
            case UnitDataClass.SWITCH:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, SwitchData.class);
                break;
            case UnitDataClass.TAMPER_DETECTOR:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, TamperDetectorData.class);
                break;
            case UnitDataClass.TELEVISION:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, TelevisionData.class);
                break;
            case UnitDataClass.TEMPERATURE_CONTROLLER:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, TemperatureControllerData.class);
                break;
            case UnitDataClass.TEMPERATURE_SENSOR:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, TemperatureSensorData.class);
                break;
            case UnitDataClass.UNIT_GROUP:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, UnitGroupData.class);
                break;
            case UnitDataClass.USER:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, UserData.class);
                break;
            case UnitDataClass.VIDEO_DEPTH_SOURCE:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, VideoDepthSourceData.class);
                break;
            case UnitDataClass.VIDEO_RGB_SOURCE:
                new StateObservation<>(unitRemote, transactionBuffer, rsbInformer, VideoRgbSourceData.class);
                break;
            default:
                if (UnitDataClass.CONNECTION.equalsIgnoreCase(dataClassName) || UnitDataClass.LOCATION.equalsIgnoreCase(dataClassName)) {
                    // ignore both to avoid exceptions...
                } else {
                    try {
                        throw new NotAvailableException("Could not identify className. Please check implementation or rather integrate " + dataClassName
                                + " to BCOConfig and call it.");
                    } catch (NotAvailableException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                    }
                }
        }
    }
}
