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
package org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation;

import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.ontology.lib.datapool.RegistryPool;
import org.openbase.bco.ontology.lib.datapool.RemotePool;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 17.01.17.
 */
public class ObservationPerformer {

    public ObservationPerformer() {

        final List<UnitConfig> unitConfigList = RegistryPool.getUnitConfigList();
        final List<UnitConfig> unitConfigListBuf = new ArrayList<>();

        for (UnitConfig unitConfig : unitConfigList) {
            if (unitConfig.getEnablingState().getValue() == State.ENABLED) {
                unitConfigListBuf.add(unitConfig);
            }
        }
        final List<UnitRemote> unitRemoteList = RemotePool.getAllUnitRemotes(unitConfigListBuf);
        for (UnitRemote unitRemote : unitRemoteList) {
            StateObservation stateObservation = new StateObservation(unitRemote);


        }
    }
}
