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
package org.openbase.bco.ontology.lib.manager.aggregation;

import org.apache.commons.lang3.tuple.Triple;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntAggregatedStateChange;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChange;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.ontology.OntNode;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.bco.ontology.lib.system.config.OntConfig.StateValueType;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author agatting on 25.03.17.
 */
@SuppressWarnings("unchecked")
public class DataAssignation extends DataAggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAssignation.class);
    private final OffsetDateTime dateTimeFrom;
    private final OffsetDateTime dateTimeUntil;
    private final long dateTimeFromMillis;
    private final Period period;
    private final Stopwatch stopwatch;
    private String unitId;
    private long unitConnectionTimeMilli;
    private String serviceType;

    //TODO if a state has multiple continuous values, than an additionally distinction is needed

    public DataAssignation(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil, final Period period) {
        super(dateTimeFrom, dateTimeUntil);

        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.dateTimeFromMillis = dateTimeFrom.toInstant().toEpochMilli();
        this.period = period;
        this.stopwatch = new Stopwatch();
    }

    protected List<RdfTriple> identifyServiceType(final HashMap<String, ?> serviceStateChangeMap, final long unitConnectionTimeMilli, final String unitId) {
        this.unitId = unitId;
        this.unitConnectionTimeMilli = unitConnectionTimeMilli;
        final List<RdfTriple> triples = new ArrayList<>();
        MultiException.ExceptionStack exceptionStack = null;

        for (final String serviceType : serviceStateChangeMap.keySet()) {
            this.serviceType = serviceType;

            try {
                switch (OntConfig.SERVICE_NAME_MAP.get(StringModifier.firstCharToLowerCase(serviceType))) {
                    case UNKNOWN:
                        LOGGER.warn("There is a serviceType UNKNOWN!");
                        break;
                    case ACTIVATION_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case BATTERY_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        triples.addAll(percentStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case BLIND_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        triples.addAll(percentStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case BRIGHTNESS_STATE_SERVICE:
                        break;
                    case BUTTON_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case COLOR_STATE_SERVICE:
                        triples.addAll(hsbStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case CONTACT_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case DOOR_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case EARTHQUAKE_ALARM_STATE_SERVICE:
                        break;
                    case FIRE_ALARM_STATE_SERVICE:
                        break;
                    case HANDLE_STATE_SERVICE:
                        triples.addAll(doubleStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case ILLUMINANCE_STATE_SERVICE:
                        triples.addAll(luxStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case INTENSITY_STATE_SERVICE:
                        break;
                    case INTRUSION_ALARM_STATE_SERVICE:
                        break;
                    case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:
                        break;
                    case MOTION_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case PASSAGE_STATE_SERVICE:
                        break;
                    case POWER_CONSUMPTION_STATE_SERVICE:
                        triples.addAll(powerStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case POWER_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case PRESENCE_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case RFID_STATE_SERVICE:
//                    triples.addAll(rfidStateValue(serviceStateChangeMap.get(serviceType)));
                        break;
                    case SMOKE_ALARM_STATE_SERVICE:
                        break;
                    case SMOKE_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        triples.addAll(percentStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case STANDBY_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case SWITCH_STATE_SERVICE:
                        triples.addAll(doubleStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case TAMPER_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case TARGET_TEMPERATURE_STATE_SERVICE:
                        break;
                    case TEMPERATURE_ALARM_STATE_SERVICE:
                        break;
                    case TEMPERATURE_STATE_SERVICE:
                        triples.addAll(celsiusStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case TEMPEST_ALARM_STATE_SERVICE:
                        break;
                    case WATER_ALARM_STATE_SERVICE:
                        break;
                    case WINDOW_STATE_SERVICE:
                        triples.addAll(bcoStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    default:
                        // no matched providerService
                        throw new NotAvailableException("Could not assign to providerService. Add" + OntConfig.SERVICE_NAME_MAP.get(serviceType));
                }
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("Could not process all service type identification or state value aggregation!", exceptionStack);
        }  catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    /**
     * Method identifies and aggregates state values based on discrete values (/bco state values like on, off, open, ...).
     *
     * @param stateChanges are the discrete state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     */
    private List<RdfTriple> bcoStateValue(final List<?> stateChanges) throws CouldNotPerformException {

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> bco = OntNode.getResources((List<OntStateChange>) stateChanges);
            return discreteValuesObservation(dismissUnusedStateValues(bco));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> bco = OntNode.getAggResources((List<OntAggregatedStateChange>) stateChanges);
            return discreteValuesAggObservation(bco);

        }
        throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
    }

    /**
     * Method identifies and aggregates state values based on data type percentage (continuous).
     *
     * @param stateChanges are the continuous state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     */
    private List<RdfTriple> percentStateValue(final List<?> stateChanges) throws CouldNotPerformException {

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> percentages = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.PERCENT);
            return continuousValuesObservation(dismissUnusedStateValues(percentages), StateValueType.PERCENT);

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> percentages = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.PERCENT);
            return continuousValuesAggObservation(percentages, StateValueType.PERCENT);
        }
        throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
    }

//    private List<RdfTriple> batteryOrBlindOrSmokeStateValue(final List<?> stateChanges) {
//        final List<RdfTriple> triples = new ArrayList<>();
//
//        try {
//            if (stateChanges.get(0) instanceof OntStateChange) {
//                List<OntStateChange> batteryValueList = new ArrayList<>();
//                List<OntStateChange> batteryLevelList = new ArrayList<>();
//
//                for (final OntStateChange stateChange : (List<OntStateChange>) stateChanges) {
////                    final String dataType = StringModifier.getLocalName(serviceDataColl.getStateValue().asLiteral().getDatatypeURI());
//
//                    if (!stateChange.getStateValue().isLiteral()) {
//                        //battery/blind/smoke value
//                        batteryValueList.add(stateChange);
//                    } else if (StringModifier.getLocalName(stateChange.getStateValue().asLiteral().getDatatypeURI()).equalsIgnoreCase("percent")) {
//                        // battery/blind/smoke level
//                        batteryLevelList.add(stateChange);
//                    }
//                }
//
//                batteryValueList = dismissUnusedStateValues(batteryValueList);
//                batteryLevelList = dismissUnusedStateValues(batteryLevelList);
//                triples.addAll(discreteValuesObservation(batteryValueList));
//                triples.addAll(continuousValuesObservation(batteryLevelList, "percent"));
//
//            } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
//                List<OntAggregatedStateChange> batteryValueList = new ArrayList<>();
//                List<OntAggregatedStateChange> batteryLevelList = new ArrayList<>();
//
//                for (final OntAggregatedStateChange serviceDataColl : (List<OntAggregatedStateChange>) stateChanges) {
////                    final String dataType = serviceDataColl.getStateValue().asLiteral().toString();
//
//                    if (!serviceDataColl.getStateValue().isLiteral()) {
//                        //battery/blind/smoke value
//                        batteryValueList.add(serviceDataColl);
//                    } else if (serviceDataColl.getStateValue().asLiteral().toString().equalsIgnoreCase("percent")) {
//                        // battery/blind/smoke level
//                        batteryLevelList.add(serviceDataColl);
//                    }
//                }
//                triples.addAll(discreteValuesAggObservation(batteryValueList));
//                triples.addAll(continuousValuesAggObservation(batteryLevelList, "percent"));
//            } else {
//                throw new CouldNotPerformException("Could not identify variable type.");
//            }
//        } catch (CouldNotPerformException ex) {
//            ExceptionPrinter.printHistory("Dropped data @batteryOrBlindOrSmokeStateValue ...!", ex, LOGGER, LogLevel.ERROR);
//        }
//        return triples;
//    }

    /**
     * Method identifies and aggregates state values based on data type hsb - hue, saturation, brightness (continuous).
     *
     * @param stateChanges are the continuous state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     */
    private List<RdfTriple> hsbStateValue(final List<?> stateChanges) throws CouldNotPerformException {
        final List<RdfTriple> triples = new ArrayList<>();

        if (stateChanges.get(0) instanceof OntStateChange) {
            List<OntStateChange> brightness = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.BRIGHTNESS);
            List<OntStateChange> hue = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.HUE);
            List<OntStateChange> saturation = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.SATURATION);

            triples.addAll(continuousValuesObservation(dismissUnusedStateValues(hue), StateValueType.HUE));
            triples.addAll(continuousValuesObservation(dismissUnusedStateValues(saturation), StateValueType.SATURATION));
            triples.addAll(continuousValuesObservation(dismissUnusedStateValues(brightness), StateValueType.BRIGHTNESS));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            List<OntAggregatedStateChange> brightness = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.BRIGHTNESS);
            List<OntAggregatedStateChange> hue = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.HUE);
            List<OntAggregatedStateChange> saturation = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.SATURATION);

            triples.addAll(continuousValuesAggObservation(hue, StateValueType.HUE));
            triples.addAll(continuousValuesAggObservation(saturation, StateValueType.SATURATION));
            triples.addAll(continuousValuesAggObservation(brightness, StateValueType.BRIGHTNESS));

        } else {
            throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
        }
        return triples;
    }

//    private List<RdfTriple> colorStateValue(final List<?> serviceDataCollList) {
//        final List<RdfTriple> triples = new ArrayList<>();
//
//        try {
//            if (serviceDataCollList.get(0) instanceof OntStateChange) {
//                List<OntStateChange> brightnessList = new ArrayList<>();
//                List<OntStateChange> hueList = new ArrayList<>();
//                List<OntStateChange> saturationList = new ArrayList<>();
//
//                for (final OntStateChange serviceDataColl : (List<OntStateChange>) serviceDataCollList) {
//                    final String dataType = StringModifier.getLocalName(serviceDataColl.getStateValue().asLiteral().getDatatypeURI());
//
//                    if (dataType.equalsIgnoreCase("brightness")) {
//                        //brightness value
//                        brightnessList.add(serviceDataColl);
//                    } else if (dataType.equalsIgnoreCase("hue")) {
//                        // hue value
//                        hueList.add(serviceDataColl);
//                    } else if (dataType.equalsIgnoreCase("saturation")) {
//                        // saturation value
//                        saturationList.add(serviceDataColl);
//                    }
//                }
//
//                hueList = dismissUnusedStateValues(hueList);
//                saturationList = dismissUnusedStateValues(saturationList);
//                brightnessList = dismissUnusedStateValues(brightnessList);
//                triples.addAll(continuousValuesObservation(hueList, "hue"));
//                triples.addAll(continuousValuesObservation(saturationList, "saturation"));
//                triples.addAll(continuousValuesObservation(brightnessList, "brightness"));
//            } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {
//                List<OntAggregatedStateChange> brightnessList = new ArrayList<>();
//                List<OntAggregatedStateChange> hueList = new ArrayList<>();
//                List<OntAggregatedStateChange> saturationList = new ArrayList<>();
//
//                for (final OntAggregatedStateChange serviceDataColl : (List<OntAggregatedStateChange>) serviceDataCollList) {
//                    final String dataType = serviceDataColl.getStateValue().asLiteral().toString();
//
//                    if (dataType.equalsIgnoreCase("brightness")) {
//                        //brightness value
//                        brightnessList.add(serviceDataColl);
//                    } else if (dataType.equalsIgnoreCase("hue")) {
//                        // hue value
//                        hueList.add(serviceDataColl);
//                    } else if (dataType.equalsIgnoreCase("saturation")) {
//                        // saturation value
//                        saturationList.add(serviceDataColl);
//                    }
//                }
//                triples.addAll(continuousValuesAggObservation(brightnessList, "brightness"));
//                triples.addAll(continuousValuesAggObservation(hueList, "hue"));
//                triples.addAll(continuousValuesAggObservation(saturationList, "saturation"));
//            } else {
//                throw new CouldNotPerformException("Could not identify variable type.");
//            }
//        } catch (CouldNotPerformException ex) {
//            ExceptionPrinter.printHistory("Dropped data @colorStateValue ...!", ex, LOGGER, LogLevel.ERROR);
//        }
//
////        final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap = getAggColorValues(hueList, saturationList, brightnessList);
////        try {
////            triples.addAll(getColorTriple(connectionTimeMilli, hsbCountMap, unitId, serviceType));
////        } catch (InterruptedException ex) {
////            ExceptionPrinter.printHistory("Dropped data @colorStateValue ...!", ex, LOGGER, LogLevel.ERROR);
////        }
//        return triples;
//    }

    /**
     * Method identifies and aggregates state values based on data type double (continuous).
     *
     * @param stateChanges are the continuous state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     */
    private List<RdfTriple> doubleStateValue(final List<?> stateChanges) throws CouldNotPerformException {

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> doubleValue = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.DOUBLE);
            return continuousValuesObservation(dismissUnusedStateValues(doubleValue), StateValueType.DOUBLE);

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> doubleValue = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.DOUBLE);
            return continuousValuesAggObservation(doubleValue, StateValueType.DOUBLE);
        }
        throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
    }

