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
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.ActionStateType.ActionState;
import rst.domotic.state.ActivationStateType.ActivationState;
import rst.domotic.state.AlarmStateType.AlarmState;
import rst.domotic.state.BatteryStateType.BatteryState;
import rst.domotic.state.BlindStateType.BlindState;
import rst.domotic.state.BrightnessStateType.BrightnessState;
import rst.domotic.state.ButtonStateType.ButtonState;
import rst.domotic.state.ColorStateType.ColorState;
import rst.domotic.state.ContactStateType.ContactState;
import rst.domotic.state.DoorStateType.DoorState;
import rst.domotic.state.EnablingStateType.EnablingState;
import rst.domotic.state.HandleStateType.HandleState;
import rst.domotic.state.IlluminanceStateType.IlluminanceState;
import rst.domotic.state.IntensityStateType.IntensityState;
import rst.domotic.state.InventoryStateType.InventoryState;
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
import rst.domotic.state.UserActivityStateType.UserActivityState;
import rst.domotic.state.UserPresenceStateType.UserPresenceState;
import rst.domotic.state.WindowStateType.WindowState;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The class contains methods for the individual stateTypes. Each method gets the stateValues(s) of the stateType and converts the stateValues(s) to the
 * SPARQL codec in string form. Additionally each method decided, if the state value is an literal (category: continuous, {@code false})
 * or an instance (category: discrete, {@code true}).
 *
 * @author agatting on 22.02.17.
 */
