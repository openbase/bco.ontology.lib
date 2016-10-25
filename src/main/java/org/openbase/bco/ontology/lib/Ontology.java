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
package org.openbase.bco.ontology.lib;

import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import rst.domotic.unit.UnitConfigType;

/**
 * Created by agatting on 20.10.16.
 */
public class Ontology {

    /**
     * Tool name.
     */
    public static final String TOOL_NAME = Ontology.class.getSimpleName() + "Tool";
    private static final Logger LOGGER = LoggerFactory.getLogger(Ontology.class);

    /**
     * Main Method starting ontology tool.
     *
     * @param args Arguments from commandline.
     */
    public static void main(String[] args) {

        LOGGER.info("Start " + TOOL_NAME + " ...");

        CreateOntology ontology = new CreateOntology();
        ontology.loadOntology("src/Ontology.owl");
        ontology.cleanOntology();

        /*UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException ex) {
            ExceptionPrinter.printHistory("Could not start App", ex,System.err);
        }

        try {
            for(UnitConfigType.UnitConfig config : registry.getUnitConfigs()) {
                System.out.println(config.getLabel());
            }

        } catch (CouldNotPerformException e) {
            e.printStackTrace();
        }*/

        LOGGER.info(TOOL_NAME + " finished!");

    }
}
