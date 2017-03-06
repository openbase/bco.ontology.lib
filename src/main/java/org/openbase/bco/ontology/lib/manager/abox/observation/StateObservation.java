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
package org.openbase.bco.ontology.lib.manager.abox.observation;

import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.MethodRegEx;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.manager.datapool.ReflectObjectPool;
import org.openbase.bco.ontology.lib.manager.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rst.processing.TimestampJavaTimeTransform;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.timing.TimestampType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
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
public class StateObservation<T> extends IdentifyStateTypeValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateObservation.class);
    private final SimpleDateFormat simpleDateFormatWithoutTimeZone = new SimpleDateFormat(OntConfig.DATE_TIME_WITHOUT_TIME_ZONE, Locale.ENGLISH); //TODO
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.ENGLISH); //TODO
    private Map<String, ServiceType> serviceTypeMap;
    private String remoteUnitId;
    private final Stopwatch stopwatch;
    private final TransactionBuffer transactionBuffer;
    private final SparqlUpdateExpression sparqlUpdateExpression = new SparqlUpdateExpression();
    private Set<Method> methodSetStateType;
    private final RSBInformer<OntologyChange> rsbInformer;
    private final UnitType unitType;
    private final OntologyChange.Category category;
    private final List<ServiceType> serviceList;
    private boolean isInit = true;

    public StateObservation(final UnitRemote unitRemote, final TransactionBuffer transactionBuffer, final RSBInformer<OntologyChange> rsbInformer)
            throws NotAvailableException {

        this.serviceList = new ArrayList<>();
        this.category = OntologyChange.Category.UNKNOWN; //TODO
        this.unitType = unitRemote.getType();
        this.rsbInformer = rsbInformer;
        this.transactionBuffer = transactionBuffer;
        this.stopwatch = new Stopwatch();

        initServiceTypeMap();

        final Observer<T> unitRemoteStateObserver = (Observable<T> observable, T unitRemoteObj) -> {
            if (isInit) {
                remoteUnitId = (String) ReflectObjectPool.getInvokedObj(unitRemoteObj, MethodRegEx.GET_ID.getName());
                methodSetStateType = ReflectObjectPool.getMethodSetByRegEx(unitRemoteObj, MethodRegEx.GET.getName(), MethodRegEx.STATE.getName());
                isInit = false;
            }

            stateUpdate(unitRemoteObj); //TODO catch exception?!
        };

        final Observer<ConnectionState> unitRemoteConnectionObserver = (Observable<ConnectionState> observable, ConnectionState connectionState) -> {
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

    private void stateUpdate(final T remoteData) throws InterruptedException, CouldNotPerformException {
        // main list, which contains complete observation instances
        List<TripleArrayList> tripleArrayLists = new ArrayList<>();
        // first collect all components of the individual observation, then add to main list (integrity reason)
        List<TripleArrayList> tripleArrayListsBuf = new ArrayList<>();

        // declaration of predicates and classes, which are static
        final String predicateIsA = OntExpr.A.getName();
        final String objectObservationClass = OntCl.OBSERVATION.getName();
        final String predicateHasUnitId = OntProp.UNIT_ID.getName();
        final String predicateHasProviderService = OntProp.PROVIDER_SERVICE.getName();
        final String predicateHasTimeStamp = OntProp.TIME_STAMP.getName();

        //TODO get stateType only, which has changed...

        // foreach stateType ... every observation point represents an serviceType respectively stateValue
        for (Method methodStateType : methodSetStateType) {
            try {
                // wait one millisecond to guarantee, that observation instances are unique
                try {
                    stopwatch.waitForStop(1);
                } catch (InterruptedException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                }

                // get method as invoked object
                final Object stateTypeObj = methodStateType.invoke(remoteData);

                final String dateTimeNow = simpleDateFormatWithoutTimeZone.format(new Date());
                final String subjectObservation = "O" + remoteUnitId + dateTimeNow;

                //### add observation instance to observation class ###\\
                tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateIsA, objectObservationClass));

                //### unitID triple ###\\
                tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasUnitId, remoteUnitId));

                //### serviceType triple ###\\
                final String serviceTypeObj = getServiceType(methodStateType.getName());
                tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasProviderService, serviceTypeObj));
