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

import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.StateValueTimestamp;
import org.openbase.bco.ontology.lib.trigger.sparql.TypeAlignment;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author agatting on 25.03.17.
 */
public class DataAssignation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAssignation.class);
    private Map<String, ServiceType> serviceTypeMap;

    public DataAssignation() {
//        calculation();
        this.serviceTypeMap = TypeAlignment.getAlignedServiceTypes();
    }

    private void relateDataForEachUnit(final HashMap<String, Long> connectionTimePerUnit, final HashMap<String, List<ObservationDataCollection>> observationDataPerUnit) {

        for (final String unitId : connectionTimePerUnit.keySet()) {
            final long connectionTimeMilli = connectionTimePerUnit.get(unitId);
            final List<ObservationDataCollection> obsDataCollList = observationDataPerUnit.get(unitId);

            relateDataForEachProviderServiceOfEachUnit(unitId, connectionTimeMilli, obsDataCollList);
        }
    }

    private void relateDataForEachProviderServiceOfEachUnit(final String unitId, final long connectionTimeMilli, final List<ObservationDataCollection> obsDataCollList) {

        final HashMap<String, List<ServiceDataCollection>> hashMap = new HashMap<>();

        for (final ObservationDataCollection tripleObs : obsDataCollList) {

            final ServiceDataCollection serviceDataCollection = new ServiceDataCollection(tripleObs.getStateValue(), tripleObs.getDataType(), tripleObs.getTimestamp());

            if (hashMap.containsKey(tripleObs.getProviderService())) {
                // there is an entry: add data
                final List<ServiceDataCollection> arrayList = hashMap.get(tripleObs.getProviderService());
                arrayList.add(serviceDataCollection);
                hashMap.put(tripleObs.getProviderService(), arrayList);
            } else {
                // there is no entry: put data
                final List<ServiceDataCollection> arrayList = new ArrayList<>();
                arrayList.add(serviceDataCollection);
                hashMap.put(tripleObs.getProviderService(), arrayList);
            }
        }



    }

    private void dataAssignationToServiceType(final HashMap<String, List<ServiceDataCollection>> providerServiceDataMap) {

        for (final String serviceTypeName : providerServiceDataMap.keySet()) {


            switch (serviceTypeMap.get(serviceTypeName)) {
                case UNKNOWN:
                    LOGGER.warn("There is a serviceType UNKNOWN!");

                case ACTIVATION_STATE_SERVICE:

                case BATTERY_STATE_SERVICE:

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
                                + serviceTypeMap.get(serviceTypeName) + " to method dataAssignationToServiceType of aggregation component.");
                    } catch (NotAvailableException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                    }
            }
        }
    }

    private void batteryStateValue(final long connectionTimeMilli, final List<ServiceDataCollection> serviceDataCollList) {

        final List<StateValueTimestamp> batteryValueList = new ArrayList<>();
        final List<StateValueTimestamp> batteryLevelList = new ArrayList<>();

        for (final ServiceDataCollection serviceDataColl : serviceDataCollList) {
            final StateValueTimestamp stateValueTimestamp = new StateValueTimestamp(serviceDataColl.getStateValue(), serviceDataColl.getTimestamp());

            if (serviceDataColl.getDataType() == null) {
                //battery value
                batteryValueList.add(stateValueTimestamp);
            } else if (serviceDataColl.getDataType().equalsIgnoreCase("percent")) {
                // battery level
                batteryLevelList.add(stateValueTimestamp);
            } else {
                LOGGER.warn("Containing dataType " + serviceDataColl.getDataType() + " doesn't match with expected dataType in batteryStateValue!");
            }
        }
        //TODO
    }
}
