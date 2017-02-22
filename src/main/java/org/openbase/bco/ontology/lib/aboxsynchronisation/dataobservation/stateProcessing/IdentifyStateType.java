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

import javafx.util.Pair;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import rst.domotic.state.PowerStateType.PowerState;

import java.util.List;

/**
 * @author agatting on 22.02.17.
 */
public class IdentifyStateType {

    // declaration of predicates and classes, which are static
    private static final String predicateIsA = ConfigureSystem.OntExpr.A.getName();
    private static final String predicateHasStateValue = ConfigureSystem.OntProp.STATE_VALUE.getName();
    private static final String stateValueClass = ConfigureSystem.OntClass.STATE_VALUE.getName();

    protected List<TripleArrayList> addStateValue(final String serviceType, final Object stateObject, final String subjectObservation
            , final List<TripleArrayList> tripleArrayListsBuf) {

        final Pair<String, Boolean> stateTypeAndIsPhysicalTypePair = compare(serviceType, stateObject);

        if (stateTypeAndIsPhysicalTypePair != null) {
            // check if stateType based on physical type. If yes = literal, if no = stateValue instance.
            if (!stateTypeAndIsPhysicalTypePair.getValue()) {
//                System.out.println(stateTypeAndIsPhysicalTypePair.getKey());
                tripleArrayListsBuf.add(new TripleArrayList(stateTypeAndIsPhysicalTypePair.getKey(), predicateIsA, stateValueClass));
            } else {

            }
            tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasStateValue, stateTypeAndIsPhysicalTypePair.getKey()));
        } else {
//            System.out.println("nuuuuull!");
        }

        return tripleArrayListsBuf;
    }

    private Pair<String, Boolean> compare(final String serviceType, final Object stateObject) {
        String serviceTypeBuf = serviceType.toLowerCase().replaceAll(ConfigureSystem.OntExpr.REMOVE.getName(), "");
        Pair<String, Boolean> stateTypeAndIsPhysicalTypePair = null;

        try {
            if (serviceTypeBuf.equalsIgnoreCase("powerStateService")) {
                final PowerState.State powerState = ValueOfServiceType.powerStateValue(stateObject);
                stateTypeAndIsPhysicalTypePair = new Pair<>(powerState.toString(), false);
                return stateTypeAndIsPhysicalTypePair;
            } else if (serviceTypeBuf.equalsIgnoreCase("")) {
                //TODO case literal
            } else {
                stateTypeAndIsPhysicalTypePair = null;
            }

        } catch (CouldNotPerformException e) {
            stateTypeAndIsPhysicalTypePair = null;
        }

        return stateTypeAndIsPhysicalTypePair;
    }
}
