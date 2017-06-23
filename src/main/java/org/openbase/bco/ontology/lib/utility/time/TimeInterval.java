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
package org.openbase.bco.ontology.lib.utility.time;

import org.openbase.jul.exception.NotAvailableException;

/**
 * @author agatting on 23.06.17.
 */
public class TimeInterval {

    private final long startMillis;
    private final long endMillis;

    /**
     * Method creates an object of a time-interval, based on a start and end time.
     *
     * @param startMillis is the start time in milliseconds. Must be less or equal than the end time.
     * @param endMillis is the end time in milliseconds. Must be greater or equal than the start time.
     * @throws NotAvailableException is thrown in case the start time is bigger than the end time.
     */
    public TimeInterval(final long startMillis, final long endMillis) throws NotAvailableException {
        this.startMillis = startMillis;
        this.endMillis = endMillis;

        if (startMillis > endMillis) {
            throw new NotAvailableException("The start value of the time interval is bigger than the end value!");
        }
    }

    /**
     * Getter for start time.
     *
     * @return the start time in milliseconds.
     */
    public long getStartMillis() {
        return startMillis; }

    /**
     * Getter for end time.
     *
     * @return the end time in milliseconds.
     */
    public long getEndMillis() {
        return endMillis; }

    /**
     * Method returns the overlap time of this interval and the input interval, if there is an overlap. Otherwise returns null.
     *
     * @param timeInterval is the another interval.
     * @return a timeInterval object, which is the overlap of the intervals. Returns null if there is no overlap.
     * @throws NotAvailableException is thrown in case the input interval is null or the start/end times are invalid.
     */
    public TimeInterval getOverlap(final TimeInterval timeInterval) throws NotAvailableException {

        if (overlaps(timeInterval)) {
            final long newStart = Math.max(startMillis, timeInterval.getStartMillis());
            final long newEnd = Math.min(endMillis, timeInterval.getEndMillis());

            return new TimeInterval(newStart, newEnd);
        }

        return null;
    }

    /**
     * Method compares this interval with the input interval, if there is an overlap.
     *
     * @param timeInterval is the another time interval.
     * @return true if the intervals overlap. Otherwise false.
     * @throws NotAvailableException is thrown in case the input interval is null.
     */
    public boolean overlaps(final TimeInterval timeInterval) throws NotAvailableException {

        if (timeInterval == null) {
            assert false;
            throw new NotAvailableException("TimeInterval is null.");
        }

        return (startMillis < timeInterval.getEndMillis() && timeInterval.getStartMillis() < endMillis);
    }

}
