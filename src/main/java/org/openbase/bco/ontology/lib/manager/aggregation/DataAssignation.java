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

            switch (serviceTypeMap.get(serviceTypeName)) {
                case UNKNOWN:
                    LOGGER.warn("There is a serviceType UNKNOWN!");

                case ACTIVATION_STATE_SERVICE:

                case BATTERY_STATE_SERVICE:
                    triples.addAll(batteryStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId));
                case BLIND_STATE_SERVICE:

                case BRIGHTNESS_STATE_SERVICE:

                case BUTTON_STATE_SERVICE:

                case COLOR_STATE_SERVICE:

                case CONTACT_STATE_SERVICE:

                case DOOR_STATE_SERVICE:

                case EARTHQUAKE_ALARM_STATE_SERVICE:

                case FIRE_ALARM_STATE_SERVICE:

                case HANDLE_STATE_SERVICE:

                case ILLUMINANCE_STATE_SERVICE:

                case INTENSITY_STATE_SERVICE:

                case INTRUSION_ALARM_STATE_SERVICE:

                case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:

                case MOTION_STATE_SERVICE:

                case PASSAGE_STATE_SERVICE:

                case POWER_CONSUMPTION_STATE_SERVICE:

                case POWER_STATE_SERVICE:

                case PRESENCE_STATE_SERVICE:

                case RFID_STATE_SERVICE:

                case SMOKE_ALARM_STATE_SERVICE:

                case SMOKE_STATE_SERVICE:

                case STANDBY_STATE_SERVICE:

                case SWITCH_STATE_SERVICE:

                case TAMPER_STATE_SERVICE:

                case TARGET_TEMPERATURE_STATE_SERVICE:

                case TEMPERATURE_ALARM_STATE_SERVICE:

                case TEMPERATURE_STATE_SERVICE:

                case TEMPEST_ALARM_STATE_SERVICE:

                case WATER_ALARM_STATE_SERVICE:

                case WINDOW_STATE_SERVICE:

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

    private List<TripleArrayList> batteryStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList, final String unitId)  {

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
        batteryValueList = selectionOfInsignificantObservations(batteryValueList);
        batteryLevelList = selectionOfInsignificantObservations(batteryLevelList);

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
    private List<StateValueWithTimestamp> selectionOfInsignificantObservations(final List<StateValueWithTimestamp> stateValueWithTimestampList) {

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
