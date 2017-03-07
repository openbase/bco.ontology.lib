/**
 * ==================================================================
 * <p>
 * This file is part of org.openbase.bco.ontology.lib.
 * <p>
 * org.openbase.bco.ontology.lib is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 * <p>
 * org.openbase.bco.ontology.lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.ontology.lib. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.ontology.lib.manager.buffer;

import javafx.util.Pair;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

/**
 * @author agatting on 10.02.17.
 */
public interface TransactionBuffer {

    /**
     * Method creates an ConcurrentLinkedQueue and starts to upload the entries of the queue to the ontology server. After successful upload, the informer
     * is used to push a notification, if the synchronizedInformer is not null.
     *
     * @param synchronizedInformer The RSB Informer to notify the trigger group. If {@code null}, than no notification via rsb (create and start queue only).
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    void createAndStartQueue(final RSBInformer<OntologyChange> synchronizedInformer) throws CouldNotPerformException;

    /**
     * Method inserts data in the queue. The data is a pair, which contains the sparql update string and a boolean, if the update should be send to all
     * databases or send to the main database only.
     *
     * @param sparqlUpdateAndToAllDataBasesPair Pair with the sparql update and a boolean, which means {@code true} send the sparql update to all databases.
     *                                          Otherwise {@code false} the sparql update is send to the main database only.
     * @throws CouldNotProcessException If the string can't be insert into the queue.
     */
    void insertData(final Pair<String, Boolean> sparqlUpdateAndToAllDataBasesPair) throws CouldNotProcessException;

}
