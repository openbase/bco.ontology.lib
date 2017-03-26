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

import org.joda.time.DateTime;

/**
 * @author agatting on 25.03.17.
 */
public class ConnectionTimeRatio {
    private final DateTime dateTimeFrom;
    private final DateTime dateTimeUntil;
    private final long unitConnectionTime;

    /**
     *
     * @param dateTimeFrom
     * @param dateTimeUntil
     * @param unitConnectionTime
     */
    public ConnectionTimeRatio(final DateTime dateTimeFrom, final DateTime dateTimeUntil, final long unitConnectionTime) {
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;
        this.unitConnectionTime = unitConnectionTime;
    }

    /**
     * Getter for dateTimeFrom.
     *
     * @return dateTimeFrom.
     */
    public DateTime getDateTimeFrom() {
        return dateTimeFrom; }

    /**
     * Getter for dateTimeUntil.
     *
     * @return dateTimeUntil.
     */
    public DateTime getDateTimeUntil() {
        return dateTimeUntil; }

    /**
     * Getter for unitConnectionTime.
     *
     * @return unitConnectionTime.
     */
    public long getUnitConnectionTime() {
        return unitConnectionTime; }
}
