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

import javafx.util.Pair;
import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.ontology.lib.commun.rsb.RsbCommunication;
import org.openbase.bco.ontology.lib.commun.web.WebInterface;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.MethodRegEx;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.manager.datapool.ReflectObjectPool;
import org.openbase.bco.ontology.lib.manager.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.trigger.sparql.TypeAlignment;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rst.processing.TimestampJavaTimeTransform;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.ActivationStateType.ActivationState.State;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.timing.TimestampType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private final UnitRemote unitRemote;
    private String s_CurConnectionPhase;
    private boolean wasConnected;
    private boolean isInit;

    public StateObservation(final UnitRemote unitRemote, final TransactionBuffer transactionBuffer, final RSBInformer<OntologyChange> rsbInformer)
            throws InstantiationException {

        try {
            this.unitType = unitRemote.getType();
            this.rsbInformer = rsbInformer;
            this.transactionBuffer = transactionBuffer;
            this.stopwatch = new Stopwatch();
            this.serviceTypeMap = TypeAlignment.getAlignedServiceTypes();
            this.unitRemote = unitRemote;
            this.isInit = true;

            initConnectionState();
//            init(classType);

            final Observer<T> unitRemoteStateObserver = (Observable<T> observable, T unitRemoteObj) -> {
            if (isInit) {
                remoteUnitId = (String) ReflectObjectPool.getInvokedObj(unitRemoteObj, MethodRegEx.GET_ID.getName());
                methodSetStateType = ReflectObjectPool.getMethodSetByRegEx(unitRemoteObj, MethodRegEx.GET.getName(), MethodRegEx.STATE.getName());
                isInit = false;
            }
                stateUpdate(unitRemoteObj); //TODO catch exception?!
            };

            final Observer<ConnectionState> unitRemoteConnectionObserver = (Observable<ConnectionState> observable, ConnectionState connectionState) -> {
                if (connectionState.equals(ConnectionState.CONNECTED) && !wasConnected) {
                    // was NOT connected and now is connected - start connection phase
                    updateConnectionPhase(State.ACTIVE);
                    wasConnected = !wasConnected;
                } else if (!connectionState.equals(ConnectionState.CONNECTED) && wasConnected){
                    // was connected and now is NOT connected - close connection phase
                    updateConnectionPhase(State.DEACTIVE);
                    wasConnected = !wasConnected;
                }
            };

            unitRemote.addDataObserver(unitRemoteStateObserver);
            unitRemote.addConnectionStateObserver(unitRemoteConnectionObserver);
        } catch (NotAvailableException | JPServiceException e) {
            throw new InstantiationException(this, e);
        }
    }

