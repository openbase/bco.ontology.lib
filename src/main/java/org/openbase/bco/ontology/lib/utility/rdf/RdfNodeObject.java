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
package org.openbase.bco.ontology.lib.utility.rdf;

import java.util.List;

/**
 * This class represents a custom data type to save state values with information about the rdf node object type.
 *
 * @author agatting on 26.09.17.
 */
public class RdfNodeObject {
    private final List<String> stateValues;
    private final boolean isLiteral;

    /**
     * Construct an rdf node object, which keeps state value(s) of a state source. Furthermore it describes the type of the node object.
     *
     * @param stateValues are the stateValues saved as string. A state source contains one (e.g. battery level) or multiple (e.g. color hsb) stateValue(s).
     * @param isLiteral describes the kind of this state source (literal or resource) to manage the ontology. A state value based on continuous value(e.g. 0.38)
     *                  should be marked as literal {@code true}. Otherwise as resource {@code false} if the state value based on discrete value (e.g. ON).
     */
    public RdfNodeObject(final List<String> stateValues, final boolean isLiteral) {
        this.stateValues = stateValues;
        this.isLiteral = isLiteral;
    }

    /**
     * Method provides the state value(s) of the state source.
     *
     * @return the state values.
     */
    public List<String> getStateValues() {
        return stateValues;
    }

    /**
     * Method provides the kind of the source.
     *
     * @return {@code true} if the state value(s) based on literal (continuous). Otherwise {@code false} if based on resource (discrete).
     */
    public boolean isLiteral() {
        return isLiteral;
    }
}
