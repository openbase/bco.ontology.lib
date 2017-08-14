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
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntPrefix;
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
    private final Stopwatch stopwatch;
    private String unitId;
    private long unitConnectionTimeMilli;
    private String serviceType;

    public DataAssignation(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil, final Period currentPeriod) {
        super(dateTimeFrom, dateTimeUntil, currentPeriod);

        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.dateTimeFromMillis = dateTimeFrom.toInstant().toEpochMilli();
        this.stopwatch = new Stopwatch();
    }

    protected List<RdfTriple> identifyServiceType(final HashMap<String, ?> serviceStateChangeMap, final long unitConnectionTimeMilli, final String unitId)
            throws InterruptedException{
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
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case BATTERY_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        triples.addAll(aggregateContinuousStateValue((List<?>) serviceStateChangeMap.get(serviceType), StateValueType.PERCENT));
                        break;
                    case BLIND_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        triples.addAll(aggregateContinuousStateValue((List<?>) serviceStateChangeMap.get(serviceType), StateValueType.PERCENT));
                        break;
                    case BRIGHTNESS_STATE_SERVICE:
                        break;
                    case BUTTON_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case COLOR_STATE_SERVICE:
                        triples.addAll(hsbStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case CONTACT_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case DOOR_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case EARTHQUAKE_ALARM_STATE_SERVICE:
                        break;
                    case FIRE_ALARM_STATE_SERVICE:
                        break;
                    case HANDLE_STATE_SERVICE:
                        triples.addAll(aggregateContinuousStateValue((List<?>) serviceStateChangeMap.get(serviceType), StateValueType.DOUBLE));
                        break;
                    case ILLUMINANCE_STATE_SERVICE:
                        triples.addAll(aggregateContinuousStateValue((List<?>) serviceStateChangeMap.get(serviceType), StateValueType.LUX));
                        break;
                    case INTENSITY_STATE_SERVICE:
                        break;
                    case INTRUSION_ALARM_STATE_SERVICE:
                        break;
                    case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:
                        break;
                    case MOTION_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case PASSAGE_STATE_SERVICE:
                        break;
                    case POWER_CONSUMPTION_STATE_SERVICE:
                        triples.addAll(powerStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case POWER_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case PRESENCE_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case RFID_STATE_SERVICE:
//                    triples.addAll(rfidStateValue(serviceStateChangeMap.get(serviceType)));
                        break;
                    case SMOKE_ALARM_STATE_SERVICE:
                        break;
                    case SMOKE_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        triples.addAll(aggregateContinuousStateValue((List<?>) serviceStateChangeMap.get(serviceType), StateValueType.PERCENT));
                        break;
                    case STANDBY_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case SWITCH_STATE_SERVICE:
                        triples.addAll(aggregateContinuousStateValue((List<?>) serviceStateChangeMap.get(serviceType), StateValueType.DOUBLE));
                        break;
                    case TAMPER_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
                        break;
                    case TARGET_TEMPERATURE_STATE_SERVICE:
                        break;
                    case TEMPERATURE_ALARM_STATE_SERVICE:
                        break;
                    case TEMPERATURE_STATE_SERVICE:
                        triples.addAll(aggregateContinuousStateValue((List<?>) serviceStateChangeMap.get(serviceType), StateValueType.CELSIUS));
                        break;
                    case TEMPEST_ALARM_STATE_SERVICE:
                        break;
                    case WATER_ALARM_STATE_SERVICE:
                        break;
                    case WINDOW_STATE_SERVICE:
                        triples.addAll(aggregateDiscreteStateValue((List<?>) serviceStateChangeMap.get(serviceType)));
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
     * Method collects and calculates the state changes to an aggregated observation. State changes based on discrete state values (/bco state values
     * like on, off, open, ...). The state changes can be not processed observations (OntStateChange) or aggregated observations (OntAggregatedStateChange).
     *
     * @param stateChanges are the discrete state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state changes.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> aggregateDiscreteStateValue(final List<?> stateChanges) throws CouldNotPerformException, InterruptedException {

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> bco = OntNode.getResources((List<OntStateChange>) stateChanges);
            return buildAggObsOfDiscreteValues(dismissUnusedStateValues(bco));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> bco = OntNode.getAggResources((List<OntAggregatedStateChange>) stateChanges);
            return buildAggObsOfDiscreteValues(bco);

        }
        throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
    }

    /**
     * Method collects and calculates the state changes to an aggregated observation. State changes based on continuous state values. The state changes can
     * be not processed observations (OntStateChange) or aggregated observations (OntAggregatedStateChange).
     *
     * @param stateChanges are the continuous state values.
     * @param stateValueType is the kind of state value to filter and attach the information to the aggregation observation.
     * @return rdf triples to insert an aggregated observation, which are calculated from the input state changes.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> aggregateContinuousStateValue(final List<?> stateChanges, final StateValueType stateValueType)
            throws CouldNotPerformException, InterruptedException {

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> stateTypeValue = OntNode.getLiterals((List<OntStateChange>) stateChanges, stateValueType);
            return buildAggObsOfContinuousValue(dismissUnusedStateValues(stateTypeValue), stateValueType);

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> stateTypeValue = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, stateValueType);
            return buildAggObsOfContinuousValue(stateTypeValue, stateValueType);
        }
        throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
    }

    /**
     * Method identifies and aggregates state values based on data type hsb - hue, saturation, brightness (continuous).
     *
     * @param stateChanges are the continuous state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> hsbStateValue(final List<?> stateChanges) throws CouldNotPerformException, InterruptedException {
        final List<RdfTriple> triples = new ArrayList<>();

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> brightness = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.BRIGHTNESS);
            final List<OntStateChange> hue = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.HUE);
            final List<OntStateChange> saturation = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.SATURATION);

            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(hue), StateValueType.HUE));
            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(saturation), StateValueType.SATURATION));
            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(brightness), StateValueType.BRIGHTNESS));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> brightness = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.BRIGHTNESS);
            final List<OntAggregatedStateChange> hue = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.HUE);
            final List<OntAggregatedStateChange> saturation = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.SATURATION);

            triples.addAll(buildAggObsOfContinuousValue(hue, StateValueType.HUE));
            triples.addAll(buildAggObsOfContinuousValue(saturation, StateValueType.SATURATION));
            triples.addAll(buildAggObsOfContinuousValue(brightness, StateValueType.BRIGHTNESS));

        } else {
            throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
        }
        return triples;
    }

    /**
     * Method identifies and aggregates state values based on power - voltage, watt, ampere (continuous).
     *
     * @param stateChanges are the continuous state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state values.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> powerStateValue(final List<?> stateChanges) throws CouldNotPerformException, InterruptedException {
        final List<RdfTriple> triples = new ArrayList<>();

        if (stateChanges.get(0) instanceof OntStateChange) {
            final List<OntStateChange> voltage = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.VOLTAGE);
            final List<OntStateChange> watt = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.WATT);
            final List<OntStateChange> ampere = OntNode.getLiterals((List<OntStateChange>) stateChanges, StateValueType.AMPERE);

            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(voltage), StateValueType.VOLTAGE));
            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(watt), StateValueType.WATT));
            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(ampere), StateValueType.AMPERE));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> voltage = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.VOLTAGE);
            final List<OntAggregatedStateChange> watt = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.WATT);
            final List<OntAggregatedStateChange> ampere = OntNode.getAggLiterals((List<OntAggregatedStateChange>) stateChanges, StateValueType.AMPERE);

            triples.addAll(buildAggObsOfContinuousValue(voltage, StateValueType.VOLTAGE));
            triples.addAll(buildAggObsOfContinuousValue(watt, StateValueType.WATT));
            triples.addAll(buildAggObsOfContinuousValue(ampere, StateValueType.AMPERE));

        } else {
            throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
        }
        return triples;
    }

    private List<RdfTriple> rfidStateValue(final List<?> serviceDataCollList) throws CouldNotPerformException {
        //TODO aggregate string...?!
        final List<RdfTriple> triples = new ArrayList<>();

        if (serviceDataCollList.get(0) instanceof OntStateChange) {
            List<OntStateChange> rfidValueList = dismissUnusedStateValues((List<OntStateChange>) serviceDataCollList);

        } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {

        } else {
            throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
        }
        return triples;
    }

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
            , final String unitId, final String serviceType, final Period nextPeriod) throws InterruptedException {

        final List<RdfTriple> triples = new ArrayList<>();
        final String timeWeighting = OntConfig.decimalFormat().format((double) connectionTimeMilli
                / (double) (dateTimeUntil.toInstant().toEpochMilli() - dateTimeFrom.toInstant().toEpochMilli()));
        final Set<Triple<Integer, Integer, Integer>> hsbSet = hsbCountMap.keySet();

        for (final Triple<Integer, Integer, Integer> hsb : hsbSet) {
            final String aggObs = getAggObsInstanceName(unitId);

            final String hueValue = String.valueOf(hsb.getLeft() * 10);
            final String saturationValue = String.valueOf(hsb.getMiddle() * 10);
            final String brightnessValue = String.valueOf(hsb.getRight() * 10);
            final String quantity = String.valueOf(hsbCountMap.get(hsb));

            triples.add(new RdfTriple(aggObs, OntExpr.IS_A.getName(), OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new RdfTriple(aggObs, OntProp.UNIT_ID.getName(), unitId));
            triples.add(new RdfTriple(aggObs, OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new RdfTriple(aggObs, OntProp.PERIOD.getName(), nextPeriod.toString().toLowerCase()));
            triples.add(new RdfTriple(aggObs, OntProp.TIME_WEIGHTING.getName(), "\"" + timeWeighting + "\"^^xsd:double"));
            triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), "\"" + hueValue + "\"^^NS:Hue"));
            triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), "\"" + saturationValue + "\"^^NS:Saturation"));
            triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), "\"" + brightnessValue + "\"^^NS:Brightness"));
            triples.add(new RdfTriple(aggObs, OntProp.QUANTITY.getName(), "\"" + quantity + "\"^^xsd:int"));
            triples.add(new RdfTriple(aggObs, OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));
        }
        return triples;
    }

    /**
     * Method builds triples to insert aggregated observations. Each agg. observation describes a discrete state value. A discrete state source keeps (maybe)
     * multiple state values like powerState source get the values ON and OFF. Therefore there are multiple aggregated observations, which are set in a loop.
     *
     * @param stateChanges are the discrete state changes.
     * @return a list of triples to insert aggregated observations to the ontology.
     * @throws CouldNotPerformException is thrown in case the input list is empty or the information could not be aggregated.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> buildAggObsOfDiscreteValues(final List<?> stateChanges) throws CouldNotPerformException, InterruptedException {

        if (stateChanges.isEmpty()) {
            throw new CouldNotPerformException("There is no state value. Empty list!");
        }

        final DiscreteStateValues discreteStateValues;

        if (stateChanges.get(0) instanceof OntStateChange) {
            discreteStateValues = new DiscreteStateValues((List<OntStateChange>) stateChanges, unitConnectionTimeMilli);
        } else {
            discreteStateValues = new DiscreteStateValues((List<OntAggregatedStateChange>) stateChanges);
        }

        final HashMap<String, Long> activationTimeMap = discreteStateValues.getActiveTimePerStateValue();
        final HashMap<String, Integer> quantityMap = discreteStateValues.getQuantityPerStateValue();
        final double timeWeighting = discreteStateValues.getTimeWeighting();
        final List<RdfTriple> triples = new ArrayList<>();

        for (final String discreteStateType : activationTimeMap.keySet()) {
            // every aggregated state value has his own aggObs instance! A state source keeps multiple discrete values like a powerState ON and OFF.
            final String aggObs = getAggObsInstanceName(unitId);

            triples.add(new RdfTriple(aggObs, OntExpr.IS_A.getName(), OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new RdfTriple(aggObs, OntProp.UNIT_ID.getName(), unitId));
            triples.add(new RdfTriple(aggObs, OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new RdfTriple(aggObs, OntProp.PERIOD.getName(), discreteStateValues.getNextPeriod().toString().toLowerCase()));
            triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), discreteStateType));
            triples.add(new RdfTriple(aggObs, OntProp.QUANTITY.getName(), "\"" + String.valueOf(quantityMap.get(discreteStateType)) + "\"^^xsd:int"));
            triples.add(new RdfTriple(aggObs, OntProp.ACTIVITY_TIME.getName(), "\"" + String.valueOf(activationTimeMap.get(discreteStateType)) + "\"^^xsd:long"));
            triples.add(new RdfTriple(aggObs, OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(timeWeighting) + "\"^^xsd:double"));
            //TODO optional timestamp...necessary by another aggregation process (time frame)?
//            triples.add(new RdfTriple(aggObs, OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));
        }
        return triples;
    }

    /**
     * Method builds triples to insert an aggregated observation. In this case there is one aggregated observation, because an continuous state source keeps
     * no individual state values (like ON, OFF, ...).
     *
     * @param stateChanges are the continuous state changes.
     * @param stateValueType is the type of the state changes.
     * @return a list of triples to insert aggregated observations to the ontology.
     * @throws CouldNotPerformException is thrown in case the input list is empty, the stateValueType is discrete or the information could not be aggregated.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> buildAggObsOfContinuousValue(final List<?> stateChanges, final StateValueType stateValueType)
            throws CouldNotPerformException, InterruptedException {

        if (stateValueType.equals(StateValueType.BCO_VALUE)) {
            throw new CouldNotPerformException("Could not perform aggregation of continuous values because the state value type is discrete...!");
        }

        if (stateChanges.isEmpty()) {
            throw new CouldNotPerformException("There is no state value of kind " + stateValueType.name() + ".");
        }

        final ContinuousStateValues continuousStateValues;

        if (stateChanges.get(0) instanceof OntStateChange) {
            continuousStateValues = new ContinuousStateValues((List<OntStateChange>) stateChanges, unitConnectionTimeMilli);
        } else {
            continuousStateValues = new ContinuousStateValues((List<OntAggregatedStateChange>) stateChanges);
        }

        final String aggObs = getAggObsInstanceName(unitId);
        final List<RdfTriple> triples = new ArrayList<>();

        triples.add(new RdfTriple(aggObs, OntExpr.IS_A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
        triples.add(new RdfTriple(aggObs, OntProp.UNIT_ID.getName(), unitId));
        triples.add(new RdfTriple(aggObs, OntProp.PROVIDER_SERVICE.getName(), serviceType));
        triples.add(new RdfTriple(aggObs, OntProp.PERIOD.getName(), continuousStateValues.getNextPeriod().toString().toLowerCase()));
        // because of distinction the dataType of the stateValue is attached as literal (property hasStateValue) ...
        triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), "\"" + stateValueType.name() + "\"^^xsd:string"));
        triples.add(new RdfTriple(aggObs, OntProp.MEAN.getName(), "\"" + String.valueOf(continuousStateValues.getMean()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(aggObs, OntProp.VARIANCE.getName(), "\"" + String.valueOf(continuousStateValues.getVariance()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(aggObs, OntProp.STANDARD_DEVIATION.getName(), "\"" + String.valueOf(continuousStateValues.getStandardDeviation()) + "\"^^xsd:double"));
        triples.add(new RdfTriple(aggObs, OntProp.QUANTITY.getName(), "\"" + String.valueOf(continuousStateValues.getQuantity()) + "\"^^xsd:int"));
        triples.add(new RdfTriple(aggObs, OntProp.TIME_WEIGHTING.getName(), "\"" + String.valueOf(continuousStateValues.getTimeWeighting()) + "\"^^xsd:double"));
        //TODO optional timestamp...necessary by another aggregation process (time frame)?
//        triples.add(new RdfTriple(aggObs, OntProp.TIME_STAMP.getName(), "\"" + dateTimeFrom.toString() + "\"^^xsd:dateTime"));

        return triples;
    }

    /**
     * Method returns the instance name of the aggregation observation based on the unit id and the current time.
     *
     * @param unitId is the unit id.
     * @return the instance name of the aggregation observation.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private String getAggObsInstanceName(final String unitId) throws InterruptedException {
        // wait one millisecond to guarantee, that aggregationObservation instances are unique
        stopwatch.waitForStop(1);

        final String dateTimeNow = OffsetDateTime.now().toString();
        return OntPrefix.AGGREGATION_OBSERVATION + unitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
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
