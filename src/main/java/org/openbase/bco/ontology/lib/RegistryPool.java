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
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by agatting on 19.12.16.
 */
public interface RegistryPool {

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(RegistryPool.class);

    /**
     * Method provides the unitRegistry.
     *
     * @return unitRegistry.
     */
    static UnitRegistry getUnitRegistry() {

        UnitRegistry unitRegistry = null;

        try {
            unitRegistry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitRegistry;
    }

    /**
     * Method returns a list of all unitConfigs, which are actual enabled.
     *
     * @return A list of all enabled unitConfigs.
     */
    static List<UnitConfig> getUnitConfigList() {

        List<UnitConfig> unitConfigList = new ArrayList<>();

        try {
            for (UnitConfig unitConfig : getUnitRegistry().getUnitConfigs()) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)) {
                    unitConfigList.add(unitConfig);
                }
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitConfigList;
    }

    /**
     * Method returns a list of unitConfigs of a specific unitType, which are actual enabled.
     *
     * @param unitType The unitType to presort the list.
     * @return A list of unitConfigs.
     */
    static List<UnitConfig> getUnitConfigListByUnitType(final UnitType unitType) {

        //TODO exception handling

        List<UnitConfig> unitConfigList = new ArrayList<>();

        try {
            for (UnitConfig unitConfig : getUnitRegistry().getUnitConfigs(unitType)) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)) {
                    unitConfigList.add(unitConfig);
                }
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitConfigList;
    }
}
