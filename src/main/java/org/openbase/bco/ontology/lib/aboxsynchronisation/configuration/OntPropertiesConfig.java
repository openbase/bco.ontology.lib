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

import org.openbase.bco.ontology.lib.DataPool;
import org.openbase.bco.registry.unit.lib.UnitRegistry;

/**
 * Created by agatting on 21.12.16.
 */
public class OntPropertiesConfig {

//    private static final Logger LOGGER = LoggerFactory.getLogger(OntPropertiesConfig.class);

    /**
     * Constructor for OntPropertiesConfig.
     */
    public OntPropertiesConfig() {
        final DataPool dataPool = new DataPool();
        final UnitRegistry unitRegistry = dataPool.getUnitRegistry();
    }


}