//    private void init(final T classType) {
//        try {
//            remoteUnitId = (String) ReflectObjectPool.getInvokedObj(classType, MethodRegEx.GET_ID.getName());
//            System.out.println("unitid: " + remoteUnitId);
//            methodSetStateType = ReflectObjectPool.getMethodSetByRegEx(classType, MethodRegEx.GET.getName(), MethodRegEx.STATE.getName());
//        } catch (CouldNotPerformException e) {
//            System.out.println("testerror"); //TODO
//        }
//    }

    private void updateConnectionPhase(final State activationState) throws JPServiceException {

        // s: subject, p: predicate, o: object
        final String pred_IsA = OntExpr.A.getName();
        final String pred_HasFirstConnection = OntProp.FIRST_CONNECTION.getName();
        final String pred_HasLastConnection = OntProp.LAST_CONNECTION.getName();
        final String pred_HasConnectionPhase = OntProp.CONNECTION_PHASE.getName();
        final String obj_ConnectionPhase = OntCl.CONNECTION_PHASE.getName();

        final List<TripleArrayList> insertTriple = new ArrayList<>();

        if (activationState.equals(State.ACTIVE)) {
            final Date now = new Date();
            s_CurConnectionPhase = "connectionPhase" + simpleDateFormatWithoutTimeZone.format(now) + remoteUnitId; // must be the same at start and close!
            final String obj_Timestamp = "\"" + simpleDateFormat.format(now) + "\"^^xsd:dateTime";

            insertTriple.add(new TripleArrayList(s_CurConnectionPhase, pred_IsA, obj_ConnectionPhase));
            insertTriple.add(new TripleArrayList(remoteUnitId, pred_HasConnectionPhase, s_CurConnectionPhase));
            insertTriple.add(new TripleArrayList(s_CurConnectionPhase, pred_HasFirstConnection, obj_Timestamp));

        } else if (activationState.equals(State.DEACTIVE)) {

            final String obj_Timestamp = "\"" + simpleDateFormat.format(new Date()) + "\"^^xsd:dateTime";
            insertTriple.add(new TripleArrayList(s_CurConnectionPhase, pred_IsA, obj_ConnectionPhase));
            insertTriple.add(new TripleArrayList(remoteUnitId, pred_HasConnectionPhase, s_CurConnectionPhase));
            insertTriple.add(new TripleArrayList(s_CurConnectionPhase, pred_HasLastConnection, obj_Timestamp));

        } else {
            LOGGER.warn("Method updateConnectionPhase is called with wrong ActivationState parameter.");
        }

        final String sparqlUpdate = sparqlUpdateExpression.getSparqlBundleUpdateInsertEx(insertTriple);
        sendToServer(sparqlUpdate);
    }

    private void initConnectionState() throws JPServiceException {
        // reduce connectionState to binary classification - connected and not connected
        if (unitRemote.getConnectionState().equals(ConnectionState.CONNECTED)) {
            wasConnected = true;
            updateConnectionPhase(State.ACTIVE);
        } else {
            wasConnected = false;
        }
    }

    private void stateUpdate(final T remoteData) throws InterruptedException, CouldNotPerformException, JPServiceException {
        
        final List<ServiceType> serviceList = new ArrayList<>();
        // main list, which contains complete observation instances
        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();
        // first collect all components of the individual observation, then add to main list (integrity reason)
        List<TripleArrayList> tripleArrayListsBuf = new ArrayList<>();

        // declaration of predicates and classes, which are static
        final String obj_Observation = OntCl.OBSERVATION.getName();
        final String pred_IsA = OntExpr.A.getName();
        final String pred_HasUnitId = OntProp.UNIT_ID.getName();
        final String pred_HasService = OntProp.PROVIDER_SERVICE.getName();
        final String pred_HasTimeStamp = OntProp.TIME_STAMP.getName();

        //TODO get stateType only, which has changed...
        // foreach stateType ... every observation point represents an serviceType respectively stateValue
        for (Method methodStateType : methodSetStateType) {
            try {
                // wait one millisecond to guarantee, that observation instances are unique
                stopwatch.waitForStop(1);

                // get method as invoked object
                final Object obj_stateType = methodStateType.invoke(remoteData);

                final String dateTimeNow = simpleDateFormatWithoutTimeZone.format(new Date());
                final String subj_Observation = "O" + remoteUnitId + dateTimeNow;

                //### add observation instance to observation class ###\\
                tripleArrayListsBuf.add(new TripleArrayList(subj_Observation, pred_IsA, obj_Observation));

                //### unitID triple ###\\
                tripleArrayListsBuf.add(new TripleArrayList(subj_Observation, pred_HasUnitId, remoteUnitId));

                //### serviceType triple ###\\
                final ServiceType serviceType = getServiceType(methodStateType.getName());
                final String obj_serviceType = serviceType.name();
                serviceList.add(serviceTypeMap.get(obj_serviceType));
                tripleArrayListsBuf.add(new TripleArrayList(subj_Observation, pred_HasService, obj_serviceType));

                //### timeStamp triple ###\\
                final TimestampType.Timestamp stateTimestamp = (TimestampType.Timestamp) ReflectObjectPool
                        .getInvokedObj(obj_stateType , MethodRegEx.GET_TIMESTAMP.getName());

                if (stateTimestamp.hasTime() && stateTimestamp.getTime() != 0) {
                    final Timestamp timestamp = new Timestamp(TimestampJavaTimeTransform.transform(stateTimestamp));
                    final String obj_dateTime = "\"" + simpleDateFormat.format(timestamp) + "\"^^xsd:dateTime";
                    tripleArrayListsBuf.add(new TripleArrayList(subj_Observation, pred_HasTimeStamp, obj_dateTime));
                }

                //### stateValue triple ###\\
                tripleArrayListsBuf = addStateValue(obj_serviceType, obj_stateType, subj_Observation, tripleArrayListsBuf);

                // no exception produced: observation individual complete. add to main list
                tripleArrayLists.addAll(tripleArrayListsBuf);

            } catch (IllegalAccessException | InvocationTargetException | CouldNotPerformException e) {
                // Could not collect all elements of observation instance
                ExceptionPrinter.printHistory("Could not get data from stateType " + methodStateType.getName() + " from unitRemote " + remoteUnitId
                        + ". Dropped.", e, LOGGER, LogLevel.WARN);
            } catch (InterruptedException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            }
            tripleArrayListsBuf.clear();
        }

        final String sparqlUpdateExpr = sparqlUpdateExpression.getSparqlBundleUpdateInsertEx(tripleArrayLists);
        System.out.println(sparqlUpdateExpr);

        final boolean isHttpSuccess = sendToServer(sparqlUpdateExpr);
        if (isHttpSuccess) {
            rsbNotification(serviceList);
        }
    }

    private boolean sendToServer(final String sparqlUpdateExpr) throws JPServiceException {
        try {
            final boolean isHttpSuccess = WebInterface.sparqlUpdateToMainOntology(sparqlUpdateExpr, OntConfig.ServerServiceForm.UPDATE);

            if (!isHttpSuccess) {
                // could not send to server - insert sparql update expression to buffer queue
                transactionBuffer.insertData(new Pair<>(sparqlUpdateExpr, false));
            }
            return isHttpSuccess;
        } catch (CouldNotPerformException e) {
            // could not send to server - insert sparql update expression to buffer queue
            transactionBuffer.insertData(new Pair<>(sparqlUpdateExpr, false));
        }
        return false;
    }

    private void rsbNotification(final List<ServiceType> serviceList) {

        final OntologyChange ontologyChange = OntologyChange.newBuilder().addUnitType(unitType).addAllServiceType(serviceList).build();
        // publish notification via rsb
        RsbCommunication.startNotification(rsbInformer, ontologyChange);
    }

    private ServiceType getServiceType(final String methodStateType) throws NoSuchElementException {

        // standardized string to allow comparison
        final String stateTypeBuf = methodStateType.toLowerCase().replaceFirst(MethodRegEx.GET.getName(), "");

        for (final String serviceType : serviceTypeMap.keySet()) {
            if (serviceType.contains(stateTypeBuf)) {
                // successful compared - return correct serviceType
                return serviceTypeMap.get(serviceType);
            }
        }
        throw new NoSuchElementException("Could not identify methodState, cause there is no element, which contains " + methodStateType);
    }
}
