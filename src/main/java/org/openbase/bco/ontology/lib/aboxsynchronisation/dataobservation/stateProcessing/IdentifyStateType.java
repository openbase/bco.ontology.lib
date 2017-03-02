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
package org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.stateProcessing;

import javafx.util.Pair;
import org.openbase.bco.ontology.lib.config.BCOConfig.ServiceTypes;
import org.openbase.bco.ontology.lib.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.BatteryStateType.BatteryState;
import rst.domotic.state.BlindStateType.BlindState;
import rst.domotic.state.BrightnessStateType.BrightnessState;
import rst.domotic.state.ButtonStateType.ButtonState;
import rst.domotic.state.ColorStateType.ColorState;
import rst.domotic.state.ContactStateType.ContactState;
import rst.domotic.state.DoorStateType.DoorState;
import rst.domotic.state.HandleStateType.HandleState;
import rst.domotic.state.IntensityStateType.IntensityState;
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
public class IdentifyStateType extends ValueOfServiceType {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IdentifyStateType.class);

    // declaration of predicates and classes, which are static
    private static final String predicateIsA = OntExpr.A.getName();
    private static final String predicateHasStateValue = OntProp.STATE_VALUE.getName();
    private static final String stateValueClass = OntCl.STATE_VALUE.getName();

    protected List<TripleArrayList> addStateValue(final String serviceType, final Object stateObject, final String subjectObservation
            , final List<TripleArrayList> tripleArrayListsBuf) {

        final Set<Pair<String, Boolean>> stateTypeAndIsLiteral = identifyState(serviceType, stateObject);

        if (stateTypeAndIsLiteral != null) {
            for (final Pair<String, Boolean> pair : stateTypeAndIsLiteral) {
                // check if stateType is a literal or not. If yes = literal, if no = stateValue instance.
                if (!pair.getValue()) { // stateValue instance
                    tripleArrayListsBuf.add(new TripleArrayList(pair.getKey(), predicateIsA, stateValueClass)); //TODO: redundant. another possibility?
                }
                tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasStateValue, pair.getKey()));
            }
        }

        return tripleArrayListsBuf;
    }

    private Set<Pair<String, Boolean>> identifyState(final String serviceType, final Object stateObject) {

        switch (serviceType) {
            case ServiceTypes.ACTIVATION_STATE_SERVICE:
                return activationStateValue((ActivationState) stateObject);
            case ServiceTypes.BATTERY_STATE_SERVICE:
                return batteryStateValue((BatteryState) stateObject);
            case ServiceTypes.BLIND_STATE_SERVICE:
                return blindStateValue((BlindState) stateObject);
            case ServiceTypes.BRIGHTNESS_STATE_SERVICE:
                return brightnessStateValue((BrightnessState) stateObject);
            case ServiceTypes.BUTTON_STATE_SERVICE:
                return buttonStateValue((ButtonState) stateObject);
            case ServiceTypes.COLOR_STATE_SERVICE:
                return colorStateValue((ColorState) stateObject);
            case ServiceTypes.CONTACT_STATE_SERVICE:
                return contactStateValue((ContactState) stateObject);
            case ServiceTypes.DOOR_STATE_SERVICE:
                return doorStateValue((DoorState) stateObject);
            case ServiceTypes.HANDLE_STATE_SERVICE:
                return handleStateValue((HandleState) stateObject);
            case ServiceTypes.INTENSITY_STATE_SERVICE:
                return intensityStateValue((IntensityState) stateObject);
            case ServiceTypes.MOTION_STATE_SERVICE:
                return motionStateValue((MotionState) stateObject);
            case ServiceTypes.PASSAGE_STATE_SERVICE:
                return passageStateValue((PassageState) stateObject);
            case ServiceTypes.POWER_CONSUMPTION_STATE_SERVICE:
                return powerConsumptionStateValue((PowerConsumptionState) stateObject);
            case ServiceTypes.POWER_STATE_SERVICE:
                return powerStateValue((PowerState) stateObject);
            case ServiceTypes.PRESENCE_STATE_SERVICE:
                return presenceStateValue((PresenceState) stateObject);
            case ServiceTypes.RFID_STATE_SERVICE:
                return rfidStateValue((RFIDState) stateObject);
            case ServiceTypes.SMOKE_STATE_SERVICE:
                return smokeStateValue((SmokeState) stateObject);
            case ServiceTypes.STANDBY_STATE_SERVICE:
                return standbyStateValue((StandbyState) stateObject);
            case ServiceTypes.SWITCH_STATE_SERVICE:
                return switchStateValue((SwitchState) stateObject);
            case ServiceTypes.TAMPER_STATE_SERVICE:
                return tamperStateValue((TamperState) stateObject);
            case ServiceTypes.TEMPERATURE_STATE_SERVICE:
                return temperatureStateValue((TemperatureState) stateObject);
            case ServiceTypes.WINDOW_STATE_SERVICE:
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
