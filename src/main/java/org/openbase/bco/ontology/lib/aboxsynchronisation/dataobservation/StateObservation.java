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
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.datapool.ReflectObjectPool;
import org.openbase.bco.ontology.lib.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.dal.ColorableLightDataType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author agatting on 09.01.17.
 */
public class StateObservation extends SparqlUpdateExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateObservation.class);
    private final SimpleDateFormat simpleDateFormatWithoutTimeZone = new SimpleDateFormat(ConfigureSystem
            .DATE_TIME_WITHOUT_TIME_ZONE, Locale.ENGLISH); //TODO
    private final Map<String, String> serviceTypeMap = new HashMap<>();
    private static String remoteUnitId = null;

    private final Observer unitRemoteStateObserver; //TODO unchecked call ->generic dataClass?!
    private final Observer<ConnectionState> unitRemoteConnectionObserver;

    private TransactionBuffer transactionBuffer;

    public StateObservation(final UnitRemote unitRemote, final UnitConfig unitConfig
            , final TransactionBuffer transactionBuffer) {

        this.transactionBuffer = transactionBuffer;

        initServiceTypeMap();

        remoteUnitId = unitConfig.getId();

        this.unitRemoteStateObserver = (Observable observable, Object unitRemoteObj) -> {
            GlobalCachedExecutorService.submit(() -> stateUpdate(unitRemoteObj));
        };

        this.unitRemoteConnectionObserver = (Observable<ConnectionState> observable, ConnectionState connectionState) -> {
            GlobalCachedExecutorService.submit(() -> {
                if (connectionState.equals(ConnectionState.CONNECTED)) {
                    System.out.println("connected!!!");
                } else {
                    System.out.println("disconnected!!!");
                }
                //TODO
            });
        };

        unitRemote.addDataObserver(unitRemoteStateObserver);
        unitRemote.addConnectionStateObserver(unitRemoteConnectionObserver);
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

    private void stateUpdate(final Object remoteData) {
        //TODO implement logic, that updates changed stateValues only

        // main list, which contains complete observation instances
        List<TripleArrayList> tripleArrayLists = new ArrayList<>();
        // first collect all components of the individual observation, then add to main list (integrity reason)
        List<TripleArrayList> tripleArrayListsBuf = new ArrayList<>();

        try {
            final Set<Method> methodSetStateType = ReflectObjectPool.getMethodSetByRegEx(remoteData
                    , ConfigureSystem.MethodRegEx.GET.getName(), ConfigureSystem.MethodRegEx.STATE.getName());
            System.out.println(methodSetStateType);

            // foreach stateType ... every observation point represents an serviceType respectively stateValue
            for (Method methodStateType : methodSetStateType) {

                try {
                    final String dateTimeNow = simpleDateFormatWithoutTimeZone.format(new Date());
                    final String subject = "O" + remoteUnitId + dateTimeNow; //TODO wait ?

                    //### unitID triple ###\\
                    tripleArrayListsBuf = addTripleHasUnitId(tripleArrayListsBuf, subject, remoteUnitId);

                    //### serviceType triple ###\\
                    final String serviceTypeInstance = getServiceTypeMapping(methodStateType.getName());
                    tripleArrayListsBuf = addTripleHasProviderService(tripleArrayListsBuf, subject, serviceTypeInstance);

                    Object stateTypeObj = null;
                    try {
                        // get method as invoked object
                        stateTypeObj = methodStateType.invoke(remoteData);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }

                    //### timeStamp triple ###\\
//                    final String timeStampObj = (String) ReflectObjectPool.getInvokedObj(stateTypeObj
//                            , ConfigureSystem.MethodRegEx.GET_TIMESTAMP.getName()); //TODO result empty
//                    tripleArrayListsBuf = addTripleHasProviderService(tripleArrayListsBuf, subject, timeStampObj);

                    //### stateValue triple ###\\
                    final boolean hasDataUnit = stateTypeHasDataUnit(stateTypeObj);
                    if (hasDataUnit) { //physical unit
                        //TODO need access to data...
                    } else {
                        final Object stateValueObj = ReflectObjectPool.getInvokedObj(stateTypeObj
                                , ConfigureSystem.MethodRegEx.GET_VALUE.getName());
                        final String stateValue = stateValueObj.toString();
                        tripleArrayListsBuf = addTripleHasProviderService(tripleArrayListsBuf, subject, stateValue);
                    }

                    // no exception produced: observation individual complete. add to main list
                    tripleArrayLists.addAll(tripleArrayListsBuf);
                    tripleArrayListsBuf.clear();

                } catch (CouldNotPerformException e) {
                    // Could not collect all elements of observation instance
                    tripleArrayListsBuf.clear();
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                }
            }

        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        final String sparqlUpdateExpr = getSparqlBundleUpdateInsertEx(tripleArrayLists);
        System.out.println(sparqlUpdateExpr);
        tripleArrayLists.clear();

        try {
            final int httpResponseCode = sparqlUpdate(sparqlUpdateExpr);
            final boolean httpSuccess = httpRequestSuccess(httpResponseCode);

            if (!httpSuccess) {
                //insert sparql update expression to buffer queue
                transactionBuffer.insertData(sparqlUpdateExpr);
            } else {
                System.out.println("success");
            }

        } catch (CouldNotPerformException e) {
            transactionBuffer.insertData(sparqlUpdateExpr);
        }
//            ((ColorableLightDataType.ColorableLightData) remoteData).
//            ((BatteryDataType.BatteryData) remoteData).getBatteryState().getLevel()
    }

}
