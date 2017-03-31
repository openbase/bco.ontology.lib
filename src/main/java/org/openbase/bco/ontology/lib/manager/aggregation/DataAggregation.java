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
import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ConnectionTimeRatio;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.StateValueTimestamp;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ValueConfidenceRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * @author agatting on 25.03.17.
 */
public class DataAggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAggregation.class);

    private ValueConfidenceRange percentCalculation(final List<StateValueTimestamp> percentValueList, final ConnectionTimeRatio connectionTimeRatio)
            throws IllegalArgumentException {

        final int quantity = percentValueList.size();
        double percentValue = 0.0;

        for (final StateValueTimestamp stateValueTimestamp : percentValueList) {
            percentValue += Double.parseDouble(stateValueTimestamp.getStateValue());
        }

        final double avgPercent = percentValue / quantity;
        final long aggregationPeriod = connectionTimeRatio.getDateTimeUntil().getMillis() - connectionTimeRatio.getDateTimeFrom().getMillis();

        if (connectionTimeRatio.getUnitConnectionTime() <= aggregationPeriod) {
            final double timeRatio = connectionTimeRatio.getUnitConnectionTime() / aggregationPeriod;
            final double invertedTimeRatio = 100.0 - timeRatio;

            final double minValue = (avgPercent * timeRatio) + (1.0 * invertedTimeRatio);
            final double maxValue = (avgPercent * timeRatio) + (100.0 * invertedTimeRatio);

            return new ValueConfidenceRange(String.valueOf(minValue), String.valueOf(maxValue));
        } else {
            throw new IllegalArgumentException("Value of connection time is bigger than concept of time!");
        }
    }

    private HashMap<String, Pair<Long, Integer>> getTotalTimeAndQuantityForEachStateValue(final List<StateValueTimestamp> valueList
            , final ConnectionTimeRatio connectionTimeRatio) {

        final HashMap<String, Pair<Long, Integer>> hashMap = new HashMap<>();

        // sort ascending (old to young)
        Collections.sort(valueList, (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

        //TODO beginning timestamp or rather stateValue from 0:00 o'clock not available...

        String lastTimestamp = null;
        String lastStateValue = null;

        final ListIterator<StateValueTimestamp> listIterator = valueList.listIterator();

        while (listIterator.hasNext()) {
            final StateValueTimestamp stateValueTimestamp = listIterator.next();

            if (lastTimestamp != null) {
                long timeDiffMillis;

                if (listIterator.hasNext()) {
                    final String currentTimestamp = stateValueTimestamp.getTimestamp();
                    timeDiffMillis = new DateTime(currentTimestamp).getMillis() - new DateTime(lastTimestamp).getMillis();
                } else {
                    // reached last entry: timestampUntil is the timestampUntil of the aggregationPeriod
                    timeDiffMillis = new DateTime(connectionTimeRatio.getDateTimeUntil()).getMillis() - new DateTime(lastTimestamp).getMillis();
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

            lastStateValue = stateValueTimestamp.getStateValue();
            lastTimestamp = stateValueTimestamp.getTimestamp();
        }

        return hashMap;
    }

    private ValueConfidenceRange getConfidenceRangeForStatevalue(final long StateValueTime, final ConnectionTimeRatio connectionTimeRatio) {

        final double minValue = 0;
        final double maxValue = 0;

        return new ValueConfidenceRange(String.valueOf(minValue), String.valueOf(maxValue));
    }
}
