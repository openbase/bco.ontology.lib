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

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.TripleArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by agatting on 21.12.16.
 */
public class OntPropertyMapping extends OntInstanceInspection {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntPropertyMapping.class);

    /**
     * Constructor for OntPropertyMapping.
     */
    public OntPropertyMapping(final OntModel ontModel) {
        super();

        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.UNIT_TYPE_LOCATION);

        Set<String> locationIndividualsSet = new HashSet<>();
        locationIndividualsSet = getIndNameOfOntSuperclass(locationIndividualsSet, ontClass);

        for (final UnitConfig unitConfig : getUnitConfigListByUnitType(UnitType.LOCATION)) {
            locationUnit(unitConfig, locationIndividualsSet);

            //TODO implement method, which tests if unit is currently available in ontology
        }

    }

    private List<TripleArrayList> locationUnit(final UnitConfig unitConfig, final Set<String> locationIndividualsSet) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        for (String locationIndividual : locationIndividualsSet) {
            if (locationIndividual.equals(unitConfig.getId())) {

            }
        }

        return tripleArrayLists;
    }

}
