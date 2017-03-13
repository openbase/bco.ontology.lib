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
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.manager.datapool.ObjectReflection;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.MethodRegEx;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.manager.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.trigger.sparql.TypeAlignment;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rst.processing.TimestampJavaTimeTransform;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.RecurrenceEventFilter;
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
    private final SimpleDateFormat dateFormat;
    private Map<String, ServiceType> serviceTypeMap;
    private String remoteUnitId;
    private final Stopwatch stopwatch;
    private final TransactionBuffer transactionBuffer;
    private Set<Method> methodSetStateType;
    private final RSBInformer<OntologyChange> rsbInformer;
    private final UnitType unitType;
    private String s_CurConnectionPhase;
    private boolean wasConnected;
    private T observerData;

    private final RecurrenceEventFilter recurrenceEventFilter = new RecurrenceEventFilter(2000) {
        @Override
        public void relay() {
            try {
                stateUpdate(observerData);
            } catch (InterruptedException | CouldNotPerformException | JPServiceException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR); //TODO handling?!
            }
        }
    };

    public StateObservation(final UnitRemote unitRemote, final TransactionBuffer transactionBuffer, final RSBInformer<OntologyChange> rsbInformer
            , final Class<T> data) throws InstantiationException {

        try {
            this.methodSetStateType = ObjectReflection.getMethodSetByRegEx(data, MethodRegEx.GET.getName(), MethodRegEx.STATE.getName());
            this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());
            this.unitType = unitRemote.getType();
            this.rsbInformer = rsbInformer;
            this.transactionBuffer = transactionBuffer;
            this.stopwatch = new Stopwatch();
            this.serviceTypeMap = TypeAlignment.getAlignedServiceTypes();
            this.remoteUnitId = unitRemote.getId().toString();

            initConnectionState(unitRemote);

            final Observer<T> unitRemoteStateObserver = (Observable<T> observable, T remoteData) -> {
                this.observerData = remoteData;
                recurrenceEventFilter.trigger();
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

        } catch (JPServiceException | CouldNotPerformException e) {
            throw new InstantiationException(this, e);
        }
    }

    private void updateConnectionPhase(final State activationState) throws JPServiceException {

        final String pred_IsA = OntExpr.A.getName();
        final String pred_HasFirstConnection = OntProp.FIRST_CONNECTION.getName();
        final String pred_HasLastConnection = OntProp.LAST_CONNECTION.getName();
        final String pred_HasConnectionPhase = OntProp.CONNECTION_PHASE.getName();
        final String obj_ConnectionPhase = OntCl.CONNECTION_PHASE.getName();

        final List<TripleArrayList> insertTriple = new ArrayList<>();

        if (activationState.equals(State.ACTIVE)) {
            final Date now = new Date();
            final String dateTime = dateFormat.format(now);
            s_CurConnectionPhase = "connectionPhase" + remoteUnitId + dateTime.substring(0, dateTime.indexOf("+")); // must be the same at start and close!
            final String obj_Timestamp = "\"" + dateFormat.format(now) + "\"^^xsd:dateTime";

            insertTriple.add(new TripleArrayList(s_CurConnectionPhase, pred_IsA, obj_ConnectionPhase));
            insertTriple.add(new TripleArrayList(remoteUnitId, pred_HasConnectionPhase, s_CurConnectionPhase));
            insertTriple.add(new TripleArrayList(s_CurConnectionPhase, pred_HasFirstConnection, obj_Timestamp));

        } else if (activationState.equals(State.DEACTIVE)) {

            final String obj_Timestamp = "\"" + dateFormat.format(new Date()) + "\"^^xsd:dateTime";
            insertTriple.add(new TripleArrayList(s_CurConnectionPhase, pred_IsA, obj_ConnectionPhase));
            insertTriple.add(new TripleArrayList(remoteUnitId, pred_HasConnectionPhase, s_CurConnectionPhase));
            insertTriple.add(new TripleArrayList(s_CurConnectionPhase, pred_HasLastConnection, obj_Timestamp));

        } else {
            LOGGER.warn("Method updateConnectionPhase is called with wrong ActivationState parameter.");
        }

        final String sparqlUpdate = SparqlUpdateExpression.getSparqlUpdateInsertBundleExpr(insertTriple);
        sendToServer(sparqlUpdate);
    }

    private void initConnectionState(final UnitRemote unitRemote) throws JPServiceException {
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

                final String dateTimeNow = dateFormat.format(new Date());
                final String subj_Observation = "O" + remoteUnitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));

                //### add observation instance to observation class ###\\
                tripleArrayListsBuf.add(new TripleArrayList(subj_Observation, pred_IsA, obj_Observation));

                //### unitID triple ###\\
                tripleArrayListsBuf.add(new TripleArrayList(subj_Observation, pred_HasUnitId, remoteUnitId));

                //### serviceType triple ###\\
                final String obj_serviceType = getServiceType(methodStateType.getName());
                serviceList.add(serviceTypeMap.get(obj_serviceType));
                tripleArrayListsBuf.add(new TripleArrayList(subj_Observation, pred_HasService, obj_serviceType));

                //### timeStamp triple ###\\
                final TimestampType.Timestamp stateTimestamp = (TimestampType.Timestamp) ObjectReflection
                        .getInvokedObject(obj_stateType , MethodRegEx.GET_TIMESTAMP.getName());

                if (stateTimestamp.hasTime() && stateTimestamp.getTime() != 0) {
                    final Timestamp timestamp = new Timestamp(TimestampJavaTimeTransform.transform(stateTimestamp));
                    final String obj_dateTime = "\"" + dateFormat.format(timestamp) + "\"^^xsd:dateTime";
                    tripleArrayListsBuf.add(new TripleArrayList(subj_Observation, pred_HasTimeStamp, obj_dateTime));
                }

                //### stateValue triple ###\\
                final int sizeBuf = tripleArrayListsBuf.size();
                tripleArrayListsBuf = addStateValue(obj_serviceType, obj_stateType, subj_Observation, tripleArrayListsBuf);

                if (tripleArrayListsBuf.size() == sizeBuf) {
                    // incomplete observation instance. dropped...
                    tripleArrayListsBuf.clear();
                }

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

        final String sparqlUpdateExpr = SparqlUpdateExpression.getSparqlUpdateInsertBundleExpr(tripleArrayLists);
        System.out.println(sparqlUpdateExpr);

        final boolean isHttpSuccess = sendToServer(sparqlUpdateExpr);
        if (isHttpSuccess) {
            rsbNotification(serviceList);
        }
    }

    private boolean sendToServer(final String sparqlUpdateExpr) throws JPServiceException {
        try {
            final boolean isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToMainOntology(sparqlUpdateExpr, OntConfig.ServerServiceForm.UPDATE);

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

    private String getServiceType(final String methodStateType) throws NoSuchElementException {

        // standardized string to allow comparison
        final String stateTypeBuf = methodStateType.replaceFirst(MethodRegEx.GET.getName(), "");
        for (final String serviceType : serviceTypeMap.keySet()) {
            if (serviceType.toLowerCase().startsWith(stateTypeBuf.toLowerCase())) {
                // successful compared - return correct serviceType name (aligned)
                return serviceType;
            }
        }
        throw new NoSuchElementException("Could not identify methodState, cause there is no element, which contains " + methodStateType);
    }
}
