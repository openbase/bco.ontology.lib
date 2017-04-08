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
 * @author agatting on 08.04.17.
 */
public class ObservationAggDataCollection {

    private final String providerService;
    private final String stateValue;
    private final String quantity;
    private final String activityTime;
    private final String variance;
    private final String standardDeviation;
    private final String mean;
    private final String timeWeighting;

    public ObservationAggDataCollection(final String providerService, final String stateValue, final String quantity, final String activityTime
            , final String variance, final String standardDeviation, final String mean, final String timeWeighting) {
        this.providerService = providerService;
        this.stateValue = stateValue;
        this.quantity = quantity;
        this.activityTime = activityTime;
        this.variance = variance;
        this.standardDeviation = standardDeviation;
        this.mean = mean;
        this.timeWeighting = timeWeighting;
    }

    public String getProviderService() {
        return providerService; }

    public String getStateValue() {
        return stateValue; }

    public String getQuantity() {
        return quantity; }

    public String getActivityTime() {
        return activityTime; }

    public String getVariance() {
        return variance; }

    public String getStandardDeviation() {
        return standardDeviation; }

    public String getMean() {
        return mean; }

    public String getTimeWeighting() {
        return timeWeighting; }
}
