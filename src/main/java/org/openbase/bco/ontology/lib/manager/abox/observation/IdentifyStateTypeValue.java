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
package org.openbase.bco.ontology.lib.manager.abox.observation;

import javafx.util.Pair;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.utility.RdfTriple;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.BatteryStateType.BatteryState;
import rst.domotic.state.BlindStateType.BlindState;
import rst.domotic.state.ButtonStateType.ButtonState;
import rst.domotic.state.ColorStateType.ColorState;
import rst.domotic.state.ContactStateType.ContactState;
import rst.domotic.state.DoorStateType.DoorState;
import rst.domotic.state.HandleStateType.HandleState;
import rst.domotic.state.IlluminanceStateType.IlluminanceState;
import rst.domotic.state.MotionStateType.MotionState;
import rst.domotic.state.PassageStateType.PassageState;
import rst.domotic.state.PowerConsumptionStateType.PowerConsumptionState;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.state.PresenceStateType.PresenceState;
import rst.domotic.state.RFIDStateType.RFIDState;
import rst.domotic.state.SmokeStateType.SmokeState;
import rst.domotic.state.StandbyStateType.StandbyState;
import rst.domotic.state.SwitchStateType.SwitchState;
import rst.domotic.state.TamperStateType.TamperState;
import rst.domotic.state.TemperatureStateType.TemperatureState;
import rst.domotic.state.WindowStateType.WindowState;

import java.util.List;
import java.util.Set;

/**
 * @author agatting on 22.02.17.
 */
public class IdentifyStateTypeValue extends StateTypeValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentifyStateTypeValue.class);

    // declaration of predicates and classes, which are static
    private static final String pred_IsA = OntExpr.A.getName();
    private static final String pred_HasStateValue = OntProp.STATE_VALUE.getName();
    private static final String obj_stateValue = OntCl.STATE_VALUE.getName();

    protected List<RdfTriple> addStateValue(final ServiceType serviceType, final Object stateObject, final String subjectObservation
            , final List<RdfTriple> rdfTripleArrayListsBuf) {

        final Set<Pair<String, Boolean>> stateTypeAndIsLiteral = identifyState(serviceType, stateObject);

        if (stateTypeAndIsLiteral != null) {
            for (final Pair<String, Boolean> pair : stateTypeAndIsLiteral) {
                // check if stateType is a literal or not. If yes = literal, if no = stateValue instance.
                if (!pair.getValue()) { // stateValue instance
//                    System.out.println(Service.getServiceStateValues(ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE));
                    rdfTripleArrayListsBuf.add(new RdfTriple(pair.getKey(), pred_IsA, obj_stateValue)); //TODO: redundant. another possibility?
                }
                rdfTripleArrayListsBuf.add(new RdfTriple(subjectObservation, pred_HasStateValue, pair.getKey()));
            }
        }

        return rdfTripleArrayListsBuf;
    }

    private Set<Pair<String, Boolean>> identifyState(final ServiceType serviceType, final Object stateObject) {

        switch (serviceType) {
            case UNKNOWN:
                LOGGER.warn("There is a serviceType UNKNOWN!");
                return null;
            case ACTIVATION_STATE_SERVICE:
                return activationStateValue((ActivationState) stateObject);
            case BATTERY_STATE_SERVICE:
                return batteryStateValue((BatteryState) stateObject);
            case BLIND_STATE_SERVICE:
                return blindStateValue((BlindState) stateObject);
            case BRIGHTNESS_STATE_SERVICE:
//                return brightnessStateValue((BrightnessState) stateObject);
                return null;
            case BUTTON_STATE_SERVICE:
                return buttonStateValue((ButtonState) stateObject);
            case COLOR_STATE_SERVICE:
                return colorStateValue((ColorState) stateObject);
            case CONTACT_STATE_SERVICE:
                return contactStateValue((ContactState) stateObject);
            case DOOR_STATE_SERVICE:
                return doorStateValue((DoorState) stateObject);
            case EARTHQUAKE_ALARM_STATE_SERVICE:
                return null;
            case FIRE_ALARM_STATE_SERVICE:
                return null;
            case HANDLE_STATE_SERVICE:
                return handleStateValue((HandleState) stateObject);
            case ILLUMINANCE_STATE_SERVICE:
                return illuminanceStateValue((IlluminanceState) stateObject);
            case INTENSITY_STATE_SERVICE:
                return null;
            case INTRUSION_ALARM_STATE_SERVICE:
                return null;
            case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:
                return null;
            case MOTION_STATE_SERVICE:
                return motionStateValue((MotionState) stateObject);
            case PASSAGE_STATE_SERVICE:
                return passageStateValue((PassageState) stateObject);
            case POWER_CONSUMPTION_STATE_SERVICE:
                return powerConsumptionStateValue((PowerConsumptionState) stateObject);
            case POWER_STATE_SERVICE:
                return powerStateValue((PowerState) stateObject);
            case PRESENCE_STATE_SERVICE:
                return presenceStateValue((PresenceState) stateObject);
            case RFID_STATE_SERVICE:
                return rfidStateValue((RFIDState) stateObject);
            case SMOKE_ALARM_STATE_SERVICE:
                return null;
            case SMOKE_STATE_SERVICE:
                return smokeStateValue((SmokeState) stateObject);
            case STANDBY_STATE_SERVICE:
                return standbyStateValue((StandbyState) stateObject);
            case SWITCH_STATE_SERVICE:
                return switchStateValue((SwitchState) stateObject);
            case TAMPER_STATE_SERVICE:
                return tamperStateValue((TamperState) stateObject);
            case TARGET_TEMPERATURE_STATE_SERVICE:
                return null;
            case TEMPERATURE_ALARM_STATE_SERVICE:
                return null;
            case TEMPERATURE_STATE_SERVICE:
                return temperatureStateValue((TemperatureState) stateObject);
            case TEMPEST_ALARM_STATE_SERVICE:
                return null;
            case WATER_ALARM_STATE_SERVICE:
                return null;
            case WINDOW_STATE_SERVICE:
                return windowStateValue((WindowState) stateObject);
            default:
                // no matched stateService
                try {
                    throw new NotAvailableException("Could not identify stateType. Please check implementation or rather integrate " + serviceType
                            + " to method identifyState.");
                } catch (NotAvailableException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                }
                return null;
        }
    }
}
