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
import org.openbase.jul.exception.MultiException;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChangeTypes.*;

import java.util.List;

/**
 * This interface is part of a data structure to provide the BCO ontology data (state values of sensors and actuators).
 * The custom data type {@link OntProviderServices} expresses the lower-level of the data structure (finest
 * granularity), which includes a concrete state change. Consider in addition the data types {@link OntUnits} and
 * {@link OntProviderServices}.
 * 
 *            1       :      N                                       1       :      N
 * {@link OntUnits} --- (includes) --- {@link OntProviderServices} --- (includes) --- {@link OntStateChange}
 * 
 * Additionally, the interface is used to build adapter for the different ontStateChange types. If new types should be
 * created, do it here. Example to generate and use an ontStateChange:
 * <pre>
 * {@code
 * final OntStateChange ontStateChangeExample = OntStateChange.asDiscrete(null, null);
 * ((Discrete) ontStateChangeExample.getOntStateChange()).getStateValue();
 * }
 * </pre>
 *
 * @author agatting on 15.09.17.
 */
public interface OntStateChange {

    /**
     * Method returns the type of the state change as enum type {@link ObservationType}.
     *
     * @return the concrete enum type of {@link ObservationType}.
     */
    ObservationType getObservationType();

    /**
     * Method returns the ontStateChange object. Cast this returned object to use the deposited data.
     * Use {@link OntStateChange#getObservationType()} to identify the cast type (e.g. {@link Discrete},
     * {@link Continuous}, {@link AggregatedDiscrete} or {@link AggregatedContinuous})
     *
     * @return an ontStateChange object. Cast to use it.
     */
    Object getOntStateChange();

    /**
     * Method is used to generate an ontStateChange instance based on the type {@link Discrete}.
     *
     * @param timestamp is the timestamp at which the state change occurred. Must be parsable to OffsetDateTime.
     * @param stateValue is the value (resource type) of the state change. Must be based on rdf resource.
     * @return an ontStateChange object based on {@link Discrete}.
     * @throws MultiException is thrown in case at least one input parameter is null/invalid or the state value
     * isn't a resource (/discrete value).
     */
    static OntStateChange asDiscrete(final String timestamp, final RDFNode stateValue) throws MultiException {

        final Discrete discreteState = new Discrete(timestamp, stateValue);

        return new OntStateChange() {

            /**
             * {@inheritDoc}
             */
            @Override
            public ObservationType getObservationType() {
                return ObservationType.DISCRETE;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Discrete getOntStateChange() {
                return discreteState;
            }
        };
    }

    /**
     * Method is used to generate an ontStateChange instance based on the type {@link Continuous}.
     *
     * @param timestamp is the timestamp at which the state change occurred. Must be parsable to OffsetDateTime.
     * @param stateValues is the value (literal type) of the state change. The rdf nodes must be based on literals.
     * @return an ontStateChange object based on {@link Continuous}.
     * @throws MultiException is thrown in case at least one input parameter is null/invalid or one state value
     * isn't a literal (/continuous value).
     */
    static OntStateChange asContinuous(final String timestamp, final List<RDFNode> stateValues) throws MultiException {

        final Continuous continuousState = new Continuous(timestamp, stateValues);

        return new OntStateChange() {

            /**
             * {@inheritDoc}
             */
            @Override
            public ObservationType getObservationType() {
                return ObservationType.CONTINUOUS;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Continuous getOntStateChange() {
                return continuousState;
            }
        };
    }

    /**
     * Method is used to generate an ontStateChange instance based on the type {@link AggregatedDiscrete}.
     *
     * @param timeWeighting is the indicator how long the unit keeps connection to the system with value range
     *                      [0..1]. E.g. an value of 0.6 means that there was connection (and observed state
     *                      changes) in 60% of the aggregated time frame. 40% of the time was not observed so that
     *                      this value is used as inaccuracy.
     * @param activityTime is the time how long the discrete state value was activated in the aggregated time frame.
     * @param quantity is the number of activation of the based state value source in the aggregated time frame.
     * @param stateValue is the specific discrete state value.
     * @return an ontStateChange object based on {@link AggregatedDiscrete}.
     * @throws MultiException is thrown in case at least one input parameter is null/invalid or the state value
     * isn't a resource (/discrete value).
     */
    static OntStateChange asAggregatedDiscrete(final String timeWeighting, final String activityTime,
                                               final String quantity, final RDFNode stateValue) throws MultiException {

        final AggregatedDiscrete aggregatedDiscreteState =
                new AggregatedDiscrete(timeWeighting, activityTime, quantity, stateValue);

        return new OntStateChange() {

            /**
             * {@inheritDoc}
             */
            @Override
            public ObservationType getObservationType() {
                return ObservationType.AGGREGATED_DISCRETE;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public AggregatedDiscrete getOntStateChange() {
                return aggregatedDiscreteState;
            }
        };
    }

    /**
     * Method is used to generate an ontStateChange instance based on the type {@link AggregatedContinuous}.
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
     * @return an ontStateChange object based on {@link AggregatedContinuous}.
     * @throws MultiException is thrown in case at least one input parameter is null/invalid or the state value
     * isn't a literal (/continuous value).
     */
    static OntStateChange asAggregatedContinuous(final String mean, final String variance,
                                                 final String standardDeviation, final String timeWeighting,
                                                 final String quantity,
                                                 final RDFNode stateValue) throws MultiException {

        final AggregatedContinuous aggregatedContinuousState =
                new AggregatedContinuous(mean, variance, standardDeviation, timeWeighting, quantity, stateValue);

        return new OntStateChange() {

            /**
             * {@inheritDoc}
             */
            @Override
            public ObservationType getObservationType() {
                return ObservationType.AGGREGATED_CONTINUOUS;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public AggregatedContinuous getOntStateChange() {
                return aggregatedContinuousState;
            }
        };
    }

}
