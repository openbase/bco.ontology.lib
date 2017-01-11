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

import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.datapool.ReflectObjectPool;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author agatting on 09.01.17.
 */
public class StateChange implements Observer {

    private Logger LOGGER = LoggerFactory.getLogger(StateChange.class);
    private ColorableLightRemote remote;

    public StateChange() {

        this.remote = test();
        remote.addDataObserver(this);

    }

    public ColorableLightRemote test() {
        ColorableLightRemote remote = new ColorableLightRemote();
        try {
            remote.initById("0008d85b-8bf1-4290-8d78-4e8aebaf1c77");
            remote.activate();
            remote.waitForData();

            System.out.println(remote.getConfig().getScope());
        } catch (InterruptedException | CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return remote;

    }

    private void getHasUnitID(final Object unitData) {

        final Set<Object> objectSetUnitID = ReflectObjectPool.getMethodByClassObject(unitData,
                ConfigureSystem.MethodRegEx.GET_ID.getName());

        try {
            if (objectSetUnitID.isEmpty()) {
                throw new CouldNotPerformException("Cannot perform update of unit ID, because "
                        + objectSetUnitID.getClass().getTypeName() + " is empty!");
            } else {
                for (final Object objectUnitID : objectSetUnitID) {
                    //TODO implement method, which returns an single object instead of set of objects
                }
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    @Override
    public void update(final Observable observable, final Object unitData) throws java.lang.Exception {
        GlobalCachedExecutorService.submit(() -> {

            final Set<Object> objectSetStateType = ReflectObjectPool.getMethodByClassObject(unitData,
                    ConfigureSystem.MethodRegEx.GET.getName(), ConfigureSystem.MethodRegEx.STATE.getName());
            try {
                if (objectSetStateType.isEmpty()) {
                    throw new CouldNotPerformException("Cannot perform update of state value, because "
                            + objectSetStateType.getClass().getTypeName() + " is empty!");
                } else {
                    for (final Object objectStateType : objectSetStateType) {
                        final Set<Object> objectSetStateValue = ReflectObjectPool.getMethodByClassObject(objectStateType
                                , ConfigureSystem.MethodRegEx.GET_VALUE.getName());
                        //TODO
                    }
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }

//            try {
//                for (Object o : objectSetStateType) {
//                    System.out.println(Arrays.toString(o.getClass().getMethods()));
//                    Object objectMethod = o.getClass().getMethod("getValue").invoke(o);
//                    System.out.println(objectMethod);
//                }
//            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                e.printStackTrace();
//            }

        });



    }
}
