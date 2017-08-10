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

import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;

/**
 * @author agatting on 25.03.17.
 */
public class AggregationImpl implements Aggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviding.class);

    public OffsetDateTime dateTimeFrom;
    public OffsetDateTime dateTimeUntil;

    private Period period;
    private int backDatedQuantity;

    public AggregationImpl() throws CouldNotPerformException, InterruptedException {

//        this.period = OntConfig.PERIOD_FOR_AGGREGATION;
//        this.backDatedQuantity = OntConfig.BACKDATED_BEGINNING_OF_PERIOD;
//
//        DateTime now = new DateTime();
//
//        this.dateTimeFrom = getAdaptedDateTime(now, backDatedQuantity);
//        this.dateTimeUntil = getAdaptedDateTime(now, backDatedQuantity - 1);

//        final OntModel ontModel = StringModifier.loadOntModelFromFile(null, "src/aggregationExampleFirstStageOfNormalData.owl");
//        final String timestampFrom = StringModifier.addXsdDateTime(dateTimeFrom);
//        final String timestampUntil = StringModifier.addXsdDateTime(dateTimeUntil);
//        final Query query = QueryFactory.create(StaticSparqlExpression.getAllAggObs(Period.DAY.toString().toLowerCase(), timestampFrom, timestampUntil));
//        final QueryExecution queryExecution = QueryExecutionFactory.create(query, ontModel);
//        final ResultSet resultSet = queryExecution.execSelect();
//
//        ResultSetFormatter.out(System.out, resultSet, query);

//        startAggregation();
    }

//    /**
//     * @param period The time frame, which should be aggregated (hour, day, week, ...).
////     * @param quantityPeriod The number of period (one day, two days, ten days, ...). Must be less than backDatedQuantity!
//     * @param backDatedQuantity The back-dated beginning of the aggregation (before two days, before 20 days, ...). Must be bigger than quantityPeriod!
//     * @throws CouldNotPerformException CouldNotPerformException
//     */
//    public void setTimeFrame(final Period period, final int backDatedQuantity) throws CouldNotPerformException {
//
//
//        this.period = period;
//        this.backDatedQuantity = backDatedQuantity;
//
//        final DateTime now = new DateTime();
//
//        this.dateTimeFrom = getAdaptedDateTime(now, backDatedQuantity);
//        this.dateTimeUntil = getAdaptedDateTime(now, backDatedQuantity);
//    }

    public void startAggregation(int currentDays) throws CouldNotPerformException, InterruptedException, ExecutionException {
        final OffsetDateTime dateTimeFromTest = OffsetDateTime.of(LocalDateTime.parse("2017-01-01T00:00:00.000"), OffsetDateTime.now().getOffset());
        final OffsetDateTime dateTimeUntilTest = OffsetDateTime.of(LocalDateTime.parse("2018-01-01T00:00:00.000"), OffsetDateTime.now().getOffset());
        currentDays += 1;

        if (currentDays % 7 == 0) {
            final Period periodTest = Period.WEEK;
            System.out.println("Start aggregation at day: " + currentDays + " : " + periodTest.toString());
            new DataTripleCollection(dateTimeFromTest, dateTimeUntilTest, periodTest);
        }
        if (currentDays % 28 == 0) {
            final Period periodTest = Period.MONTH;
            System.out.println("Start aggregation at day: " + currentDays + " : " + periodTest.toString());
            new DataTripleCollection(dateTimeFromTest, dateTimeUntilTest, periodTest);
        }
        if (currentDays % 364 == 0) {
            final Period periodTest = Period.YEAR;
            System.out.println("Start aggregation at day: " + currentDays + " : " + periodTest.toString());
            new DataTripleCollection(dateTimeFromTest, dateTimeUntilTest, periodTest);
        }

//        final Period periodTest = Period.DAY;
//        new DataTripleCollection(dateTimeFrom, dateTimeUntil, period);
//        new DataTripleCollection(dateTimeFromTest, dateTimeUntilTest, periodTest);
    }

//    private OffsetDateTime getAdaptedDateTime(OffsetDateTime dateTime, final int timeToReduce) throws NotAvailableException {
//
//        switch (period) {
//            case HOUR:
//                dateTime = dateTime.minusHours(timeToReduce);
//                return OffsetDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), dateTime.getHour(), 0, 0, 0, ZoneOffset.UTC);
//            case DAY:
//                dateTime = dateTime.minusDays(timeToReduce);
//                return OffsetDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(), 0, 0, 0, 0, ZoneOffset.UTC);
////            case WEEK:
////                dateTime = dateTime.minusWeeks(timeToReduce);
////                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), 0, 0);
//            case MONTH:
//                dateTime = dateTime.minusMonths(timeToReduce);
//                return OffsetDateTime.of(dateTime.getYear(), dateTime.getMonthValue(), 1, 0, 0, 0, 0, ZoneOffset.UTC);
//            case YEAR:
//                dateTime = dateTime.minusYears(timeToReduce);
//                return OffsetDateTime.of(dateTime.getYear(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
//            default:
//                throw new NotAvailableException("Could not perform adaption of dateTime for aggregation. Cause period time "
//                        + period.toString() + " could not be identified!");
//        }
//    }
}
