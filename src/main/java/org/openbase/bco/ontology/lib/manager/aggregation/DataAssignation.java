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

import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.StateValueWithTimestamp;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.trigger.sparql.TypeAlignment;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author agatting on 25.03.17.
 */
public class DataAssignation extends DataAggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAssignation.class);
    private final Map<String, ServiceType> serviceTypeMap;
    private final DateTime dateTimeFrom;
    private final DateTime dateTimeUntil;
    private final OntConfig.Period period;
    private final SimpleDateFormat dateFormat;
    private final Stopwatch stopwatch;

    //TODO if a state (like battery state) has multiple continuous values, than an additionally distinction is needed

    public DataAssignation(final DateTime dateTimeFrom, final DateTime dateTimeUntil, final OntConfig.Period period) {
        super(dateTimeFrom, dateTimeUntil);

        this.serviceTypeMap = TypeAlignment.getAlignedServiceTypes();
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.period = period;
        this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());
        this.stopwatch = new Stopwatch();
    }

    protected List<TripleArrayList> identifyServiceType(final HashMap<String, List<ServiceDataCollection>> serviceDataMap, final long connectionTimeMilli, final String unitId) {
        final List<TripleArrayList> triples = new ArrayList<>();

        for (final String serviceTypeName : serviceDataMap.keySet()) {

            switch (serviceTypeMap.get(serviceTypeName)) {
                case UNKNOWN:
                    LOGGER.warn("There is a serviceType UNKNOWN!");
                case ACTIVATION_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case BATTERY_STATE_SERVICE:
                    triples.addAll(batteryStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case BLIND_STATE_SERVICE:
                    triples.addAll(blindStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId,serviceTypeName));
                    break;
                case BRIGHTNESS_STATE_SERVICE:
                    break;
                case BUTTON_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case COLOR_STATE_SERVICE:
                    triples.addAll(colorStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case CONTACT_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case DOOR_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case EARTHQUAKE_ALARM_STATE_SERVICE:
                    break;
                case FIRE_ALARM_STATE_SERVICE:
                    break;
                case HANDLE_STATE_SERVICE:
                    triples.addAll(handleStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case ILLUMINANCE_STATE_SERVICE:
                    triples.addAll(illuminanceStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case INTENSITY_STATE_SERVICE:
                    break;
                case INTRUSION_ALARM_STATE_SERVICE:
                    break;
                case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:
                    break;
                case MOTION_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case PASSAGE_STATE_SERVICE:
                    break;
                case POWER_CONSUMPTION_STATE_SERVICE:
                    triples.addAll(powerConsumptionStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case POWER_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case PRESENCE_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case RFID_STATE_SERVICE:
//                    triples.addAll(rfidStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case SMOKE_ALARM_STATE_SERVICE:
                    break;
                case SMOKE_STATE_SERVICE:
                    triples.addAll(smokeStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case STANDBY_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case SWITCH_STATE_SERVICE:
                    triples.addAll(switchStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId,serviceTypeName));
                    break;
                case TAMPER_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case TARGET_TEMPERATURE_STATE_SERVICE:
                    break;
                case TEMPERATURE_ALARM_STATE_SERVICE:
                    break;
                case TEMPERATURE_STATE_SERVICE:
                    triples.addAll(temperatureStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case TEMPEST_ALARM_STATE_SERVICE:
                    break;
                case WATER_ALARM_STATE_SERVICE:
                    break;
                case WINDOW_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                default:
                    // no matched providerService
                    try {
                        throw new NotAvailableException("Could not assign to providerService. Please check implementation or rather integrate "
                                + serviceTypeMap.get(serviceTypeName) + " to method identifyServiceType of aggregation component.");
                    } catch (NotAvailableException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                    }
                    break;
            }
        }
        return triples;
    }

    // method only for serviceTypes with one stateValue (bco simpleDiscreteValues stateValues) - no individual distinction necessary
    private List<TripleArrayList> genericBcoStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> genericValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType() == null) {
                genericValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType()
                        + " doesn't match with expected dataType in genericStateValue with serviceType: " + serviceType + " !");
            }
        }
        genericValueList = dismissInsignificantObservations(genericValueList);

        try {
            triples.addAll(simpleDiscreteValues(connectionTimeMilli, genericValueList, unitId, serviceType));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @genericBcoStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> batteryStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> batteryValueList = new ArrayList<>();
        List<StateValueWithTimestamp> batteryLevelList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType() == null) {
                //battery value
                batteryValueList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("percent")) {
                // battery level
                batteryLevelList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in batteryStateValue!");
            }
        }
        batteryValueList = dismissInsignificantObservations(batteryValueList);
        batteryLevelList = dismissInsignificantObservations(batteryLevelList);

        try {
            triples.addAll(simpleDiscreteValues(connectionTimeMilli, batteryValueList, unitId, serviceType));
            triples.addAll(simpleContinuousValues(connectionTimeMilli, batteryLevelList, unitId, serviceType, "percent"));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @batteryStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> blindStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> blindMovementStateList = new ArrayList<>();
        List<StateValueWithTimestamp> blindOpeningRationList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType() == null) {
                //blind movement state
                blindMovementStateList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("percent")) {
                // blind opening ratio
                blindOpeningRationList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in blindStateValue!");
            }
        }
        blindMovementStateList = dismissInsignificantObservations(blindMovementStateList);
        blindOpeningRationList = dismissInsignificantObservations(blindOpeningRationList);

        try {
            triples.addAll(simpleDiscreteValues(connectionTimeMilli, blindMovementStateList, unitId, serviceType));
            triples.addAll(simpleContinuousValues(connectionTimeMilli, blindOpeningRationList, unitId, serviceType, "percent"));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @blindStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> colorStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> brightnessList = new ArrayList<>();
        List<StateValueWithTimestamp> hueList = new ArrayList<>();
        List<StateValueWithTimestamp> saturationList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("brightness")) {
                //brightness value
                brightnessList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("hue")) {
                // hue value
                hueList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("saturation")) {
                // saturation value
                saturationList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in colorStateValue!");
            }
        }
        hueList = dismissInsignificantObservations(hueList);
        saturationList = dismissInsignificantObservations(saturationList);
        brightnessList = dismissInsignificantObservations(brightnessList);

        final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap = getAggColorValues(hueList, saturationList, brightnessList);
        try {
            triples.addAll(getColorTriple(connectionTimeMilli, hsbCountMap, unitId, serviceType));
        } catch (InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @colorStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> handleStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> handleValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("double")) {
                //TODO 0...360...another calculation of mean?
                //position value
                handleValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in handleStateValue!");
            }
        }
        handleValueList = dismissInsignificantObservations(handleValueList);

        try {
            triples.addAll(simpleContinuousValues(connectionTimeMilli, handleValueList, unitId, serviceType, "double"));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @handleStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> illuminanceStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList
            , final String unitId, final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> illuminanceValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("lux")) {
                //illuminance value
                illuminanceValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in illuminanceStateValue!");
            }
        }
        illuminanceValueList = dismissInsignificantObservations(illuminanceValueList);

        try {
            triples.addAll(simpleContinuousValues(connectionTimeMilli, illuminanceValueList, unitId, serviceType, "lux"));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @illuminanceStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> powerConsumptionStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList
            , final String unitId, final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> voltageList = new ArrayList<>();
        List<StateValueWithTimestamp> wattList = new ArrayList<>();
        List<StateValueWithTimestamp> ampereList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("voltage")) {
                //voltage value
                voltageList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("watt")) {
                // watt value
                wattList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("ampere")) {
                // ampere value
                ampereList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in powerConsumptionStateValue!");
            }
        }
        voltageList = dismissInsignificantObservations(voltageList);
        wattList = dismissInsignificantObservations(wattList);
        ampereList = dismissInsignificantObservations(ampereList);

        try {
            triples.addAll(simpleContinuousValues(connectionTimeMilli, voltageList, unitId, serviceType, "voltage"));
            triples.addAll(simpleContinuousValues(connectionTimeMilli, wattList, unitId, serviceType, "watt"));
            triples.addAll(simpleContinuousValues(connectionTimeMilli, ampereList, unitId, serviceType, "ampere"));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @powerConsumptionStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> rfidStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> rfidValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("string")) {
                //rfid string
                rfidValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in rfidStateValue!");
            }
        }
        rfidValueList = dismissInsignificantObservations(rfidValueList);

        return null;//TODO
    }

    private List<TripleArrayList> smokeStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> smokeValueList = new ArrayList<>();
        List<StateValueWithTimestamp> smokeLevelList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType() == null) {
                //smoke value
                smokeValueList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("percent")) {
                // smoke level
                smokeLevelList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in smokeStateValue!");
            }
        }
        smokeValueList = dismissInsignificantObservations(smokeValueList);
        smokeLevelList = dismissInsignificantObservations(smokeLevelList);

        try {
            triples.addAll(simpleDiscreteValues(connectionTimeMilli, smokeValueList, unitId, serviceType));
            triples.addAll(simpleContinuousValues(connectionTimeMilli, smokeLevelList, unitId, serviceType, "percent"));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @smokeStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> switchStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> switchValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("double")) {
                ////TODO 0...1...another calculation of mean?
                //position value
                switchValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in switchStateValue!");
            }
        }
        switchValueList = dismissInsignificantObservations(switchValueList);

        try {
            triples.addAll(simpleContinuousValues(connectionTimeMilli, switchValueList, unitId, serviceType, "double"));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @switchStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private List<TripleArrayList> temperatureStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList
            , final String unitId, final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> temperatureValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("celsius")) {
                //temperature value
                temperatureValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in temperatureStateValue!");
            }
        }
        temperatureValueList = dismissInsignificantObservations(temperatureValueList);

        try {
            triples.addAll(simpleContinuousValues(connectionTimeMilli, temperatureValueList, unitId, serviceType, "celsius"));
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @temperatureStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return triples;
    }

    private HashMap<Triple<Integer, Integer, Integer>, Integer> getAggColorValues(final List<StateValueWithTimestamp> hueList
            , List<StateValueWithTimestamp> saturationList, final List<StateValueWithTimestamp> brightnessList) {

        if (hueList.size() != saturationList.size() || hueList.size() != brightnessList.size()) {
            LOGGER.error("List sizes of hue, saturation and brightness are not equal!");
        }

        // sort ascending (old to young)
        Collections.sort(hueList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        Collections.sort(saturationList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        Collections.sort(brightnessList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap = new HashMap<>();

        for (int i = 0; i < hueList.size(); i++) {
            final int hue = (int) (Double.parseDouble(hueList.get(i).getStateValue()) / 10);
            final int saturation = (int) (Double.parseDouble(saturationList.get(i).getStateValue()) / 10);
            final int brightness = (int) (Double.parseDouble(brightnessList.get(i).getStateValue()) / 10);

            final Triple<Integer, Integer, Integer> hsb = new MutableTriple<>(hue, saturation, brightness);

            if (hsbCountMap.containsKey(hsb)) {
                // there is an entry with the hsb values
                hsbCountMap.put(hsb, hsbCountMap.get(hsb) + 1);
            } else {
                // set new entry
                hsbCountMap.put(hsb, 1);
            }
        }
        return hsbCountMap;
    }

    private List<TripleArrayList> getColorTriple(final long connectionTimeMilli, final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap
            , final String unitId, final String serviceType) throws InterruptedException {

        final List<TripleArrayList> triples = new ArrayList<>();
        final double timeWeighting = connectionTimeMilli / (dateTimeUntil.getMillis() - dateTimeFrom.getMillis());
        final Set<Triple<Integer, Integer, Integer>> hsbSet = hsbCountMap.keySet();

        for (final Triple<Integer, Integer, Integer> hsb : hsbSet) {
            final String subj_AggObs = getAggObsInstance(unitId);

            final String hueValue = String.valueOf(hsb.getLeft() * 10);
            final String saturationValue = String.valueOf(hsb.getMiddle() * 10);
            final String brightnessValue = String.valueOf(hsb.getRight() * 10);
            final String quantity = String.valueOf(hsbCountMap.get(hsb));

            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntExpr.A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), period.toString().toLowerCase()));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(timeWeighting) + "\"^^xsd:double"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + hueValue + "\"^^NS:Hue"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + saturationValue + "\"^^NS:Saturation"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + brightnessValue + "\"^^NS:Brightness"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + quantity + "\"^^xsd:int"));
        }
        return triples;
    }

    private List<TripleArrayList> simpleDiscreteValues(final long connectionTimeMilli, final List<StateValueWithTimestamp> discreteList, final String unitId
            , final String serviceType) throws CouldNotPerformException, InterruptedException {

        final List<TripleArrayList> triples = new ArrayList<>();
        final DiscreteStateValues discreteStateValues = new DiscreteStateValues(connectionTimeMilli, discreteList);

        final HashMap<String, Long> activationTimeMap = discreteStateValues.getActiveTimePerStateValue();
        final HashMap<String, Integer> quantityMap = discreteStateValues.getQuantityPerStateValue();
        final double timeWeighting = discreteStateValues.getTimeWeighting();

        for (final String bcoStateType : activationTimeMap.keySet()) {
            // every stateType has his own aggObs instance!
            final String subj_AggObs = getAggObsInstance(unitId);

            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntExpr.A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), period.toString().toLowerCase()));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), bcoStateType));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + String.valueOf(quantityMap.get(bcoStateType)) + "\"^^xsd:int"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.ACTIVITY_TIME.getName(), "\"" + String.valueOf(activationTimeMap.get(bcoStateType)) + "\"^^xsd:long"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(timeWeighting) + "\"^^xsd:double"));
