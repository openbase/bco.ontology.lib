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
package org.openbase.bco.ontology.lib.trigger.webcommun;

import org.openbase.bco.ontology.lib.config.OntologyChange.Category;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote;

import java.io.IOException;
import java.util.Collection;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface OntologyRemote {

    boolean match(final String query) throws IOException;

    void addConnectionStateObserver(Observer<Remote.ConnectionState> observer);

    void removeConnectionStateObserver(Observer<Remote.ConnectionState> observer);

    void addOntologyObserver(Observer<Collection<Category>> observer);

    void removeOntologyObserver(Observer<Collection<Category>> observer);
}
