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
package org.openbase.bco.ontology.lib.manager.aggregation.datatype;

import org.openbase.bco.ontology.lib.utility.Preconditions;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;

import java.util.HashMap;
import java.util.Optional;

/**
 * This class is used to describe and store the time aspect of the connection between units and BCO system or rather the
 * ontology manager. Means, every unit be liable to his link to the system. E.g. physical reasons (cable) can lead to
 * lost the connection. To store the connection time(s) of every unit ensures in the aggregation process the validation
 * of the aggregated state values for every consumer of the data. It's an indicator for the precision of the statistical
 * information.
 *
 * @author agatting on 23.11.17.
 */
public class OntUnitConnectionTimes {

    private final HashMap<String, Long> ontUnitConnectionTimes;

    /**
     * Constructor creates a hashMap, which describes the unitId(s) (key) and his related connection time (value) in
     * milliseconds. the information based on the connection phases of the BCO ontology.
     */
    public OntUnitConnectionTimes() {
        this.ontUnitConnectionTimes = new HashMap<>();
    }

    /**
     * Method returns the unitIds with the related connection times.
     *
     * @return the hashMap with unitIds and related connection times in milliseconds.
     */
    public HashMap<String, Long> getOntConnectionTimesMilli() {
        return ontUnitConnectionTimes;
    }

    /**
     * Method adds a new entry with unitId as key and connection time as value to the hashMap. If there is already an
     * entry with associated key, the value will be added.
     *
     * @param unitId is the unitId extracted from the ontology.
     * @param connectionTime is the connection time in milliseconds belongs to the unit.
     * @throws NotAvailableException is thrown in case the unitId is null.
     */
    public void addUnitConnectionTime(final String unitId, final long connectionTime) throws NotAvailableException {

        Preconditions.checkNotNull(unitId, "Parameter unitId is null!");

        if (ontUnitConnectionTimes.containsKey(unitId)) {
            ontUnitConnectionTimes.put(unitId, ontUnitConnectionTimes.get(unitId) + connectionTime);
        } else {
            ontUnitConnectionTimes.put(unitId, connectionTime);
        }
    }

}