//            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), ));
        }
        return triples;
    }

    private List<TripleArrayList> simpleContinuousValues(final long connectionTimeMilli, final List<StateValueWithTimestamp> continuousList, final String unitId
            , final String serviceType, final String dataType) throws CouldNotPerformException, InterruptedException {

        final List<TripleArrayList> triples = new ArrayList<>();
        final ContinuousStateValues continuousStateValues = new ContinuousStateValues(connectionTimeMilli, continuousList);
        final String subj_AggObs = getAggObsInstance(unitId);

        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntExpr.A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), period.toString().toLowerCase()));
        // because of distinction the dataType of the stateValue is attached as literal (property hasStateValue) ...
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + dataType + "\"^^xsd:string"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.MEAN.getName(), "\"" + String.valueOf(continuousStateValues.getMean()) + "\"^^xsd:double"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.VARIANCE.getName(), "\"" + String.valueOf(continuousStateValues.getVariance()) + "\"^^xsd:double"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STANDARD_DEVIATION.getName(), "\"" + String.valueOf(continuousStateValues.getStandardDeviation()) + "\"^^xsd:double"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + String.valueOf(continuousStateValues.getQuantity()) + "\"^^xsd:int"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(continuousStateValues.getTimeWeighting()) + "\"^^xsd:double"));
//      triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), ));

        return triples;
    }

    //    private String getTimestampPeriodCode(final String timestamp) throws NotAvailableException {
