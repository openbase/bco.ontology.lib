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
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;

/**
 * @author agatting on 09.01.17.
 */
public interface OntInstanceMapping {
    /**
     * Method compares the unit(Config)s with the units (OntSuperClass Unit!) in the ontology model. The missing units
     * are convert into triples and are stored in the returned list.
     *
     * @param ontModel The ontology model.
     * @param unitConfigList The unit(Config)s, which are compared with the existing unit (OntSuperClass Unit!)
     *                       instances in the ontology.
     * @return A list with unit triple information.
     */
    List<TripleArrayList> getMissingOntTripleOfUnitsAfterInspection(final OntModel ontModel
            , final List<UnitConfig> unitConfigList);

    /**
     * Method converts the unit(Config)s into triples (OntSuperClass Unit!) and returns a triple list.
     *
     * @param ontModel The ontology model.
     * @param unitConfigList The unit(Config)s, which are convert into triples.
     * @return A list with unit triple information.
     */
    List<TripleArrayList> getMissingOntTripleOfUnits(final OntModel ontModel, final List<UnitConfig> unitConfigList);

    //TODO adapt inspection of units in context 'State' -> a unit can keep multiple states
//    /**
//     * Method compares the unit(Config)s with the units (OntSuperClass State!) in the ontology model. The missing units
//     * are convert into triples and are stored in the returning list.
//     *
//     * @param ontModel The ontology model.
//     * @param unitConfigList The unit(Config)s, which are compared with the existing unit (OntSuperClass State!)
//     *                       instances in the ontology.
//     * @return A list with triple information.
//     */
//    List<TripleArrayList> getMissingOntTripleOfStatesAfterInspection(final OntModel ontModel
//            , final List<UnitConfig> unitConfigList);

    /**
     * Method converts the unit(Config)s into triples (OntSuperClass State!) and returns a triple list.
     *
     * @param ontModel The ontology model.
     * @param unitConfigList The unit(Config)s, which are convert into triples.
     * @return A list with triple information.
     */
    List<TripleArrayList> getMissingOntTripleOfStates(final OntModel ontModel, final List<UnitConfig> unitConfigList);

    /**
     * Method compares the registry providerServices with the providerServices (OntSuperClass ProviderService!) in the
     * ontology model. The missing providerServices are convert into triples and are stored in the returned list.
     *
     * @param ontModel The ontology model.
     * @return A list with triple information.
     */
    List<TripleArrayList> getMissingOntTripleOfProviderServices(final OntModel ontModel);

    /**
     * Method returns a list of triples, which contains delete triple to remove the unitConfigs instances in the
     * ontology.
     *
     * @param unitConfigList The unitConfig list.
     * @return A list with delete triple information.
     */
    List<TripleArrayList> getDeleteTripleOfUnitsAndStates(final List<UnitConfig> unitConfigList);

    /**
     * Method returns a list of triples, which contains delete triple to remove the unitConfig instance in the ontology.
     *
     * @param unitConfig The unitConfig.
     * @return A delete triple.
     */
    TripleArrayList getDeleteTripleOfUnitsAndStates(final UnitConfig unitConfig);
}
