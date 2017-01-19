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
package org.openbase.bco.ontology.lib.datapool;

import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.UnitRemoteFactoryImpl;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.EnablingStateType;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 09.01.17.
 */
public interface RemotePool {

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(RemotePool.class);

    /**
     * Method provides the unitRemote, which is initialized by unitConfig.
     *
     * @param unitConfig The unitConfig.
     * @return The unitRemote.
     */
    static UnitRemote getUnitRemoteByUnitConfig(final UnitConfig unitConfig) {

        UnitRemote unitRemote = null;

        try {
            unitRemote = UnitRemoteFactoryImpl.getInstance().newInitializedInstance(unitConfig);
            unitRemote.activate();
            unitRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitRemote;
    }

    /**
     * Method provides all unitRemotes, which are initialized by all unitConfigs.
     *
     * @param unitConfigList The unitConfigs.
     * @return A list with unitRemotes.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    static List<UnitRemote> getAllUnitRemotes(final List<UnitConfig> unitConfigList) {

        List<UnitRemote> unitRemoteList = new ArrayList<>();
        UnitRemote unitRemote;
        MultiException.ExceptionStack exceptionStack = null;

        try {
            for (UnitConfig unitConfig : unitConfigList) {
                try {
                    if (UnitConfigProcessor.isDalUnit(unitConfig) && (unitConfig.getEnablingState().getValue() == EnablingStateType.EnablingState.State.ENABLED)) {
                        unitRemote = UnitRemoteFactoryImpl.getInstance().newInitializedInstance(unitConfig);
                        unitRemote.activate();
//                        unitRemote.waitForData();
                        unitRemote.waitForData(300, TimeUnit.MILLISECONDS);
                        if (!unitRemote.isDataAvailable() || !unitRemote.isConnected()) {
                            unitRemote.deactivate();
                            unitRemote.shutdown();
                        } else {
                            System.out.println(unitConfig.getType());
                            unitRemoteList.add(unitRemote);
                        }
                    }
                } catch (InterruptedException | CouldNotPerformException e) {
                    // maybe all unitRemotes fail, because of one reason -> discreet behavior by quantity constraint
                    if (MultiException.size(exceptionStack) < 10) {
                        exceptionStack = MultiException.push(null, e, exceptionStack);
                    }
                }
            }

            MultiException.checkAndThrow("Could not process all unitRemotes!", exceptionStack);

        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitRemoteList;
    }
}
