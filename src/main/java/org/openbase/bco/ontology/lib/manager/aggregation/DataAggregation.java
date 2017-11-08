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

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntAggregatedStateChange;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChangeBuf;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * @author agatting on 25.03.17.
 */
public class DataAggregation {

    private OffsetDateTime dateTimeFrom;
    private OffsetDateTime dateTimeUntil;
    private final Period currentPeriod;
    private long timeFrameMilliS;

    public DataAggregation(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil, final Period currentPeriod) throws CouldNotPerformException  {

        if (currentPeriod == null) {
            throw new CouldNotPerformException("Could not perform aggregation of aggregated data, because current period is null!");
        }

        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.currentPeriod = currentPeriod;
        this.timeFrameMilliS = dateTimeUntil.toInstant().toEpochMilli() - dateTimeFrom.toInstant().toEpochMilli();
    }

    protected class DiscreteStateValues {

        private final double unitTimeWeighting;
        private final Period nextPeriod;
        // the active time in milliseconds for each state value (e.g. ON)
        private final HashMap<String, Long> activeTimeMap = new HashMap<>();
        // the quantity of activation for each state value
        private final HashMap<String, Integer> quantityMap = new HashMap<>();

        public DiscreteStateValues(final List<OntStateChangeBuf> stateChanges, final long unitConnectionTime) throws CouldNotPerformException {
            this.unitTimeWeighting = calcTimeWeighting(unitConnectionTime);
            this.nextPeriod = Period.DAY;

            computeMetadata(preparingStateChanges(unitConnectionTime, stateChanges));
        }

        public DiscreteStateValues(final List<OntAggregatedStateChange> stateChanges) throws CouldNotPerformException {
            this.unitTimeWeighting = calcTimeWeighting(getTimeWeightingArray(stateChanges), getPeriodLength(currentPeriod));
            this.nextPeriod = setNextPeriod();

            computeAggregatedMetadata(stateChanges);
        }

        /**
         * Getter for statistical information: unit connection time weighting. A ratio of the connection time of the unit and the aggregation time frame.
         *
         * @return a value of range 0..1 with 0: zero connection and 1: full connection of the unit in the aggregation time frame.
         */
        public double getUnitTimeWeighting() {
            return unitTimeWeighting;
        }

        /**
         * Getter for aggregation period.
         *
         * @return the aggregation period.
         */
        public Period getNextPeriod() {
            return nextPeriod;
        }

        /**
         * Getter for statistical information: active time (value) for each state value (key).
         *
         * @return a map with a active time in milliseconds for each state value.
         */
        public HashMap<String, Long> getActiveTimes() {
            return activeTimeMap;
        }

        /**
         * Getter for statistical information: quantity (value) for each state value (key).
         *
         * @return a map with a quantity value for each state value.
         */
        public HashMap<String, Integer> getQuantities() {
            return quantityMap;
        }

        /**
         * Method computes all metadata (aggregation components) of the input discrete state changes.
         * Via iteration over the input state change list, the metadata are collected like the quantity for each individual state value.
         * More metadata should be implemented here if it based on the state changes explicitly.
         *
         * @param stateChanges are the state changes to compute the aggregated metadata.
         * @throws NotAvailableException is thrown in case the state change could not be transformed into a string.
         */
        private void computeMetadata(final List<OntStateChangeBuf> stateChanges) throws NotAvailableException {
            // special case: there is no new state change. The single list entry is the state change before the aggregation time frame
            if (stateChanges.size() == 1) {
                final String stateValue = StringModifier.getLocalName(stateChanges.get(0).getStateValues().get(0).asResource().toString());
                // add metadata solution to the hash maps
                registerActiveTime(stateValue, timeFrameMilliS);
                registerQuantity(stateValue, 0); // old state change so that quantity is zero
                return;
            }

            long currentTimestampMilliS = 0;
            long nextTimestampMilliS = 0;
            long stateValueTimeMilliS;

            // principle: for each current state change (value) the time borders are taken. Means the current state change (i) and the timestamp of the next 
            // state change (i + 1)
            for (int i = 0; i < stateChanges.size(); i++) {
                // consider border cases, which are represented by the aggregation time frame (from and until)
                if (i == 0) {
                    nextTimestampMilliS = OffsetDateTime.parse(stateChanges.get(i + 1).getTimestamp()).toInstant().toEpochMilli();
                    stateValueTimeMilliS = nextTimestampMilliS - dateTimeFrom.toInstant().toEpochMilli();
                } else if (i == stateChanges.size() - 1) {
                    stateValueTimeMilliS = dateTimeUntil.toInstant().toEpochMilli() - currentTimestampMilliS;
                } else {
                    nextTimestampMilliS = OffsetDateTime.parse(stateChanges.get(i + 1).getTimestamp()).toInstant().toEpochMilli();
                    stateValueTimeMilliS = nextTimestampMilliS - currentTimestampMilliS;
                }

                currentTimestampMilliS = nextTimestampMilliS;
                // discrete state changes contains one value only
                final String stateValue = StringModifier.getLocalName(stateChanges.get(i).getStateValues().get(0).asResource().toString());

                // add metadata solution of this loop pass to the hash maps
                registerActiveTime(stateValue, stateValueTimeMilliS);
                registerQuantity(stateValue, 1);
            }
        }

