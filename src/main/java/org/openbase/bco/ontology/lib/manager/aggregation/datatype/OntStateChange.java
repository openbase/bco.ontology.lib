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


import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.openbase.bco.ontology.lib.system.config.OntConfig.ObservationType;
import org.openbase.bco.ontology.lib.utility.Preconditions;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is part of a data structure to provide the BCO ontology data (state values of sensors and actuators). The
 * custom data type {@link OntProviderServices} expresses the lower-level of the data structure (finest
 * granularity), which includes a concrete state change. Consider in addition the data types {@link OntUnits} and
 * {@link OntProviderServices}.
 *
 *           1       :      N                       1       :      N
 * {@link OntUnits} --- (includes) --- {@link OntProviderServices} --- (includes) --- {@link OntStateChange}
 *
 * @author agatting on 15.09.17.
 */
public class OntStateChange<T> {

    private final T type;
    private final ObservationType observationType;

    /**
     * Constructor creates a stateChange object, which describes his value parts based on the defined type.
     *
     * @param type is the type/subclass of the ontStateChange. It should be one of the classes
     *             {@link Discrete} , {@link Continuous}, {@link AggregatedDiscrete} or {@link AggregatedContinuous}.
     * @throws InvalidStateException is thrown in case the input parameter is invalid (null or has wrong type).
     */
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

    /**
     * Method returns the generic type class parameter (type of the state change).
     *
     * @return one of {@link Discrete}, {@link Continuous}, {@link AggregatedDiscrete} or {@link AggregatedContinuous}.
     */
    public T getType() {
        return type;
    }

    /**
     * Method returns the type of the state change as enum type {@link ObservationType}.
     *
     * @return the concrete enum type of {@link ObservationType}.
     */
    public ObservationType getObservationType() {
        return observationType;
    }

    /**
     * This subclass defines an concrete instance/type of a state change of {@link OntStateChange}. It should be
     * used, if the state change is not manipulated as far and the state value based on a discrete value (e.g. BCO
     * values like ON, OFF, OPEN, CLOSED).
     */
    public static class Discrete {

        private final OffsetDateTime timestamp;
        private final Resource stateValue;

        /**
         * Constructor creates an discrete state change instance, which based on not manipulated state values (e.g.
         * ON, OFF, ...) to store them. The information parts (input argument) will be convert to specific data types
         * (see getter of this class).
         *
         * @param timestamp is the timestamp at which the state change occurred. Must be parsable to OffsetDateTime.
         * @param stateValue is the value (resource type) of the state change. Must be based on rdf resource.
         * @throws MultiException is thrown in case at least one input parameter is null/invalid or the state value
         * isn't a resource (discrete value).
         */
        public Discrete(final String timestamp, final RDFNode stateValue) throws MultiException {
            final ExceptionStack exceptionStack = Preconditions.multipleCheckNotNull(this, null, timestamp, stateValue);

            this.timestamp = Preconditions.callFunction(OffsetDateTime::parse, timestamp, this, exceptionStack);
            this.stateValue = Preconditions.callSupplier(stateValue::asResource, this, exceptionStack);

            MultiException.checkAndThrow("Input argument invalid. Null or no resource type!", exceptionStack);
        }

        /**
         * Method returns the timestamp at which the state change occurred.
         *
         * @return the timestamp with the format "yyyy-MM-dd'T'HH:mm:ss.SSSXXX".
         */
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        /**
         * Method returns the state value of the state change.
         *
         * @return the state value as rdf resource.
         */
        public Resource getStateValue() {
            return stateValue;
        }
    }

    /**
     * This subclass defines an concrete instance/type of a state change of {@link OntStateChange}. It should be
     * used, if the state change is not manipulated as far and the state value(s) based on continuous values
     * (e.g. numbers like hsb).
     */
    public static class Continuous {

        private final OffsetDateTime timestamp;
        private final List<Literal> stateValues;

