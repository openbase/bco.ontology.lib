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

import org.joda.time.DateTime;
import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.ontology.lib.manager.datapool.ObjectReflection;
import org.openbase.bco.ontology.lib.utility.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.MethodRegEx;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
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
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.timing.TimestampType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author agatting on 09.01.17.
 */
public class StateObservation<T> extends IdentifyStateTypeValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateObservation.class);
    private final SimpleDateFormat dateFormat;
    private String remoteUnitId;
    private final Stopwatch stopwatch;
    private Set<Method> methodSetStateType;
    private final RSBInformer<OntologyChange> rsbInformer;
    private final UnitType unitType;
    private final ConnectionPhase connectionPhase;
    private T observerData;

    private final RecurrenceEventFilter recurrenceEventFilter = new RecurrenceEventFilter(1) {
        @Override
        public void relay() {
            try {
                stateUpdate(observerData);
            } catch (InterruptedException | CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR); //TODO handling?!
            }
        }
    };

    public StateObservation(final UnitRemote unitRemote, final Class<T> data) throws InstantiationException {
        try {
            this.methodSetStateType = ObjectReflection.getMethodSetByRegEx(data, MethodRegEx.GET.getName(), MethodRegEx.STATE.getName());
            this.unitType = unitRemote.getType();
            this.rsbInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(OntConfig.ONTOLOGY_SCOPE, OntologyChange.class);
            this.stopwatch = new Stopwatch();
            this.remoteUnitId = unitRemote.getId().toString();
            this.connectionPhase = new ConnectionPhase(unitRemote);
            this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());

            final Observer<T> unitRemoteStateObserver = (final Observable<T> observable, final T remoteData) -> {
                this.observerData = remoteData;
                recurrenceEventFilter.trigger();
            };

            final Observer<ConnectionState> unitRemoteConnectionObserver = (final Observable<ConnectionState> observable
                    , final ConnectionState connectionState) -> connectionPhase.identifyConnection(connectionState);

            unitRemote.addDataObserver(unitRemoteStateObserver);
            unitRemote.addConnectionStateObserver(unitRemoteConnectionObserver);

        } catch (CouldNotPerformException e) {
            throw new InstantiationException(this, e);
        }
    }

    private void stateUpdate(final T remoteData) throws InterruptedException, CouldNotPerformException {
//        if (Measurement.measurementWatch.isRunning()) {
//            Measurement.measurementWatch.stop();
//            Measurement.unitChange.add(Measurement.measurementWatch.getTime());
//            Measurement.measurementWatch.restart();
//        }
        final List<ServiceType> serviceList = new ArrayList<>();
        // main list, which contains complete observation instances
        final List<RdfTriple> rdfTriples = new ArrayList<>();
        // first collect all components of the individual observation, then add to main list (integrity reason)
        List<RdfTriple> rdfTripleArrayListsBuf = new ArrayList<>();

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

//                final String dateTimeNow = dateFormat.format(new Date());
                final String dateTimeNow = new DateTime().toString();
                final String subj_Observation = "O" + remoteUnitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));

                //### timeStamp triple ###\\
                final TimestampType.Timestamp stateTimestamp = (TimestampType.Timestamp) ObjectReflection
                        .getInvokedObject(obj_stateType , MethodRegEx.GET_TIMESTAMP.getName());

                if (stateTimestamp.hasTime() && stateTimestamp.getTime() != 0) {
                    final Timestamp timestamp = new Timestamp(TimestampJavaTimeTransform.transform(stateTimestamp));
                    final String obj_dateTime = "\"" + dateFormat.format(timestamp) + "\"^^xsd:dateTime";
//                    final String obj_dateTime = "\"" + timestamp + "\"^^xsd:dateTime";
                    rdfTripleArrayListsBuf.add(new RdfTriple(subj_Observation, pred_HasTimeStamp, obj_dateTime));

                    //### add observation instance to observation class ###\\
                    rdfTripleArrayListsBuf.add(new RdfTriple(subj_Observation, pred_IsA, obj_Observation));

                    //### unitID triple ###\\
                    rdfTripleArrayListsBuf.add(new RdfTriple(subj_Observation, pred_HasUnitId, remoteUnitId));

                    //### serviceType triple ###\\
                    final String serviceTypeName = StringModifier.getServiceTypeNameFromStateMethodName(methodStateType.getName());
                    serviceList.add(OntConfig.SERVICE_NAME_MAP.get(serviceTypeName));
                    rdfTripleArrayListsBuf.add(new RdfTriple(subj_Observation, pred_HasService, serviceTypeName));

                    //### stateValue triple ###\\
                    final int sizeBuf = rdfTripleArrayListsBuf.size();
                    rdfTripleArrayListsBuf = addStateValue(OntConfig.SERVICE_NAME_MAP.get(serviceTypeName), obj_stateType, subj_Observation, rdfTripleArrayListsBuf);

                    if (rdfTripleArrayListsBuf.size() == sizeBuf) {
                        // incomplete observation instance. dropped...
                        rdfTripleArrayListsBuf.clear();
                    }

                    // no exception produced: observation individual complete. add to main list
                    rdfTriples.addAll(rdfTripleArrayListsBuf);
                }
            } catch (IllegalAccessException | InvocationTargetException | CouldNotPerformException e) {
                // Could not collect all elements of observation instance
                ExceptionPrinter.printHistory("Could not get data from stateType " + methodStateType.getName() + " from unitRemote " + remoteUnitId
                        + ". Dropped.", e, LOGGER, LogLevel.WARN);
            } catch (InterruptedException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            } catch (NoSuchElementException e) {

            }
            rdfTripleArrayListsBuf.clear();
        }
        final String sparqlUpdateExpr = SparqlUpdateExpression.getSparqlUpdateExpression(rdfTriples);
//        System.out.println(sparqlUpdateExpr);
        final boolean isHttpSuccess = connectionPhase.sendToServer(sparqlUpdateExpr); //TODO

        if (isHttpSuccess) {
            rsbNotification(serviceList);
        }
    }

    private void rsbNotification(final List<ServiceType> serviceList) throws InterruptedException, CouldNotPerformException {

        final OntologyChange ontologyChange = OntologyChange.newBuilder().addUnitType(unitType).addAllServiceType(serviceList).build();
        // publish notification via rsb
        rsbInformer.activate();
        rsbInformer.publish(ontologyChange);
        rsbInformer.deactivate();
    }
}
