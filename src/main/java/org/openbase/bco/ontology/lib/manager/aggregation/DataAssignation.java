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

import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntAggregatedStateChange;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChange;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChangeBuf;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.ontology.OntNodeHandler;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntPrefix;
import org.openbase.bco.ontology.lib.system.config.OntConfig.StateValueType;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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

    public DataAssignation(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil, final Period currentPeriod) throws CouldNotPerformException {
        super(dateTimeFrom, dateTimeUntil, currentPeriod);

        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.dateTimeFromMillis = dateTimeFrom.toInstant().toEpochMilli();
        this.stopwatch = new Stopwatch();
    }

    /**
     * Method identifies the based service type and relates associated aggregation processes to get ontology triples for aggregation observations. This method
     * call based on unit (id) level. Incorrect calculations are dropped and associated exceptions are logged.
     *
     * @param providerService is the providerService.
     * @param ontStateChanges are the associated state changes.
     * @param unitConnectionTimeMilli is the whole connection time in milliseconds of the current unit. If the state changes based on aggregated observations
     *                                the value will be ignored. Instead the value of the aggregated observations are taken in DataAggregation.
     * @param unitId is the id of the current based unit, which contains the input services and state changes.
     * @return a list of triples to insert the aggregation observation in the ontology.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    List<RdfTriple> identifyServiceType(final String providerService, final List<OntStateChange> ontStateChanges,
                                        final long unitConnectionTimeMilli, final String unitId) throws InterruptedException {

        this.unitId = unitId;
        this.unitConnectionTimeMilli = unitConnectionTimeMilli;
        this.serviceType = providerService;
        final List<RdfTriple> triples = new ArrayList<>();
        ExceptionStack exceptionStack = null;

        try {
            switch (OntConfig.SERVICE_NAME_MAP.get(StringModifier.firstCharToLowerCase(serviceType))) {
                case ACTIVATION_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case BATTERY_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    triples.addAll(aggregateContinuousStateValue(ontStateChanges, StateValueType.PERCENT));
                    break;
                case BLIND_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    triples.addAll(aggregateContinuousStateValue(ontStateChanges, StateValueType.PERCENT));
                    break;
                case BRIGHTNESS_STATE_SERVICE:
                    break;
                case BUTTON_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case COLOR_STATE_SERVICE:
                    triples.addAll(hsbStateValue(ontStateChanges));
                    break;
                case CONTACT_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case DOOR_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case EARTHQUAKE_ALARM_STATE_SERVICE:
                    break;
                case FIRE_ALARM_STATE_SERVICE:
                    break;
                case HANDLE_STATE_SERVICE:
                    triples.addAll(aggregateContinuousStateValue(ontStateChanges, StateValueType.DOUBLE));
                    break;
                case ILLUMINANCE_STATE_SERVICE:
                    triples.addAll(aggregateContinuousStateValue(ontStateChanges, StateValueType.LUX));
                    break;
                case INTENSITY_STATE_SERVICE:
                    break;
                case INTRUSION_ALARM_STATE_SERVICE:
                    break;
                case MEDICAL_EMERGENCY_ALARM_STATE_SERVICE:
                    break;
                case MOTION_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case PASSAGE_STATE_SERVICE:
                    break;
                case POWER_CONSUMPTION_STATE_SERVICE:
                    triples.addAll(powerStateValue(ontStateChanges));
                    break;
                case POWER_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case PRESENCE_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case RFID_STATE_SERVICE:
//                    triples.addAll(rfidStateValue(serviceStateChangeMap.get(serviceType)));
                    break;
                case SMOKE_ALARM_STATE_SERVICE:
                    break;
                case SMOKE_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    triples.addAll(aggregateContinuousStateValue(ontStateChanges, StateValueType.PERCENT));
                    break;
                case STANDBY_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case SWITCH_STATE_SERVICE:
                    triples.addAll(aggregateContinuousStateValue(ontStateChanges, StateValueType.DOUBLE));
                    break;
                case TAMPER_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case TARGET_TEMPERATURE_STATE_SERVICE:
                    break;
                case TEMPERATURE_ALARM_STATE_SERVICE:
                    break;
                case TEMPERATURE_STATE_SERVICE:
                    triples.addAll(aggregateContinuousStateValue(ontStateChanges, StateValueType.CELSIUS));
                    break;
                case TEMPEST_ALARM_STATE_SERVICE:
                    break;
                case WATER_ALARM_STATE_SERVICE:
                    break;
                case WINDOW_STATE_SERVICE:
                    triples.addAll(aggregateDiscreteStateValue(ontStateChanges));
                    break;
                case UNKNOWN:
                    // invalid service state
                    throw new NotAvailableException("Could not assign to providerService UNKNOWN");
                default:
                    // no matched providerService
                    throw new NotAvailableException("Could not assign to providerService. Add" + OntConfig.SERVICE_NAME_MAP.get(serviceType));
            }
        } catch (CouldNotPerformException ex) {
            exceptionStack = MultiException.push(this, ex, exceptionStack);
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
     * like on, off, open, ...). The state changes can be not processed observations (OntStateChangeBuf) or aggregated observations (OntAggregatedStateChange).
     *
     * @param stateChanges are the discrete state values.
     * @return rdf triples to insert aggregated information which are calculated from the input state changes.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> aggregateDiscreteStateValue(final List<?> stateChanges) throws CouldNotPerformException, InterruptedException {

        //TODO replace data type!!!
        if (stateChanges.get(0) instanceof OntStateChangeBuf) {
            final List<OntStateChangeBuf> bco = OntNodeHandler.getResourceElements((List<OntStateChangeBuf>) stateChanges);
            return buildAggObsOfDiscreteValues(dismissUnusedStateValues(bco));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> bco = OntNodeHandler.getAggResourceElements((List<OntAggregatedStateChange>) stateChanges);
            return buildAggObsOfDiscreteValues(bco);

        }
        throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
    }

    /**
     * Method collects and calculates the state changes to an aggregated observation. State changes based on continuous state values. The state changes can
     * be not processed observations (OntStateChangeBuf) or aggregated observations (OntAggregatedStateChange).
     *
     * @param stateChanges are the continuous state values.
     * @param stateValueType is the kind of state value to filter and attach the information to the aggregation observation.
     * @return rdf triples to insert an aggregated observation, which are calculated from the input state changes.
     * @throws CouldNotPerformException is thrown in case the parameter type is unknown.
     * @throws InterruptedException is thrown in case the application was interrupted.
     */
    private List<RdfTriple> aggregateContinuousStateValue(final List<?> stateChanges, final StateValueType stateValueType)
            throws CouldNotPerformException, InterruptedException {

        if (stateChanges.get(0) instanceof OntStateChangeBuf) {
            final List<OntStateChangeBuf> stateTypeValue = OntNodeHandler.getLiteralElements((List<OntStateChangeBuf>) stateChanges, stateValueType);
            return buildAggObsOfContinuousValue(dismissUnusedStateValues(stateTypeValue), stateValueType);

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> stateTypeValue = OntNodeHandler.getAggLiteralElements((List<OntAggregatedStateChange>) stateChanges, stateValueType);
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

        if (stateChanges.get(0) instanceof OntStateChangeBuf) {
            final List<OntStateChangeBuf> brightness = OntNodeHandler.getLiteralElements((List<OntStateChangeBuf>) stateChanges, StateValueType.BRIGHTNESS);
            final List<OntStateChangeBuf> hue = OntNodeHandler.getLiteralElements((List<OntStateChangeBuf>) stateChanges, StateValueType.HUE);
            final List<OntStateChangeBuf> saturation = OntNodeHandler.getLiteralElements((List<OntStateChangeBuf>) stateChanges, StateValueType.SATURATION);

            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(hue), StateValueType.HUE));
            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(saturation), StateValueType.SATURATION));
            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(brightness), StateValueType.BRIGHTNESS));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> brightness = OntNodeHandler.getAggLiteralElements((List<OntAggregatedStateChange>) stateChanges, StateValueType.BRIGHTNESS);
            final List<OntAggregatedStateChange> hue = OntNodeHandler.getAggLiteralElements((List<OntAggregatedStateChange>) stateChanges, StateValueType.HUE);
            final List<OntAggregatedStateChange> saturation = OntNodeHandler.getAggLiteralElements((List<OntAggregatedStateChange>) stateChanges, StateValueType.SATURATION);

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

        if (stateChanges.get(0) instanceof OntStateChangeBuf) {
            final List<OntStateChangeBuf> voltage = OntNodeHandler.getLiteralElements((List<OntStateChangeBuf>) stateChanges, StateValueType.VOLTAGE);
            final List<OntStateChangeBuf> watt = OntNodeHandler.getLiteralElements((List<OntStateChangeBuf>) stateChanges, StateValueType.WATT);
            final List<OntStateChangeBuf> ampere = OntNodeHandler.getLiteralElements((List<OntStateChangeBuf>) stateChanges, StateValueType.AMPERE);

            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(voltage), StateValueType.VOLTAGE));
            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(watt), StateValueType.WATT));
            triples.addAll(buildAggObsOfContinuousValue(dismissUnusedStateValues(ampere), StateValueType.AMPERE));

        } else if (stateChanges.get(0) instanceof OntAggregatedStateChange) {
            final List<OntAggregatedStateChange> voltage = OntNodeHandler.getAggLiteralElements((List<OntAggregatedStateChange>) stateChanges, StateValueType.VOLTAGE);
            final List<OntAggregatedStateChange> watt = OntNodeHandler.getAggLiteralElements((List<OntAggregatedStateChange>) stateChanges, StateValueType.WATT);
            final List<OntAggregatedStateChange> ampere = OntNodeHandler.getAggLiteralElements((List<OntAggregatedStateChange>) stateChanges, StateValueType.AMPERE);

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

        if (serviceDataCollList.get(0) instanceof OntStateChangeBuf) {
            List<OntStateChangeBuf> rfidValueList = dismissUnusedStateValues((List<OntStateChangeBuf>) serviceDataCollList);

        } else if (serviceDataCollList.get(0) instanceof OntAggregatedStateChange) {

        } else {
            throw new CouldNotPerformException("Could not identify parameter type. Dropped data...");
        }
        return triples;
    }