        /**
         * Constructor creates an continuous state change instance, which based on not manipulated state values
         * (literals like e.g. 17.35^^xsd:int) to store them. The information parts (input argument) will be convert
         * to specific data types (see getter of this class).
         *
         * @param timestamp is the timestamp at which the state change occurred. Must be parsable to OffsetDateTime.
         * @param stateValues is the value (literal type) of the state change. The rdf nodes must be based on literals.
         * @throws MultiException is thrown in case at least one input parameter is null/invalid or one state value
         * isn't a literal (continuous value).
         */
        public Continuous(final String timestamp, final List<RDFNode> stateValues) throws MultiException {
            final ExceptionStack exceptionStack =
                    Preconditions.multipleCheckNotNull(this, null, timestamp, stateValues);

            this.timestamp = Preconditions.callFunction(OffsetDateTime::parse, timestamp, this, exceptionStack);
            this.stateValues = new ArrayList<>();

            for (final RDFNode stateValue : stateValues) {
                // convert rdf node to literal and add to list. Invalid nodes will be stacked
                this.stateValues.add(Preconditions.callSupplier(stateValue::asLiteral, this, exceptionStack));
            }

            MultiException.checkAndThrow("Input argument(s) invalid. Null or no literal type!", exceptionStack);
        }

        /**
         * Method returns the timestamp at which the state change occurred.
         *
         * @return the timestamp with the format "yyyy-MM-dd'T'HH:mm:ss.SSSXXX".
         */
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        /**
         * Method returns the state value(s) of the state change.
         *
         * @return the state value(s) as literal list.
         */
        public List<Literal> getStateValues() {
            return stateValues;
        }

        /**
         * Method converts the rdfNode to literal and adds to the stateValue list.
         *
         * @param stateValue to add to the list. Must be a literal.
         * @throws NotAvailableException is thrown in case the parameter is invalid.
         */
        public void addStateValue(final RDFNode stateValue) throws NotAvailableException {
            Preconditions.checkNotNull(stateValue, "Input stateValue is null");
            stateValues.add(Preconditions.callSupplier(stateValue::asLiteral));
        }

    }

    /**
     * This subclass defines an concrete instance/type of a state change of {@link OntStateChange}. It should be
     * used, if the state change was manipulated as far (already aggregated) and the state value based on a discrete
     * value (e.g. BCO values like ON, OFF, OPEN, CLOSED).
     */
    public static class AggregatedDiscrete {

        private final double timeWeighting;
        private final long activityTime;
        private final int quantity;
        private final Resource stateValue;

        /**
         * Constructor creates an discrete state change instance, which based on manipulated (already aggregated) state
         * values (e.g. ON, OFF, ...) to store them. The information parts (input argument) will be convert to specific
         * data types (see getter of this class).
         *
         * @param timeWeighting is the indicator how long the unit keeps connection to the system with value range
         *                      [0..1]. E.g. an value of 0.6 means that there was connection (and observed state
         *                      changes) in 60% of the aggregated time frame. 40% of the time was not observed so that
         *                      this value is used as inaccuracy.
         * @param activityTime is the time how long the discrete state value was activated in the aggregated time frame.
         * @param quantity is the number of activation of the based state value source in the aggregated time frame.
         * @param stateValue is the specific discrete state value.
         * @throws MultiException is thrown in case at least one input parameter is null/invalid or the state value
         * isn't a resource (discrete value).
         */
        public AggregatedDiscrete(final String timeWeighting, final String activityTime,
                                  final String quantity, final RDFNode stateValue) throws MultiException {
            final ExceptionStack exceptionStack =
                    Preconditions.multipleCheckNotNull(this, null, timeWeighting, activityTime, quantity, stateValue);

            this.timeWeighting = Optional.ofNullable(Preconditions.callFunction(Double::parseDouble, timeWeighting,
                    this, exceptionStack)).orElse(0.0);
            this.activityTime = Optional.ofNullable(Preconditions.callFunction(Long::parseLong, activityTime, this,
                    exceptionStack)).orElse(0L);
            this.quantity = Optional.ofNullable(Preconditions.callFunction(Integer::parseInt, quantity, this,
                    exceptionStack)).orElse(0);
            this.stateValue = (stateValue == null) ? null : Preconditions.callSupplier(stateValue::asResource, this,
                    exceptionStack);

            MultiException.checkAndThrow("Input is invalid.", exceptionStack);
        }

        /**
         * Method returns the time weighting. Look at argument {@link AggregatedDiscrete#timeWeighting} for information.
         *
         * @return the time weighting.
         */
        public double getTimeWeighting() {
            return timeWeighting;
        }

        /**
         * Method returns the activity time. Look at argument {@link AggregatedDiscrete#activityTime} for information.
         *
         * @return the activity time.
         */
        public long getActivityTime() {
            return activityTime;
        }