        /**
         * Method computes all metadata (aggregation components) of the input aggregated, discrete state changes.
         * Via iteration over the input state change list, the metadata are collected like the quantity for each individual state value.
         * More metadata should be implemented here if it based on aggregated state changes explicitly.
         *
         * @param stateChanges are the aggregated state changes, which should be aggregated again.
         * @throws NotAvailableException is thrown in case the state change could not be transformed into a string.
         */
        private void computeAggregatedMetadata(final List<OntAggregatedStateChange> stateChanges) throws NotAvailableException {

            for (final OntAggregatedStateChange stateChange : stateChanges) {
                // get the metadata of the state change
                final String stateValue = StringModifier.getLocalName(stateChange.getStateValue().asResource().toString());
                final long activeTime = Long.parseLong(stateChange.getActivityTime());
                final int quantity = Integer.parseInt(stateChange.getQuantity());

                // add metadata solution of this loop pass to the hash maps
                registerActiveTime(stateValue, activeTime);
                registerQuantity(stateValue, quantity);
            }
        }

        /**
         * Method registers the results of time to the active time hashMap.
         *
         * @param stateValueKey is the individual state value.
         * @param activeTimeVal is the active time of the state value in milliseconds.
         */
        private void registerActiveTime(final String stateValueKey, final long activeTimeVal) {
            if (activeTimeMap.containsKey(stateValueKey)) {
                // there is an entry: add data
                final long totalTime = activeTimeMap.get(stateValueKey) + activeTimeVal;
                activeTimeMap.put(stateValueKey, totalTime);
            } else {
                // there is no entry: put data
                activeTimeMap.put(stateValueKey, activeTimeVal);
            }
        }

        /**
         * Method registers the quantity to the quantity hashMap. Existing counting is added.
         *
         * @param stateValueKey is the individual state value.
         * @param quantityValue is the number, which should be added to the quantity counter of the input stateValue.
         */
        private void registerQuantity(final String stateValueKey, final int quantityValue) {
            if (quantityMap.containsKey(stateValueKey)) {
                // there is an entry: add data
                final int totalQuantity = quantityMap.get(stateValueKey) + quantityValue;
                quantityMap.put(stateValueKey, totalQuantity);
            } else {
                // there is no entry: put data
                quantityMap.put(stateValueKey, quantityValue);
            }
        }

    }

    protected class ContinuousStateValues {
        //TODO first state change before time frame necessary @ continuous state values?
        private final double mean;
        private final double variance;
        private final double standardDeviation;
        private final double timeWeighting;
        private final int quantity;
        private final Period nextPeriod;

        public ContinuousStateValues(List<OntStateChangeBuf> stateChanges, final long unitConnectionTime) throws CouldNotPerformException {
            stateChanges = preparingStateChanges(unitConnectionTime, stateChanges);

            final List<String> stateValuesString = getStateValues(stateChanges);
            final List<Double> stateValuesDouble = convertStringToDouble(stateValuesString);
            final double stateValuesArray[] = convertToArray(stateValuesDouble);

            this.mean = calcMean(stateValuesArray);
            this.variance = calcVariance(stateValuesArray);
            this.standardDeviation = calcStandardDeviation(stateValuesArray);
            this.timeWeighting = calcTimeWeighting(unitConnectionTime);
            this.quantity = calcQuantity(stateValuesString);
            this.nextPeriod = Period.DAY;
        }

