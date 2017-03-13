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
package org.openbase.bco.ontology.lib.system.config;

/**
 * @author agatting on 25.02.17.
 */
public class BCOConfig {

    /**
     * Enumeration for unit data classes.
     */
    public enum UnitDataClass {

        /**
         * Empty dummy object to group static string in an enum. Reason of readability.
         */
        UNKNOWN;

        /**
         * App unit data class.
         */
        public static final String APP = "appdata";

        /**
         * Agent unit data class.
         */
        public static final String AGENT = "agentdata";

        /**
         * AudioSink unit data class.
         */
        public static final String AUDIO_SINK = "audiosinkdata";

        /**
         * AudioSource unit data class.
         */
        public static final String AUDIO_SOURCE = "audiosourcedata";

        /**
         * AuthorizationGroup unit data class.
         */
        public static final String AUTHORIZATION_GROUP = "authorizationgroup";

        /**
         * Battery unit data class.
         */
        public static final String BATTERY = "batterydata";

        /**
         * BrightnessSensor unit data class.
         */
        public static final String BRIGHTNESS_SENSOR = "brightnesssensordata";

        /**
         * Button unit data class.
         */
        public static final String BUTTON = "buttondata";

        /**
         * ColorableLight unit data class.
         */
        public static final String COLORABLE_LIGHT = "colorablelightdata";

        /**
         * Connection unit data class.
         */
        public static final String CONNECTION = "connectiondata";

        /**
         * Device unit data class.
         */
        public static final String DEVICE = "devicedata";

        /**
         * DimmableLight unit data class.
         */
        public static final String DIMMABLE_LIGHT = "dimmablelightdata";

        /**
         * Dimmer unit data class.
         */
        public static final String DIMMER = "dimmerdata";

        /**
         * Display unit data class.
         */
        public static final String DISPLAY = "displaydata";

        /**
         * Handle unit data class.
         */
        public static final String HANDLE = "handledata";

        /**
         * Light unit data class.
         */
        public static final String LIGHT = "lightdata";

        /**
         * LightSensor unit data class.
         */
        public static final String LIGHT_SENSOR = "lightsensordata";

        /**
         * Location unit data class.
         */
        public static final String LOCATION = "locationdata";

        /**
         * Monitor unit data class.
         */
        public static final String MONITOR = "monitordata";

        /**
         * MotionDetector unit data class.
         */
        public static final String MOTION_DETECTOR = "motiondetectordata";

        /**
         * PowerConsumptionSensor unit data class.
         */
        public static final String POWER_CONSUMPTION_SENSOR = "powerconsumptionsensordata";

        /**
         * PowerSwitch unit data class.
         */
        public static final String POWER_SWITCH = "powerswitchdata";

        /**
         * ReedContact unit data class.
         */
        public static final String REED_CONTACT = "reedcontactdata";

        /**
         * RFID unit data class.
         */
        public static final String RFID = "rfiddata";

        /**
         * RollerShutter unit data class.
         */
        public static final String ROLLER_SHUTTER = "rollershutterdata";

        /**
         * Scene unit data class.
         */
        public static final String SCENE = "scenedata";

        /**
         * SmokeDetector unit data class.
         */
        public static final String SMOKE_DETECTOR = "smokedetectordata";

        /**
         * Switch unit data class.
         */
        public static final String SWITCH = "switchdata";

        /**
         * TamperDetector unit data class.
         */
        public static final String TAMPER_DETECTOR = "tamperdetectordata";

        /**
         * Television unit data class.
         */
        public static final String TELEVISION = "televisiondata";

        /**
         * TemperatureController unit data class.
         */
        public static final String TEMPERATURE_CONTROLLER = "temperaturecontrollerdata";

        /**
         * TemperatureSensor unit data class.
         */
        public static final String TEMPERATURE_SENSOR = "temperaturesensordata";

        /**
         * UnitGroup unit data class.
         */
        public static final String UNIT_GROUP = "unitgroupdata";

        /**
         * User unit data class.
         */
        public static final String USER = "userdata";

        /**
         * VideoDepthSource unit data class.
         */
        public static final String VIDEO_DEPTH_SOURCE = "videodepthsourcedata";

        /**
         * VideoRgbSource unit data class.
         */
        public static final String VIDEO_RGB_SOURCE = "videorgbsourcedata";
    }