public class StateTypeValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateTypeValue.class);

    /**
     * Method returns state values of the given actionState.
     *
     * @param actionState The ActionState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> actionStateValue(final ActionState actionState) {

        final Set<Pair<String, Boolean>> actionValuePairSet = new HashSet<>();
        actionValuePairSet.add(new Pair<>(actionState.getValue().toString(), false));

        return actionValuePairSet;
    }

    /**
     * Method returns state values of the given activationState.
     *
     * @param activationState The ActivationState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> activationStateValue(final ActivationState activationState) {

        final Set<Pair<String, Boolean>> activationValuePairSet = new HashSet<>();
        activationValuePairSet.add(new Pair<>(activationState.getValue().toString(), false));

        return activationValuePairSet;
    }

    /**
     * Method returns state values of the given alarmState.
     *
     * @param alarmState The AlarmState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> alarmStateValue(final AlarmState alarmState) {

        final Set<Pair<String, Boolean>> alarmValuePairSet = new HashSet<>();
        alarmValuePairSet.add(new Pair<>(alarmState.getValue().toString(), false));

        return alarmValuePairSet;
    }

    /**
     * Method returns state values of the given batteryState.
     *
     * @param batteryState The BatteryState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> batteryStateValue(final BatteryState batteryState) {

        final Set<Pair<String, Boolean>> batteryValuesPairSet = new HashSet<>();
        batteryValuesPairSet.add(new Pair<>(batteryState.getValue().toString(), false));
        batteryValuesPairSet.add(new Pair<>("\"" + String.valueOf(batteryState.getLevel()) + "\"^^NAMESPACE:Percent", true));

        return batteryValuesPairSet;
    }

    /**
     * Method returns state values of the given blindState.
     *
     * @param blindState The BlindState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> blindStateValue(final BlindState blindState) {

        final Set<Pair<String, Boolean>> blindValuePairSet = new HashSet<>();
        blindValuePairSet.add(new Pair<>(blindState.getMovementState().toString(), false));
        blindValuePairSet.add(new Pair<>("\"" + String.valueOf(blindState.getOpeningRatio()) + "\"^^NAMESPACE:Percent", true));

        return blindValuePairSet;
    }

    /**
     * Method returns state values of the given brightnessState.
     *
     * @param brightnessState The BrightnessState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> brightnessStateValue(final BrightnessState brightnessState) {

        final Set<Pair<String, Boolean>> brightnessValuePairSet = new HashSet<>();
        final BrightnessState.DataUnit dataUnit = brightnessState.getBrightnessDataUnit();

        switch (dataUnit) {
            case PERCENT:
                brightnessValuePairSet.add(new Pair<>("\"" + String.valueOf(brightnessState.getBrightness()) + "\"^^NAMESPACE:Percent", true));
                break;
            case UNKNOWN:
                LOGGER.warn("Dropped brightness state value, cause dataUnit is UNKNOWN.");
                break;
            default:
                LOGGER.warn("DataUnit of brightness state could not be detected. Please add " + dataUnit + " to ontologyManager implementation.");
        }

        return brightnessValuePairSet;
    }

    /**
     * Method returns state values of the given buttonState.
     *
     * @param buttonState The ButtonState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> buttonStateValue(final ButtonState buttonState) {

        final Set<Pair<String, Boolean>> buttonValuePairSet = new HashSet<>();
        buttonValuePairSet.add(new Pair<>(buttonState.getValue().toString(), false));

        return buttonValuePairSet;
    }

    /**
     * Method returns state values of the given colorState.
     *
     * @param colorState The ColorState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> colorStateValue(final ColorState colorState) {

        final Set<Pair<String, Boolean>> hsbValuesPairSet = new HashSet<>();

        if (colorState.getColor().hasHsbColor()) {
            final double brightness = colorState.getColor().getHsbColor().getBrightness();
            final double saturation = colorState.getColor().getHsbColor().getSaturation();
            final double hue = colorState.getColor().getHsbColor().getHue();

            hsbValuesPairSet.add(new Pair<>("\"" + hue + "\"^^NAMESPACE:Hue", true));
            hsbValuesPairSet.add(new Pair<>("\"" + saturation + "\"^^NAMESPACE:Saturation", true));
            hsbValuesPairSet.add(new Pair<>("\"" + brightness + "\"^^NAMESPACE:Brightness", true));

        } else if (colorState.getColor().hasRgbColor()) {
            final int red = colorState.getColor().getRgbColor().getRed();
            final int green = colorState.getColor().getRgbColor().getGreen();
            final int blue = colorState.getColor().getRgbColor().getBlue();

            float[] hsb = new float[3];
            hsb = Color.RGBtoHSB(red, green, blue, hsb);
            final double hue = hsb[0];
            final double saturation = hsb[1];
            final double brightness = hsb[2];

            hsbValuesPairSet.add(new Pair<>("\"" + hue + "\"^^NAMESPACE:Hue", true));
            hsbValuesPairSet.add(new Pair<>("\"" + saturation + "\"^^NAMESPACE:Saturation", true));
            hsbValuesPairSet.add(new Pair<>("\"" + brightness + "\"^^NAMESPACE:Brightness", true));

        } else {
            LOGGER.error("Could not set colorValue of colorState. Color is not set!");
        }

        return hsbValuesPairSet;
    }

    /**
     * Method returns state values of the given contactState.
     *
     * @param contactState The ContactState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> contactStateValue(final ContactState contactState) {

        final Set<Pair<String, Boolean>> contactValuePairSet = new HashSet<>();
        contactValuePairSet.add(new Pair<>(contactState.getValue().toString(), false));

        return contactValuePairSet;
    }

    /**
     * Method returns state values of the given doorState.
     *
     * @param doorState The DoorState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> doorStateValue(final DoorState doorState) {

        final Set<Pair<String, Boolean>> doorValuePairSet = new HashSet<>();
        doorValuePairSet.add(new Pair<>(doorState.getValue().toString(), false));

        return doorValuePairSet;
    }

    /**
     * Method returns state values of the given enablingState.
     *
     * @param enablingState The EnablingState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> enablingStateValue(final EnablingState enablingState) {

        final Set<Pair<String, Boolean>> enablingValuePairSet = new HashSet<>();
        enablingValuePairSet.add(new Pair<>(enablingState.getValue().toString(), false));

        return enablingValuePairSet;
    }

    /**
     * Method returns state values of the given handleState.
     *
     * @param handleState The HandleState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> handleStateValue(final HandleState handleState) {

        final Set<Pair<String, Boolean>> handleValuePairSet = new HashSet<>();
        handleValuePairSet.add(new Pair<>("\"" + String.valueOf(handleState.getPosition()) + "\"^^xsd:double", true));

        return handleValuePairSet;
    }

    /**
     * Method returns state values of the given illuminanceState.
     *
     * @param illuminanceState The IlluminanceState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> illuminanceStateValue(final IlluminanceState illuminanceState) {

        final Set<Pair<String, Boolean>> illuminanceValuePairSet = new HashSet<>();
        final IlluminanceState.DataUnit dataUnit = illuminanceState.getIlluminanceDataUnit();

        switch (dataUnit) {
            case LUX:
                illuminanceValuePairSet.add(new Pair<>("\"" + String.valueOf(illuminanceState.getIlluminance()) + "\"^^NAMESPACE:Lux", true));
                break;
            case UNKNOWN:
                LOGGER.warn("Dropped illuminance state value, cause dataUnit is UNKNOWN.");
                break;
            default:
                LOGGER.warn("DataUnit of intensity state could not be detected. Please add " + dataUnit + " to ontologyManager implementation.");
        }

        return illuminanceValuePairSet;
    }


    /**
     * Method returns state values of the given intensityState.
     *
     * @param intensityState The IntensityState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    @Deprecated
    protected Set<Pair<String, Boolean>> intensityStateValue(final IntensityState intensityState) {

        final Set<Pair<String, Boolean>> intensityValuePairSet = new HashSet<>();
        final IntensityState.DataUnit dataUnit = intensityState.getIntensityDataUnit();

        switch (dataUnit) {
            case PERCENT:
                intensityValuePairSet.add(new Pair<>("\"" + String.valueOf(intensityState.getIntensity()) + "\"^^NAMESPACE:Percent", true));
                break;
            case UNKNOWN:
                LOGGER.warn("Dropped intensity state value, cause dataUnit is UNKNOWN.");
                break;
            default:
                LOGGER.warn("DataUnit of intensity state could not be detected. Please add " + dataUnit + " to ontologyManager implementation.");
        }

        return intensityValuePairSet;
    }

    /**
     * Method returns state values of the given inventoryState.
     *
     * @param inventoryState The InventoryState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> inventoryStateValue(final InventoryState inventoryState) {
        //TODO no identification in ontology...

        final Set<Pair<String, Boolean>> inventoryValuePairSet = new HashSet<>();
        inventoryValuePairSet.add(new Pair<>(inventoryState.getValue().toString(), false));
//        inventoryValuePairSet.add(new Pair<>("\"" + inventoryState.getLocationId() + "\"^^xsd:string", true));
//        inventoryValuePairSet.add(new Pair<>("\"" + inventoryState.getOwnerId() + "\"^^xsd:string", true));

        return inventoryValuePairSet;
    }

    /**
     * Method returns state values of the given motionState.
     *
     * @param motionState The MotionState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> motionStateValue(final MotionState motionState) {

        final Set<Pair<String, Boolean>> motionValuePairSet = new HashSet<>();
        motionValuePairSet.add(new Pair<>(motionState.getValue().toString(), false));

        return motionValuePairSet;
    }

    /**
     * Method returns state values of the given passageState.
     *
     * @param passageState The PassageState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> passageStateValue(final PassageState passageState) {

        final Set<Pair<String, Boolean>> passageValuePairSet = new HashSet<>();
//        passageValuePairSet.add(new Pair<>(passageState..., false)); //TODO

        return passageValuePairSet;
    }

    /**
     * Method returns state values of the given powerConsumptionState.
     *
     * @param powerConsumptionState The PowerConsumptionState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> powerConsumptionStateValue(final PowerConsumptionState powerConsumptionState) {

        final Set<Pair<String, Boolean>> powerConsumptionValuePairSet = new HashSet<>();
        powerConsumptionValuePairSet.add(new Pair<>("\"" + String.valueOf(powerConsumptionState.getVoltage()) + "\"^^NAMESPACE:Voltage", true));
        powerConsumptionValuePairSet.add(new Pair<>("\"" + String.valueOf(powerConsumptionState.getConsumption()) + "\"^^NAMESPACE:Watt", true));
        powerConsumptionValuePairSet.add(new Pair<>("\"" + String.valueOf(powerConsumptionState.getCurrent()) + "\"^^NAMESPACE:Ampere", true));

        return powerConsumptionValuePairSet;
    }

    /**
     * Method returns state values of the given powerState.
     *
     * @param powerState The PowerState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> powerStateValue(final PowerState powerState) {

        final Set<Pair<String, Boolean>> powerValuePairSet = new HashSet<>();
        powerValuePairSet.add(new Pair<>(powerState.getValue().toString(), false));

        return powerValuePairSet;
    }

    /**
     * Method returns state values of the given presenceState.
     *
     * @param presenceState The PresenceState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> presenceStateValue(final PresenceState presenceState) {

        final Set<Pair<String, Boolean>> presenceValuePairSet = new HashSet<>();
        presenceValuePairSet.add(new Pair<>(presenceState.getValue().toString(), false));

        return presenceValuePairSet;
    }

    /**
     * Method returns state values of the given rfidState.
     *
     * @param rfidState The RFIDState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> rfidStateValue(final RFIDState rfidState) {

        final Set<Pair<String, Boolean>> rfidValuePairSet = new HashSet<>();
        rfidValuePairSet.add(new Pair<>("\"" + rfidState.getData().toString()  + "\"^^xsd:string", true));

        return rfidValuePairSet;
    }

    /**
     * Method returns state values of the given smokeState.
     *
     * @param smokeState The SmokeState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> smokeStateValue(final SmokeState smokeState) {

        final Set<Pair<String, Boolean>> smokeValuePairSet = new HashSet<>();
        smokeValuePairSet.add(new Pair<>(smokeState.getValue().toString(), false));
        smokeValuePairSet.add(new Pair<>("\"" + String.valueOf(smokeState.getSmokeLevel()) + "\"^^NAMESPACE:Percent", true));

        return smokeValuePairSet;
    }

    /**
     * Method returns state values of the given standbyState.
     *
     * @param standbyState The StandbyState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> standbyStateValue(final StandbyState standbyState) {

        final Set<Pair<String, Boolean>> standbyValuePairSet = new HashSet<>();
        standbyValuePairSet.add(new Pair<>(standbyState.getValue().toString(), false));

        return standbyValuePairSet;
    }

    /**
     * Method returns state values of the given switchState.
     *
     * @param switchState The SwitchState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> switchStateValue(final SwitchState switchState) {

        final Set<Pair<String, Boolean>> switchValuePairSet = new HashSet<>();
        switchValuePairSet.add(new Pair<>("\"" + String.valueOf(switchState.getPosition()) + "\"^^xsd:double", true));

        return switchValuePairSet;
    }

    /**
     * Method returns state values of the given tamperState.
     *
     * @param tamperState The TamperState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> tamperStateValue(final TamperState tamperState) {

        final Set<Pair<String, Boolean>> tamperValuePairSet = new HashSet<>();
        tamperValuePairSet.add(new Pair<>(tamperState.getValue().toString(), false));

        return tamperValuePairSet;
    }

    /**
     * Method returns state values of the given temperatureState.
     *
     * @param temperatureState The TemperatureState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> temperatureStateValue(final TemperatureState temperatureState) {

        final Set<Pair<String, Boolean>> temperatureValuePairSet = new HashSet<>();
        final TemperatureState.DataUnit dataUnit = temperatureState.getTemperatureDataUnit();
        double temperature;

        switch (dataUnit) {
            case CELSIUS:
                temperature = temperatureState.getTemperature();
                temperatureValuePairSet.add(new Pair<>("\"" + String.valueOf(temperature) + "\"^^NAMESPACE:Celsius", true));
                break;
            case FAHRENHEIT:
                temperature = ((temperatureState.getTemperature() - OntConfig.FREEZING_POINT_FAHRENHEIT) / 1.8);
                temperatureValuePairSet.add(new Pair<>("\"" + String.valueOf(temperature) + "\"^^NAMESPACE:Celsius", true));
                break;
            case KELVIN:
                temperature = temperatureState.getTemperature() - OntConfig.ABSOLUTE_ZERO_POINT_CELSIUS;
                temperatureValuePairSet.add(new Pair<>("\"" + String.valueOf(temperature) + "\"^^NAMESPACE:Celsius", true));
                break;
            case UNKNOWN:
                LOGGER.warn("Dropped temperature state value, cause dataUnit is UNKNOWN.");
                break;
            default:
                LOGGER.warn("DataUnit of temperature state could not be detected. Please add " + dataUnit + " to ontologyManager implementation.");
        }

        return temperatureValuePairSet;
    }

    /**
     * Method returns state values of the given userActivityState.
     *
     * @param userActivityState The UserActivityState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> userActivityStateValue(final UserActivityState userActivityState) {

        final Set<Pair<String, Boolean>> userActivityValuePairSet = new HashSet<>();
        userActivityValuePairSet.add(new Pair<>("\"" + userActivityState.getCurrentActivity().toString() + "\"^^NAMESPACE:CurrentActivity", true));
        userActivityValuePairSet.add(new Pair<>("\"" + userActivityState.getNextActivity().toString() + "\"^^NAMESPACE:NextActivity", true));

        return userActivityValuePairSet;
    }

    /**
     * Method returns state values of the given userPresenceState.
     *
     * @param userPresenceState The UserPresenceState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> userPresenceStateValue(final UserPresenceState userPresenceState) {

        final Set<Pair<String, Boolean>> userPresenceValuePairSet = new HashSet<>();
        userPresenceValuePairSet.add(new Pair<>(userPresenceState.getValue().toString(), false));

        return userPresenceValuePairSet;
    }

    /**
     * Method returns state values of the given windowState.
     *
     * @param windowState The WindowState.
     * @return PairSet of the state values. The pair contains the state value as string and if it is a literal ({@code false}) or no literal ({@code true}).
     * The size of the set describes the number of state values the individual state keeps.
     */
    protected Set<Pair<String, Boolean>> windowStateValue(final WindowState windowState) {

        final Set<Pair<String, Boolean>> windowValuePairSet = new HashSet<>();
        windowValuePairSet.add(new Pair<>(windowState.getValue().toString(), false));

        return windowValuePairSet;
    }
}
