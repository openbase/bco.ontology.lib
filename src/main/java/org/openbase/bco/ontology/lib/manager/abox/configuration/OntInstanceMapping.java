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

import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;

/**
 * @author agatting on 09.01.17.
 */
public interface OntInstanceMapping {

    //TODO illegalArgumentException?

    /**
     * Method bundles {@link #getMissingUnitTriplesViaOntModel(OntModel, List)}, {@link #getMissingStateTriplesViaOntModel(OntModel, List)} and
     * {@link #getMissingServiceTriplesViaOntModel(OntModel)} to one triple list.
     *
     * @param ontModel The ontModel for comparing of ontology elements.
     * @param unitConfigs The unitConfigs.
     * @return A list of triple to update the ontology with unit config data.
     * @throws CouldNotPerformException Exception is thrown, if the needed ontClass could not be extract from the ontModel (empty or not available).
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getAllMissingConfigTriplesViaOntModel(final OntModel ontModel, final List<UnitConfig> unitConfigs)
            throws CouldNotPerformException, IllegalArgumentException;

    /**
     * Method bundles {@link #getMissingUnitTriples(List)}, {@link #getMissingStateTriples(List)} and {@link #getMissingServiceTriples(List)} to one triple list.
     *
     * @param unitConfigs The unitConfigs.
     * @return A list of triple to update the ontology with unit config data.
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getAllMissingConfigTriples(final List<UnitConfig> unitConfigs) throws IllegalArgumentException;

    /**
     * Method compares the unit(Config)s with the units (OntSuperClass Unit!) of the ontology model. The missing units are converted into triples and are
     * stored in the returned list.
     *
     * @param ontModel The ontModel for comparing of ontology elements.
     * @param unitConfigs The unit(Config)s, which are compared with the existing unit (OntSuperClass Unit!) instances in the ontology.
     * @return A list with unit triple information.
     * @throws CouldNotPerformException Exception is thrown, if the needed ontClass could not be extract from the ontModel (empty or not available).
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getMissingUnitTriplesViaOntModel(final OntModel ontModel, final List<UnitConfig> unitConfigs)
            throws CouldNotPerformException, IllegalArgumentException;

    /**
     * Method compares the registry providerServices with the providerServices (OntSuperClass ProviderService!) in the
     * ontology model. The missing providerServices are convert into triples and are stored in the returned list.
     *
     * @param ontModel The ontModel for comparing of ontology elements.
     * @return A list with service triple information.
     * @throws CouldNotPerformException Exception is thrown, if the needed ontClass could not be extract from the ontModel (empty or not available).
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getMissingServiceTriplesViaOntModel(final OntModel ontModel) throws CouldNotPerformException, IllegalArgumentException;

    /**
     * Method compares the unit(Config)s with the units (OntSuperClass State!) in the ontology model. The missing units
     * are convert into triples and are stored in the returning list.
     *
     * @param ontModel The ontModel for comparing of ontology elements.
     * @param unitConfigs The unit(Config)s, which are compared with the existing unitState (OntSuperClass State!) instances in the ontology.
     * @return A list with unitState triple information.
     * @throws CouldNotPerformException Exception is thrown, if the needed ontClass could not be extract from the ontModel (empty or not available).
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getMissingStateTriplesViaOntModel(final OntModel ontModel, final List<UnitConfig> unitConfigs)
            throws CouldNotPerformException, IllegalArgumentException;

    /**
     * Method converts the unit(Config)s into triples (OntSuperClass Unit!) and returns a triple list without comparing.
     *
     * @param unitConfigs The unit(Config)s, which are converted into triples.
     * @return A list with unit triple information.
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getMissingUnitTriples(final List<UnitConfig> unitConfigs) throws IllegalArgumentException;

    /**
     * Method converts the unit(Config)s into triples (OntSuperClass ProviderService!) and returns a triple list without comparing.
     *
     * @param unitConfigs The unit(Config)s, which are converted into triples.
     * @return A list with service triple information.
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getMissingServiceTriples(final List<UnitConfig> unitConfigs) throws IllegalArgumentException;

    /**
     * Method converts the unit(Config)s into triples (OntSuperClass State!) and returns a triple list.
     *
     * @param unitConfigs The unit(Config)s, which are converted into triples.
     * @return A list with unitState triple information.
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getMissingStateTriples(final List<UnitConfig> unitConfigs) throws IllegalArgumentException;

    /**
     * Method returns a list of triples, which contains delete triple to remove the unitConfigs instances in the ontology. Thereby the concrete units in
     * unitType and unitState will be deleted.
     *
     * @param unitConfigs The unitConfig list, which are converted into delete triples.
     * @return A list with delete triple information (unitTypes and unitStates).
     * @throws IllegalArgumentException
     */
    List<TripleArrayList> getDeleteTripleOfUnitsAndStates(final List<UnitConfig> unitConfigs) throws IllegalArgumentException;

    /**
     * Method returns a list of triples, which contains delete triple to remove the unitConfig instance in the ontology.
     *
     * @param unitConfig The unitConfig, which is converted into delete triple.
     * @return A delete triple (unitType and unitState).
     * @throws IllegalArgumentException
     */
    TripleArrayList getDeleteTripleOfUnitsAndStates(final UnitConfig unitConfig) throws IllegalArgumentException;
}