    public enum ServiceTypes {

        /**
         * Empty dummy object to group static string in an enum. Reason of readability.
         */
        UNKNOWN;
        /**
         * Activation service type.
         */
        public static final String ACTIVATION_STATE_SERVICE = "activationstateservice";

        /**
         * Battery service type.
         */
        public static final String BATTERY_STATE_SERVICE = "batterystateservice";

        /**
         * Blind Service.
         */
        public static final String BLIND_STATE_SERVICE = "blindstateservice";

        /**
         * Brightness service.
         */
        public static final String BRIGHTNESS_STATE_SERVICE = "brightnessstateservice";

        /**
         * Button service.
         */
        public static final String BUTTON_STATE_SERVICE = "buttonstateservice";

        /**
         * Color service.
         */
        public static final String COLOR_STATE_SERVICE = "colorstateservice";

        /**
         * Contact service.
         */
        public static final String CONTACT_STATE_SERVICE = "contactstateservice";

        /**
         * Door state service.
         */
        public static final String DOOR_STATE_SERVICE = "doorstateservice";

        /**
         * Earthquake alarm state service.
         */
        public static final String EARTHQUAKE_ALARM_STATE_SERVICE = "earthquakealarmstateservice";

        /**
         * Fire alarm state service.
         */
        public static final String FIRE_ALARM_STATE_SERVICE = "firealarmstateservice";

        /**
         * Handle service.
         */
        public static final String HANDLE_STATE_SERVICE = "handlestateservice";

        /**
         * Illuminance service.
         */
        public static final String ILLUMINANCE_STATE_SERVICE = "illuminancestateservice";

        /**
         * Intrusion alarm state service.
         */
        public static final String INTRUSION_ALARM_STATE_SERVICE = "intrusionalarmstateservice";

        /**
         * Intensity service.
         */
        public static final String INTENSITY_STATE_SERVICE = "intensitystateservice";

        /**
         * Medial emergency alarm state service.
         */
        public static final String MEDICAL_EMERGENCY_ALARM_STATE_SERVICE = "medicalemergencyalarmstateservice";

        /**
         * Motion service.
         */
        public static final String MOTION_STATE_SERVICE = "motionstateservice";

        /**
         * Passage state service.
         */
        public static final String PASSAGE_STATE_SERVICE = "passagestateservice";

        /**
         * Power-Consumption service.
         */
        public static final String POWER_CONSUMPTION_STATE_SERVICE = "powerconsumptionstateservice";

        /**
         * Power service.
         */
        public static final String POWER_STATE_SERVICE = "powerstateservice";

        /**
         * Presence state e.g. used for locations to give feedback about human presence.
         */
        public static final String PRESENCE_STATE_SERVICE = "presencestateservice";

        /**
         * Service used by RFIDs.
         */
        public static final String RFID_STATE_SERVICE = "rfidstateservice";

        /**
         * Smoke Alarm state service.
         */
        public static final String SMOKE_ALARM_STATE_SERVICE = "smokealarmstateservice";

        /**
         * Smoke state service.
         */
        public static final String SMOKE_STATE_SERVICE = "smokestateservice";

        /**
         * Standby service.
         */
        public static final String STANDBY_STATE_SERVICE = "standbystateservice";

        /**
         * Tamper service.
         */
        public static final String TAMPER_STATE_SERVICE = "tamperstateservice";

        /**
         * Target temperature service.
         */
        public static final String TARGET_TEMPERATURE_STATE_SERVICE = "targettemperaturestateservice";

        /**
         * Temperature alarm state service.
         */
        public static final String TEMPERATURE_ALARM_STATE_SERVICE = "temperaturealarmstateservice";

        /**
         * Temperature service.
         */
        public static final String TEMPERATURE_STATE_SERVICE = "temperaturestateservice";

        /**
         * Tempest alarm state service.
         */
        public static final String TEMPEST_ALARM_STATE_SERVICE = "tempestalarmstateservice";

        /**
         * Switch service.
         */
        public static final String SWITCH_STATE_SERVICE = "switchstateservice";

        /**
         * Water alarm state service.
         */
        public static final String WATER_ALARM_STATE_SERVICE = "wateralarmstateservice";

        /**
         * Window state service.
         */
        public static final String WINDOW_STATE_SERVICE = "windowstateservice";
    }
}