//    private List<RdfTriple> handleStateValue(final List<?> serviceDataCollList) {
//        final List<RdfTriple> triples = new ArrayList<>();
//
//        try {
//            if (serviceDataCollList.get(0) instanceof OntStateChange) {
//                List<OntStateChange> handleValueList = dismissUnusedStateValues((List<OntStateChange>) serviceDataCollList);
//                triples.addAll(continuousValuesObservation(handleValueList, "double"));
//            } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {
//                triples.addAll(continuousValuesAggObservation((List<OntAggregatedStateChange>) serviceDataCollList, "double"));
//            } else {
//                throw new CouldNotPerformException("Could not identify variable type.");
//            }
//        } catch (CouldNotPerformException ex) {
//            ExceptionPrinter.printHistory("Dropped data @handleStateValue ...!", ex, LOGGER, LogLevel.ERROR);
//        }
//        return triples;
//    }

    /**
     * Method identifies and aggregates state values based on data type lux (continuous).
     *
     * @param stateChanges are the continuous state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     */
    private List<RdfTriple> luxStateValue(final List<?> stateChanges) throws CouldNotPerformException {

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> lux = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.LUX);
            return continuousValuesObservation(dismissUnusedStateValues(lux), StateValueType.LUX);

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> lux = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.LUX);
            return continuousValuesAggObservation(lux, StateValueType.LUX);
        }
        throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
    }

