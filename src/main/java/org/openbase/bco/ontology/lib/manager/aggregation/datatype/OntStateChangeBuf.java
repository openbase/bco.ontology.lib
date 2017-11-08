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
 * @author agatting on 25.03.17.
 */
public class OntStateChangeBuf {

    private final List<RDFNode> stateValues;
    private final String timestamp;

    /**
     * Method creates a ontology stateChange data type, which describes the state values to a specific time.
     *
     * @param stateValues are the values of an state change to a specific time.
     * @param timestamp is the specific time in format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX.
     */
    public OntStateChangeBuf(final List<RDFNode> stateValues, final String timestamp) {
        this.stateValues = stateValues;
        this.timestamp = timestamp;
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
     * Getter for observation data: timestamp.
     *
     * @return the timestamp in format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public String getTimestamp() {
        return timestamp;
    }

}
