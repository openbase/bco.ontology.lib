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

import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.StateValueWithTimestamp;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.trigger.sparql.TypeAlignment;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author agatting on 25.03.17.
 */
public class DataAssignation extends DataAggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAssignation.class);
    private final Map<String, ServiceType> serviceTypeMap;
    private final DateTime dateTimeFrom;

    public DataAssignation(final DateTime dateTimeFrom, final DateTime dateTimeUntil) {
        super(dateTimeFrom, dateTimeUntil);
        this.serviceTypeMap = TypeAlignment.getAlignedServiceTypes();
        this.dateTimeFrom = dateTimeFrom;
    }

    protected List<TripleArrayList> identifyServiceType(final HashMap<String, List<ServiceDataCollection>> serviceDataMap, final long connectionTimeMilli, final String unitId) {

        final List<TripleArrayList> triples = new ArrayList<>();

        for (final String serviceTypeName : serviceDataMap.keySet()) {
            final ServiceType serviceType = serviceTypeMap.get(serviceTypeName); //TODO

            switch (serviceTypeMap.get(serviceTypeName)) {
                case UNKNOWN:
                    LOGGER.warn("There is a serviceType UNKNOWN!");
                case ACTIVATION_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case BATTERY_STATE_SERVICE:
                    triples.addAll(batteryStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case BLIND_STATE_SERVICE:
                    triples.addAll(blindStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case BRIGHTNESS_STATE_SERVICE:

                case BUTTON_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case COLOR_STATE_SERVICE:
                    triples.addAll(colorStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case CONTACT_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case DOOR_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case EARTHQUAKE_ALARM_STATE_SERVICE:

                case FIRE_ALARM_STATE_SERVICE:

                case HANDLE_STATE_SERVICE:
                    triples.addAll(handleStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case ILLUMINANCE_STATE_SERVICE:
                    triples.addAll(illuminanceStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case INTENSITY_STATE_SERVICE:

                case INTRUSION_ALARM_STATE_SERVICE:

                case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:

                case MOTION_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case PASSAGE_STATE_SERVICE:

                case POWER_CONSUMPTION_STATE_SERVICE:
                    triples.addAll(powerConsumptionStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case POWER_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case PRESENCE_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case RFID_STATE_SERVICE:
                    triples.addAll(rfidStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case SMOKE_ALARM_STATE_SERVICE:

                case SMOKE_STATE_SERVICE:
                    triples.addAll(smokeStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case STANDBY_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case SWITCH_STATE_SERVICE:
                    triples.addAll(switchStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case TAMPER_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                case TARGET_TEMPERATURE_STATE_SERVICE:

                case TEMPERATURE_ALARM_STATE_SERVICE:

                case TEMPERATURE_STATE_SERVICE:
                    triples.addAll(temperatureStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case TEMPEST_ALARM_STATE_SERVICE:

                case WATER_ALARM_STATE_SERVICE:

                case WINDOW_STATE_SERVICE:
                    triples.addAll(genericBcoStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceType));
                default:
                    // no matched providerService
                    try {
                        throw new NotAvailableException("Could not assign to providerService. Please check implementation or rather integrate "
                                + serviceTypeMap.get(serviceTypeName) + " to method identifyServiceType of aggregation component.");
                    } catch (NotAvailableException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                    }
            }
        }
        return triples;
    }

    // method only for serviceTypes with one stateValue (bco discrete stateValues) - no individual distinction necessary
    private List<TripleArrayList> genericBcoStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId
            , final ServiceType serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> genericValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType() == null) {
                genericValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType()
                        + " doesn't match with expected dataType in genericStateValue with serviceType: " + serviceType.toString() + " !");
            }
        }
        genericValueList = dismissInsignificantObservations(genericValueList);

        return null;//TODO
    }

    private List<TripleArrayList> batteryStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

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

        return null;//TODO
    }

    private List<TripleArrayList> blindStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> blindMovementStateList = new ArrayList<>();
        List<StateValueWithTimestamp> blindOpeningRationList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType() == null) {
                //blind movement state
                blindMovementStateList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("double")) {
                // blind opening ratio
                blindOpeningRationList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in blindStateValue!");
            }
        }
        blindMovementStateList = dismissInsignificantObservations(blindMovementStateList);
        blindOpeningRationList = dismissInsignificantObservations(blindOpeningRationList);

        return null;//TODO
    }

    private List<TripleArrayList> colorStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

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
        brightnessList = dismissInsignificantObservations(brightnessList);
        hueList = dismissInsignificantObservations(hueList);
        saturationList = dismissInsignificantObservations(saturationList);

        return null;//TODO
    }

    private List<TripleArrayList> handleStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> handleValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("double")) {
                //position value
                handleValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in handleStateValue!");
            }
        }
        handleValueList = dismissInsignificantObservations(handleValueList);

        return null;//TODO
    }

    private List<TripleArrayList> illuminanceStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

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

        return null;//TODO
    }

    private List<TripleArrayList> powerConsumptionStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

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

        return null;//TODO
    }

    private List<TripleArrayList> rfidStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

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

    private List<TripleArrayList> smokeStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> smokeValueList = new ArrayList<>();
        List<StateValueWithTimestamp> smokeLevelList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType() == null) {
                //smoke value
                smokeValueList.add(stateValueWithTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("double")) {
                // smoke level
                smokeLevelList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in smokeStateValue!");
            }
        }
        smokeValueList = dismissInsignificantObservations(smokeValueList);
        smokeLevelList = dismissInsignificantObservations(smokeLevelList);

        return null;//TODO
    }

    private List<TripleArrayList> switchStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

        final List<TripleArrayList> triples = new ArrayList<>();
        List<StateValueWithTimestamp> switchValueList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueWithTimestamp stateValueWithTimestamp = new StateValueWithTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType().equalsIgnoreCase("double")) {
                //position value
                switchValueList.add(stateValueWithTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in switchStateValue!");
            }
        }
        switchValueList = dismissInsignificantObservations(switchValueList);

        return null;//TODO
    }

    private List<TripleArrayList> temperatureStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId) {

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

        return null;//TODO
    }

    private List<String> discrete(final long connectionTimeMilli, final List<StateValueWithTimestamp> discreteList) throws CouldNotPerformException {

        final List<String> stateValueObjects = new ArrayList<>();
        final DiscreteStateValues discreteStateValues = new DiscreteStateValues(connectionTimeMilli, discreteList);

        stateValueObjects.add(String.valueOf(discreteStateValues.getTimeWeighting()) + "\"^^NS:TimeWeighting");
        //TODO convert values...

        return stateValueObjects;
    }

    private List<String> continuous(final long connectionTimeMilli, final List<StateValueWithTimestamp> continuousList) throws CouldNotPerformException {

        final List<String> stateValueObjects = new ArrayList<>();
        final ContinuousStateValues continuousStateValues = new ContinuousStateValues(connectionTimeMilli, continuousList);

        stateValueObjects.add(String.valueOf(continuousStateValues.getMean()) + "\"^^NS:Mean");
        stateValueObjects.add(String.valueOf(continuousStateValues.getQuantity()) + "\"^^NS:Quantity");
        stateValueObjects.add(String.valueOf(continuousStateValues.getStandardDeviation()) + "\"^^NS:StandardDeviation");
        stateValueObjects.add(String.valueOf(continuousStateValues.getVariance()) + "\"^^NS:Variance");
        stateValueObjects.add(String.valueOf(continuousStateValues.getTimeWeighting()) + "\"^^NS:TimeWeighting");

        return stateValueObjects;
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
