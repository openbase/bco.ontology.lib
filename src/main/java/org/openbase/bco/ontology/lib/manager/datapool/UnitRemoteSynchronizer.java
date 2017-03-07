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

import javafx.util.Pair;
import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.ontology.lib.commun.rsb.RsbCommunication;
import org.openbase.bco.ontology.lib.system.config.BCOConfig.UnitDataClass;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.abox.observation.StateObservation;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.system.jp.JPRsbScope;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 07.02.17.
 */
public class UnitRemoteSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRemoteSynchronizer.class);
    private ScheduledFuture scheduledFutureTask;
    private ScheduledFuture scheduledFutureTaskRemainingUnitRemotes;

    public UnitRemoteSynchronizer(final TransactionBuffer transactionBuffer, final RSBInformer<OntologyChange> rsbInformer)
            throws InstantiationException, InitializationException {

            // get classes via reflextion...
//        Reflections reflections = new Reflections("rst.domotic.unit.dal", new SubTypesScanner(false));
//        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        try {
            getAndMapUnitRemotesWithStateObservation(transactionBuffer, rsbInformer);
        } catch (CouldNotPerformException e) {
            throw new InstantiationException(this, e);
        }
    }

    private void getAndMapUnitRemotesWithStateObservation(final TransactionBuffer transactionBuffer, final RSBInformer<OntologyChange> rsbInformer)
            throws NotAvailableException {

        scheduledFutureTask = GlobalScheduledExecutorService.scheduleWithFixedDelay(() -> {

            try {
                final List<UnitConfig> unitConfigList = Units.getUnitRegistry().getUnitConfigs();
                final Set<Pair<UnitRemote, UnitConfig>> unitPairSet = getAndActivateUnitRemotes(unitConfigList, transactionBuffer, rsbInformer);

                if (!unitPairSet.isEmpty()) {
                    processOfRemainingUnitRemotes(unitPairSet, transactionBuffer, rsbInformer);
                } else {
                    LOGGER.info("All unitRemotes loaded successfully.");
                }

                scheduledFutureTask.cancel(true);

            } catch (NotAvailableException e) {
                //TODO
            } catch (CouldNotPerformException e) {
                // retry via scheduled thread
                ExceptionPrinter.printHistory("Could not get unitRegistry! Retry in "
                        + OntConfig.BIG_RETRY_PERIOD_SECONDS + " seconds!", e, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException e) {
                //TODO
            }
        }, 0, OntConfig.BIG_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    private Set<Pair<UnitRemote, UnitConfig>> getAndActivateUnitRemotes(final List<UnitConfig> unitConfigList, final TransactionBuffer transactionBuffer
            , final RSBInformer<OntologyChange> rsbInformer) throws InterruptedException {

        MultiException.ExceptionStack exceptionStack = null;
        final Set<Pair<UnitRemote, UnitConfig>> unitPairSet = new HashSet<>();

        for (final UnitConfig unitConfig : unitConfigList) {
            if (unitConfig.getEnablingState().getValue() == State.ENABLED) {

                UnitRemote unitRemote = null;
                try {
                    unitRemote = Units.getUnit(unitConfig, false);
                } catch (NotAvailableException e) {
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                }

                if (unitRemote != null) {
                    try {
                        unitRemote.waitForData(1, TimeUnit.SECONDS);

                        if (unitRemote.isDataAvailable()) {
                            // unitRemote is ready. add stateObservation
                            identifyUnitRemote(unitRemote, unitConfig, transactionBuffer, rsbInformer);
                        } else {
                            unitPairSet.add(new Pair<>(unitRemote, unitConfig));
                        }
                    } catch (CouldNotPerformException e) {
                        // collect unitRemotes with missing data (wait for data timeout)
                        unitPairSet.add(new Pair<>(unitRemote, unitConfig));
                    }
                }
            }
        }

        try {
            MultiException.checkAndThrow("Could not process all unitRemotes!", exceptionStack);
        } catch (MultiException e) {
            LOGGER.warn("There are " + (exceptionStack != null ? exceptionStack.size() : 0)
                    + " unitRemotes without data. Retry to solve in " + OntConfig.BIG_RETRY_PERIOD_SECONDS + " seconds.");
        }
        // return a set of unitRemotes, which have no data yet
        return unitPairSet;
    }

    private void processOfRemainingUnitRemotes(Set<Pair<UnitRemote, UnitConfig>> unitPairSet, final TransactionBuffer transactionBuffer
            , final RSBInformer<OntologyChange> rsbInformer) throws NotAvailableException {

        Set<Pair<UnitRemote, UnitConfig>> unitPairSetBuf = new HashSet<>();

        scheduledFutureTaskRemainingUnitRemotes = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {
            MultiException.ExceptionStack exceptionStack = null;

            for (final Pair<UnitRemote, UnitConfig> unitPair : unitPairSet) {
                try {
                    unitPair.getKey().waitForData(1, TimeUnit.SECONDS);

                    if (unitPair.getKey().isDataAvailable()) {
                        // unitRemote is ready. add stateObservation
                        identifyUnitRemote(unitPair.getKey(), unitPair.getValue(), transactionBuffer, rsbInformer);
                    } else {
                        unitPairSetBuf.add(unitPair);
                    }
                } catch (CouldNotPerformException e) {
                    // add to set and stack exception
                    unitPairSetBuf.add(unitPair);
                    exceptionStack = MultiException.push(this, e, exceptionStack);
                } catch (InterruptedException e) {
                    //TODO
                }
            }

            try {
                MultiException.checkAndThrow("Could not process all unitRemotes!", exceptionStack);
            } catch (MultiException e) {
                LOGGER.warn("There are " + (exceptionStack != null ? exceptionStack.size() : 0)
                        + " unitRemotes without data. Retry to solve in " + OntConfig.BIG_RETRY_PERIOD_SECONDS + " seconds.");
            }

            if (unitPairSetBuf.isEmpty()) {
                scheduledFutureTaskRemainingUnitRemotes.cancel(true);
                LOGGER.info("All unitRemotes loaded successfully.");
            } else {
                unitPairSet.clear();
                unitPairSet.addAll(unitPairSetBuf);
                unitPairSetBuf.clear();
            }
        }, OntConfig.BIG_RETRY_PERIOD_SECONDS, OntConfig.BIG_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    //TODO set generic dataClass?! Following static process not nice...
    private void identifyUnitRemote(final UnitRemote unitRemote, final UnitConfig unitConfig, final TransactionBuffer transactionBuffer
            , final RSBInformer<OntologyChange> rsbInformer) throws NotAvailableException {

        final String dataClassName = unitRemote.getDataClass().getSimpleName().toLowerCase();

        switch (dataClassName) {
            case UnitDataClass.APP:
                new StateObservation<AppData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.AGENT:
                new StateObservation<AgentData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.AUDIO_SOURCE:
                new StateObservation<AudioSourceData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.AUTHORIZATION_GROUP:
                new StateObservation<AuthorizationGroupData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.BATTERY:
                new StateObservation<BatteryData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.BRIGHTNESS_SENSOR:
                new StateObservation<BrightnessSensorData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.BUTTON:
                new StateObservation<ButtonData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.COLORABLE_LIGHT:
                new StateObservation<ColorableLightData>(unitRemote, transactionBuffer, rsbInformer);
                break;
//            case UnitDataClass.CONNECTION:
//                new StateObservation<ConnectionData>(unitRemote, transactionBuffer, rsbInformer);
//                break;
            case UnitDataClass.DEVICE:
                new StateObservation<DeviceData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.DIMMABLE_LIGHT:
                new StateObservation<DimmableLightData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.DIMMER:
                new StateObservation<DimmerData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.DISPLAY:
                new StateObservation<DisplayData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.HANDLE:
                new StateObservation<HandleData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.LIGHT:
                new StateObservation<LightData>(unitRemote, transactionBuffer, rsbInformer);
                break;
//            case UnitDataClass.LOCATION:
//                new StateObservation<LocationData>(unitRemote, transactionBuffer, rsbInformer);
//                break;
            case UnitDataClass.MONITOR:
                new StateObservation<MonitorData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.MOTION_DETECTOR:
                new StateObservation<MotionDetectorData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.POWER_CONSUMPTION_SENSOR:
                new StateObservation<PowerConsumptionSensorData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.POWER_SWITCH:
                new StateObservation<PowerSwitchData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.REED_CONTACT:
                new StateObservation<ReedContactData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.RFID:
                new StateObservation<RFIDData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.ROLLER_SHUTTER:
                new StateObservation<RollerShutterData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.SCENE:
                new StateObservation<SceneData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.SMOKE_DETECTOR:
                new StateObservation<SmokeDetectorData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.SWITCH:
                new StateObservation<SwitchData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.TAMPER_DETECTOR:
                new StateObservation<TamperDetectorData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.TELEVISION:
                new StateObservation<TelevisionData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.TEMPERATURE_CONTROLLER:
                new StateObservation<TemperatureControllerData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.TEMPERATURE_SENSOR:
                new StateObservation<TemperatureSensorData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.UNIT_GROUP:
                new StateObservation<UnitGroupData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.USER:
                new StateObservation<UserData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.VIDEO_DEPTH_SOURCE:
                new StateObservation<VideoDepthSourceData>(unitRemote, transactionBuffer, rsbInformer);
                break;
            case UnitDataClass.VIDEO_RGB_SOURCE:
                new StateObservation<VideoRgbSourceData>(unitRemote, transactionBuffer, rsbInformer);
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
