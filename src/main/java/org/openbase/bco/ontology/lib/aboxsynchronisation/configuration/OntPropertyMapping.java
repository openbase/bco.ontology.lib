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
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by agatting on 21.12.16.
 */
public class OntPropertyMapping extends OntInstanceInspection {

    /**
     * Constructor for OntPropertyMapping.
     *
     * @param ontModel The ontology model.
     */
    public OntPropertyMapping(final OntModel ontModel) {
        super();

        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS
                + ConfigureSystem.OntClass.LOCATION.getName());
        List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        for (final UnitConfig unitConfig : getUnitConfigListByUnitType(UnitType.LOCATION)) {

            final String unitId = unitConfig.getId();

            if (existIndInOnt(ontClass, unitId)) {
                // unit does exist in ontology
                tripleArrayLists = getTriplePropSubLocation(tripleArrayLists, unitConfig);
            } else {
                // unit doesn't exist in ontology
                //TODO
            }
        }

    }

    private List<TripleArrayList> getTriplePropSubLocation(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.SUB_LOCATION.getName();
        String object;

        // get all child IDs of the unit location
        for (final String childId : unitConfig.getLocationConfig().getChildIdList()) {
            object = childId;
            tripleArrayLists.add(new TripleArrayList(subject, predicate, object));
        }

        return tripleArrayLists;
    }

}
