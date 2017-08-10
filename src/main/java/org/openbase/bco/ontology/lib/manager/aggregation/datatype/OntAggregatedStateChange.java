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

import org.apache.jena.rdf.model.RDFNode;

/**
 * @author agatting on 08.04.17.
 */
public class OntAggregatedStateChange {

    private final String standardDeviation;
    private final String timeWeighting;
    private final String activityTime;
    private final RDFNode stateValue;
    private final String quantity;
    private final String variance;
    private final String mean;

    /**
     * Method creates an aggregated ontology observation. It contains aggregated values of state changes from a providerService in a specific
     * time frame (/period). Dependent on the kind of state value (discrete/continuous) some of the parameter are null. The parameter itself based on the
     * data type string, because read/write from/to the ontology (SPARQL).
     *
     * @param stateValue is the state value (discrete or continuous) of an state change to a specific time.
     * @param quantity is a statistical value to describe the quantity of fulfilled state changes.
     * @param activityTime is a statistical value to describe the duration of the state value in milliseconds.
     * @param variance is a statistical value to describe the variance of the state value.
     * @param standardDeviation is a statistical value to describe the standard deviation of the state value
     * @param mean is a statistical value to describe the mean of continuous state values.
     * @param timeWeighting is a statistical value to describe the duration of connection of the unit. 1 means max connection time in a specific period and
     *                      0 zero connection time.
     */
    public OntAggregatedStateChange(final RDFNode stateValue, final String quantity, final String activityTime, final String variance
            , final String standardDeviation, final String mean, final String timeWeighting) {
        this.standardDeviation = standardDeviation;
        this.timeWeighting = timeWeighting;
        this.activityTime = activityTime;
        this.stateValue = stateValue;
        this.quantity = quantity;
        this.variance = variance;
        this.mean = mean;
        //TODO insert period?
    }

    /**
     * Getter for (aggregated) observation data: stateValue (discrete or continuous).
     *
     * @return the state value.
     */
    public RDFNode getStateValue() {
        return stateValue; }

    /**
     * Getter for aggregated observation data: quantity.
     *
     * @return the number of same state changes in the specific time.
     */
    public String getQuantity() {
        return quantity; }

    /**
     * Getter for aggregated observation data: activity time.
     *
     * @return the duration of a discrete state value in milliseconds.
     */
    public String getActivityTime() {
        return activityTime; }

    /**
     * Getter for aggregated observation data: variance.
     *
     * @return the variance of a continuous state value.
     */
    public String getVariance() {
        return variance; }

    /**
     * Getter for aggregated observation data: standard deviation.
     *
     * @return the standard deviation of a continuous state value.
     */
    public String getStandardDeviation() {
        return standardDeviation; }

    /**
     * Getter for aggregated observation data: mean.
     *
     * @return the mean value of a continuous state value.
     */
    public String getMean() {
        return mean; }

    /**
     * Getter for aggregated observation data: timeWeighting.
     *
     * @return the statistical connection time of the unit from 0 - 1. Where 1 is max and 0 zero connection time of the specific period.
     */
    public String getTimeWeighting() {
        return timeWeighting; }
}
