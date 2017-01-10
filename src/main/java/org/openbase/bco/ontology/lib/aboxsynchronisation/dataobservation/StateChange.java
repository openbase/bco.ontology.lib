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

import javafx.application.Platform;
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import rst.domotic.state.PowerStateType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by agatting on 09.01.17.
 */
public class StateChange implements Observer {

    public StateChange() {
        System.out.println(Arrays.toString(getUnitDataClass().getClass().getMethods()));
        test();
    }

    public void test() {
        ColorableLightRemote remote = new ColorableLightRemote();
        try {
            remote.initById("0008d85b-8bf1-4290-8d78-4e8aebaf1c77");
            remote.activate();
            remote.waitForData();
//            System.out.println(remote.getPowerState().getValue());
//            System.out.println(remote.getConfig());

        } catch (InterruptedException | CouldNotPerformException e) {
                e.printStackTrace();
        }


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
        Platform.runLater(() -> {

            //no paramater
            Class noparams[] = {};

            try {
                Class light = getUnitDataClass();
                Object obj = light.newInstance();

                Object method = light.getDeclaredMethod("getPowerState", noparams);
                Method method1 = (Method) method.getClass().getDeclaredMethod("getValue").invoke(method);

//                final Object objectMethod = object.getClass().getMethod(method.getName()).invoke(object);


//                Method method = light.getClass().cast(unitData).getMethod("getPowerState", null);
//                Object o = light.getClass().cast(unitData).getMethod("getPowerState", null);
//                Method method1 = o.getClass().getMethod()

//                final Object objectMethod = light.getClass().cast(unitData).getClass().getMethod("getPowerState").invoke(light.getClass().cast(unitData).getClass());
//                final Method method = objectMethod.getClass().getMethod("getValue");
//
                final PowerStateType.PowerState.State powerState = (PowerStateType.PowerState.State) method1.invoke(obj, null);
//                System.out.println(powerState);

//                final State powerState = ((ColorableLightData) colorableLight).getPowerState().getValue();

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }


        });
    }
}
