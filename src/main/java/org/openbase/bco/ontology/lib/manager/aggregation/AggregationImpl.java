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
package org.openbase.bco.ontology.lib.manager.aggregation;

import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationDataCollection;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * @author agatting on 25.03.17.
 */
public class AggregationImpl implements Aggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviding.class);

    public DateTime dateTimeFrom;
    public DateTime dateTimeUntil;

    private Period period;
    private int quantityPeriod;
    private int backDatedQuantity;

    public AggregationImpl() {

        this.period = Period.DAY;
        this.quantityPeriod = OntConfig.QUANTITY_OF_PERIOD;
        this.backDatedQuantity = OntConfig.BACKDATED_BEGINNING_OF_PERIOD;

        final DateTime now = new DateTime();
        final int diffPeriodUntil = backDatedQuantity - quantityPeriod;

        this.dateTimeFrom = getAdaptedDateTime(now, diffPeriodUntil);
        this.dateTimeUntil = getAdaptedDateTime(now, diffPeriodUntil);
    }

    /**
     * @param period The time frame, which should be aggregated (hour, day, week, ...).
     * @param quantityPeriod The number of period (one day, two days, ten days, ...). Must be less than backDatedQuantity!
     * @param backDatedQuantity The back-dated beginning of the aggregation (before two days, before 20 days, ...). Must be bigger than quantityPeriod!
     * @throws CouldNotPerformException CouldNotPerformException
     */
    public void setTimeFrame(final Period period, final int quantityPeriod, final int backDatedQuantity) throws CouldNotPerformException {

        if (quantityPeriod > backDatedQuantity) {
            throw new CouldNotPerformException("Could not aggregate, because set time frame is illegal! Should aggregate time frame of " + quantityPeriod + " "
                    + period.toString() + " back-dated from " + backDatedQuantity + " " + period.toString() + "!");
        }

        this.period = period;
        this.quantityPeriod = quantityPeriod;
        this.backDatedQuantity = backDatedQuantity;

        final DateTime now = new DateTime();
        final int diffPeriodUntil = backDatedQuantity - quantityPeriod;

        this.dateTimeFrom = getAdaptedDateTime(now, diffPeriodUntil);
        this.dateTimeUntil = getAdaptedDateTime(now, diffPeriodUntil);
    }

    public void startAgg() {

    }

    private void collect() {
        final DataProviding dataProviding = new DataProviding(dateTimeFrom, dateTimeUntil);

        final HashMap<String, Long> connTimeEachUnit = dataProviding.getConnectionTimeForEachUnit();
        final HashMap<String, List<ObservationDataCollection>> observationsEachUnit = dataProviding.getObservationsForEachUnit();

        final DataAssignation dataAssignation = new DataAssignation(dateTimeFrom, dateTimeUntil);

    }

    private DateTime getAdaptedDateTime(DateTime dateTime, final int reducedTime) {

        switch (period) {
            case HOUR:
                dateTime = dateTime.minusHours(reducedTime);
                break;
            case DAY:
                dateTime = dateTime.minusDays(reducedTime);
                break;
            case WEEK:
                dateTime = dateTime.minusWeeks(reducedTime);
                break;
            case MONTH:
                dateTime = dateTime.minusMonths(reducedTime);
                break;
            case YEAR:
                dateTime = dateTime.minusYears(reducedTime);
                break;
            default:
                try {
                    throw new NotAvailableException("Could not perform adaption of dateTime for aggregation. Cause period time "
                            + OntConfig.PERIOD_FOR_AGGREGATION + " could not be identified!");
                } catch (NotAvailableException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
        }

        return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), 0, 0);
    }
}
