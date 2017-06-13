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
package org.openbase.bco.ontology.lib.manager.tbox;

import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;

/**
 * @author agatting on 12.05.17.
 */
public interface OntClassMapping {

    /**
     * Method returns the triple information to insert the ontology classes based on the given unitConfigs.
     *
     * @param unitConfigs The unitConfigs, which contains the unit types or rather ontology class information.
     * @return a list of triples, which contains the ontology classes.
     */
    List<RdfTriple> getUnitTypeClasses(final List<UnitConfig> unitConfigs);

    /**
     * Method returns the triple information to insert ALL ontology classes.
     *
     * @return a list of triples, which contains the ontology classes.
     */
    List<RdfTriple> getUnitTypeClasses();
}
