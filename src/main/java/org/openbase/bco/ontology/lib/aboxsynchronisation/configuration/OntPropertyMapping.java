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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.Set;

/**
 * Created by agatting on 21.12.16.
 */
public class OntPropertyMapping extends OntInstanceInspection {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntPropertyMapping.class);

    //TODO getUnitRegistry -> state:enabled

    /**
     * Constructor for OntPropertyMapping.
     */
    public OntPropertyMapping(final OntModel ontModel) {
        super(ontModel);


    }

    private void blub(final Set<UnitConfig> unitConfigSet) {

        for (UnitConfig unitConfig : unitConfigSet) {

            if (unitConfig.getType().equals(UnitType.LOCATION)) {

            }

        }
    }

    private void locationUnit(final UnitConfig unitConfig) {

        String locationTypeName = unitConfig.getLocationConfig().getType().name().toLowerCase();
        char[] charVar = locationTypeName.toCharArray();
        charVar[0] = Character.toUpperCase(charVar[0]);
        locationTypeName = new String(charVar);
    }

}
