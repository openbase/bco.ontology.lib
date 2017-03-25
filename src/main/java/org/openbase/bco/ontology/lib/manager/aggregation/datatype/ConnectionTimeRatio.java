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

/**
 * @author agatting on 25.03.17.
 */
public class ConnectionTimeRatio {
    private final long unitConnectionTime;
    private final long timeConcept;

    /**
     * Method creates a pair of the connection time of a unit and the concept of time (hour, day, week, month, ...). Both values represent the ratio of the
     * connection time to the concept of time. E.g. 1hour/24hours.
     *
     * @param unitConnectionTime
     * @param timeConcept
     */
    public ConnectionTimeRatio(final long unitConnectionTime, final long timeConcept) {
        this.unitConnectionTime = unitConnectionTime;
        this.timeConcept = timeConcept;
    }

    /**
     * Getter for unitConnectionTime.
     *
     * @return unitConnectionTime.
     */
    public long getUnitConnectionTime() {
        return unitConnectionTime; }

    /**
     * Getter for timeConcept.
     *
     * @return timeConcept.
     */
    public long getTimeConcept() {
        return timeConcept; }
}