//    private List<RdfTriple> illuminanceStateValue(final List<?> serviceDataCollList) {
//        final List<RdfTriple> triples = new ArrayList<>();
//
//        try {
//            if (serviceDataCollList.get(0) instanceof OntStateChange) {
//                List<OntStateChange> illuminanceValueList = dismissUnusedStateValues((List<OntStateChange>) serviceDataCollList);
//                triples.addAll(continuousValuesObservation(illuminanceValueList, "lux"));
//            } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {
//                triples.addAll(continuousValuesAggObservation((List<OntAggregatedStateChange>) serviceDataCollList, "lux"));
//            } else {
//                throw new CouldNotPerformException("Could not identify variable type.");
//            }
//        } catch (CouldNotPerformException ex) {
//            ExceptionPrinter.printHistory("Dropped data @illuminanceStateValue ...!", ex, LOGGER, LogLevel.ERROR);
//        }
//        return triples;
//    }

    /**
     * Method identifies and aggregates state values based on power - voltage, watt, ampere (continuous).
     *
     * @param stateChanges are the continuous state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     */
    private List<RdfTriple> powerStateValue(final List<?> stateChanges) throws CouldNotPerformException {
        final List<RdfTriple> triples = new ArrayList<>();

        if (stateChanges.get(0) instanceof OntStateChange) {
            List<OntStateChange> voltage = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.VOLTAGE);
            List<OntStateChange> watt = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.WATT);
            List<OntStateChange> ampere = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.AMPERE);

            triples.addAll(continuousValuesObservation(dismissUnusedStateValues(voltage), StateValueType.VOLTAGE));
            triples.addAll(continuousValuesObservation(dismissUnusedStateValues(watt), StateValueType.WATT));
            triples.addAll(continuousValuesObservation(dismissUnusedStateValues(ampere), StateValueType.AMPERE));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            List<OntAggregatedStateChange> voltage = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.VOLTAGE);
            List<OntAggregatedStateChange> watt = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.WATT);
            List<OntAggregatedStateChange> ampere = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.AMPERE);

            triples.addAll(continuousValuesAggObservation(voltage, StateValueType.VOLTAGE));
            triples.addAll(continuousValuesAggObservation(watt, StateValueType.WATT));
            triples.addAll(continuousValuesAggObservation(ampere, StateValueType.AMPERE));

        } else {
            throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
        }
        return triples;
    }

