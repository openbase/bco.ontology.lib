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
package org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.stateProcessing;

import rst.domotic.state.ColorStateType.ColorState;
import rst.domotic.state.PowerStateType.PowerState;

import java.util.HashSet;
import java.util.Set;

/**
 * The class contains methods for the individual stateTypes. Each method gets the stateValues(s) of the stateType and converts the stateValues(s) to the
 * SPARQL codec in string form. Each method returns a set of strings independent of the number of entries, because of processing reason in class
 * IdentifyStateType.
 *
 * @author agatting on 22.02.17.
 */
public class ValueOfServiceType {

    protected Set<String> audioStateValue() {
        final Set<String> stringSet = new HashSet<>();

        return stringSet;
    }

    /**
     * Method returns the state values of the given colorState as stringSet.
     *
     * @param stateType The colorState.
     * @return Set of color state value (brightness, hue, saturation) strings.
     */
    protected Set<String> colorStateValue(final ColorState stateType) {

        final Set<String> hsvValuesInSparqlCodec = new HashSet<>();

        final double brightness = stateType.getColor().getHsbColor().getBrightness();
        hsvValuesInSparqlCodec.add("\"" + brightness + "\"^^NS:Brightness");

        final double hue = stateType.getColor().getHsbColor().getHue();
        hsvValuesInSparqlCodec.add("\"" + hue + "\"^^NS:Hue");

        final double saturation = stateType.getColor().getHsbColor().getSaturation();
        hsvValuesInSparqlCodec.add("\"" + saturation + "\"^^NS:Saturation");

        return hsvValuesInSparqlCodec;
    }



    /**
     * Method returns the state value of the given powerState as stringSet. See class hint.
     *
     * @param stateType The powerState.
     * @return Set of state value strings.
     */
    protected Set<String> powerStateValue(final PowerState stateType) {
        final Set<String> stringSet = new HashSet<>();
        stringSet.add(stateType.getValue().toString());

        return stringSet;
    }

}