        /**
         * Method returns the quantity. Look at argument {@link AggregatedDiscrete#quantity} for information.
         *
         * @return the quantity.
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Method returns the discrete state value.
         *
         * @return the state value.
         */
        public Resource getStateValue() {
            return stateValue;
        }
    }

    /**
     * This subclass defines an concrete instance/type of a state change of {@link OntStateChange}. It should be
     * used, if the state change was manipulated as far (already aggregated) and the state value(s) based on continuous
     * value(s) (e.g. numbers like hsb).
     */
    public static class AggregatedContinuous {

        private final double mean;
        private final double variance;
        private final double standardDeviation;
        private final double timeWeighting;
        private final int quantity;
        private final Literal stateValue;

        /**
         * Constructor creates an continuous state change instance, which based on manipulated (already aggregated)
         * state values (e.g. numbers like hsb) to store them. The information parts (input argument) will be convert
         * to specific data types (see getter of this class).
         *
         * @param mean of all state values from the same source, which are stored about the aggregated time frame.
         *             Lossy Statistical information.
         * @param variance of all state values from the same source, which are stored about the aggregated time frame.
         *                 Lossy Statistical information.
         * @param standardDeviation of all state values from the same source, which are stored about the aggregated
         *                          time frame. Lossy Statistical information.
         * @param timeWeighting is the indicator how long the unit keeps connection to the system with value range
         *                      [0..1]. E.g. an value of 0.6 means that there was connection (and observed state
         *                      changes) in 60% of the aggregated time frame. 40% of the time was not observed so that
         *                      this value is used as inaccuracy.
         * @param quantity is the number of activation of the based state value source in the aggregated time frame.
         * @param stateValue is the specific continuous state value.
         * @throws MultiException is thrown in case at least one input parameter is null/invalid or the state value
         * isn't a literal (continuous value).
         */
        public AggregatedContinuous(final String mean, final String variance,
                                    final String standardDeviation, final String timeWeighting,
                                    final String quantity, final RDFNode stateValue) throws MultiException {
            final ExceptionStack exceptionStack = Preconditions.multipleCheckNotNull(this, null, mean, variance,
                    standardDeviation, timeWeighting, quantity, stateValue);

            this.mean = Optional.ofNullable(Preconditions.callFunction(Double::parseDouble, mean, this,
                    exceptionStack)).orElse(0.0);
            this.variance = Optional.ofNullable(Preconditions.callFunction(Double::parseDouble, variance, this,
                    exceptionStack)).orElse(0.0);
            this.standardDeviation = Optional.ofNullable(Preconditions.callFunction(Double::parseDouble,
                    standardDeviation, this, exceptionStack)).orElse(0.0);
            this.timeWeighting = Optional.ofNullable(Preconditions.callFunction(Double::parseDouble, timeWeighting,
                    this, exceptionStack)).orElse(0.0);
            this.quantity = Optional.ofNullable(Preconditions.callFunction(Integer::parseInt, quantity, this,
                    exceptionStack)).orElse(0);
            this.stateValue = (stateValue == null) ? null : Preconditions.callSupplier(stateValue::asLiteral, this,
                    exceptionStack);

            MultiException.checkAndThrow("Input is invalid.", exceptionStack);
        }

        /**
         * Method returns the mean value. Look at argument {@link AggregatedContinuous#mean}.
         *
         * @return the mean value.
         */
        public double getMean() {
            return mean;
        }

        /**
         * Method returns the variance. Look at argument {@link AggregatedContinuous#variance}.
         *
         * @return the variance.
         */
        public double getVariance() {
            return variance;
        }

        /**
         * Method returns the standard deviation. Look at argument {@link AggregatedContinuous#standardDeviation}.
         *
         * @return the standard deviation.
         */
        public double getStandardDeviation() {
            return standardDeviation;
        }

        /**
         * Method returns the time weighting. Look at argument {@link AggregatedContinuous#timeWeighting}.
         *
         * @return the time weighting.
         */
        public double getTimeWeighting() {
            return timeWeighting;
        }

        /**
         * Method returns the quantity. Look at argument {@link AggregatedContinuous#quantity}.
         *
         * @return the quantity.
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Method returns the continuous state value.
         *
         * @return the state value as literal.
         */
        public Literal getStateValue() {
            return stateValue;
        }
    }

}
