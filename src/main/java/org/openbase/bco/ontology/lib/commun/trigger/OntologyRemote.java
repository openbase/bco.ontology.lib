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
package org.openbase.bco.ontology.lib.commun.trigger;

import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

import java.io.IOException;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface OntologyRemote {

    /**
     * Method verifies a match of the trigger query via sparql remote to the ontology server.
     *
     * @param query is the sparql query, which is send to the ontology server.
     * @return true if the query has a match. Otherwise false.
     * @throws IOException is thrown in case there is a connection problem.
     */
    boolean match(final String query) throws IOException;

    /**
     * Method adds the observer to the server connection state observable.
     *
     * @param observer is the object, which should be informed.
     */
    void addConnectionStateObserver(final Observer<ConnectionState> observer);

    /**
     * Method removes the observer from the server connection state observable.
     *
     * @param observer is the object, which should not be informed anymore.
     */
    void removeConnectionStateObserver(final Observer<ConnectionState> observer);

    /**
     * Method adds the observer to the ontology change observable.
     *
     * @param observer is the object, which should be informed.
     */
    void addOntologyObserver(final Observer<OntologyChange> observer);

    /**
     * Method removes the observer from the ontology change observable.
     *
     * @param observer is the object, which should not be informed anymore.
     */
    void removeOntologyObserver(final Observer<OntologyChange> observer);
}