//    private HashMap<Triple<Integer, Integer, Integer>, Integer> getAggColorValues(final List<OntStateChangeBuf> hueList
//            , List<OntStateChangeBuf> saturationList, final List<OntStateChangeBuf> brightnessList) { //TODO
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

//    private List<RdfTriple> getColorTriple(final long connectionTimeMilli, final HashMap<Triple<Integer, Integer, Integer>, Integer> hsbCountMap
//            , final String unitId, final String serviceType, final Period nextPeriod) throws InterruptedException, NotAvailableException {
//
//        final List<RdfTriple> triples = new ArrayList<>();
//        final String timeWeighting = OntConfig.decimalFormat().format((double) connectionTimeMilli
//                / (double) (dateTimeUntil.toInstant().toEpochMilli() - dateTimeFrom.toInstant().toEpochMilli()));
//        final Set<Triple<Integer, Integer, Integer>> hsbSet = hsbCountMap.keySet();
//
//        for (final Triple<Integer, Integer, Integer> hsb : hsbSet) {
//            final String aggObs = getAggObsInstanceName(unitId);
//
//            final String hueValue = String.valueOf(hsb.getLeft() * 10);
//            final String saturationValue = String.valueOf(hsb.getMiddle() * 10);
//            final String brightnessValue = String.valueOf(hsb.getRight() * 10);
//            final String quantity = String.valueOf(hsbCountMap.get(hsb));
//
//            triples.add(new RdfTriple(aggObs, OntExpr.IS_A.getName(), OntCl.AGGREGATION_OBSERVATION.getName()));
//            triples.add(new RdfTriple(aggObs, OntProp.UNIT_ID.getName(), unitId));
//            triples.add(new RdfTriple(aggObs, OntProp.PROVIDER_SERVICE.getName(), serviceType));
//            triples.add(new RdfTriple(aggObs, OntProp.PERIOD.getName(), nextPeriod.toString().toLowerCase()));
//            triples.add(new RdfTriple(aggObs, OntProp.TIME_WEIGHTING.getName(), StringModifier.convertToLiteral(timeWeighting, XsdType.DOUBLE)));
//            triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), "\"" + hueValue + "\"^^NS:Hue"));
//            triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), "\"" + saturationValue + "\"^^NS:Saturation"));
//            triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), "\"" + brightnessValue + "\"^^NS:Brightness"));
//            triples.add(new RdfTriple(aggObs, OntProp.QUANTITY.getName(), StringModifier.convertToLiteral(quantity, XsdType.INT)));
//            triples.add(new RdfTriple(aggObs, OntProp.TIME_STAMP.getName(), StringModifier.convertToLiteral(dateTimeFrom.toString(), XsdType.DATE_TIME)));
//        }
//        return triples;
//    }

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

        if (stateChanges.get(0) instanceof OntStateChangeBuf) {
            discreteStateValues = new DiscreteStateValues((List<OntStateChangeBuf>) stateChanges, unitConnectionTimeMilli);
        } else {
            discreteStateValues = new DiscreteStateValues((List<OntAggregatedStateChange>) stateChanges);
        }

        final HashMap<String, Long> activationTimeMap = discreteStateValues.getActiveTimes();
        final HashMap<String, Integer> quantityMap = discreteStateValues.getQuantities();
        final double timeWeighting = discreteStateValues.getUnitTimeWeighting();
        final List<RdfTriple> triples = new ArrayList<>();

        for (final String discreteStateType : activationTimeMap.keySet()) {
            // every aggregated state value has his own aggObs instance! A state source keeps multiple discrete values like a powerState ON and OFF.
            final String aggObs = getAggObsInstanceName(unitId);
            // ontology resources
            triples.add(new RdfTriple(aggObs, OntExpr.IS_A.getName(), OntCl.AGGREGATION_OBSERVATION.getName()));
            triples.add(new RdfTriple(aggObs, OntProp.UNIT_ID.getName(), unitId));
            triples.add(new RdfTriple(aggObs, OntProp.PROVIDER_SERVICE.getName(), serviceType));
            triples.add(new RdfTriple(aggObs, OntProp.PERIOD.getName(), discreteStateValues.getNextPeriod().toString().toLowerCase()));
            triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), discreteStateType));
            // ontology literals
            triples.add(new RdfTriple(aggObs, OntProp.QUANTITY.getName(), StringModifier.convertToLiteral(quantityMap.get(discreteStateType), XsdType.INT)));
            triples.add(new RdfTriple(aggObs, OntProp.ACTIVITY_TIME.getName(), StringModifier.convertToLiteral(activationTimeMap.get(discreteStateType), XsdType.LONG)));
            triples.add(new RdfTriple(aggObs, OntProp.TIME_WEIGHTING.getName(), StringModifier.convertToLiteral(timeWeighting, XsdType.DOUBLE)));
            //TODO optional timestamp...necessary by another aggregation process (time frame)?
