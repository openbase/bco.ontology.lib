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
package org.openbase.bco.ontology.lib.config;

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
        DUMMY;
        public static final String AUDIO_SOURCE = "audiosourcedata";
        public static final String BATTERY = "batterydata";
        public static final String BRIGHTNESS_SENSOR = "brightnesssensordata";
        public static final String BUTTON = "buttondata";
        public static final String COLORABLE_LIGHT = "colorablelightdata";
        public static final String DIMMABLE_LIGHT = "dimmablelightdata";
        public static final String DIMMER = "dimmerdata";
        public static final String DISPLAY = "displaydata";
        public static final String HANDLE = "handledata";
        public static final String LIGHT = "lightdata";
        public static final String MONITOR = "monitordata";
        public static final String MOTION_DETECTOR = "motiondetectordata";
        public static final String POWER_CONSUMPTION_SENSOR = "powerconsumptionsensordata";
        public static final String POWER_SWITCH = "powerswitchdata";
        public static final String REED_CONTACT = "reedcontactdata";
        public static final String RFID = "rfiddata";
        public static final String ROLLER_SHUTTER = "rollershutterdata";
        public static final String SMOKE_DETECTOR = "smokedetectordata";
        public static final String SWITCH = "switchdata";
        public static final String TAMPER_DETECTOR = "tamperdetectordata";
        public static final String TELEVISION = "televisiondata";
        public static final String TEMPERATURE_CONTROLLER = "temperaturecontrollerdata";
        public static final String TEMPERATURE_SENSOR = "temperaturesensordata";
        public static final String VIDEO_DEPTH_SOURCE = "videodepthsourcedata";
        public static final String VIDEO_RGB_SOURCE = "videorgbsourcedata";
    }

    public enum ServiceTypes {

        /**
         * Empty dummy object to group static string in an enum. Reason of readability.
         */
        Dummy;
        /**
         * Activation service type.
         */
        public static final String ACTIVATION_STATE_SERVICE = "ACTIVATION_STATE_SERVICE";

        /**
         * Battery service type..
         */
        public static final String BATTERY_STATE_SERVICE = "BATTERY_STATE_SERVICE";

        /**
         * Brightness service.
         */
        public static final String BRIGHTNESS_STATE_SERVICE = "BRIGHTNESS_STATE_SERVICE";

        /**
         * Intensity service.
         */
        public static final String INTENSITY_STATE_SERVICE = "INTENSITY_STATE_SERVICE";

        /**
         * Button service.
         */
        public static final String BUTTON_STATE_SERVICE = "BUTTON_STATE_SERVICE";

        /**
         * Switch service.
         */
        public static final String SWITCH_STATE_SERVICE = "SWITCH_STATE_SERVICE";

        /**
         * Color service.
         */
        public static final String COLOR_STATE_SERVICE = "COLOR_STATE_SERVICE";

        /**
         * Handle service.
         */
        public static final String HANDLE_STATE_SERVICE = "HANDLE_STATE_SERVICE";

        /**
         * Motion service.
         */
        public static final String MOTION_STATE_SERVICE = "MOTION_STATE_SERVICE";

        /**
         * Power-Comsunption service.
         */
        public static final String POWER_CONSUMPTION_STATE_SERVICE = "POWER_CONSUMPTION_STATE_SERVICE";

        /**
         * Power service.
         */
        public static final String POWER_STATE_SERVICE = "POWER_STATE_SERVICE";

        /**
         * Contanct service.
         */
        public static final String CONTACT_STATE_SERVICE = "CONTACT_STATE_SERVICE";

        /**
         * Blind Service.
         */
        public static final String BLIND_STATE_SERVICE = "BLIND_STATE_SERVICE";

        /**
         * Tamper service.
         */
        public static final String TAMPER_STATE_SERVICE = "TAMPER_STATE_SERVICE";

        /**
         * Target temperature service.
         */
        public static final String TARGET_TEMPERATURE_STATE_SERVICE = "TARGET_TEMPERATURE_STATE_SERVICE";

        /**
         * Temperature service.
         */
        public static final String TEMPERATURE_STATE_SERVICE = "TEMPERATURE_STATE_SERVICE";

        /**
         * Standby service.
         */
        public static final String STANDBY_STATE_SERVICE = "STANDBY_STATE_SERVICE";

        /**
         * Smoke Alarm state service.
         */
        public static final String SMOKE_ALARM_STATE_SERVICE = "SMOKE_ALARM_STATE_SERVICE";

        /**
         * Temperature alarm state service.
         */
        public static final String TEMPERATURE_ALARM_STATE_SERVICE = "TEMPERATURE_ALARM_STATE_SERVICE";

        /**
         * Smoke state service.
         */
        public static final String SMOKE_STATE_SERVICE = "SMOKE_STATE_SERVICE";

        /**
         * Water alarm state service.
         */
        public static final String WATER_ALARM_STATE_SERVICE = "WATER_ALARM_STATE_SERVICE";

        /**
         * Fire alarm state service.
         */
        public static final String FIRE_ALARM_STATE_SERVICE = "FIRE_ALARM_STATE_SERVICE";

        /**
         * Tempest alarm state service.
         */
        public static final String TEMPEST_ALARM_STATE_SERVICE = "TEMPEST_ALARM_STATE_SERVICE";

        /**
         * Earthquake alarm state service.
         */
        public static final String EARTHQUAKE_ALARM_STATE_SERVICE = "EARTHQUAKE_ALARM_STATE_SERVICE";

        /**
         * Intrusion alarm state service.
         */
        public static final String INTRUSION_ALARM_STATE_SERVICE = "INTRUSION_ALARM_STATE_SERVICE";

        /**
         * Medial emergency alarm state service.
         */
        public static final String MEDICAL_EMERGENCY_ALARM_STATE_SERVICE = "MEDICAL_EMERGENCY_ALARM_STATE_SERVICE";

        /**
         * Door state service.
         */
        public static final String DOOR_STATE_SERVICE = "DOOR_STATE_SERVICE";

        /**
         * Window state service.
         */
        public static final String WINDOW_STATE_SERVICE = "WINDOW_STATE_SERVICE";

        /**
         * Passage state service.
         */
        public static final String PASSAGE_STATE_SERVICE = "PASSAGE_STATE_SERVICE";

        /**
         * Service used by RFIDs.
         */
        public static final String RFID_STATE_SERVICE = "RFID_STATE_SERVICE";

        /**
         * Presence state e.g. used for locations to give feedback about human presence.
         */
        public static final String PRESENCE_STATE_SERVICE = "PRESENCE_STATE_SERVICE";
    }
}
