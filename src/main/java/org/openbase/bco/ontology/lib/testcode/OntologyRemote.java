package org.openbase.bco.ontology.lib.testcode;

import java.util.Collection;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote;

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
