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
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.PowerStateType.PowerState.State;
import rst.domotic.unit.UnitConfigType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Future;

/**
 * @author agatting on 09.01.17.
 */
public class StateChange implements Observer {

    private Logger LOGGER = LoggerFactory.getLogger(StateChange.class);

    public StateChange() {

        ColorableLightRemote remote = test();
        remote.addDataObserver(this);


        UnitConfigType.UnitConfig unitConfig = null;
        try {
            unitConfig = CachedUnitRegistryRemote.getRegistry().getUnitConfigById("0008d85b-8bf1-4290-8d78-4e8aebaf1c77");
            System.out.println(unitConfig.isInitialized());
            System.out.println(unitConfig.getEnablingState().getValue());
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
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

    public Class getUnitDataClass() {
        Class aClass = null;
        try {
            aClass = Class.forName("rst.domotic.unit.dal.ColorableLightDataType$ColorableLightData");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return aClass;
    }


    @Override
    public void update(final Observable observable, final Object unitData) throws java.lang.Exception {
        Future taskFuture = GlobalCachedExecutorService.submit(() -> {
            System.out.println("test");
        });
//        Platform.runLater(() -> {
//
//            //no paramater
////            Class noparams[] = {};
//            System.out.println("bla");

//            try {
//                Class light = getUnitDataClass();
//                Object obj = light.newInstance();
//
//                Method method = light.getDeclaredMethod("getPowerState().getValue", noparams);
//
//                State powerState = (State) method.invoke(obj, null);
//                System.out.println(powerState);


//                Method method1 = (Method) method.getClass().getDeclaredMethod("getValue").invoke(method);

//                final Object objectMethod = object.getClass().getMethod(method.getName()).invoke(object);


//                Method method = light.getClass().cast(unitData).getMethod("getPowerState", null);
//                Object o = light.getClass().cast(unitData).getMethod("getPowerState", null);
//                Method method1 = o.getClass().getMethod()

//                final Object objectMethod = light.getClass().cast(unitData).getClass().getMethod("getPowerState").invoke(light.getClass().cast(unitData).getClass());
//                final Method method = objectMethod.getClass().getMethod("getValue");
//

//                final State powerState = ((ColorableLightData) colorableLight).getPowerState().getValue();

//            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            }


//        });
    }
}