//    private List<RdfTriple> powerConsumptionStateValue(final List<?> serviceDataCollList) {
//        final List<RdfTriple> triples = new ArrayList<>();
//
//        try {
//            if (serviceDataCollList.get(0) instanceof OntStateChange) {
//                List<OntStateChange> voltageList = new ArrayList<>();
//                List<OntStateChange> wattList = new ArrayList<>();
//                List<OntStateChange> ampereList = new ArrayList<>();
//
//                for (final OntStateChange serviceDataColl : (List<OntStateChange>) serviceDataCollList) {
//                    final String dataType = StringModifier.getLocalName(serviceDataColl.getStateValue().asLiteral().getDatatypeURI());
//
//                    if (dataType.equalsIgnoreCase("voltage")) {
//                        //voltage value
//                        voltageList.add(serviceDataColl);
//                    } else if (dataType.equalsIgnoreCase("watt")) {
//                        // watt value
//                        wattList.add(serviceDataColl);
//                    } else if (dataType.equalsIgnoreCase("ampere")) {
//                        // ampere value
//                        ampereList.add(serviceDataColl);
//                    }
//                }
//                voltageList = dismissUnusedStateValues(voltageList);
//                wattList = dismissUnusedStateValues(wattList);
//                ampereList = dismissUnusedStateValues(ampereList);
//                triples.addAll(continuousValuesObservation(voltageList, "voltage"));
//                triples.addAll(continuousValuesObservation(wattList, "watt"));
//                triples.addAll(continuousValuesObservation(ampereList, "ampere"));
//            } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {
//                List<OntAggregatedStateChange> voltageList = new ArrayList<>();
//                List<OntAggregatedStateChange> wattList = new ArrayList<>();
//                List<OntAggregatedStateChange> ampereList = new ArrayList<>();
//
//                for (final OntAggregatedStateChange serviceDataColl : (List<OntAggregatedStateChange>) serviceDataCollList) {
//                    final String dataType = serviceDataColl.getStateValue().asLiteral().toString();
//
//                    if (dataType.equalsIgnoreCase("voltage")) {
//                        //voltage value
//                        voltageList.add(serviceDataColl);
//                    } else if (dataType.equalsIgnoreCase("watt")) {
//                        // watt value
//                        wattList.add(serviceDataColl);
//                    } else if (dataType.equalsIgnoreCase("ampere")) {
//                        // ampere value
//                        ampereList.add(serviceDataColl);
//                    }
//                }
//                triples.addAll(continuousValuesAggObservation(voltageList, "brightness"));
//                triples.addAll(continuousValuesAggObservation(wattList, "hue"));
//                triples.addAll(continuousValuesAggObservation(ampereList, "saturation"));
//            } else {
//                throw new CouldNotPerformException("Could not identify variable type.");
//            }
//        } catch (CouldNotPerformException ex) {
//            ExceptionPrinter.printHistory("Dropped data @colorStateValue ...!", ex, LOGGER, LogLevel.ERROR);
//        }
//        return triples;
//    }

    private List<RdfTriple> rfidStateValue(final List<?> serviceDataCollList) { //TODO
        final List<RdfTriple> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof OntStateChange) {
                List<OntStateChange> rfidValueList = dismissUnusedStateValues((List<OntStateChange>) serviceDataCollList);
            } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {

            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Dropped data @rfidStateValue ...!", ex, LOGGER, LogLevel.ERROR);
        }

        return null;//TODO
    }

