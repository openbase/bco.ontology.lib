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

import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.StateObservation;
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.TransactionBuffer;
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.TransactionBufferImpl;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 07.02.17.
 */
public class UnitRemoteSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRemoteSynchronizer.class);

    public UnitRemoteSynchronizer(final TransactionBuffer transactionBuffer) {

        List<UnitConfig> unitConfigList;
        boolean retry = true;
        MultiException.ExceptionStack exceptionStack = null;
        final Stopwatch stopwatch = new Stopwatch();

        while (retry) {
            try {
                unitConfigList = Units.getUnitRegistry().getUnitConfigs();
                retry = false;

                for (UnitConfig unitConfig : unitConfigList) {
                    if (unitConfig.getEnablingState().getValue() == State.ENABLED) {

                        try {
                            final UnitRemote unitRemote = Units.getUnit(unitConfig, false);
                            unitRemote.activate();
                            unitRemote.waitForData(500, TimeUnit.MILLISECONDS); //TODO retry?

                            if (unitRemote.isDataAvailable()) {
                                StateObservation stateObservation
                                        = new StateObservation(unitRemote, unitConfig, transactionBuffer);
                            }
                        } catch (InterruptedException | CouldNotPerformException e) {
                            exceptionStack = MultiException.push(this, e, exceptionStack);
                        }
                    }
                }

                MultiException.checkAndThrow("Could not process all unitRemotes!", exceptionStack);
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            } catch (InterruptedException e) {
                ExceptionPrinter.printHistory("Interrupted by getting unitRegistry! Retry in "
                        + ConfigureSystem.waitTimeMilliSeconds + "milliseconds!", e, LOGGER);

                try {
                    stopwatch.waitForStop(ConfigureSystem.waitTimeMilliSeconds);
                } catch (InterruptedException ex) {
                    assert false;
                }
            }
        }
    }
}
