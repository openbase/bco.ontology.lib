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
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by agatting on 19.12.16.
 */
public class DataPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ontology.class);

    public UnitRegistry getUnitRegistry() {
        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return registry;
    }

    public DataPool() {
    }

//    public Remote getRemote() {
//        BrightnessSensorRemote remote = new BrightnessSensorRemote();
//        try {
//            remote.initById("3249a1a5-52d1-4be1-910f-2063974b53f5");
//            remote.activate();
//            remote.waitForData();
//            remote.getBrightnessState().getBrightness();
//
//            //System.out.println(remote.getData().getBrightnessState().getBrightnessDataUnit());
//        } catch (InterruptedException | CouldNotPerformException e) {
//                e.printStackTrace();
//        }
//    }
}