//    private List<RdfTriple> switchStateValue(final List<?> serviceDataCollList) {
//        final List<RdfTriple> triples = new ArrayList<>();
//
//        try {
//            if (serviceDataCollList.get(0) instanceof OntStateChange) {
//                //TODO 0...1...another calculation of mean?
//                List<OntStateChange> switchValueList = dismissUnusedStateValues((List<OntStateChange>) serviceDataCollList);
//                triples.addAll(continuousValuesObservation(switchValueList, "double"));
//            } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {
//                triples.addAll(continuousValuesAggObservation((List<OntAggregatedStateChange>) serviceDataCollList, "double"));
//
//            } else {
//                throw new CouldNotPerformException("Could not identify variable type.");
//            }
//        } catch (CouldNotPerformException ex) {
//            ExceptionPrinter.printHistory("Dropped data @switchStateValue ...!", ex, LOGGER, LogLevel.ERROR);
//        }
//        return triples;
//    }

    /**
     * Method identifies and aggregates state values based on data type celsius (continuous).
     *
     * @param stateChanges are the continuous state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     */
    private List<RdfTriple> celsiusStateValue(final List<?> stateChanges) throws CouldNotPerformException {

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> temperature = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.CELSIUS);
            return continuousValuesObservation(dismissUnusedStateValues(temperature), StateValueType.CELSIUS);

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> temperature = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.CELSIUS);
            return continuousValuesAggObservation(temperature, StateValueType.CELSIUS);
        }
        throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
    }

