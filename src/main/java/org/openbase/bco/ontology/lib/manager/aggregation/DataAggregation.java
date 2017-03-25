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

import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ConnectionTimeRatio;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.StateValueAtTime;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ValueConfidenceRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author agatting on 25.03.17.
 */
public class DataAggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAggregation.class);

    private ValueConfidenceRange percentCalculation(final List<StateValueAtTime> percentValueList, final ConnectionTimeRatio connectionTimeRatio)
            throws IllegalArgumentException {

        final int quantity = percentValueList.size();
        double percentValue = 0.0;

        for (final StateValueAtTime stateValueAtTime : percentValueList) {
            percentValue += Double.parseDouble(stateValueAtTime.getStateValue());
        }

        final double avgPercent = percentValue / quantity;

        if (connectionTimeRatio.getUnitConnectionTime() <= connectionTimeRatio.getTimeConcept()) {
            final double timeRatio = connectionTimeRatio.getUnitConnectionTime() / connectionTimeRatio.getTimeConcept();
            final double invertedTimeRatio = 100.0 - timeRatio;

            final double minValue = (avgPercent * timeRatio) + (1.0 * invertedTimeRatio);
            final double maxValue = (avgPercent * timeRatio) + (100.0 * invertedTimeRatio);

            return new ValueConfidenceRange(String.valueOf(minValue), String.valueOf(maxValue));
        } else {
            throw new IllegalArgumentException("Value of connection time is bigger than concept of time!");
        }
    }

    private void bcoStateValueCalculation(final List<StateValueAtTime> valueList) {

        final int quantity = valueList.size();

        // sort ascending (old to young)
        Collections.sort(valueList, new Comparator<StateValueAtTime>() {
            @Override
            public int compare(StateValueAtTime o1, StateValueAtTime o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
        //TODO get time of each statValue...
    }
}
