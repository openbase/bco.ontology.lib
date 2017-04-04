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
package org.openbase.bco.ontology.lib.trigger.sparql;

import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author agatting on 08.03.17.
 */
public interface TypeAlignment {

    String[] locationCategories = new String[] {"region", "tile", "zone"};
    String[] connectionCategories = new String[] {"door", "passage", "window"};

    /**
     * Method returns a map with aligned unit types. An map entry contains an aligned unit type (lower case & removed special signs) as key and the original
     * unit type as value.
     *
     * @return A Map with aligned unit types as keys and original unit types as values.
     */
    static Map<String, UnitType> getAlignedUnitTypes() {

        final Map<String, UnitType> alignedUnitTypes = new HashMap<>();

        for (final UnitType unitType : UnitType.values()) {

            final String alignedUnitType = OntologyToolkit.convertToNounSyntax(unitType.name());
            alignedUnitTypes.put(alignedUnitType, unitType);
        }
        return alignedUnitTypes;
    }

    /**
     * Method returns a map with aligned service types. An map entry contains an aligned service type (lower case & removed special signs) as key and the
     * original service type as value.
     *
     * @return A Map with aligned service types as keys and original service types as values.
     */
    static Map<String, ServiceType> getAlignedServiceTypes() {

        final Map<String, ServiceType> alignedServiceTypes = new HashMap<>();

        for (final ServiceType serviceType : ServiceType.values()) {

            final String alignedServiceType = OntologyToolkit.convertToNounSyntax(serviceType.name());
            alignedServiceTypes.put(alignedServiceType, serviceType);
        }
        return alignedServiceTypes;
    }
}