//    private List<RdfTriple> temperatureStateValue(final List<?> serviceDataCollList) {
//        final List<RdfTriple> triples = new ArrayList<>();
//
//        try {
//            if (serviceDataCollList.get(0) instanceof OntStateChange) {
//                List<OntStateChange> temperatureValueList = dismissUnusedStateValues((List<OntStateChange>) serviceDataCollList);
//                triples.addAll(continuousValuesObservation(temperatureValueList, "celsius"));
//            } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {
//                triples.addAll(continuousValuesAggObservation((List<OntAggregatedStateChange>) serviceDataCollList, "celsius"));
//            } else {
//                throw new CouldNotPerformException("Could not identify variable type.");
//            }
//        } catch (CouldNotPerformException ex) {
//            ExceptionPrinter.printHistory("Dropped data @temperatureStateValue ...!", ex, LOGGER, LogLevel.ERROR);
//        }
//        return triples;
//    }

//    private HashMap<Triple<Integer, Integer, Integer>, Integer> getAggColorValues(final List<OntStateChange> hueList
//            , List<OntStateChange> saturationList, final List<OntStateChange> brightnessList) { //TODO
//
//        if (hueList.size() != saturationList.size() || hueList.size() != brightnessList.size()) {
//            LOGGER.error("List sizes of hue, saturation and brightness are not equal!");
//        }
//
//        // sort ascending (old to young)
//        Collections.sort(hueList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
//        Collections.sort(saturationList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
//        Collections.sort(brightnessList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
//
//        final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap = new HashMap<>();
//
//        for (int i = 0; i < hueList.size(); i++) {
//            final int hue = (int) (Double.parseDouble(hueList.get(i).getStateValue().asLiteral().getLexicalForm()) / 10);
//            final int saturation = (int) (Double.parseDouble(saturationList.get(i).getStateValue().asLiteral().getLexicalForm()) / 10);
//            final int brightness = (int) (Double.parseDouble(brightnessList.get(i).getStateValue().asLiteral().getLexicalForm()) / 10);
//
//            final Triple<Integer, Integer, Integer> hsb = new MutableTriple<>(hue, saturation, brightness);
//
//            if (hsbCountMap.containsKey(hsb)) {
//                // there is an entry with the hsb values
//                hsbCountMap.put(hsb, hsbCountMap.get(hsb) + 1);
//            } else {
//                // set new entry
//                hsbCountMap.put(hsb, 1);
//            }
//        }
//        return hsbCountMap;
//    }

    private List<RdfTriple> getColorTriple(final long connectionTimeMilli, final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap
            , final String unitId, final String serviceType) {

        final List<RdfTriple> triples = new ArrayList<>();
        final String timeWeighting = OntConfig.decimalFormat().format((double) connectionTimeMilli
                / (double) (dateTimeUntil.toInstant().toEpochMilli() - dateTimeFrom.toInstant().toEpochMilli()));
        final Set<Triple<Integer, Integer, Integer>> hsbSet = hsbCountMap.keySet();

        for (final Triple<Integer, Integer, Integer> hsb : hsbSet) {
            final String subj_AggObs = getAggObsInstance(unitId);

            final String hueValue = String.valueOf(hsb.getLeft() * 10);
            final String saturationValue = String.valueOf(hsb.getMiddle() * 10);
            final String brightnessValue = String.valueOf(hsb.getRight() * 10);
            final String quantity = String.valueOf(hsbCountMap.get(hsb));

            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntExpr.IS_A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), period.toString().toLowerCase()));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + timeWeighting + "\"^^xsd:double"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + hueValue + "\"^^NAMESPACE:Hue"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + saturationValue + "\"^^NAMESPACE:Saturation"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + brightnessValue + "\"^^NAMESPACE:Brightness"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + quantity + "\"^^xsd:int"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));
        }
        return triples;
    }

    private List<RdfTriple> discreteValuesObservation(final List<OntStateChange> discreteList) throws CouldNotPerformException {

        if (discreteList.isEmpty()) {
            throw new CouldNotPerformException("There is no state value. Empty list!");
        }

        final List<RdfTriple> triples = new ArrayList<>();
        final DiscreteStateValues discreteStateValues = new DiscreteStateValues(unitConnectionTimeMilli, discreteList);

        final HashMap<String, Long> activationTimeMap = discreteStateValues.getActiveTimePerStateValue();
        final HashMap<String, Integer> quantityMap = discreteStateValues.getQuantityPerStateValue();
        final double timeWeighting = discreteStateValues.getTimeWeighting();

        for (final String bcoStateType : activationTimeMap.keySet()) {
            // every stateType has his own aggObs instance!
            final String subj_AggObs = getAggObsInstance(unitId);

            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntExpr.IS_A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), period.toString().toLowerCase()));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), bcoStateType));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + String.valueOf(quantityMap.get(bcoStateType)) + "\"^^xsd:int"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.ACTIVITY_TIME.getName(), "\"" + String.valueOf(activationTimeMap.get(bcoStateType)) + "\"^^xsd:long"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(timeWeighting) + "\"^^xsd:double"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));
        }
        return triples;
    }

    private List<RdfTriple> discreteValuesAggObservation(final List<OntAggregatedStateChange> aggDiscreteList) throws CouldNotPerformException {

        if (aggDiscreteList.isEmpty()) {
            throw new CouldNotPerformException("There is no state value. Empty list!");
        }

        final List<RdfTriple> triples = new ArrayList<>();
        final OntConfig.Period toAggregatedPeriod = period; //TODO to aggregated period...
        final DiscreteStateValues discreteStateValues = new DiscreteStateValues(aggDiscreteList, toAggregatedPeriod);

        final HashMap<String, Long> activationTimeMap = discreteStateValues.getActiveTimePerStateValue();
        final HashMap<String, Integer> quantityMap = discreteStateValues.getQuantityPerStateValue();
        final double timeWeighting = discreteStateValues.getTimeWeighting();

        for (final String bcoStateType : activationTimeMap.keySet()) {
            // every stateType has his own aggObs instance!
            final String subj_AggObs = getAggObsInstance(unitId);

            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntExpr.IS_A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), period.toString().toLowerCase()));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), bcoStateType));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + String.valueOf(quantityMap.get(bcoStateType)) + "\"^^xsd:int"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.ACTIVITY_TIME.getName(), "\"" + String.valueOf(activationTimeMap.get(bcoStateType)) + "\"^^xsd:long"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(timeWeighting) + "\"^^xsd:double"));
            triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime")); //TODO generic timestamp...
        }
        return triples;
    }

    private List<RdfTriple> continuousValuesObservation(final List<OntStateChange> continuousList, final StateValueType dataType) throws CouldNotPerformException {

        if (continuousList.isEmpty()) {
            throw new CouldNotPerformException("There is no state value of kind " + dataType.name() + ".");
        }

        final List<RdfTriple> triples = new ArrayList<>();
        final ContinuousStateValues continuousStateValues = new ContinuousStateValues(unitConnectionTimeMilli, continuousList);
        final String subj_AggObs = getAggObsInstance(unitId);

        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntExpr.IS_A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), period.toString().toLowerCase()));
        // because of distinction the dataType of the stateValue is attached as literal (property hasStateValue) ...
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + dataType.name() + "\"^^xsd:string"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.MEAN.getName(), "\"" + String.valueOf(continuousStateValues.getMean()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.VARIANCE.getName(), "\"" + String.valueOf(continuousStateValues.getVariance()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STANDARD_DEVIATION.getName(), "\"" + String.valueOf(continuousStateValues.getStandardDeviation()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + String.valueOf(continuousStateValues.getQuantity()) + "\"^^xsd:int"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(continuousStateValues.getTimeWeighting()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));

        return triples;
    }

    private List<RdfTriple> continuousValuesAggObservation(final List<OntAggregatedStateChange> aggContinuousList, final StateValueType dataType) throws CouldNotPerformException {

        if (aggContinuousList.isEmpty()) {
            throw new CouldNotPerformException("There is no state value of kind " + dataType.name() + ".");
        }

        final List<RdfTriple> triples = new ArrayList<>();
        final OntConfig.Period toAggregatedPeriod = period; //TODO to aggregated period...
        final ContinuousStateValues continuousStateValues = new ContinuousStateValues(aggContinuousList, toAggregatedPeriod);
        final String subj_AggObs = getAggObsInstance(unitId);

        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntExpr.IS_A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), period.toString().toLowerCase()));
        // because of distinction the dataType of the stateValue is attached as literal (property hasStateValue) ...
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + dataType.name() + "\"^^xsd:string"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.MEAN.getName(), "\"" + String.valueOf(continuousStateValues.getMean()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.VARIANCE.getName(), "\"" + String.valueOf(continuousStateValues.getVariance()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.STANDARD_DEVIATION.getName(), "\"" + String.valueOf(continuousStateValues.getStandardDeviation()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + String.valueOf(continuousStateValues.getQuantity()) + "\"^^xsd:int"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(continuousStateValues.getTimeWeighting()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime")); //TODO generic timestamp...

        return triples;
    }

    private String getAggObsInstance(final String unitId) {
        // wait one millisecond to guarantee, that aggregationObservation instances are unique
        try {
            stopwatch.waitForStop(1);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        final String dateTimeNow = OffsetDateTime.now().toString();

        return "AggObs" + unitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
    }

    /**
     * Method dismisses all state values, which are not in the time frame (from - until), besides the oldest. Main problem/idea is that the oldest timestamp
     * before the time frame must be known to keep state value information about the whole time frame. The first (/oldest) timestamp inside the time frame can
     * be start, maybe, 5 seconds after the start timestamp of aggregation. Without the state value (+ timestamp) before the time frame, the information is
     * missing.
     *
     * @param stateChanges are the state values of an unit.
     * @return a reduced list with state values, which are relevant for the aggregation time frame.
     */
    private List<OntStateChange> dismissUnusedStateValues(final List<OntStateChange> stateChanges) {

        // sort ascending (old to young)
        Collections.sort(stateChanges, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        final List<OntStateChange> bufDataList = new ArrayList<>();
        OntStateChange bufData = null;

        for (final OntStateChange stateChange : stateChanges) {
            final long timestampMillis = OffsetDateTime.parse(stateChange.getTimestamp()).toInstant().toEpochMilli();

            if (timestampMillis <= dateTimeFromMillis) {
                bufData = stateChange;
            } else {
                if (bufData != null) {
                    bufDataList.add(bufData);
                    bufData = null;
                }
                bufDataList.add(stateChange);
            }
        }
        return bufDataList;
    }

}
