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
package org.openbase.bco.ontology.lib.manager.aggregation.datatype;

/**
 * @author agatting on 25.03.17.
 */
public class ValueConfidenceRange {
    private final String minValue;
    private final String maxValue;

    /**
     * Method creates a pair of minimal and maximal values for stateValue confidence. The values describe the range (/interval) of a stateValue from an unit.
     * An interval (minValue != maxValue) means, that the stateValue has an average peak in this interval over a time value. No interval (minValue = maxValue)
     * means, that the stateValue is to 100 percent probability the average peak over a time value.
     *
     * @param minValue The possible minimal value.
     * @param maxValue The possible maximal value.
     */
    public ValueConfidenceRange(final String minValue, final String maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Getter for confidence minimal value.
     *
     * @return minValue.
     */
    public String getMinValue() {
        return minValue; }

    /**
     * Getter for confidence maximal value.
     *
     * @return maxValue.
     */
    public String getMaxValue() {
        return maxValue; }

}
