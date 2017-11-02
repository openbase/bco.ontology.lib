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

import org.openbase.bco.ontology.lib.system.config.OntConfig.AggregationTense;
import org.openbase.jul.exception.CouldNotPerformException;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;

/**
 * @author agatting on 25.03.17.
 */
public interface Aggregation {

    /**
     * Method starts the aggregation of the ontology content.
     *
     * @param currentDays currentDays
     * @throws CouldNotPerformException CouldNotPerformException
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException ExecutionException
     */
    void startAggregation(int currentDays) throws CouldNotPerformException, InterruptedException, ExecutionException;

    void startAggregation(final OffsetDateTime dateTimeFrom, final OffsetDateTime dateTimeUntil, final AggregationTense aggregationTense);
}
