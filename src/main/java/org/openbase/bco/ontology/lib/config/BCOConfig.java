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
}
