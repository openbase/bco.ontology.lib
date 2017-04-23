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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.Period;
import org.openbase.bco.ontology.lib.system.config.StaticSparqlExpression;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author agatting on 25.03.17.
 */
public class AggregationImpl implements Aggregation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviding.class);

    public DateTime dateTimeFrom;
    public DateTime dateTimeUntil;

    private Period period;
    private int backDatedQuantity;

    public AggregationImpl() throws CouldNotPerformException, InterruptedException, JPServiceException {

//        this.period = OntConfig.PERIOD_FOR_AGGREGATION;
//        this.backDatedQuantity = OntConfig.BACKDATED_BEGINNING_OF_PERIOD;
//
//        DateTime now = new DateTime();
//
//        this.dateTimeFrom = getAdaptedDateTime(now, backDatedQuantity);
//        this.dateTimeUntil = getAdaptedDateTime(now, backDatedQuantity - 1);

//        final OntModel ontModel = OntologyToolkit.loadOntModelFromFile(null, "src/aggregationExampleFirstStageOfNormalData.owl");
//        final String timestampFrom = OntologyToolkit.addXsdDateTime(dateTimeFrom);
//        final String timestampUntil = OntologyToolkit.addXsdDateTime(dateTimeUntil);
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

    public void startAggregation(int currentDays) throws CouldNotPerformException, InterruptedException, JPServiceException {

        final DateTime dateTimeFromTest = new DateTime(2017, 1, 1, 0, 0, 0, 0);
        final DateTime dateTimeUntilTest = new DateTime(2018, 1, 1, 0, 0, 0, 0);
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

    private DateTime getAdaptedDateTime(DateTime dateTime, final int timeToReduce) throws NotAvailableException {

        switch (period) {
            case HOUR:
                dateTime = dateTime.minusHours(timeToReduce);
                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), 0, 0);
            case DAY:
                dateTime = dateTime.minusDays(timeToReduce);
                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0, 0);
//            case WEEK:
//                dateTime = dateTime.minusWeeks(timeToReduce);
//                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), dateTime.getHourOfDay(), 0, 0);
            case MONTH:
                dateTime = dateTime.minusMonths(timeToReduce);
                return new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), 1, 0, 0, 0);
            case YEAR:
                dateTime = dateTime.minusYears(timeToReduce);
                return new DateTime(dateTime.getYear(), 1, 1, 0, 0, 0);
            default:
                throw new NotAvailableException("Could not perform adaption of dateTime for aggregation. Cause period time "
                        + period.toString() + " could not be identified!");
        }
    }
}