//            triples.add(new RdfTriple(aggObs, OntProp.TIME_STAMP.getName(), StringModifier.convertToLiteral(dateTimeFrom.toString(), XsdType.DATE_TIME)));
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

        if (stateChanges.get(0) instanceof OntStateChangeBuf) {
            continuousStateValues = new ContinuousStateValues((List<OntStateChangeBuf>) stateChanges, unitConnectionTimeMilli);
        } else {
            continuousStateValues = new ContinuousStateValues((List<OntAggregatedStateChange>) stateChanges);
        }

        final String aggObs = getAggObsInstanceName(unitId);
        final List<RdfTriple> triples = new ArrayList<>();
        // ontology resources
        triples.add(new RdfTriple(aggObs, OntExpr.IS_A.getName(), OntConfig.OntCl.AGGREGATION_OBSERVATION.getName()));
        triples.add(new RdfTriple(aggObs, OntProp.UNIT_ID.getName(), unitId));
        triples.add(new RdfTriple(aggObs, OntProp.PROVIDER_SERVICE.getName(), serviceType));
        triples.add(new RdfTriple(aggObs, OntProp.PERIOD.getName(), continuousStateValues.getNextPeriod().toString().toLowerCase()));
        // ontology literals
        triples.add(new RdfTriple(aggObs, OntProp.STATE_VALUE.getName(), StringModifier.convertToLiteral(stateValueType.name(), XsdType.STRING)));
        triples.add(new RdfTriple(aggObs, OntProp.MEAN.getName(), StringModifier.convertToLiteral(continuousStateValues.getMean(), XsdType.DOUBLE)));
        triples.add(new RdfTriple(aggObs, OntProp.VARIANCE.getName(), StringModifier.convertToLiteral(continuousStateValues.getVariance(), XsdType.DOUBLE)));
        triples.add(new RdfTriple(aggObs, OntProp.STANDARD_DEVIATION.getName(), StringModifier.convertToLiteral(continuousStateValues.getStandardDeviation(), XsdType.DOUBLE)));
        triples.add(new RdfTriple(aggObs, OntProp.QUANTITY.getName(), StringModifier.convertToLiteral(continuousStateValues.getQuantity(), XsdType.INT)));
        triples.add(new RdfTriple(aggObs, OntProp.TIME_WEIGHTING.getName(), StringModifier.convertToLiteral(continuousStateValues.getTimeWeighting(), XsdType.DOUBLE)));
        //TODO optional timestamp...necessary by another aggregation process (time frame)?
//        triples.add(new RdfTriple(aggObs, OntProp.TIME_STAMP.getName(), StringModifier.convertToLiteral(dateTimeFrom.toString(), XsdType.DATE_TIME)));

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
    private List<OntStateChangeBuf> dismissUnusedStateValues(final List<OntStateChangeBuf> stateChanges) {
        // sort ascending (old to young)
        stateChanges.sort(Comparator.comparing(OntStateChangeBuf::getTimestamp));

        final List<OntStateChangeBuf> bufDataList = new ArrayList<>();
        OntStateChangeBuf bufData = null;

        for (final OntStateChangeBuf stateChange : stateChanges) {
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
