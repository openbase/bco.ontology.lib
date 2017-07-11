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

import org.apache.jena.rdf.model.RDFNode;

import java.util.List;

/**
 * @author agatting on 05.07.17.
 */
public class Observation {

    private final String providerService;
    private final List<RDFNode> stateValues;
    private final String timestamp;

    public Observation(final String providerService, final List<RDFNode> stateValues, final String timestamp) {
        this.providerService = providerService;
        this.stateValues = stateValues;
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
    public List<RDFNode> getStateValues() {
        return stateValues; }

    public boolean addValue(final RDFNode stateValue) {
        return stateValues.add(stateValue);
    }

    /**
     * Getter for observation data: timestamp.
     *
     * @return timestamp.
     */
    public String getTimestamp() {
        return timestamp; }
}
