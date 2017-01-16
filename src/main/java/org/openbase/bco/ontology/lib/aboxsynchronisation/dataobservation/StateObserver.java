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

import com.google.protobuf.Descriptors;
import org.openbase.bco.dal.remote.unit.BatteryRemote;
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
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author agatting on 09.01.17.
 */
public class StateObserver implements Observer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateObserver.class);
    private final Map<String, String> serviceTypeMap = new HashMap<>();

    public StateObserver() {

        ColorableLightRemote remote = test();
        remote.addDataObserver(this);

        buildServiceTypeMap();

    }

    private void buildServiceTypeMap() {
        for (final ServiceType serviceType : ServiceType.values()) {
            final String serviceTypeName = serviceType.name();
            final String reducedServiceTypeName = serviceTypeName.toLowerCase()
                    .replaceAll(ConfigureSystem.OntExpr.REMOVE.getName(), "");

            serviceTypeMap.put(reducedServiceTypeName, serviceTypeName);
        }
    }


    public ColorableLightRemote test() {
        ColorableLightRemote remote = new ColorableLightRemote();
        try {
            remote.initById("0b26889b-ff0a-4ba0-98ac-51a481c6b559");
//            remote.initById("0fd58bf2-fec4-4675-8388-24d1fe42f9c1");
            remote.activate();
            remote.waitForData();

            for (Descriptors.FieldDescriptor fieldDescriptor : remote.getData().getAllFields().keySet()) {
            }

        } catch (InterruptedException | CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        return remote;
    }


    private boolean stateTypeHasDataUnit(final Object invokedMethod) throws CouldNotPerformException  {

        try {
            if (invokedMethod == null) {
                throw new IllegalArgumentException("Cause parameter object is null!");
            }

            // approach: compare with the method, which has NO dataUnit. They are standardized with "getValue"
            return !ReflectObjectPool.hasMethodByRegEx(invokedMethod
                    , ConfigureSystem.MethodRegEx.GET_VALUE.getName());
        } catch (IllegalArgumentException e) {
            throw new CouldNotPerformException("Cannot check availability of dataUnit method!", e);
        }
    }

    private String getServiceTypeMapping(final String methodStateTypeName) throws CouldNotPerformException {

        try {
            if (methodStateTypeName == null) {
                throw new IllegalArgumentException("Cause String is null!");
            }
            // standardized string to allow comparison
            String stateTypebuf = methodStateTypeName.toLowerCase()
                    .replaceFirst(ConfigureSystem.MethodRegEx.GET.getName(), "");

            for (final String serviceTypeName : serviceTypeMap.keySet()) {
                if (serviceTypeName.contains(stateTypebuf)) {
                    // successful compared - return correct serviceType (ontology individual name)
                    return serviceTypeMap.get(serviceTypeName);
                }
            }

            throw new NoSuchElementException("Cause there is no element, which contains " + methodStateTypeName);

        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw new CouldNotPerformException("Cannot perform reflection!", e);
        }

    }

    @Override
    public void update(final Observable observable, final Object remoteData) throws java.lang.Exception {
        GlobalCachedExecutorService.submit(() -> {

            //TODO implement logic, that updates changed stateValues only

            try {
                // get unitID
                final Object unitId = ReflectObjectPool.getInvokedObj(remoteData
                        , ConfigureSystem.MethodRegEx.GET_ID.getName());

                final Set<Method> methodSetStateType = ReflectObjectPool.getMethodSetByRegEx(remoteData
                        , ConfigureSystem.MethodRegEx.GET.getName(), ConfigureSystem.MethodRegEx.STATE.getName());

                // foreach stateType ...
                for (Method methodStateType : methodSetStateType) {

                    // get method as invoked object
                    Object stateTypeObj = null;
                    try {
                        stateTypeObj = methodStateType.invoke(remoteData);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }

                    // get timeStamp
                    final Object timeStampObj = ReflectObjectPool.getInvokedObj(stateTypeObj
                            , ConfigureSystem.MethodRegEx.GET_TIMESTAMP.getName()); //TODO result empty

                    // get serviceType
                    final String serviceTypeIndividual = getServiceTypeMapping(methodStateType.getName());

                    // get stateValue
                    final boolean hasDataUnit = stateTypeHasDataUnit(stateTypeObj);

                    if (hasDataUnit) {
                        //TODO need access to data...
                    } else {
                        final Object stateValueObj = ReflectObjectPool.getInvokedObj(stateTypeObj
                                , ConfigureSystem.MethodRegEx.GET_VALUE.getName());
                    }
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }

//            ((ColorableLightDataType.ColorableLightData) remoteData).getPowerState().
//            ((BatteryDataType.BatteryData) remoteData).getBatteryState().getLevel()
        });
    }
}
