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
import org.openbase.bco.ontology.lib.DataPool;
import org.openbase.bco.ontology.lib.TripleArrayList;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by agatting on 23.12.16.
 */
public class OntInstancesMapping extends OntInstancesInspection {

    /**
     * Constructor for OntInstancesInspection.
     *
     * @param ontModel The actual ontology model.
     */
    public OntInstancesMapping(final OntModel ontModel) {
        super(ontModel);

        final DataPool dataPool = new DataPool();
        final UnitRegistry unitRegistry = dataPool.getUnitRegistry();

        final Set<UnitConfig> unitConfigSet = inspectionOfUnits(ontModel, unitRegistry);

        final OntClass ontClass = ontModel.getOntClass(ConfigureSystem.NS + ConfigureSystem.UNIT_SUPERCLASS);
        Set<OntClass> ontClassSet = new HashSet<>();
        ontClassSet = getAllSubclassesOfOntSuperclass(ontClassSet, ontClass, true);

        getIndPositionInOnt(ontClassSet, unitConfigSet);
    }

    private void getIndPositionInOnt(final Set<OntClass> ontClassSet, final Set<UnitConfig> unitConfigSet) {

        final Map<String, String> unitTypeUnitIdMap = new HashMap<>();

        // list all unitTypes and their unitIds of the unitConfigSet in a hashMap
        for (final UnitConfig unitConfig : unitConfigSet) {
            String unitType = unitConfig.getType().toString().toLowerCase();
            unitType = unitType.replaceAll("[^\\p{Alpha}]", "");

            unitTypeUnitIdMap.put(unitType, unitConfig.getId());
        }

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        for (OntClass ontClass : ontClassSet) {
            final String ontClassName = ontClass.getLocalName().toLowerCase();

            for (final Map.Entry<String, String> entry : unitTypeUnitIdMap.entrySet()) {
                if (entry.getKey().equals(ontClassName)) {
                    tripleArrayLists.add(new TripleArrayList("I_" + entry.getValue(), "a", ontClass.getLocalName()));
                }
            }


            //TODO differentiate between door, window, passage and region, tile, zone

            //TODO Prefix
//            if (className.startsWith("u_")) {
//                System.out.println(className);
//                className.getChars(2, className.length(), className.toCharArray(), 0);
//                System.out.println(className);
//            }

        }
    }

}
