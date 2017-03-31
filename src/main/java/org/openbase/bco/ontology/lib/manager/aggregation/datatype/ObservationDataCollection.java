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
 * @author agatting on 24.03.17.
 */
public class ObservationDataCollection {

    private final String providerService;
    private final String stateValue;
    private final String dataType;
    private final String timestamp;

    public ObservationDataCollection(final String providerService, final String stateValue, final String dataType, final String timestamp) {
        this.providerService = providerService;
        this.stateValue = stateValue;
        this.dataType = dataType;
        this.timestamp = timestamp;
    }

    /**
     * Getter for observation data: providerService.
     *
     * @return providerService.
     */
    public String getProviderService() {
        return providerService; }

    /**
     * Getter for observation data: stateValue.
     *
     * @return stateValue.
     */
    public String getStateValue() {
        return stateValue; }

    /**
     * Getter for observation data: dataType.
     *
     * @return dataType.
     */
    public String getDataType() {
        return dataType; }

    /**
     * Getter for observation data: timestamp.
     *
     * @return timestamp.
     */
    public String getTimestamp() {
        return timestamp; }
}
