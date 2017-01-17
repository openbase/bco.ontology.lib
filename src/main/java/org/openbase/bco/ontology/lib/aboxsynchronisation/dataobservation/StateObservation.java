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
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.datapool.ReflectObjectPool;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author agatting on 09.01.17.
 */
public class StateObservation implements Observer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateObservation.class);
    private final Map<String, String> serviceTypeMap = new HashMap<>();
    private static String remoteUnitId = "";
    private static int obsCount = 0; //TODO find better way to set unique observer name
    private static List<TripleArrayList> tripleArrayListBuf = new ArrayList<>();

    public StateObservation(UnitRemote unitRemote) {

        initServiceTypeMap();
//        ColorableLightRemote remote = test();

        // get unitID
        try {
            remoteUnitId = (String) unitRemote.getId();
            System.out.println(remoteUnitId);
        } catch (NotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        unitRemote.addDataObserver(this);
    }

    private String getObsInstanceName() {

        final String obsInstanceName = "O" + remoteUnitId + obsCount;
        ++obsCount;

        return obsInstanceName;
    }

    //TODO swap out...
    private void initServiceTypeMap() {
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
            final String stateTypeBuf = methodStateTypeName.toLowerCase()
                    .replaceFirst(ConfigureSystem.MethodRegEx.GET.getName(), "");

            for (final String serviceTypeName : serviceTypeMap.keySet()) {
                if (serviceTypeName.contains(stateTypeBuf)) {
                    // successful compared - return correct serviceType (ontology individual name)
                    return serviceTypeMap.get(serviceTypeName);
                }
            }

            throw new NoSuchElementException("Cause there is no element, which contains " + methodStateTypeName);

        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw new CouldNotPerformException("Cannot perform reflection!", e);
        }

    }

    private List<TripleArrayList> addTripleHasUnitId(final List<TripleArrayList> tripleArrayLists
            , final String subject, final String object) {

        final String predicate = ConfigureSystem.OntProp.UNIT_ID.getName();

        // add triple: observation - hasUnitId - unit
        tripleArrayLists.add(new TripleArrayList(subject, predicate, object));

        return tripleArrayLists;
    }

    private List<TripleArrayList> addTripleHasTimeStamp(final List<TripleArrayList> tripleArrayLists
            , final String subject, final String object) {

        final String predicate = ConfigureSystem.OntProp.TIME_STAMP.getName();

        // add triple: observation - hasTimeStamp - timeStamp
        tripleArrayLists.add(new TripleArrayList(subject, predicate, object));

        return tripleArrayLists;
    }

    private List<TripleArrayList> addTripleHasProviderService(final List<TripleArrayList> tripleArrayLists
            , final String subject, final String object) {

        final String predicate = ConfigureSystem.OntProp.PROVIDER_SERVICE.getName();

        // add triple: observation - hasProviderService - providerService
        tripleArrayLists.add(new TripleArrayList(subject, predicate, object));

        return tripleArrayLists;
    }

    private List<TripleArrayList> addTripleHasStateValue(final List<TripleArrayList> tripleArrayLists
            , final String subject, final String object) {

        final String predicate = ConfigureSystem.OntProp.STATE_VALUE.getName();

        // add triple: observation - hasStateValue - stateValue
        tripleArrayLists.add(new TripleArrayList(subject, predicate, object));

        return tripleArrayLists;
    }

    protected void setTripleArray(final List<TripleArrayList> tripleArrayLists) {
        if (tripleArrayListBuf.isEmpty()) {
            tripleArrayListBuf = tripleArrayLists;
        } else {
            tripleArrayListBuf.addAll(tripleArrayLists);
            LOGGER.warn("List is not empty ...");
        }
    }

    @Override
    public void update(final Observable observable, final Object remoteData) throws java.lang.Exception {
        Future future = GlobalCachedExecutorService.submit(() -> {
            System.out.println("test");
            //TODO implement logic, that updates changed stateValues only

            try {

                List<TripleArrayList> tripleArrayLists = new ArrayList<>();
                final Set<Method> methodSetStateType = ReflectObjectPool.getMethodSetByRegEx(remoteData
                        , ConfigureSystem.MethodRegEx.GET.getName(), ConfigureSystem.MethodRegEx.STATE.getName());

                // foreach stateType ...
                for (Method methodStateType : methodSetStateType) {

                    // every observation point represents an serviceType respectively stateValue
                    final String subject = getObsInstanceName();

                    //### unitID triple ###\\
                    tripleArrayLists = addTripleHasUnitId(tripleArrayLists, subject, remoteUnitId);

                    //### serviceType triple ###\\
                    final String serviceTypeInstance = getServiceTypeMapping(methodStateType.getName());
                    tripleArrayLists = addTripleHasProviderService(tripleArrayLists, subject, serviceTypeInstance);

                    // get method as invoked object
                    Object stateTypeObj = null;
                    try {
                        stateTypeObj = methodStateType.invoke(remoteData);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }

                    //### timeStamp triple ###\\
                    final String timeStampObj = (String) ReflectObjectPool.getInvokedObj(stateTypeObj
                            , ConfigureSystem.MethodRegEx.GET_TIMESTAMP.getName()); //TODO result empty
                    tripleArrayLists = addTripleHasProviderService(tripleArrayLists, subject, timeStampObj);


                    //### stateValue triple ###\\
                    final boolean hasDataUnit = stateTypeHasDataUnit(stateTypeObj);

                    if (hasDataUnit) {
                        //TODO need access to data...
                    } else {
                        final String stateValueObj = (String) ReflectObjectPool.getInvokedObj(stateTypeObj
                                , ConfigureSystem.MethodRegEx.GET_VALUE.getName());
                        tripleArrayLists = addTripleHasProviderService(tripleArrayLists, subject, stateValueObj);
                    }

                    setTripleArray(tripleArrayLists);
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }

//            ((ColorableLightDataType.ColorableLightData) remoteData).getPowerState().
//            ((BatteryDataType.BatteryData) remoteData).getBatteryState().getLevel()
        });

        GlobalCachedExecutorService.submit(() -> {
            if (future.isDone()) {
                System.out.println("test fertig");
            }
        });

    }
}
