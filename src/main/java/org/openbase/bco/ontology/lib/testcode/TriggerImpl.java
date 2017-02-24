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
package org.openbase.bco.ontology.lib.testcode;

import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import rst.domotic.state.ActivationStateType.ActivationState;

/**
 * @author agatting on 21.12.16.
 */
public class TriggerImpl implements Trigger {

    private final ObservableImpl<ActivationState.State> activationObservable;

    /**
     * Constructor for TriggerImpl.
     */
    public TriggerImpl() {
        activationObservable = new ObservableImpl<>();



//        notifyObservers(true);
    }

    public void addObserver(Observer<ActivationState.State> observer) {
        
    }

    public void removeObserver(Observer<ActivationState.State> observer) {

    }
}
