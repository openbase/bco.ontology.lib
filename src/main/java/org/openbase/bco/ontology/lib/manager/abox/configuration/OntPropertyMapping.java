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
package org.openbase.bco.ontology.lib.manager.abox.configuration;

import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;

/**
 * @author agatting on 06.01.17.
 */
public interface OntPropertyMapping {

    /**
     * Method returns a list of triples, which contains multiple insert triple to add properties of the unitConfigs in
     * the ontology.
     *
     * @param unitConfigList The unitConfig list.
     * @return A list with insert triple information.
     */
    List<TripleArrayList> getPropertyTripleOfUnitConfigs(final List<UnitConfig> unitConfigList);

    /**
     * Method returns a list of triples, which contains multiple insert triple to add properties of the unitConfig in
     * the ontology.
     *
     * @param unitConfig The unitConfig, which should be synchronized.
     * @return A list with insert triple information.
     */
    List<TripleArrayList> getPropertyTripleOfSingleUnitConfig(final UnitConfig unitConfig);

    /**
     * Method returns a list of triples, which contains delete triple to remove properties of the unitConfigs in the
     * ontology.
     *
     * @param unitConfigList The unitConfig list.
     * @return A list with delete triple information.
     */
    List<TripleArrayList> getPropertyDeleteTripleOfUnitConfigs(final List<UnitConfig> unitConfigList);

    /**
     * Method returns a list of triples, which contains delete triple to remove properties of the unitConfig in the
     * ontology.
     *
     * @param unitConfig The unitConfig, which should be synchronized.
     * @return A list with delete triple information.
     */
    List<TripleArrayList> getPropertyDeleteTripleOfSingleUnitConfig(final UnitConfig unitConfig);
}
