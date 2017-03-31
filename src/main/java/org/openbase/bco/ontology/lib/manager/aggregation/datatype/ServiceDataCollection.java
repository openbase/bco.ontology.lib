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
public class ServiceDataCollection {
    private final String stateValue;
    private final String dataType;
    private final String timestamp;

    /**
     * Method contains a data collection of stateValue, dataType and timestamp, which a providerService can contain.
     * A dataType can be null, then it's a BCO stateValue (like ON/OFF) or not null, then it's a physical unit or a specific membership.
     *
     * @param stateValue The value as discrete (BCO value) or continuous.
     * @param dataType The physical dataType/specific membership or null (no dataType).
     * @param timestamp The timestamp of the stateValue and dataType.
     */
    public ServiceDataCollection(final String stateValue, final String dataType, final String timestamp) {
        this.stateValue = stateValue;
        this.dataType = dataType;
        this.timestamp = timestamp;
    }

    /**
     * Getter for stateValue.
     *
     * @return stateValue.
     */
    public String getStateValue() {
        return stateValue; }

    /**
     * Getter for dataType.
     *
     * @return dataType.
     */
    public String getDataType() {
        return dataType; }

    /**
     * Getter for timestamp.
     *
     * @return timestamp.
     */
    public String getTimestamp() {
        return timestamp; }

}
