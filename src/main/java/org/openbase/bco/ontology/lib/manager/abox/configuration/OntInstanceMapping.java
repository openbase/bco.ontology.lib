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

import org.openbase.bco.ontology.lib.manager.sparql.RdfTriple;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;

/**
 * @author agatting on 09.01.17.
 */
public interface OntInstanceMapping {

    /**
     * Method returns instance information of units, services and states based on the input unitConfigs.
     *
     * @param unitConfigs contains the the units and their appropriate services + states.
     * @return a list with rdf triples to insert the units, services and states.
     */
    List<RdfTriple> getInsertConfigInstances(final List<UnitConfig> unitConfigs);

    /**
     * Method returns instance information of ALL services and states.
     *
     * @return a list with rdf triples to insert the services and states.
     */
    List<RdfTriple> getInsertStateAndServiceInstances();

    /**
     * Method returns instance information of units based on the input unitConfigs.
     *
     * @param unitConfigs contains the units.
     * @return a list with rdf triples to insert the units.
     */
    List<RdfTriple> getInsertUnitInstances(final List<UnitConfig> unitConfigs);

    /**
     * Method returns instance information of services, which are taken from the input unitConfigs or from the rst environment.
     *
     * @param unitConfigs contains the services or if {@code null} all services are taken.
     * @return a list with rdf triples to insert the services.
     */
    List<RdfTriple> getInsertProviderServiceInstances(final List<UnitConfig> unitConfigs);

    /**
     * Method returns instance information of states, which are taken from the input unitConfigs or from the rst environment.
     *
     * @param unitConfigs contains the states or if {@code null} ALL states are taken.
     * @return a list with rdf triples to insert the states.
     */
    List<RdfTriple> getInsertStateInstances(final List<UnitConfig> unitConfigs);

    /**
     * Method returns instance information to delete units, which are represented in the ontology.
     *
     * @param unitConfigs contains the units, which should be deleted in the ontology.
     * @return a list with rdf triples to delete the units.
     */
    List<RdfTriple> getDeleteUnitInstances(final List<UnitConfig> unitConfigs);

    /**
     * Method returns instance information to delete services, which are represented in the ontology.
     *
     * @param serviceTypes contains the services, which should be deleted in the ontology.
     * @return a list with rdf triples to delete the services.
     */
    List<RdfTriple> getDeleteProviderServiceInstances(final List<ServiceType> serviceTypes);

    /**
     * Method returns instance information to delete states, which are represented in the ontology.
     *
     * @param serviceTypes contains the states, which should be deleted in the ontology.
     * @return a list with rdf triples to delete the states.
     */
    List<RdfTriple> getDeleteStateInstances(final List<ServiceType> serviceTypes);
}
