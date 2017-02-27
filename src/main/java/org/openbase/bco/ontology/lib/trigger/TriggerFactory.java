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
package org.openbase.bco.ontology.lib.trigger;

import javafx.util.Callback;
import org.openbase.bco.ontology.lib.testcode.OntologyRemote;
import org.openbase.bco.ontology.lib.testcode.OntologyRemoteImpl;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Factory;
import org.openbase.jul.pattern.Observable;
import rst.domotic.state.ActivationStateType.ActivationState;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TriggerFactory implements Factory {

    private Callback<ActivationState.State, ActivationState.State> callback;

    public TriggerFactory(Callback<ActivationState.State, ActivationState.State> callback) {
        this.callback = callback;
    }

    @Override
    public Trigger newInstance(Object config) throws InstantiationException, InterruptedException {

//        this.callback = new Callback<ActivationState.State, ActivationState.State>() {
//            @Override
//            public ActivationState.State call(ActivationState.State param) {
//                return param;
//            }
//        };

        OntologyRemote ontologyRemote = new OntologyRemoteImpl();
        Trigger trigger = new TriggerImpl(ontologyRemote);

        try {
            trigger.init((TriggerConfig) config);
            trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
                // do useful stuff
            });
            trigger.activate();
            return trigger;
        } catch (CouldNotPerformException e) {

        }

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}