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
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceAggDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceDataCollection;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * @author agatting on 25.03.17.
 */
public class DataAggregation {

    private DateTime dateTimeFrom;
    private DateTime dateTimeUntil;
    private long timeFrameMilli;

    //TODO corruption by youngest values before time frame -> handle in calculation
    //TODO general survey of calculation ...

    public DataAggregation(final DateTime dateTimeFrom, final DateTime dateTimeUntil) {
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.timeFrameMilli = dateTimeUntil.getMillis() - dateTimeFrom.getMillis();
    }

    protected class DiscreteStateValues {

        private final double timeWeighting;
        private final HashMap<String, Pair<Long, Integer>> activeTimeAndQuantityPerStateValue;

        public DiscreteStateValues(final long unitConnectionTime, final List<ServiceDataCollection> bcoValuesAndTimestamps) throws CouldNotPerformException {

            checkTimeValidity(unitConnectionTime);

            this.timeWeighting = calcTimeWeighting(unitConnectionTime);
            this.activeTimeAndQuantityPerStateValue = getActiveTimeAndQuantityPerStateValue(bcoValuesAndTimestamps);
        }

        public DiscreteStateValues(final List<ServiceAggDataCollection> aggDataList, final OntConfig.Period toAggPeriod) throws CouldNotPerformException {

            this.timeWeighting = calcTimeWeighting(getTimeWeightingArray(aggDataList), getPeriodLength(toAggPeriod));
            this.activeTimeAndQuantityPerStateValue = getAggActiveTimeAndAggQuantityPerStateValue(aggDataList);
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

        private HashMap<String, Pair<Long, Integer>> getAggActiveTimeAndAggQuantityPerStateValue(final List<ServiceAggDataCollection> aggDataCollList)
                throws CouldNotPerformException {

            final HashMap<String, Pair<Long, Integer>> hashMap = new HashMap<>();

            final HashMap<String, List<String>> hashMapQuantity = new HashMap<>();
            final HashMap<String, List<String>> hashMapActivityTime = new HashMap<>();

            for (final ServiceAggDataCollection serviceAggDataColl : aggDataCollList) {
                final String stateValue = serviceAggDataColl.getStateValue().asResource().getLocalName();

                if (hashMapQuantity.containsKey(stateValue)) {
                    // there is an entry: add data quantity
                    final List<String> quantityListBuf = hashMapQuantity.get(stateValue);
                    quantityListBuf.add(serviceAggDataColl.getQuantity());
                    hashMapQuantity.put(stateValue, quantityListBuf);
                    // there is an entry: add data activityTime
                    final List<String> activityTimeListBuf = hashMapActivityTime.get(stateValue);
                    activityTimeListBuf.add(serviceAggDataColl.getActivityTime());
                    hashMapActivityTime.put(stateValue, activityTimeListBuf);
                } else {
                    // there is no entry: put data quantity
                    final List<String> quantityListBuf = new ArrayList<>();
                    quantityListBuf.add(serviceAggDataColl.getQuantity());
                    hashMapQuantity.put(stateValue, quantityListBuf);
                    // there is no entry: put data activityTime
                    final List<String> activityTimeListBuf = new ArrayList<>();
                    activityTimeListBuf.add(serviceAggDataColl.getActivityTime());
                    hashMapActivityTime.put(stateValue, activityTimeListBuf);
                }
            }

            for (final String stateValue : hashMapQuantity.keySet()) {
                final List<Integer> quantityList = convertStringToInteger(hashMapQuantity.get(stateValue));
                final int quantitySum = getQuantitySum(quantityList);

                final List<Long> activityTimeList = convertStringToLong(hashMapActivityTime.get(stateValue));
                final long activityTimeSum = getActivityTimeSum(activityTimeList);

                hashMap.put(stateValue, new Pair<>(activityTimeSum, quantitySum));
            }
            return hashMap;
        }

        private int getQuantitySum(final List<Integer> quantityList) {
            return quantityList.stream().mapToInt(Integer::intValue).sum();
        }

        private long getActivityTimeSum(final List<Long> activityTimeList) {
            return activityTimeList.stream().mapToLong(Long::longValue).sum();
        }

        private HashMap<String, Pair<Long, Integer>> getActiveTimeAndQuantityPerStateValue(final List<ServiceDataCollection> bcoValuesAndTimestamps) throws NotAvailableException {

            final HashMap<String, Pair<Long, Integer>> hashMap = new HashMap<>();

            // sort ascending (old to young)
            Collections.sort(bcoValuesAndTimestamps, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

            String lastTimestamp = null;
            String lastStateValue = null;

            final ListIterator<ServiceDataCollection> listIterator = bcoValuesAndTimestamps.listIterator();

            while (listIterator.hasNext()) {
                final ServiceDataCollection stateValueDataCollection = listIterator.next();

                if (lastTimestamp != null) {
                    long timeDiffMillis;

                    if (listIterator.hasNext()) {
                        final String currentTimestamp = stateValueDataCollection.getTimestamp();
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

//                if (!stateValueDataCollection.getStateValue().isLiteral()) {
                    lastStateValue = StringModifier.getLocalName(stateValueDataCollection.getStateValue().asResource().toString());
//                } else {
//                    lastStateValue = "UNKNOWN"; //TODO (bad hack)
//                }

                lastTimestamp = stateValueDataCollection.getTimestamp();

                if (new DateTime(lastTimestamp).getMillis() < dateTimeFrom.getMillis()) {
                    lastTimestamp = dateTimeFrom.toString();
                }
            }
            return hashMap;
        }
    }

    protected class ContinuousStateValues {

        private final double mean;
        private final double variance;
        private final double standardDeviation;
        private final double timeWeighting;
        private final int quantity;

        public ContinuousStateValues(final long unitConnectionTime, final List<ServiceDataCollection> stateValueDataCollectionList) throws CouldNotPerformException {

            checkTimeValidity(unitConnectionTime);

            final List<String> stateValuesString = getStateValues(stateValueDataCollectionList);
            final List<Double> stateValuesDouble = convertStringToDouble(stateValuesString);
            final double stateValuesArray[] = convertToArray(stateValuesDouble);

            this.mean = calcMean(stateValuesArray);
            this.variance = calcVariance(stateValuesArray);
            this.standardDeviation = calcStandardDeviation(stateValuesArray);
            this.timeWeighting = calcTimeWeighting(unitConnectionTime);
            this.quantity = calcQuantity(stateValuesString);
        }

        public ContinuousStateValues(final List<ServiceAggDataCollection> aggDataList, final OntConfig.Period toAggPeriod) throws CouldNotPerformException {

            this.mean = calcMean(getMeanList(aggDataList));
            this.variance = calcVariance(getVarianceList(aggDataList));
            this.standardDeviation = calcStandardDeviation(getStandardDeviationList(aggDataList));
            this.timeWeighting = calcTimeWeighting(getTimeWeightingArray(aggDataList), getPeriodLength(toAggPeriod));
            this.quantity = calcQuantity(getQuantity(aggDataList));
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

        private double[] getMeanList(final List<ServiceAggDataCollection> aggDataList) throws CouldNotPerformException {
            final List<String> aggMeanBuf = aggDataList.stream().map(ServiceAggDataCollection::getMean).collect(Collectors.toList());
            return convertToArray(convertStringToDouble(aggMeanBuf));
        }

        private double[] getVarianceList(final List<ServiceAggDataCollection> aggDataList) throws CouldNotPerformException {
            final List<String> aggVarianceBuf = aggDataList.stream().map(ServiceAggDataCollection::getVariance).collect(Collectors.toList());
            return convertToArray(convertStringToDouble(aggVarianceBuf));
        }

        private double[] getStandardDeviationList(final List<ServiceAggDataCollection> aggDataList) throws CouldNotPerformException {
            final List<String> aggStandardDeviationBuf = aggDataList.stream().map(ServiceAggDataCollection::getStandardDeviation).collect(Collectors.toList());
            return convertToArray(convertStringToDouble(aggStandardDeviationBuf));
        }

        private List<Integer> getQuantity(final List<ServiceAggDataCollection> aggDataList) throws CouldNotPerformException {
            final List<String> aggQuantityBuf = aggDataList.stream().map(ServiceAggDataCollection::getQuantity).collect(Collectors.toList());
            return convertStringToInteger(aggQuantityBuf);
        }

        private List<String> getStateValues(final List<ServiceDataCollection> stateValueDataCollectionList) throws NotAvailableException {
            return stateValueDataCollectionList.stream().map(serviceDataCollection -> {
                try {
                    return StringModifier.getLocalName(serviceDataCollection.getStateValue().asLiteral().getLexicalForm());
                } catch (NotAvailableException e) {
                    return ""; //TODO
                }
            }).collect(Collectors.toList());
        }

//        private ValueConfidenceRange percentCalculation() throws CouldNotPerformException {
//
//            //TODO other types the same way? -> interval needed like battery 1-100
//            //TODO if yes: distinction of this types...
//            final double avgPercent = mean;
//
//            final double timeRatio = calcTimeWeighting(unitConnectionTime);
//            final double invertedTimeRatio = 100.0 - timeRatio;
//
//            final double minValue = (avgPercent * timeRatio) + (1.0 * invertedTimeRatio);
//            final double maxValue = (avgPercent * timeRatio) + (100.0 * invertedTimeRatio);
//
//            return new ValueConfidenceRange(String.valueOf(minValue), String.valueOf(maxValue));
//        }
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

    private void checkTimeValidity(final long unitConnectionTime) throws CouldNotPerformException {
        if (unitConnectionTime > timeFrameMilli) {
            throw new CouldNotPerformException("Could not process stateValues, because unitConnectionTime is bigger than the time frame of aggregation!");
        }
    }

    private double[] getTimeWeightingArray(final List<ServiceAggDataCollection> aggDataList) throws CouldNotPerformException {
        final List<String> aggQuantityBuf = aggDataList.stream().map(ServiceAggDataCollection::getTimeWeighting).collect(Collectors.toList());
        return convertToArray(convertStringToDouble(aggQuantityBuf));
    }

    private List<Double> convertStringToDouble(final List<String> stringValues) throws CouldNotPerformException {
        try {
            return stringValues.stream().map(Double::parseDouble).collect(Collectors.toList());
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not perform aggregation because stateValueList contains discrete values: " + stringValues);
        }
    }

    private List<Integer> convertStringToInteger(final List<String> stringValues) throws CouldNotPerformException {
        try {
            return stringValues.stream().map(Integer::parseInt).collect(Collectors.toList());
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not perform aggregation because stateValueList contains discrete values: " + stringValues);
        }
    }

    private List<Long> convertStringToLong(final List<String> stringValues) throws CouldNotPerformException {
        try {
            return stringValues.stream().map(Long::parseLong).collect(Collectors.toList());
        } catch (Exception e) {
            throw new CouldNotPerformException("Could not perform aggregation because stateValueList contains discrete values: " + stringValues);
        }
    }

    /**
     * Method calculates the time weighting of a connection time. Means a value, which describes the ratio of connection time and period time. If an unit has
     * connection the whole period the value is 1. If the unit has connection half of the period time the value is 0.5. The range is [0..1].
     *
     * @param unitConnectionTime unitConnectionTime
     * @return The time weighting.
     */
    private double calcTimeWeighting(final long unitConnectionTime) {
        return Double.parseDouble(OntConfig.decimalFormat().format((double) unitConnectionTime / (double) timeFrameMilli));
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
