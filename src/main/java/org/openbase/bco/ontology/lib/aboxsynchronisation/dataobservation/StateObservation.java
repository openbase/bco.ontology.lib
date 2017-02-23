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

import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.stateProcessing.IdentifyStateType;
import org.openbase.bco.ontology.lib.datapool.ReflectObjectPool;
import org.openbase.bco.ontology.lib.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rst.processing.TimestampJavaTimeTransform;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.dal.ColorableLightDataType;
import rst.timing.TimestampType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
public class StateObservation<T> extends IdentifyStateType {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateObservation.class);
    private final SimpleDateFormat simpleDateFormatWithoutTimeZone = new SimpleDateFormat(ConfigureSystem
            .DATE_TIME_WITHOUT_TIME_ZONE, Locale.ENGLISH); //TODO
    private final Map<String, String> serviceTypeMap = new HashMap<>();
    private static String remoteUnitId = null;
    private final Stopwatch stopwatch;
    private TransactionBuffer transactionBuffer;
    private SparqlUpdateExpression sparqlUpdateExpression = new SparqlUpdateExpression();


    public StateObservation(final UnitRemote unitRemote, final UnitConfig unitConfig
            , final TransactionBuffer transactionBuffer) {


        this.transactionBuffer = transactionBuffer;
        stopwatch = new Stopwatch();

        initServiceTypeMap();

        remoteUnitId = unitConfig.getId();

        Observer<T> unitRemoteStateObserver = (Observable<T> observable, T unitRemoteObj) ->
                GlobalCachedExecutorService.submit(() -> stateUpdate(unitRemoteObj));

        Observer<ConnectionState> unitRemoteConnectionObserver = (Observable<ConnectionState> observable, ConnectionState connectionState) -> {
            GlobalCachedExecutorService.submit(() -> {
                if (connectionState.equals(ConnectionState.CONNECTED)) {
//                    System.out.println("connected!!!");
                } else {
//                    System.out.println("disconnected!!!");
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

    private void stateUpdate(final T remoteData) {
        //TODO implement logic, that updates changed stateValues only

        // main list, which contains complete observation instances
        List<TripleArrayList> tripleArrayLists = new ArrayList<>();
        // first collect all components of the individual observation, then add to main list (integrity reason)
        List<TripleArrayList> tripleArrayListsBuf = new ArrayList<>();

        // declaration of predicates and classes, which are static
        final String predicateIsA = ConfigureSystem.OntExpr.A.getName();
        final String objectObservationClass = ConfigureSystem.OntClass.OBSERVATION.getName();
        final String objectStateValueClass = ConfigureSystem.OntClass.STATE_VALUE.getName();
        final String predicateHasUnitId = ConfigureSystem.OntProp.UNIT_ID.getName();
        final String predicateHasProviderService = ConfigureSystem.OntProp.PROVIDER_SERVICE.getName();
        final String predicateHasTimeStamp = ConfigureSystem.OntProp.TIME_STAMP.getName();
        final String predicateHasStateValue = ConfigureSystem.OntProp.STATE_VALUE.getName();

//        System.out.println(Arrays.toString(remoteData.getClass().getMethods()));
//        System.out.println("--------------------");

        //TODO get stateType only, which has changed...

        try {
            final Set<Method> methodSetStateType = ReflectObjectPool.getMethodSetByRegEx(remoteData
                    , ConfigureSystem.MethodRegEx.GET.getName(), ConfigureSystem.MethodRegEx.STATE.getName());

            // foreach stateType ... every observation point represents an serviceType respectively stateValue
            for (Method methodStateType : methodSetStateType) {

//                System.out.println(methodStateType);
                try {
                    // wait one millisecond to guarantee, that observation instances are unique
                    try {
                        stopwatch.waitForStop(1);
                    } catch (InterruptedException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                    }

                    // get method as invoked object
                    Object stateTypeObj;
                    try {
                        stateTypeObj = methodStateType.invoke(remoteData); //TODO
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new CouldNotPerformException(e);
                    }

                    final String dateTimeNow = simpleDateFormatWithoutTimeZone.format(new Date());
                    final String subjectObservation = "O" + remoteUnitId + dateTimeNow;

                    //### add observation instance to observation class ###\\
                    tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateIsA, objectObservationClass));

                    //### unitID triple ###\\
                    tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasUnitId, remoteUnitId));

                    //### serviceType triple ###\\
                    final String objectServiceType = getServiceTypeMapping(methodStateType.getName());
                    tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasProviderService
                            , objectServiceType));

                    //### timeStamp triple ###\\
                    TimestampType.Timestamp stateTimestamp = (TimestampType.Timestamp) ReflectObjectPool
                            .getInvokedObj(stateTypeObj , ConfigureSystem.MethodRegEx.GET_TIMESTAMP.getName());

                    if (stateTimestamp.hasTime() && stateTimestamp.getTime() != 0) {
                        final Timestamp timestamp = new Timestamp(TimestampJavaTimeTransform.transform(stateTimestamp));
                        final String timestampXsd = "\"" + timestamp + "\"^^xsd:dateTime";
                        tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasTimeStamp
                                , timestampXsd));
                    }

                    //### stateValue triple ###\\
//                    final boolean hasDataUnit = stateTypeHasDataUnit(stateTypeObj);
//                    if (hasDataUnit) { //physical unit
//                        //TODO need access to data...
//                    } else {
//                        final Object stateValueObj = ReflectObjectPool.getInvokedObj(stateTypeObj
//                                , ConfigureSystem.MethodRegEx.GET_VALUE.getName());
//                        final String objectStateValue = stateValueObj.toString();
//
//                        tripleArrayListsBuf.add(new TripleArrayList(objectStateValue, predicateIsA
//                                , objectStateValueClass)); // TODO: redundant. another possibility?
//                        tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasStateValue
//                                , objectStateValue));
//                    }

                    //### Test ###\\
                    tripleArrayListsBuf = addStateValue(objectServiceType, stateTypeObj, subjectObservation, tripleArrayListsBuf);


                    // no exception produced: observation individual complete. add to main list
                    tripleArrayLists.addAll(tripleArrayListsBuf);

                } catch (CouldNotPerformException e) {
                    // Could not collect all elements of observation instance
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                }
                tripleArrayListsBuf.clear();
            }
//            System.out.println("-----------");

        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        final String sparqlUpdateExpr = sparqlUpdateExpression.getSparqlBundleUpdateInsertEx(tripleArrayLists);
        System.out.println(sparqlUpdateExpr);
        tripleArrayLists.clear();

        try {
            final int httpResponseCode = sparqlUpdateExpression.sparqlUpdate(sparqlUpdateExpr);
            final boolean httpSuccess = sparqlUpdateExpression.httpRequestSuccess(httpResponseCode);

            if (!httpSuccess) {
                //insert sparql update expression to buffer queue
                transactionBuffer.insertData(sparqlUpdateExpr);
            } else {
//                System.out.println("success");
            }

        } catch (IOException e) {
            transactionBuffer.insertData(sparqlUpdateExpr);
        }

//        ((ColorableLightDataType.ColorableLightData) remoteData).getColorState().getColor().getHsbColor().getBrightness()
//            ((BatteryDataType.BatteryData) remoteData).getBatteryState().getLevel()
    }

}
