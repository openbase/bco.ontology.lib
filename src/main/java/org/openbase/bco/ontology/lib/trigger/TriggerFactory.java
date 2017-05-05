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

import org.openbase.bco.ontology.lib.commun.rsb.RsbCommunication;
import org.openbase.bco.ontology.lib.jp.JPOntologyScope;
import org.openbase.bco.ontology.lib.commun.trigger.OntologyRemote;
import org.openbase.bco.ontology.lib.commun.trigger.OntologyRemoteImpl;
import org.openbase.bco.ontology.lib.commun.monitor.ServerConnection;
import org.openbase.bco.ontology.lib.trigger.sparql.QueryParser;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Factory;
import org.openbase.jul.pattern.ObservableImpl;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.ontology.TriggerConfigType.TriggerConfig;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TriggerFactory implements Factory {

    public static ObservableImpl<OntologyChange> changeCategoryObservable = null;

    public TriggerFactory() throws CouldNotPerformException {

        changeCategoryObservable = new ObservableImpl<>(false, this);
        new ServerConnection();

        try {
            RsbCommunication.startRsbListener(JPService.getProperty(JPOntologyScope.class).getValue(), changeCategoryObservable);
        } catch (InterruptedException | JPNotAvailableException e) {
            throw new CouldNotPerformException("Could not activate rsb listener!", e);
        }
    }

    @Override
    public Trigger newInstance(final Object config) throws InstantiationException, InterruptedException {

        if (config == null) {
            throw new IllegalArgumentException("TriggerConfig is null!");
        }

        final TriggerConfig triggerConfig = ((TriggerConfig) config);

        if (triggerConfig.getLabel() == null || triggerConfig.getQuery() == null || triggerConfig.getDependingOntologyChange() == null) {
            throw new IllegalArgumentException("At least one element of the triggerConfig is null!");
        }

        final OntologyRemote ontologyRemote = new OntologyRemoteImpl();
        final Trigger trigger = new TriggerImpl(ontologyRemote);

        return initTrigger(trigger, triggerConfig);
    }

    public Trigger newInstance(final String label, final String query) throws InstantiationException, InterruptedException
            , IllegalArgumentException {

        if (label == null || query == null) {
            throw new IllegalArgumentException("Trigger label or trigger query is null!");
        }

        final OntologyChange ontologyChange = getOntologyChange(label, query);

        final OntologyRemote ontologyRemote = new OntologyRemoteImpl();
        final Trigger trigger = new TriggerImpl(ontologyRemote);
        final TriggerConfig triggerConfig
                = TriggerConfig.newBuilder().setLabel(label).setQuery(query).setDependingOntologyChange(ontologyChange).build();

        return initTrigger(trigger, triggerConfig);
    }

    public OntologyChange getOntologyChange(final String label, final String query) throws IllegalArgumentException {

        if (label == null || query == null) {
            throw new IllegalArgumentException("Trigger label or trigger query is null!");
        }

        final QueryParser queryParser = new QueryParser(label, query);
        return queryParser.getOntologyChange();
    }

    public TriggerConfig buildTriggerConfig(final String label, final String query) throws IllegalArgumentException {

        if (label == null || query == null) {
            throw new IllegalArgumentException("Trigger label or trigger query is null!");
        }

        final QueryParser queryParser = new QueryParser(label, query);
        final OntologyChange ontologyChange = queryParser.getOntologyChange();

        return TriggerConfig.newBuilder().setLabel(label).setQuery(query).setDependingOntologyChange(ontologyChange).build();
    }

    private Trigger initTrigger(final Trigger trigger, final TriggerConfig config) throws InterruptedException, InstantiationException {
        try {
            trigger.init(config);
            trigger.activate();

            return trigger;
        } catch (CouldNotPerformException e) {
            throw new InstantiationException("Could not initiate trigger instance!", e);
        }
    }
    
}