        public ContinuousStateValues(final List<OntAggregatedStateChange> stateChanges) throws CouldNotPerformException {
            if (currentPeriod == null) {
                throw new CouldNotPerformException("Could not perform aggregation of aggregated data, because current period is null!");
            }

            this.mean = calcMean(getMeanList(stateChanges));
            this.variance = calcVariance(getVarianceList(stateChanges));
            this.standardDeviation = calcStandardDeviation(getStandardDeviationList(stateChanges));
            this.timeWeighting = calcTimeWeighting(getTimeWeightingArray(stateChanges), getPeriodLength(currentPeriod));
            this.quantity = calcQuantity(getQuantity(stateChanges));
            this.nextPeriod = setNextPeriod();
        }

        public Period getNextPeriod() {
            return nextPeriod;
        }

        public double getMean() {
            return mean;
        }

        public double getVariance() {
            return variance;
        }

        public double getStandardDeviation() {
            return standardDeviation;
        }

        public double getTimeWeighting() {
            return timeWeighting;
        }

        public int getQuantity() {
            return quantity;
        }

        private double[] getMeanList(final List<OntAggregatedStateChange> aggDataList) throws CouldNotPerformException {
            final List<String> aggMeanBuf = aggDataList.stream().map(OntAggregatedStateChange::getMean).collect(Collectors.toList());
            return convertToArray(convertStringToDouble(aggMeanBuf));
        }

        private double[] getVarianceList(final List<OntAggregatedStateChange> aggDataList) throws CouldNotPerformException {
            final List<String> aggVarianceBuf = aggDataList.stream().map(OntAggregatedStateChange::getVariance).collect(Collectors.toList());
            return convertToArray(convertStringToDouble(aggVarianceBuf));
        }

        private double[] getStandardDeviationList(final List<OntAggregatedStateChange> aggDataList) throws CouldNotPerformException {
            final List<String> aggStandardDeviationBuf = aggDataList.stream().map(OntAggregatedStateChange::getStandardDeviation).collect(Collectors.toList());
            return convertToArray(convertStringToDouble(aggStandardDeviationBuf));
        }

        private List<Integer> getQuantity(final List<OntAggregatedStateChange> aggDataList) throws CouldNotPerformException {
            final List<String> aggQuantityBuf = aggDataList.stream().map(OntAggregatedStateChange::getQuantity).collect(Collectors.toList());
            return convertStringToInteger(aggQuantityBuf);
        }

        private List<String> getStateValues(final List<OntStateChangeBuf> stateValueDataCollectionList) throws NotAvailableException {
            return stateValueDataCollectionList.stream().map(ontStateChangeBuf -> {
                try {
                    return StringModifier.getLocalName(ontStateChangeBuf.getStateValues().get(0).asLiteral().getLexicalForm()); //TODO extend to list...
                } catch (NotAvailableException ex) {
                    return ""; //TODO
                }
            }).collect(Collectors.toList());
        }

    }

