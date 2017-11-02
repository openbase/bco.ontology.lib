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
import org.openbase.bco.ontology.lib.system.config.OntConfig.ObservationType;
import org.openbase.bco.ontology.lib.utility.Preconditions;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author agatting on 15.09.17.
 */
public class OntStateChange<T> {

    final private T type;
    private final ObservationType observationType;

    public OntStateChange(final T type) throws InvalidStateException {
        this.type = Preconditions.checkNotNull(type, "Generic type parameter is null!");

        if (type instanceof Discrete) {
            this.observationType = ObservationType.DISCRETE;
        } else if (type instanceof Continuous) {
            this.observationType = ObservationType.CONTINUOUS;
        } else if (type instanceof AggregatedDiscrete) {
            this.observationType = ObservationType.AGGREGATED_DISCRETE;
        } else if (type instanceof AggregatedContinuous) {
            this.observationType = ObservationType.AGGREGATED_CONTINUOUS;
        } else {
            throw new InvalidStateException("Generic class is non of the observation types. Found: " + type.getClass());
        }
    }

    public T getType() {
        return type;
    }

    public ObservationType getObservationType() {
        return observationType;
    }

    public static class Discrete {

        private final OffsetDateTime timestamp;
        private final RDFNode stateValue;

        public Discrete(final String timestamp, final RDFNode stateValue) throws MultiException, NotAvailableException {
//            ExceptionStack exceptionStack = Preconditions.checkNotNull(timestamp, "Parameter Timestamp is null!", null);
//            exceptionStack = Preconditions.checkNotNull(stateValue, "Parameter stateValue is null!", exceptionStack);
//            MultiException.checkAndThrow("Input is invalid.", exceptionStack);

            this.timestamp = OffsetDateTime.parse(Preconditions.checkNotNull(timestamp, "Timestamp is null!"));
            this.stateValue = Preconditions.checkNotNull(stateValue, "State value is null!");
        }

        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public RDFNode getStateValue() {
            return stateValue;
        }
    }

    public static class Continuous {

        private final OffsetDateTime timestamp;
        private final List<RDFNode> stateValues;

        public Continuous(final String timestamp, final List<RDFNode> stateValues) throws InvalidStateException {
            this.timestamp = OffsetDateTime.parse(Preconditions.checkNotNull(timestamp, "Timestamp is null!"));
            this.stateValues = stateValues;
        }

        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public List<RDFNode> getStateValues() {
            return stateValues;
        }

    }

    public static class AggregatedDiscrete {

        private final String timeWeighting;
        private final String activityTime;
        private final String quantity;
        private final RDFNode stateValue;

        public AggregatedDiscrete(final String timeWeighting, final String activityTime, final String quantity, final RDFNode stateValue) {
            this.timeWeighting = timeWeighting;
            this.activityTime = activityTime;
            this.quantity = quantity;
            this.stateValue = stateValue;
        }

        public String getTimeWeighting() {
            return timeWeighting;
        }

        public String getActivityTime() {
            return activityTime;
        }

        public String getQuantity() {
            return quantity;
        }

        public RDFNode getStateValue() {
            return stateValue;
        }
    }

    public static class AggregatedContinuous {

        private final String mean;
        private final String variance;
        private final String standardDeviation;
        private final String timeWeighting;
        private final String quantity;
        private final RDFNode stateValue;

        public AggregatedContinuous(final String mean, final String variance, final String standardDeviation, final String timeWeighting
                , final String quantity, final RDFNode stateValue) {
            this.mean = mean;
            this.variance = variance;
            this.standardDeviation = standardDeviation;
            this.timeWeighting = timeWeighting;
            this.quantity = quantity;
            this.stateValue = stateValue;
        }

        public String getMean() {
            return mean;
        }

        public String getVariance() {
            return variance;
        }

        public String getStandardDeviation() {
            return standardDeviation;
        }

        public String getTimeWeighting() {
            return timeWeighting;
        }

        public String getQuantity() {
            return quantity;
        }

        public RDFNode getStateValue() {
            return stateValue;
        }
    }

}
