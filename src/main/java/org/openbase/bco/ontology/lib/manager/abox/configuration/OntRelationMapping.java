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

import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.jul.exception.NotAvailableException;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;

/**
 * @author agatting on 06.01.17.
 */
public interface OntRelationMapping {

    /**
     * Method returns triple information to insert relations of the units into the ontology.
     *
     * @param unitConfigs contains the units, which should be linked (relations).
     * @return a list of triple information to insert relations of units.
     */
    List<RdfTriple> getInsertUnitRelations(final List<UnitConfig> unitConfigs);

    /**
     * Method returns triple information to insert relations of the unit into the ontology.
     *
     * @param unitConfig contains the unit, which should be linked (relations).
     * @return a list of triple information to insert relations of a unit.
     */
    List<RdfTriple> getInsertUnitRelations(final UnitConfig unitConfig);

    /**
     * Method returns triple information to delete relations of the units from the ontology.
     *
     * @param unitConfigs contains the units to identify the relations, which should be deleted.
     * @return a list of triple information to delete relations.
     */
    List<RdfTriple> getDeleteUnitRelations(final List<UnitConfig> unitConfigs);

    /**
     * Method returns triple information to delete relations of the unit from the ontology.
     *
     * @param unitConfig contains the unit to identify the relations, which should be deleted.
     * @return a list of triple information to delete relations.
     */
    List<RdfTriple> getDeleteUnitRelations(final UnitConfig unitConfig);

    /**
     * Method returns triple information to insert relation of the service/state into the ontology.
     *
     * @param serviceType contains the service/state, which should be linked (relation).
     * @return triple information to insert a relation.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    RdfTriple getInsertStateRelation(final ServiceType serviceType) throws NotAvailableException;

    /**
     * Method returns triple information to insert relations of the services/states into the ontology.
     *
     * @param serviceTypes contains the services/states, which should be linked (relations). If {@code null} than all services/states are taken.
     * @return a list of triple information to insert relations.
     */
    List<RdfTriple> getInsertStateRelations(final List<ServiceType> serviceTypes);

    /**
     * Method returns triple information to delete relation of the service/state from the ontology.
     *
     * @param serviceType contains the service/state to identify the relation, which should be deleted.
     * @return triple information to delete a relation.
     * @throws NotAvailableException is thrown in case the input is null.
     */
    RdfTriple getDeleteStateRelation(final ServiceType serviceType) throws NotAvailableException;

    /**
     * Method returns triple information to delete relations of the services/states from the ontology.
     *
     * @param serviceTypes contains the services/states to identify the relations, which should be deleted.
     * @return a list of triple information to delete relations.
     */
    List<RdfTriple> getDeleteStateRelation(final List<ServiceType> serviceTypes);
}
