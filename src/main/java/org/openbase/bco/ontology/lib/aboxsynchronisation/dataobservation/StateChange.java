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
import org.openbase.bco.dal.remote.unit.UnitRemote;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author agatting on 09.01.17.
 */
public class StateChange implements Observer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateChange.class);
    private UnitRemote unitRemote;

    private UnitRemote getUnitRemote() {
        return unitRemote;
    }

    public StateChange() {

        ColorableLightRemote remote = test();
        remote.addDataObserver(this);

    }


    public ColorableLightRemote test() {
        ColorableLightRemote remote = new ColorableLightRemote();
        try {
//            remote.initById("0008d85b-8bf1-4290-8d78-4e8aebaf1c77");
            remote.initById("0b26889b-ff0a-4ba0-98ac-51a481c6b559");
            remote.activate();
            remote.waitForData();

        } catch (InterruptedException | CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return remote;

    }

    private String getUnitID(final Object remoteData) throws CouldNotPerformException {

        try {
            if (remoteData == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            final Method methodGetId = ReflectObjectPool.getMethodByName(remoteData
                    , ConfigureSystem.MethodRegEx.GET_ID.getName());

            return (String) methodGetId.invoke(remoteData, (Object[]) null);

        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new CouldNotPerformException("Cannot get unit ID!", e);
        }
    }

    private String getTimeStamp(final Object remoteData) throws CouldNotPerformException {

        try {
            if (remoteData == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            final Method methodGetTimeStamp = ReflectObjectPool.getMethodByName(remoteData
                    , ConfigureSystem.MethodRegEx.GET_TIMESTAMP.getName());

            //TODO no value in the moment
            return (String) methodGetTimeStamp.invoke(remoteData, (Object[]) null);

        } catch (IllegalArgumentException | CouldNotPerformException | IllegalAccessException
                | InvocationTargetException e) {
            throw new CouldNotPerformException("Cannot get timestamp!", e); //TODO "of type..."?!
        }
    }

    private String getStateValue(final Object remoteData) throws CouldNotPerformException {

        try {
            if (remoteData == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            final Method methodGetTimeStamp = ReflectObjectPool.getMethodByName(remoteData
                    , ConfigureSystem.MethodRegEx.GET_VALUE.getName());

            return (String) methodGetTimeStamp.invoke(remoteData, (Object[]) null);

        } catch (IllegalArgumentException | CouldNotPerformException | IllegalAccessException
                | InvocationTargetException e) {
            throw new CouldNotPerformException("Cannot get state value!", e);
        }
    }

    private boolean remoteHasDataUnit(final Object remoteData) throws CouldNotPerformException  {

        try {
            if (remoteData == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            final Set<Method> methodSetStateType = ReflectObjectPool.getMethodSetByRegEx(remoteData
                    , ConfigureSystem.MethodRegEx.GET.getName(), ConfigureSystem.MethodRegEx.STATE.getName());

            for (Method methodStateType : methodSetStateType) {

                final Method methodDataUnit = ReflectObjectPool.getMethodByRegEx(methodStateType
                        , ConfigureSystem.MethodRegEx.DATA_UNIT.getName());
                //TODO ...
            }

            return false;

        } catch (IllegalArgumentException | CouldNotPerformException e) {
            throw new CouldNotPerformException("", e);
        }

    }



    @Override
    public void update(final Observable observable, final Object remoteData) throws java.lang.Exception {
        GlobalCachedExecutorService.submit(() -> {

            //TODO implement logic, that updates changed stateValues only
            //TODO test multiple reflection...

//            System.out.println(((ColorableLightDataType.ColorableLightData) remoteData).);
//            ((TemperatureSensorDataType.TemperatureSensorData) remoteData).getTemperatureState().


            try {
                getUnitID(remoteData);
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }

//            final Set<Object> objectSetStateType = ReflectObjectPool.getMethodSetByRegEx(remoteData,
//                    ConfigureSystem.MethodRegEx.GET.getName(), ConfigureSystem.MethodRegEx.STATE.getName());
//            try {
//                if (objectSetStateType.isEmpty()) {
//                    throw new CouldNotPerformException("Cannot perform update of state value, because "
//                            + objectSetStateType.getClass().getTypeName() + " is empty!");
//                } else {
//                    for (final Object objectStateType : objectSetStateType) {
//                        final Set<Object> objectSetStateValue = ReflectObjectPool.getMethodSetByRegEx(objectStateType
//                                , ConfigureSystem.MethodRegEx.GET_VALUE.getName());
//                        //TODO
//                    }
//                }
//            } catch (CouldNotPerformException e) {
//                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
//            }

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
