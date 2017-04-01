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

import javafx.util.Pair;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.StateValueWithTimestamp;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ValueConfidenceRange;
import org.openbase.jul.exception.CouldNotPerformException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * @author agatting on 25.03.17.
 */
public class DataAggregation {

    private DateTime dateTimeFrom;
    private DateTime dateTimeUntil;
    private long timeFrameMilli;

    //TODO aggregation of aggregation...

    public DataAggregation(DateTime dateTimeFrom, DateTime dateTimeUntil) {
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.timeFrameMilli = dateTimeUntil.getMillis() - dateTimeFrom.getMillis();
    }

    protected class DiscreteStateValues {

        private final long unitConnectionTime;
        private final List<StateValueWithTimestamp> bcoValuesAndTimestamps;

        private final double timeWeighting;
        private final HashMap<String, Pair<Long, Integer>> activeTimeAndQuantityPerStateValue;

        public DiscreteStateValues(final long unitConnectionTime, final List<StateValueWithTimestamp> bcoValuesAndTimestamps) throws CouldNotPerformException {
            this.unitConnectionTime = unitConnectionTime;
            this.bcoValuesAndTimestamps = bcoValuesAndTimestamps;

            checkTimeValidity();

            final List<String> bcoValuesString = getStateValues(bcoValuesAndTimestamps);

            this.timeWeighting = calcTimeWeighting(unitConnectionTime);
            this.activeTimeAndQuantityPerStateValue = getActiveTimeAndQuantityPerStateValue();
        }

        public double getTimeWeighting() {
            return timeWeighting;
        }

        public HashMap<String, Long> getActiveTimePerStateValue() {
            final HashMap<String, Long> activeTimeMap = new HashMap<>();

            for (final String stateValue : activeTimeAndQuantityPerStateValue.keySet()) {
                activeTimeMap.put(stateValue, activeTimeAndQuantityPerStateValue.get(stateValue).getKey());
            }

            return activeTimeMap;
        }

        public HashMap<String, Integer> getQuantityPerStateValue() {
            final HashMap<String, Integer> quantityMap = new HashMap<>();

            for (final String stateValue : activeTimeAndQuantityPerStateValue.keySet()) {
                quantityMap.put(stateValue, activeTimeAndQuantityPerStateValue.get(stateValue).getValue());
            }

            return quantityMap;
        }

        private HashMap<String, Pair<Long, Integer>> getActiveTimeAndQuantityPerStateValue() {

            final HashMap<String, Pair<Long, Integer>> hashMap = new HashMap<>();

            // sort ascending (old to young)
            Collections.sort(bcoValuesAndTimestamps, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

            String lastTimestamp = null;
            String lastStateValue = null;

            final ListIterator<StateValueWithTimestamp> listIterator = bcoValuesAndTimestamps.listIterator();

            while (listIterator.hasNext()) {
                final StateValueWithTimestamp stateValueWithTimestamp = listIterator.next();

                if (lastTimestamp != null) {
                    long timeDiffMillis;

                    if (listIterator.hasNext()) {
                        final String currentTimestamp = stateValueWithTimestamp.getTimestamp();
                        timeDiffMillis = new DateTime(currentTimestamp).getMillis() - new DateTime(lastTimestamp).getMillis();
                    } else {
                        // reached last entry: timestampUntil is the timestampUntil of the aggregationPeriod
                        timeDiffMillis = new DateTime(dateTimeUntil).getMillis() - new DateTime(lastTimestamp).getMillis();
                    }

                    if (hashMap.containsKey(lastStateValue)) {
                        // there is an entry: add data
                        final long totalTime = hashMap.get(lastStateValue).getKey() + timeDiffMillis;
                        final int quantity = hashMap.get(lastStateValue).getValue() + 1;
                        hashMap.put(lastStateValue, new Pair<>(totalTime, quantity));
                    } else {
                        // there is no entry: put data
                        hashMap.put(lastStateValue, new Pair<>(timeDiffMillis, 1));
                    }
                }

                lastStateValue = stateValueWithTimestamp.getStateValue();
                lastTimestamp = stateValueWithTimestamp.getTimestamp();
            }

            return hashMap;
        }


        private void checkTimeValidity() throws CouldNotPerformException {
            if (unitConnectionTime > timeFrameMilli) {
                throw new CouldNotPerformException("Could not process stateValues, because unitConnectionTime is bigger than the time frame of aggregation!");
            }
        }

    }

    protected class ContinuousStateValues {

        private final long unitConnectionTime;

        private final double mean;
        private final double variance;
        private final double standardDeviation;
        private final double timeWeighting;
        private final int quantity;

        public ContinuousStateValues(final long unitConnectionTime, final List<StateValueWithTimestamp> stateValueWithTimestampList) throws CouldNotPerformException {
            this.unitConnectionTime = unitConnectionTime;

            checkTimeValidity();

            final List<String> stateValuesString = getStateValues(stateValueWithTimestampList);
            final List<Double> stateValuesDouble = convertStateValuesStringToDouble(stateValuesString);
            final double stateValuesArray[] = convertToArray(stateValuesDouble);

            this.mean = calcMean(stateValuesArray);
            this.variance = calcVariance(stateValuesArray);
            this.standardDeviation = calcStandardDeviation(stateValuesArray);
            this.timeWeighting = calcTimeWeighting(unitConnectionTime);
            this.quantity = calcQuantity(stateValuesDouble);
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

        private ValueConfidenceRange percentCalculation() throws CouldNotPerformException {

            //TODO other types the same way? -> interval needed like battery 1-100
            //TODO if yes: distinction of this types...
            final double avgPercent = mean;

            final double timeRatio = calcTimeWeighting(unitConnectionTime);
            final double invertedTimeRatio = 100.0 - timeRatio;

            final double minValue = (avgPercent * timeRatio) + (1.0 * invertedTimeRatio);
            final double maxValue = (avgPercent * timeRatio) + (100.0 * invertedTimeRatio);

            return new ValueConfidenceRange(String.valueOf(minValue), String.valueOf(maxValue));
        }

        private void checkTimeValidity() throws CouldNotPerformException {
            if (unitConnectionTime > timeFrameMilli) {
                throw new CouldNotPerformException("Could not process stateValues, because unitConnectionTime is bigger than the time frame of aggregation!");
            }
        }

    }

    private List<String> getStateValues(final List<StateValueWithTimestamp> stateValueWithTimestampList) {
        return stateValueWithTimestampList.stream().map(StateValueWithTimestamp::getStateValue).collect(Collectors.toList());
    }

    private List<Double> convertStateValuesStringToDouble(final List<String> stateValuesString) throws CouldNotPerformException {

        final List<Double> stateValuesDouble = new ArrayList<>();

        try {
            stateValuesDouble.addAll(stateValuesString.stream().map(Double::parseDouble).collect(Collectors.toList()));

            return stateValuesDouble;
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not perform aggregation because stateValueList contains discrete values: " + stateValuesString);
        }
    }

    /**
     * Method calculates the time weighting of a connection time. Means a value, which describes the ratio of connection time and period time. If an unit has
     * connection the whole period the value is 1. If the unit has connection half of the period time the value is 0.5. The range is [0..1].
     *
     * @param unitConnectionTime
     * @return
     */
    private double calcTimeWeighting(final long unitConnectionTime) {
        return unitConnectionTime / timeFrameMilli;
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

    private int calcQuantity(final List<Double> stateValues) {
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
