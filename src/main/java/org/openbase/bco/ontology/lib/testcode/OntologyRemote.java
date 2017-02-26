package org.openbase.bco.ontology.lib.testcode;

import org.openbase.bco.ontology.lib.trigger.TriggerConfig;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote;

import java.util.Collection;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface OntologyRemote {

    public boolean match(final String query);

    public void addConnectionStateObserver(Observer<Remote.ConnectionState> observer);

    public void removeConnectionStateObserver(Observer<Remote.ConnectionState> observer);

    public void addOntologyObserver(Observer<Collection<TriggerConfig.ChangeCategory>> observer);

    public void removeOntologyObserver(Observer<Collection<TriggerConfig.ChangeCategory>> observer);
}
