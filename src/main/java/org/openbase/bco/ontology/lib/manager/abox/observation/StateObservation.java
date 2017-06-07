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
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.datapool.ObjectReflection;
import org.openbase.bco.ontology.lib.utility.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.MethodRegEx;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.utility.sparql.StaticSparqlExpression;
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

    public StateObservation(final UnitRemote unitRemote, final Class data) throws InstantiationException {
        try {
            this.methodSetStateType = ObjectReflection.getMethodSetByRegEx(data, MethodRegEx.GET.getName(), MethodRegEx.STATE.getName());
            this.unitType = unitRemote.getType();
            this.rsbInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(OntConfig.ONTOLOGY_RSB_SCOPE, OntologyChange.class);
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
        final List<ServiceType> services = new ArrayList<>();
        // main list, which contains complete observation instances
        final List<RdfTriple> insert = new ArrayList<>();
        final List<RdfTriple> delete = new ArrayList<>();
        // first collect all components of the individual observation, then add to main list (integrity reason)
        List<RdfTriple> insertBuf = new ArrayList<>();

        //TODO get stateType only, which has changed...
        // foreach stateType ... every observation point represents an serviceType respectively stateValue
        for (Method methodStateType : methodSetStateType) {
            try {
                // wait one millisecond to guarantee, that observation instances are unique
                stopwatch.waitForStop(1);

                // get method as invoked object
                final Object obj_stateType = methodStateType.invoke(remoteData);

                //### timeStamp triple ###\\
                final TimestampType.Timestamp stateTimestamp = (TimestampType.Timestamp) ObjectReflection
                        .getInvokedObject(obj_stateType , MethodRegEx.GET_TIMESTAMP.getName());

                if (stateTimestamp.hasTime() && stateTimestamp.getTime() != 0) {
                    final String serviceTypeName = StringModifier.getServiceTypeNameFromStateMethodName(methodStateType.getName());
                    final String obsInstName;

                    if (OntConfig.ONTOLOGY_MODE_HISTORIC_DATA) {
                        final String dateTimeNow = new DateTime().toString();
                        obsInstName = OntConfig.OntInstPrefix.OBSERVATION.getPrefixName() + remoteUnitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
                    } else {
                        obsInstName = OntConfig.OntInstPrefix.OBSERVATION.getPrefixName() + remoteUnitId + serviceTypeName;
                        delete.add(new RdfTriple(obsInstName, null, null));
                    }

                    final Timestamp timestamp = new Timestamp(TimestampJavaTimeTransform.transform(stateTimestamp));
                    final String obj_dateTime = StringModifier.addXsdDateTime(dateFormat.format(timestamp));
                    insertBuf.add(new RdfTriple(obsInstName, OntProp.TIME_STAMP.getName(), obj_dateTime));

                    //### add observation instance to observation class ###\\
                    insertBuf.add(new RdfTriple(obsInstName, OntExpr.IS_A.getName(), OntCl.OBSERVATION.getName()));

                    //### unitID triple ###\\
                    insertBuf.add(new RdfTriple(obsInstName, OntProp.UNIT_ID.getName(), remoteUnitId));

                    //### serviceType triple ###\\
                    services.add(OntConfig.SERVICE_NAME_MAP.get(serviceTypeName));
                    insertBuf.add(new RdfTriple(obsInstName, OntProp.PROVIDER_SERVICE.getName(), serviceTypeName));

                    //### stateValue triple ###\\
                    final int sizeBuf = insertBuf.size(); //TODO
                    insertBuf = addStateValue(OntConfig.SERVICE_NAME_MAP.get(serviceTypeName), obj_stateType, obsInstName, insertBuf);

                    if (insertBuf.size() == sizeBuf) {
                        // incomplete observation instance. dropped...
                        insertBuf.clear();
                    }

                    // no exception produced: observation individual complete. add to main list
                    insert.addAll(insertBuf);
                }
            } catch (IllegalAccessException | InvocationTargetException | CouldNotPerformException e) {
                // Could not collect all elements of observation instance
                ExceptionPrinter.printHistory("Could not get data from stateType " + methodStateType.getName() + " from unitRemote " + remoteUnitId
                        + ". Dropped.", e, LOGGER, LogLevel.WARN);
            } catch (InterruptedException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            } catch (NoSuchElementException e) {

            }
            insertBuf.clear();
        }

        final String sparql;

        if (OntConfig.ONTOLOGY_MODE_HISTORIC_DATA) {
            sparql = SparqlUpdateExpression.getSparqlInsertExpression(insert);
        } else {
            sparql = SparqlUpdateExpression.getSparqlUpdateExpression(delete, insert, StaticSparqlExpression.getNullWhereExpression());
        }
        System.out.println(sparql);

        if (sendData(sparql)) {
            rsbNotification(services);
        }
    }

    private void rsbNotification(final List<ServiceType> serviceList) throws InterruptedException, CouldNotPerformException {

        final OntologyChange ontologyChange = OntologyChange.newBuilder().addUnitType(unitType).addAllServiceType(serviceList).build();
        // publish notification via rsb
        rsbInformer.activate();
        rsbInformer.publish(ontologyChange);
        rsbInformer.deactivate();
    }

    private boolean sendData(final String sparql) {
        try {
            return SparqlHttp.uploadSparqlRequest(sparql);
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("At least one element is null or whole update string is bad!", e, LOGGER, LogLevel.ERROR);
            return false;
        }
    }
}
