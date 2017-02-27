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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openbase.bco.ontology.lib.config.CategoryConfig.ChangeCategory;
import org.openbase.bco.ontology.lib.testcode.OntologyRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote;
import rst.domotic.state.ActivationStateType.ActivationState;

/**
 * @author agatting on 21.12.16.
 */
public class TriggerImpl implements Trigger {

    private static final List<ChangeCategory> UNKNOWN_CHANGE = new ArrayList<>();

    static {
        UNKNOWN_CHANGE.add(ChangeCategory.UNKNOWN);
    }

    private final ObservableImpl<ActivationState.State> activationObservable;
    private boolean active;
    private TriggerConfig config;
    private final OntologyRemote ontologyRemote;
    private final Observer<Remote.ConnectionState> connectionObserver;
    private final Observer<Collection<ChangeCategory>> ontologyObserver;

    /**
     * Constructor for TriggerImpl.
     */
    public TriggerImpl(final OntologyRemote ontologyRemote) {
        this.ontologyRemote = ontologyRemote;
        this.activationObservable = new ObservableImpl<>(this);
        this.connectionObserver = new Observer<Remote.ConnectionState>() {
            @Override
            public void update(Observable<Remote.ConnectionState> source, Remote.ConnectionState data) throws Exception {
                switch (data) {
                    case CONNECTED:
                        notifyOntologyChange(UNKNOWN_CHANGE);
                        break;
                    case DISCONNECTED:
                    case UNKNOWN:
                    default:
                        activationObservable.notifyObservers(ActivationState.State.UNKNOWN);

                }
            }
        };
        this.ontologyObserver = new Observer<Collection<ChangeCategory>>() {
            @Override
            public void update(Observable<Collection<ChangeCategory>> source, Collection<ChangeCategory> data) throws Exception {
                notifyOntologyChange(data);
            }
        };
    }

    public void addObserver(Observer<ActivationState.State> observer) {
        activationObservable.addObserver(observer);
    }

    public void removeObserver(Observer<ActivationState.State> observer) {
        activationObservable.removeObserver(observer);
    }

    public void init(String label, String query, Collection<ChangeCategory> changeCategoryList) throws InitializationException, InterruptedException {
        try {
            init(new TriggerConfig(label, query, changeCategoryList));
            activationObservable.notifyObservers(ActivationState.State.UNKNOWN);
        } catch (CouldNotPerformException e) {
            throw new InitializationException(this, e);
        }
    }

    @Override
    public void init(final TriggerConfig config) throws InitializationException, InterruptedException {
        try {
            this.config = config;
            activationObservable.notifyObservers(ActivationState.State.UNKNOWN);
        } catch (CouldNotPerformException e) {
            throw new InitializationException(this, e);
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        active = true;
        ontologyRemote.addConnectionStateObserver(connectionObserver);
        ontologyRemote.addOntologyObserver(ontologyObserver);
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        active = false;
        ontologyRemote.removeConnectionStateObserver(connectionObserver);
        ontologyRemote.removeOntologyObserver(ontologyObserver);
    }

    protected void notifyOntologyChange(final Collection<ChangeCategory> categoryCollection) throws CouldNotPerformException {
        if (categoryCollection.contains(ChangeCategory.UNKNOWN) || isRelatedChange(categoryCollection)) {
            try {
                if (ontologyRemote.match(config.getQuery())) {
                    activationObservable.notifyObservers(ActivationState.State.ACTIVE);
                } else {
                    activationObservable.notifyObservers(ActivationState.State.DEACTIVE);
                }
            } catch (IOException e) {

            }
        }
    }

    public boolean isRelatedChange(final Collection<ChangeCategory> categoryCollection) {
        for (final ChangeCategory category : categoryCollection) {
            if (config.getChangeCategory().contains(category)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
