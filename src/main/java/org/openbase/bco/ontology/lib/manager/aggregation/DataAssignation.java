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
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceAggDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceDataCollection;
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
@SuppressWarnings("unchecked")
public class DataAssignation extends DataAggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAssignation.class);
    private final Map<String, ServiceType> serviceTypeMap;
    private final DateTime dateTimeFrom;
    private final DateTime dateTimeUntil;
    private final OntConfig.Period period;
//    private final SimpleDateFormat dateFormat;
    private final Stopwatch stopwatch;

    //TODO if a state has multiple continuous values, than an additionally distinction is needed

    public DataAssignation(final DateTime dateTimeFrom, final DateTime dateTimeUntil, final OntConfig.Period period) {
        super(dateTimeFrom, dateTimeUntil);

        this.serviceTypeMap = TypeAlignment.getAlignedServiceTypes();
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.period = period;
//        this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());
        this.stopwatch = new Stopwatch();
    }

    protected List<TripleArrayList> identifyServiceType(final HashMap<String, ?> serviceDataMap, final long connectionTimeMilli, final String unitId) {
        final List<TripleArrayList> triples = new ArrayList<>();

        for (final String serviceTypeName : serviceDataMap.keySet()) {

            switch (serviceTypeMap.get(serviceTypeName)) {
                case UNKNOWN:
                    LOGGER.warn("There is a serviceType UNKNOWN!");
                    break;
                case ACTIVATION_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case BATTERY_STATE_SERVICE:
                    triples.addAll(batteryOrBlindOrSmokeStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case BLIND_STATE_SERVICE:
                    triples.addAll(batteryOrBlindOrSmokeStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId,serviceTypeName));
                    break;
                case BRIGHTNESS_STATE_SERVICE:
                    break;
                case BUTTON_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case COLOR_STATE_SERVICE:
                    triples.addAll(colorStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case CONTACT_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case DOOR_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case EARTHQUAKE_ALARM_STATE_SERVICE:
                    break;
                case FIRE_ALARM_STATE_SERVICE:
                    break;
                case HANDLE_STATE_SERVICE:
                    triples.addAll(handleStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case ILLUMINANCE_STATE_SERVICE:
                    triples.addAll(illuminanceStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case INTENSITY_STATE_SERVICE:
                    break;
                case INTRUSION_ALARM_STATE_SERVICE:
                    break;
                case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:
                    break;
                case MOTION_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case PASSAGE_STATE_SERVICE:
                    break;
                case POWER_CONSUMPTION_STATE_SERVICE:
                    triples.addAll(powerConsumptionStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case POWER_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case PRESENCE_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case RFID_STATE_SERVICE:
//                    triples.addAll(rfidStateValue(connectionTimeMilli, serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case SMOKE_ALARM_STATE_SERVICE:
                    break;
                case SMOKE_STATE_SERVICE:
                    triples.addAll(batteryOrBlindOrSmokeStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case STANDBY_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case SWITCH_STATE_SERVICE:
                    triples.addAll(switchStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId,serviceTypeName));
                    break;
                case TAMPER_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case TARGET_TEMPERATURE_STATE_SERVICE:
                    break;
                case TEMPERATURE_ALARM_STATE_SERVICE:
                    break;
                case TEMPERATURE_STATE_SERVICE:
                    triples.addAll(temperatureStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
                    break;
                case TEMPEST_ALARM_STATE_SERVICE:
                    break;
                case WATER_ALARM_STATE_SERVICE:
                    break;
                case WINDOW_STATE_SERVICE:
                    triples.addAll(bcoStateValue(connectionTimeMilli, (List<?>) serviceDataMap.get(serviceTypeName), unitId, serviceTypeName));
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
    private List<TripleArrayList> bcoStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {

        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                List<ServiceDataCollection> genericValueList = dismissInsignificantObservations((List<ServiceDataCollection>) serviceDataCollList);
                triples.addAll(simpleDiscreteValues(connectionTimeMilli, genericValueList, unitId, serviceType));

            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {
                triples.addAll(simpleAggDiscreteValues((List<ServiceAggDataCollection>) serviceDataCollList, unitId, serviceType));
            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @bcoStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private List<TripleArrayList> batteryOrBlindOrSmokeStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {
        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                List<ServiceDataCollection> batteryValueList = new ArrayList<>();
                List<ServiceDataCollection> batteryLevelList = new ArrayList<>();

                for (final ServiceDataCollection serviceDataColl : (List<ServiceDataCollection>) serviceDataCollList) {
                    final String dataType = OntologyToolkit.getLocalName(serviceDataColl.getStateValue().asLiteral().getDatatypeURI());

                    if (!serviceDataColl.getStateValue().isLiteral()) {
                        //battery/blind/smoke value
                        batteryValueList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("percent")) {
                        // battery/blind/smoke level
                        batteryLevelList.add(serviceDataColl);
                    }
                }
                batteryValueList = dismissInsignificantObservations(batteryValueList);
                batteryLevelList = dismissInsignificantObservations(batteryLevelList);
                triples.addAll(simpleDiscreteValues(connectionTimeMilli, batteryValueList, unitId, serviceType));
                triples.addAll(simpleContinuousValues(connectionTimeMilli, batteryLevelList, unitId, serviceType, "percent"));

            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {
                List<ServiceAggDataCollection> batteryValueList = new ArrayList<>();
                List<ServiceAggDataCollection> batteryLevelList = new ArrayList<>();

                for (final ServiceAggDataCollection serviceDataColl : (List<ServiceAggDataCollection>) serviceDataCollList) {
                    final String dataType = serviceDataColl.getStateValue().asLiteral().toString();

                    if (!serviceDataColl.getStateValue().isLiteral()) {
                        //battery/blind/smoke value
                        batteryValueList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("percent")) {
                        // battery/blind/smoke level
                        batteryLevelList.add(serviceDataColl);
                    }
                }
                triples.addAll(simpleAggDiscreteValues(batteryValueList, unitId, serviceType));
                triples.addAll(simpleAggContinuousValues(batteryLevelList, unitId, serviceType, "percent"));
            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @batteryOrBlindOrSmokeStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private List<TripleArrayList> colorStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {
        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                List<ServiceDataCollection> brightnessList = new ArrayList<>();
                List<ServiceDataCollection> hueList = new ArrayList<>();
                List<ServiceDataCollection> saturationList = new ArrayList<>();

                for (final ServiceDataCollection serviceDataColl : (List<ServiceDataCollection>) serviceDataCollList) {
                    final String dataType = OntologyToolkit.getLocalName(serviceDataColl.getStateValue().asLiteral().getDatatypeURI());

                    if (dataType.equalsIgnoreCase("brightness")) {
                        //brightness value
                        brightnessList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("hue")) {
                        // hue value
                        hueList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("saturation")) {
                        // saturation value
                        saturationList.add(serviceDataColl);
                    }
                }

                hueList = dismissInsignificantObservations(hueList);
                saturationList = dismissInsignificantObservations(saturationList);
                brightnessList = dismissInsignificantObservations(brightnessList);
                triples.addAll(simpleContinuousValues(connectionTimeMilli, hueList, unitId, serviceType, "hue"));
                triples.addAll(simpleContinuousValues(connectionTimeMilli, saturationList, unitId, serviceType, "saturation"));
                triples.addAll(simpleContinuousValues(connectionTimeMilli, brightnessList, unitId, serviceType, "brightness"));
            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {
                List<ServiceAggDataCollection> brightnessList = new ArrayList<>();
                List<ServiceAggDataCollection> hueList = new ArrayList<>();
                List<ServiceAggDataCollection> saturationList = new ArrayList<>();

                for (final ServiceAggDataCollection serviceDataColl : (List<ServiceAggDataCollection>) serviceDataCollList) {
                    final String dataType = serviceDataColl.getStateValue().asLiteral().toString();

                    if (dataType.equalsIgnoreCase("brightness")) {
                        //brightness value
                        brightnessList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("hue")) {
                        // hue value
                        hueList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("saturation")) {
                        // saturation value
                        saturationList.add(serviceDataColl);
                    }
                }
                triples.addAll(simpleAggContinuousValues(brightnessList, unitId, serviceType, "brightness"));
                triples.addAll(simpleAggContinuousValues(hueList, unitId, serviceType, "hue"));
                triples.addAll(simpleAggContinuousValues(saturationList, unitId, serviceType, "saturation"));
            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @colorStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

//        final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap = getAggColorValues(hueList, saturationList, brightnessList);
//        try {
//            triples.addAll(getColorTriple(connectionTimeMilli, hsbCountMap, unitId, serviceType));
//        } catch (InterruptedException e) {
//            ExceptionPrinter.printHistory("Dropped data @colorStateValue ...!", e, LOGGER, LogLevel.ERROR);
//        }
        return triples;
    }

    private List<TripleArrayList> handleStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {
        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                List<ServiceDataCollection> handleValueList = dismissInsignificantObservations((List<ServiceDataCollection>) serviceDataCollList);
                triples.addAll(simpleContinuousValues(connectionTimeMilli, handleValueList, unitId, serviceType, "double"));
            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {
                triples.addAll(simpleAggContinuousValues((List<ServiceAggDataCollection>) serviceDataCollList, unitId, serviceType, "double"));
            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @handleStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private List<TripleArrayList> illuminanceStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {
        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                List<ServiceDataCollection> illuminanceValueList = dismissInsignificantObservations((List<ServiceDataCollection>) serviceDataCollList);
                triples.addAll(simpleContinuousValues(connectionTimeMilli, illuminanceValueList, unitId, serviceType, "lux"));
            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {
                triples.addAll(simpleAggContinuousValues((List<ServiceAggDataCollection>) serviceDataCollList, unitId, serviceType, "lux"));
            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @illuminanceStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private List<TripleArrayList> powerConsumptionStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {
        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                List<ServiceDataCollection> voltageList = new ArrayList<>();
                List<ServiceDataCollection> wattList = new ArrayList<>();
                List<ServiceDataCollection> ampereList = new ArrayList<>();

                for (final ServiceDataCollection serviceDataColl : (List<ServiceDataCollection>) serviceDataCollList) {
                    final String dataType = OntologyToolkit.getLocalName(serviceDataColl.getStateValue().asLiteral().getDatatypeURI());

                    if (dataType.equalsIgnoreCase("voltage")) {
                        //voltage value
                        voltageList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("watt")) {
                        // watt value
                        wattList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("ampere")) {
                        // ampere value
                        ampereList.add(serviceDataColl);
                    }
                }
                voltageList = dismissInsignificantObservations(voltageList);
                wattList = dismissInsignificantObservations(wattList);
                ampereList = dismissInsignificantObservations(ampereList);
                triples.addAll(simpleContinuousValues(connectionTimeMilli, voltageList, unitId, serviceType, "voltage"));
                triples.addAll(simpleContinuousValues(connectionTimeMilli, wattList, unitId, serviceType, "watt"));
                triples.addAll(simpleContinuousValues(connectionTimeMilli, ampereList, unitId, serviceType, "ampere"));
            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {
                List<ServiceAggDataCollection> voltageList = new ArrayList<>();
                List<ServiceAggDataCollection> wattList = new ArrayList<>();
                List<ServiceAggDataCollection> ampereList = new ArrayList<>();

                for (final ServiceAggDataCollection serviceDataColl : (List<ServiceAggDataCollection>) serviceDataCollList) {
                    final String dataType = serviceDataColl.getStateValue().asLiteral().toString();

                    if (dataType.equalsIgnoreCase("voltage")) {
                        //voltage value
                        voltageList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("watt")) {
                        // watt value
                        wattList.add(serviceDataColl);
                    } else if (dataType.equalsIgnoreCase("ampere")) {
                        // ampere value
                        ampereList.add(serviceDataColl);
                    }
                }
                triples.addAll(simpleAggContinuousValues(voltageList, unitId, serviceType, "brightness"));
                triples.addAll(simpleAggContinuousValues(wattList, unitId, serviceType, "hue"));
                triples.addAll(simpleAggContinuousValues(ampereList, unitId, serviceType, "saturation"));
            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @colorStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private List<TripleArrayList> rfidStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {
        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                List<ServiceDataCollection> rfidValueList = dismissInsignificantObservations((List<ServiceDataCollection>) serviceDataCollList);
            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {

            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("Dropped data @rfidStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }

        return null;//TODO
    }

    private List<TripleArrayList> switchStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {
        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                //TODO 0...1...another calculation of mean?
                List<ServiceDataCollection> switchValueList = dismissInsignificantObservations((List<ServiceDataCollection>) serviceDataCollList);
                triples.addAll(simpleContinuousValues(connectionTimeMilli, switchValueList, unitId, serviceType, "double"));
            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {
                triples.addAll(simpleAggContinuousValues((List<ServiceAggDataCollection>) serviceDataCollList, unitId, serviceType, "double"));

            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @switchStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private List<TripleArrayList> temperatureStateValue(final long connectionTimeMilli, final List<?> serviceDataCollList, final String unitId, final String serviceType) {
        final List<TripleArrayList> triples = new ArrayList<>();

        try {
            if (serviceDataCollList.get(0) instanceof ServiceDataCollection) {
                List<ServiceDataCollection> temperatureValueList = dismissInsignificantObservations((List<ServiceDataCollection>) serviceDataCollList);
                triples.addAll(simpleContinuousValues(connectionTimeMilli, temperatureValueList, unitId, serviceType, "celsius"));
            } else if (serviceDataCollList.get(0) instanceof ServiceAggDataCollection) {
                triples.addAll(simpleAggContinuousValues((List<ServiceAggDataCollection>) serviceDataCollList, unitId, serviceType, "celsius"));
            } else {
                throw new CouldNotPerformException("Could not identify variable type.");
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory("Dropped data @temperatureStateValue ...!", e, LOGGER, LogLevel.ERROR);
        }
        return triples;
    }

    private HashMap<Triple<Integer, Integer, Integer>, Integer> getAggColorValues(final List<ServiceDataCollection> hueList
            , List<ServiceDataCollection> saturationList, final List<ServiceDataCollection> brightnessList) {

        if (hueList.size() != saturationList.size() || hueList.size() != brightnessList.size()) {
            LOGGER.error("List sizes of hue, saturation and brightness are not equal!");
        }

        // sort ascending (old to young)
        Collections.sort(hueList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        Collections.sort(saturationList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        Collections.sort(brightnessList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap = new HashMap<>();

        for (int i = 0; i < hueList.size(); i++) {
            final int hue = (int) (Double.parseDouble(hueList.get(i).getStateValue().asLiteral().getLexicalForm()) / 10);
            final int saturation = (int) (Double.parseDouble(saturationList.get(i).getStateValue().asLiteral().getLexicalForm()) / 10);
            final int brightness = (int) (Double.parseDouble(brightnessList.get(i).getStateValue().asLiteral().getLexicalForm()) / 10);

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
        final String timeWeighting = OntConfig.decimalFormat().format((double) connectionTimeMilli / (double) (dateTimeUntil.getMillis() - dateTimeFrom.getMillis()));
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
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + timeWeighting + "\"^^xsd:double"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + hueValue + "\"^^NS:Hue"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + saturationValue + "\"^^NS:Saturation"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + brightnessValue + "\"^^NS:Brightness"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + quantity + "\"^^xsd:int"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));
        }
        return triples;
    }

    private List<TripleArrayList> simpleDiscreteValues(final long connectionTimeMilli, final List<ServiceDataCollection> discreteList, final String unitId
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
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));
        }
        return triples;
    }

    private List<TripleArrayList> simpleAggDiscreteValues(final List<ServiceAggDataCollection> aggDiscreteList, final String unitId, final String serviceType)
            throws CouldNotPerformException, InterruptedException {

        final List<TripleArrayList> triples = new ArrayList<>();
        final OntConfig.Period toAggregatedPeriod = OntConfig.Period.WEEK; //TODO to aggregated period...
        final DiscreteStateValues discreteStateValues = new DiscreteStateValues(aggDiscreteList, toAggregatedPeriod);

        final HashMap<String, Long> activationTimeMap = discreteStateValues.getActiveTimePerStateValue();
        final HashMap<String, Integer> quantityMap = discreteStateValues.getQuantityPerStateValue();
        final double timeWeighting = discreteStateValues.getTimeWeighting();

        for (final String bcoStateType : activationTimeMap.keySet()) {
            // every stateType has his own aggObs instance!
            final String subj_AggObs = getAggObsInstance(unitId);

            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntExpr.A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), toAggregatedPeriod.toString().toLowerCase()));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), bcoStateType));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + String.valueOf(quantityMap.get(bcoStateType)) + "\"^^xsd:int"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.ACTIVITY_TIME.getName(), "\"" + String.valueOf(activationTimeMap.get(bcoStateType)) + "\"^^xsd:long"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(timeWeighting) + "\"^^xsd:double"));
            triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime")); //TODO generic timestamp...
        }
        return triples;
    }

    private List<TripleArrayList> simpleContinuousValues(final long connectionTimeMilli, final List<ServiceDataCollection> continuousList, final String unitId
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
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));

        return triples;
    }

    private List<TripleArrayList> simpleAggContinuousValues(final List<ServiceAggDataCollection> aggContinuousList, final String unitId
            , final String serviceType, final String dataType) throws CouldNotPerformException, InterruptedException {

        final List<TripleArrayList> triples = new ArrayList<>();
        final OntConfig.Period toAggregatedPeriod = OntConfig.Period.WEEK; //TODO to aggregated period...
        final ContinuousStateValues continuousStateValues = new ContinuousStateValues(aggContinuousList, toAggregatedPeriod);
        final String subj_AggObs = getAggObsInstance(unitId);

        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntExpr.A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.UNIT_ID.getName(), unitId));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PROVIDER_SERVICE.getName(), serviceType));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.PERIOD.getName(), toAggregatedPeriod.toString().toLowerCase()));
        // because of distinction the dataType of the stateValue is attached as literal (property hasStateValue) ...
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STATE_VALUE.getName(), "\"" + dataType + "\"^^xsd:string"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.MEAN.getName(), "\"" + String.valueOf(continuousStateValues.getMean()) + "\"^^xsd:double"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.VARIANCE.getName(), "\"" + String.valueOf(continuousStateValues.getVariance()) + "\"^^xsd:double"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.STANDARD_DEVIATION.getName(), "\"" + String.valueOf(continuousStateValues.getStandardDeviation()) + "\"^^xsd:double"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.QUANTITY.getName(), "\"" + String.valueOf(continuousStateValues.getQuantity()) + "\"^^xsd:int"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(continuousStateValues.getTimeWeighting()) + "\"^^xsd:double"));
        triples.add(new TripleArrayList(subj_AggObs, OntConfig.OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime")); //TODO generic timestamp...

        return triples;
    }

    private String getAggObsInstance(final String unitId) throws InterruptedException {
        // wait one millisecond to guarantee, that aggregationObservation instances are unique
        stopwatch.waitForStop(1);

        final String dateTimeNow = new DateTime().toString();

        return "AggObs" + unitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
    }

    // dismiss all observations below the dateTimeFrom. BESIDES the youngest observation below the dateTimeFrom.
    private List<ServiceDataCollection> dismissInsignificantObservations(final List<ServiceDataCollection> stateValueDataCollectionList) {

        // sort ascending (old to young)
        Collections.sort(stateValueDataCollectionList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        final List<ServiceDataCollection> bufDataList = new ArrayList<>();
        boolean insignificant = true;
        ServiceDataCollection bufData = null;
        final long dateTimeFromMillis = dateTimeFrom.getMillis();

        for (final ServiceDataCollection stateValueDataCollection : stateValueDataCollectionList) {
            final long stateValueMillis = new DateTime(stateValueDataCollection.getTimestamp()).getMillis();

            if (insignificant && stateValueMillis <= dateTimeFromMillis) {
                bufData = stateValueDataCollection;
            } else {
                if (insignificant && bufData != null) {
                    bufDataList.add(bufData);
                    insignificant = false;
                }
                bufDataList.add(stateValueDataCollection);
            }
        }
        return bufDataList;
    }

}
