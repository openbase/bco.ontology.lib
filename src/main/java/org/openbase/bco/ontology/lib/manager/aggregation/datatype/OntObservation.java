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
public class OntObservation {

    private final String providerService;
    private final List<RDFNode> stateValues;
    private final String timestamp;

    /**
     * Constructor for creating an ontology observation. It describes a state change regarding to one specific providerService with an timestamp.
     *
     * @param providerService is the providerService to identify the kind of state values.
     * @param stateValues are the values of an state change to a specific time.
     * @param timestamp is the specific time with the format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX.
     */
    public OntObservation(final String providerService, final List<RDFNode> stateValues, final String timestamp) {
        this.providerService = providerService;
        this.stateValues = stateValues;
        this.timestamp = timestamp;
    }

    /**
     * Getter for observation data: providerService.
     *
     * @return the providerService to identify the kind of state values.
     */
    public String getProviderService() {
        return providerService;
    }

    /**
     * Getter for observation data: timestamp.
     *
     * @return the timestamp in format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for observation data: stateValues.
     *
     * @return the state values.
     */
    public List<RDFNode> getStateValues() {
        return stateValues;
    }

    /**
     * Method adds a single state value to the list of stateValues.
     *
     * @param stateValue is the stateValue, which should be added.
     * @return true, if added successfully. Otherwise false.
     */
    public boolean addValue(final RDFNode stateValue) {
        return stateValues.add(stateValue);
    }

}