//                System.out.println(serviceTypeObj);

                //### timeStamp triple ###\\
                final TimestampType.Timestamp stateTimestamp = (TimestampType.Timestamp) ReflectObjectPool
                        .getInvokedObj(stateTypeObj , MethodRegEx.GET_TIMESTAMP.getName());

                if (stateTimestamp.hasTime() && stateTimestamp.getTime() != 0) {
                    final Timestamp timestamp = new Timestamp(TimestampJavaTimeTransform.transform(stateTimestamp));
                    final String dateTime = "\"" + simpleDateFormat.format(timestamp) + "\"^^xsd:dateTime";
                    tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasTimeStamp, dateTime));
                }

                //### stateValue triple ###\\
                tripleArrayListsBuf = addStateValue(serviceTypeObj, stateTypeObj, subjectObservation, tripleArrayListsBuf);

                // no exception produced: observation individual complete. add to main list
                tripleArrayLists.addAll(tripleArrayListsBuf);

            } catch (IllegalAccessException | InvocationTargetException | CouldNotPerformException e) {
                // Could not collect all elements of observation instance
                ExceptionPrinter.printHistory("Could not get data from stateType " + methodStateType.getName() + " from unitRemote " + remoteUnitId
                        + ". Dropped.", e, LOGGER, LogLevel.WARN);
            }
            tripleArrayListsBuf.clear();
        }

        final String sparqlUpdateExpr = sparqlUpdateExpression.getSparqlBundleUpdateInsertEx(tripleArrayLists);
        System.out.println(sparqlUpdateExpr);
        tripleArrayLists.clear();

        sendToServer(sparqlUpdateExpr);
    }

    private void sendToServer(final String sparqlUpdateExpr) {
        try {
            final boolean isHttpSuccess = sparqlUpdateExpression.sparqlUpdateToMainOntology(sparqlUpdateExpr);

            if (isHttpSuccess) {
                final OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(category).addUnitType(unitType)
                        .addAllServiceType(serviceList).build();

                // publish notification via rsb
                rsbInformer.publish(ontologyChange);
            } else {
                // could not send to server - insert sparql update expression to buffer queue
                transactionBuffer.insertData(sparqlUpdateExpr);
            }
        } catch (IOException e) {
            transactionBuffer.insertData(sparqlUpdateExpr);
        } catch (InterruptedException | CouldNotPerformException e) {
            ExceptionPrinter.printHistory("Could not notify trigger via rsb!", e, LOGGER, LogLevel.ERROR);
        }
        serviceList.clear();
    }

    private void initServiceTypeMap() {
        serviceTypeMap  = new HashMap<>();

        for (final ServiceType serviceType : ServiceType.values()) {
            final String reducedServiceTypeName = serviceType.name().toLowerCase().replaceAll(OntExpr.REMOVE.getName(), "");

            serviceTypeMap.put(reducedServiceTypeName, serviceType);
        }
    }

    private String getServiceType(final String methodStateType) throws NoSuchElementException {

        // standardized string to allow comparison
        final String stateTypeBuf = methodStateType.toLowerCase().replaceFirst(MethodRegEx.GET.getName(), "");

        for (final String serviceType : serviceTypeMap.keySet()) {
            if (serviceType.contains(stateTypeBuf)) {
                // successful compared - return correct serviceType (ontology individual name)
                serviceList.add(serviceTypeMap.get(serviceType));
                return serviceTypeMap.get(serviceType).name();
            }
        }
        throw new NoSuchElementException("Could not identify methodState, cause there is no element, which contains " + methodStateType);
    }
}