//        final DateTime dateTime = new DateTime(timestamp);
//
//        switch (period) {
//            case HOUR:
//                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), 0, 0).toString();
//            case DAY:
//                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0, 0).toString();
//            case WEEK:
//                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0, 0).toString(); //TODO
//            case MONTH:
//                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), 0, 0, 0, 0).toString();
//            case YEAR:
//                return new DateTime(dateTime.getYear(), 0, 0, 0, 0, 0).toString();
//            default:
//                throw new NotAvailableException("Could not perform adaption of dateTime for aggregation. Cause period time "
//                        + period.toString() + " could not be identified!");
//        }
//    }

    private String getAggObsInstance(final String unitId) throws InterruptedException {
        // wait one millisecond to guarantee, that aggregationObservation instances are unique
        stopwatch.waitForStop(1);

        final String dateTimeNow = dateFormat.format(new Date());

        return "AggObs" + unitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
    }

    // dismiss all observations below the dateTimeFrom. BESIDES the youngest observation below the dateTimeFrom.
    private List<StateValueWithTimestamp> dismissInsignificantObservations(final List<StateValueWithTimestamp> stateValueWithTimestampList) {

        // sort ascending (old to young)
        Collections.sort(stateValueWithTimestampList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        final List<StateValueWithTimestamp> bufDataList = new ArrayList<>();
        boolean insignificant = true;
        StateValueWithTimestamp bufData = null;
        final long dateTimeFromMillis = dateTimeFrom.getMillis();

        for (final StateValueWithTimestamp stateValueWithTimestamp : stateValueWithTimestampList) {
            final long stateValueMillis = new DateTime(stateValueWithTimestamp.getTimestamp()).getMillis();

            if (insignificant && stateValueMillis <= dateTimeFromMillis) {
                bufData = stateValueWithTimestamp;
            } else {
                if (insignificant && bufData != null) {
                    bufDataList.add(bufData);
                    insignificant = false;
                }
                bufDataList.add(stateValueWithTimestamp);
            }
        }

        return bufDataList;
    }

}
