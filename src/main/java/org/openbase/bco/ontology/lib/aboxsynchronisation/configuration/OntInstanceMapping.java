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

import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;

import java.util.List;

/**
 * @author agatting on 09.01.17.
 */
public interface OntInstanceMapping {
    /**
     * Method returns a list of triples, which contains the missing unitTypes in the ontology.
     *
     * @param ontModel The ontology model.
     * @return A list with triple information.
     */
    List<TripleArrayList> getMissingOntTripleOfUnitTypes(final OntModel ontModel);

    /**
     * Method returns a list of triples, which contains the missing units (at stateTypes) in the ontology.
     *
     * @param ontModel The ontology model.
     * @return A list with triple information.
     */
    List<TripleArrayList> getMissingOntTripleOfStates(final OntModel ontModel);

    /**
     * Method returns a list of triples, which contains the missing providerServices in the ontology.
     *
     * @param ontModel The ontology model.
     * @return A list with triple information.
     */
    List<TripleArrayList> getMissingOntTripleOfProviderServices(final OntModel ontModel);
}
