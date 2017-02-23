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

import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.datapool.ReflectObjectPool;
import org.openbase.jul.exception.CouldNotPerformException;
import rst.domotic.state.ColorStateType;
import rst.domotic.state.PowerStateType.PowerState;

/**
 * @author agatting on 22.02.17.
 */
public interface ValueOfServiceType {

    static PowerState.State powerStateValue(final Object stateObject) throws CouldNotPerformException {
        return (PowerState.State) ReflectObjectPool.getInvokedObj(stateObject, ConfigureSystem.StateTypeExpr.GET_VALUE.getName());
    }

    static void colorStateValue(final Object stateObject) {
//        ((ColorStateType.ColorState) stateObject).getColor().getHsbColor().
    }

}
