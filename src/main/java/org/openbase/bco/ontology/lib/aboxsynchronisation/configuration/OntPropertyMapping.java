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
import java.util.List;

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

        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS
                + ConfigureSystem.OntClass.LOCATION.getName());

//        Set<String> locationIndividualsSet = new HashSet<>();
//        locationIndividualsSet = getIndNameOfOntSuperclass(locationIndividualsSet, ontClass);

        for (final UnitConfig unitConfig : getUnitConfigListByUnitType(UnitType.LOCATION)) {

            final String unitId = unitConfig.getId();

            if (isOntIndAvailable(ontModel, unitId)) {
                // unit does exist in ontology
                locationUnit(unitConfig);
            } else {
                // unit doesn't exist in ontology
                //TODO
            }
        }

        ConfigureSystem configureSystem = new ConfigureSystem();
        configureSystem.initialTestConfig(ontModel);



    }

    private List<TripleArrayList> locationUnit(final UnitConfig unitConfig) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();



//        System.out.println(ConfigureSystem.OntProp.SUB_LOCATION.getName());

//        for (String locationIndividual : locationIndividualsSet) {
//            if (locationIndividual.equals(unitConfig.getId())) {
//
//            }
//        }

        return tripleArrayLists;
    }

}
