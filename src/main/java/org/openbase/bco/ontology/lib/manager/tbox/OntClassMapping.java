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

import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import rst.domotic.unit.UnitConfigType;

import java.util.List;

/**
 * @author agatting on 12.05.17.
 */
public interface OntClassMapping {

    OntModel extendTBoxViaOntModel(final List<UnitConfigType.UnitConfig> unitConfigList);

    List<TripleArrayList> extendTBoxViaTriple(final List<UnitConfigType.UnitConfig> unitConfigs);
}