    /**
     * Method is used to check and prepare the input information, which should be aggregated.
     *
     * @param unitConnectionTime is the time, which describes the connection time in milliseconds between unit and bco. Inconspicuous connection states should
     *                           have connection times equal the time frame of aggregation.
     * @param stateChanges are the state changes, which should be sorted ascending by including timestamp.
     * @return the sorted list of state changes.
     * @throws MultiException is thrown in case the verification of input information, which should be aggregated, is invalid.
     */
    private List<OntStateChangeBuf> preparingStateChanges(final long unitConnectionTime, final List<OntStateChangeBuf> stateChanges) throws MultiException {
        MultiException.ExceptionStack exceptionStack = null;

        try {
            if (unitConnectionTime > timeFrameMilliS) {
                throw new VerificationFailedException("The unitConnectionTime is bigger than the time frame of aggregation!");
            }
        } catch (VerificationFailedException e) {
            exceptionStack = MultiException.push(this, e, null);
        }
        try {
            if (stateChanges.isEmpty()) {
                throw new VerificationFailedException("The list of state changes is empty!");
            }
        } catch (VerificationFailedException e) {
            exceptionStack = MultiException.push(this, e, exceptionStack);
        }
        try {
            if (OffsetDateTime.parse(stateChanges.get(0).getTimestamp()).isAfter(dateTimeFrom)) {
                throw new VerificationFailedException("First state change is after the beginning aggregation time frame! No information about the state in " +
                        "the beginning time frame! First state change entry should be, chronological, before/equal the beginning time frame.");
            }
        } catch (VerificationFailedException e) {
            exceptionStack = MultiException.push(this, e, exceptionStack);
        }

        MultiException.checkAndThrow("Could not perform aggregation!", exceptionStack);

        // sort ascending (old to young)
        stateChanges.sort(Comparator.comparing(OntStateChangeBuf::getTimestamp));

        return stateChanges;
    }

    private Period setNextPeriod() {
        switch (currentPeriod) {
            case DAY:
                return Period.WEEK;
            case WEEK:
                return Period.MONTH;
            case MONTH:
                return Period.YEAR;
            default:
                return null; //TODO
        }
    }

    private int getPeriodLength(final OntConfig.Period period) throws NotAvailableException {
        //TODO develop logic unit to get the real number dependent on current moment ...
        switch (period) {
            case DAY:
                return 24;
            case WEEK:
                return 7;
            case MONTH:
                return 4;
            case YEAR:
                return 12;
            default:
                throw new NotAvailableException("Could not perform adaption of dateTime for aggregation. Cause period time "
                        + period.toString() + " could not be identified!");
        }
    }

    private double[] getTimeWeightingArray(final List<OntAggregatedStateChange> aggDataList) throws CouldNotPerformException {
        final List<String> aggQuantityBuf = aggDataList.stream().map(OntAggregatedStateChange::getTimeWeighting).collect(Collectors.toList());
        return convertToArray(convertStringToDouble(aggQuantityBuf));
    }

    private List<Double> convertStringToDouble(final List<String> stringValues) throws CouldNotPerformException {
        try {
            return stringValues.stream().map(Double::parseDouble).collect(Collectors.toList());
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not perform aggregation because stateValueList contains discrete values: " + stringValues);
        }
    }

    private List<Integer> convertStringToInteger(final List<String> stringValues) throws CouldNotPerformException {
        try {
            return stringValues.stream().map(Integer::parseInt).collect(Collectors.toList());
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not perform aggregation because stateValueList contains discrete values: " + stringValues);
        }
    }

    /**
     * Method calculates the time weighting of a connection time. Means a value, which describes the ratio of connection time and period time. If an unit has
     * connection the whole period the value is 1. If the unit has connection half of the period time the value is 0.5. The range is [0..1].
     *
     * @param unitConnectionTime is the connection time of the unit.
     * @return the time weighting in the range of [0..1].
     */
    private double calcTimeWeighting(final long unitConnectionTime) {
        return Double.parseDouble(OntConfig.decimalFormat().format((double) unitConnectionTime / (double) timeFrameMilliS));
    }

    private double calcTimeWeighting(final double[] timeWeightingArray, final int periodLength) {
        return DoubleStream.of(timeWeightingArray).sum() / periodLength;
    }

    private double calcVariance(final double stateValuesArray[]) {
        return StatUtils.variance(stateValuesArray);
    }

    private double calcStandardDeviation(final double stateValuesArray[]) {
        return FastMath.sqrt(StatUtils.variance(stateValuesArray));
    }

    private double calcMean(final double stateValuesArray[]) {
        return StatUtils.mean(stateValuesArray);
    }

    private int calcQuantity(final List<?> stateValues) {
        return stateValues.size();
    }

    private double[] convertToArray(final List<Double> stateValues) {
        final double stateValuesArray[] = new double[stateValues.size()];

        for (int i = 0; i < stateValues.size(); i++) {
            stateValuesArray[i] = stateValues.get(i);
        }

        return stateValuesArray;
    }

}
