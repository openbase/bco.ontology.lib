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
package org.openbase.bco.ontology.lib.aboxsynchronisation.configuration;

import org.openbase.bco.ontology.lib.TripleArrayList;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;

/**
 * Created by agatting on 06.01.17.
 */
public interface OntPropertyMapping {

    /**
     * Method returns a list of triples, which contains a single delete-triple to remove old properties and multiple
     * insert-triple to add properties to the ontology. Here, all unitConfigs are processed.
     *
     * @return A list with triple information.
     */
    List<TripleArrayList> getPropertyTripleOfAllUnitConfigs();

    /**
     * Method returns a list of triples, which contains a single delete-triple to remove old properties and multiple
     * insert-triple to add properties to the ontology. Here, a single unitConfig is processed.
     *
     * @param unitConfig The unitConfig, which should be synchronized.
     * @return A list with triple information.
     */
    List<TripleArrayList> getPropertyTripleOfSingleUnitConfig(final UnitConfig unitConfig);
}
