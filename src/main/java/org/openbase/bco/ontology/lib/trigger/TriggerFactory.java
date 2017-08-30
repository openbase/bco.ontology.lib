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

import org.openbase.bco.ontology.lib.commun.trigger.OntologyRemote;
import org.openbase.bco.ontology.lib.commun.trigger.OntologyRemoteImpl;
import org.openbase.bco.ontology.lib.commun.monitor.ServerConnection;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.trigger.sparql.QueryParser;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.jul.pattern.Factory;
import org.openbase.jul.pattern.ObservableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.ontology.TriggerConfigType.TriggerConfig;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TriggerFactory implements Factory {

    /**
     * Informs observer about changed categories.
     */
    public static final ObservableImpl<OntologyChange> ONTOLOGY_CHANGE_OBSERVABLE = new ObservableImpl<>(false);

    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerFactory.class);

    /**
     * Constructor initializes the base elements to create multiple trigger instances. That means (1) an independent server connection to monitor the
     * connection state between trigger interface and ontology server and (2) the rsb communication.
     *
     * @throws CouldNotPerformException is thrown in case at least one base element could not be initialized.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    public TriggerFactory() throws CouldNotPerformException, InterruptedException {
        ServerConnection.newServerConnectionObservable();
        initRsb();
    }

    /**
     * Method creates a new individual trigger instance, which will be informed if there are relevant ontology changes. The method needs the trigger config.
     *
     * @param config is the trigger config, which includes the label, query and ontologyChange.
     * @return the trigger instance.
     * @throws InstantiationException is thrown in case the input is invalid (null or bad query) or the trigger could not activated.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    @Override
    public Trigger newInstance(final Object config) throws InstantiationException, InterruptedException {

        if (config == null) {
            throw new IllegalArgumentException("TriggerConfig is null!");
        }

        final TriggerConfig triggerConfig = ((TriggerConfig) config);
        checkTriggerConfig(triggerConfig);

        final OntologyRemote ontologyRemote = new OntologyRemoteImpl();
        final Trigger trigger = new TriggerImpl(ontologyRemote);

        return initTrigger(trigger, triggerConfig);
    }

    /**
     * Method creates a new individual trigger instance, which will be informed if there are relevant ontology changes. The method needs label and query of the
     * trigger only. The ontologyChange will be parsed from the input query.
     *
     * @param label is the label of the trigger.
     * @param query is the query of the trigger.
     * @return the trigger instance.
     * @throws InstantiationException is thrown in case the input is invalid (null or bad query) or the trigger could not activated.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    public Trigger newInstance(final String label, final String query) throws InstantiationException, InterruptedException {
        try {
            final OntologyChange ontologyChange = getOntologyChange(label, query);
            final OntologyRemote ontologyRemote = new OntologyRemoteImpl();
            final Trigger trigger = new TriggerImpl(ontologyRemote);
            final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel(label).setQuery(query).setDependingOntologyChange(ontologyChange).build();

            return initTrigger(trigger, triggerConfig);
        } catch (NotAvailableException | MultiException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    /**
     * Method creates an individual ontologyChange for the input query string (of the trigger with input label). The ontologyChange contains three types of
     * change values, which aggregate to (1) change categories, (2) service types and (3) unit types. Consider: the query string should not contain any negation
     * phrase, because of the parse complexity.
     *
     * @param label is the label of the trigger.
     * @param query is the query of the trigger.
     * @return the ontologyChange with change categories, service types and unit types.
     * @throws NotAvailableException is thrown in case the input label or query is null.
     * @throws MultiException is thrown in case the ontologyChange could not be parsed from the input query.
     */
    public OntologyChange getOntologyChange(final String label, final String query) throws NotAvailableException, MultiException {
        final QueryParser queryParser = new QueryParser();
        return queryParser.getOntologyChange(label, query);
    }

    /**
     * Method returns a trigger config based on the input label and query. The ontologyChange will be parsed from the input query.
     *
     * @param label is the label of the trigger.
     * @param query is the query of the trigger.
     * @return the trigger config.
     * @throws NotAvailableException is thrown in case the input label or query is null.
     * @throws MultiException is thrown in case the ontologyChange could not be parsed from the input query.
     */
    public TriggerConfig buildTriggerConfig(final String label, final String query) throws NotAvailableException, MultiException {
        return TriggerConfig.newBuilder().setLabel(label).setQuery(query).setDependingOntologyChange(getOntologyChange(label, query)).build();
    }

    /**
     * Method returns the trigger config based on input label, query and ontologyChange.
     *
     * @param label is the label of the trigger.
     * @param query is the query of the trigger.
     * @param ontologyChange is the ontologyChange (categories, service types, unit types) of the trigger.
     * @return the trigger config.
     */
    public TriggerConfig buildTriggerConfig(final String label, final String query, final OntologyChange ontologyChange) {
        return TriggerConfig.newBuilder().setLabel(label).setQuery(query).setDependingOntologyChange(ontologyChange).build();
    }

    private Trigger initTrigger(final Trigger trigger, final TriggerConfig triggerConfig) throws InterruptedException, InstantiationException {
        try {
            trigger.init(triggerConfig);
            trigger.activate();

            return trigger;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException("Could not initiate trigger instance!", ex);
        }
    }

    private void initRsb() throws CouldNotPerformException, InterruptedException {
        final RSBListener rsbListener = RSBFactoryImpl.getInstance().createSynchronizedListener(OntConfig.getOntologyRsbScope());

        rsbListener.activate();
        rsbListener.addHandler(event -> {
            try {
                ONTOLOGY_CHANGE_OBSERVABLE.notifyObservers((OntologyChange) event.getData());
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        }, false);
    }

    private void checkTriggerConfig(final TriggerConfig triggerConfig) throws InstantiationException {
        MultiException.ExceptionStack exceptionStack = null;

        try {
            if (triggerConfig.getLabel() == null) {
                throw new NotAvailableException("Trigger label is null!");
            }
        } catch (NotAvailableException ex) {
            exceptionStack = MultiException.push(this, ex, null);
        }

        try {
            if (triggerConfig.getQuery() == null) {
                throw new NotAvailableException("Trigger query is null!");
            }
        } catch (NotAvailableException ex) {
            exceptionStack = MultiException.push(this, ex, exceptionStack);
        }

        try {
            if (triggerConfig.getDependingOntologyChange() == null) {
                throw new NotAvailableException("Trigger ontologyChange is null!");
            }
        } catch (NotAvailableException ex) {
            exceptionStack = MultiException.push(this, ex, exceptionStack);
        }

        try {
            MultiException.checkAndThrow("Could not create trigger, because at least one element of the trigger config is null!", exceptionStack);
        } catch (MultiException ex) {
            throw new InstantiationException(this, ex);
        }
    }
}
