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
package org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.extension.rsb.iface.RSBInformer;

/**
 * @author agatting on 10.02.17.
 */
public interface TransactionBuffer {

    /**
     * Method creates an ConcurrentLinkedQueue and starts to upload the entries of the queue to the ontology server.
     * After successful upload, the informer is used to push a notification.
     *
     * @param synchronizedInformer The RSB Informer to notify the trigger group
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    void createAndStartQueue(final RSBInformer<String> synchronizedInformer) throws CouldNotPerformException;

    /**
     * Method creates an ConcurrentLinkedQueue and starts to upload the entries of the queue to the ontology server.
     *
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    void createAndStartQueue() throws CouldNotPerformException;

    /**
     * Method inserts data in the queue.
     *
     * @param sparqlUpdateExpr The string that should be insert.
     * @throws CouldNotProcessException If the string can't be insert into the queue.
     */
    void insertData(final String sparqlUpdateExpr) throws CouldNotProcessException;

}
