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
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.utility.ReflectionUtility;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.MethodRegEx;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class creates an instance of an observation, which is used for an unit. The observation sends the state data with timestamp to the ontology server.
 *
 * @param <Type> is used to identify the unit data class.
 * @author agatting on 09.01.17.
 */
public class StateObservation<Type> extends IdentifyStateTypeValue {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateObservation.class);
    private final SimpleDateFormat dateFormat;
    private final String remoteUnitId;
    private final Stopwatch stopwatch;
    private final Set<Method> methodSetStateType;
    private final RSBInformer<OntologyChange> rsbInformer;
    private final UnitType unitType;
    private final ConnectionPhase connectionPhase;
    private Type observerData;

    private final RecurrenceEventFilter recurrenceEventFilter = new RecurrenceEventFilter(1) {
        @Override
        public void relay() {
            try {
                stateUpdate(observerData);
            } catch (InterruptedException | CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR); //TODO handling?!
            }
        }
    };

    /**
     * Constructor initiates the observation of the input unitRemote to detect the state value and send it to the ontology server.
     *
     * @param unitRemote contains the state data, which should be observed.
     * @throws InstantiationException is thrown in case the observation could not be initiated, because at least one component failed.
     */
    public StateObservation(final UnitRemote unitRemote) throws InstantiationException {
        try {
            this.methodSetStateType = ReflectionUtility.detectMethods(unitRemote.getDataClass(), MethodRegEx.STATE_METHOD.getName(), Pattern.CASE_INSENSITIVE);
            this.unitType = unitRemote.getTemplate().getType();
            this.rsbInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(OntConfig.getOntologyRsbScope(), OntologyChange.class);
            this.stopwatch = new Stopwatch();
            this.remoteUnitId = unitRemote.getId().toString();
            this.connectionPhase = new ConnectionPhase(unitRemote);
            this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());

            final Observer<Type> unitRemoteStateObserver = (final Observable<Type> observable, final Type remoteData) -> {
                this.observerData = remoteData;
                recurrenceEventFilter.trigger();
            };

            final Observer<ConnectionState> unitRemoteConnectionObserver = (final Observable<ConnectionState> observable, final ConnectionState connectionState)
                    -> connectionPhase.identifyConnectionState(connectionState);
            unitRemote.addDataObserver(unitRemoteStateObserver);
            unitRemote.addConnectionStateObserver(unitRemoteConnectionObserver);

        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    private void stateUpdate(final Type remoteData) throws InterruptedException, CouldNotPerformException {
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
                final Object stateType = methodStateType.invoke(remoteData);

                //### timeStamp triple ###\\
                final TimestampType.Timestamp stateTimestamp = (TimestampType.Timestamp) ReflectionUtility
                        .invokeMethod(stateType, MethodRegEx.GET_TIMESTAMP.getName(), Pattern.CASE_INSENSITIVE);

                if (stateTimestamp.hasTime() && stateTimestamp.getTime() != 0) {
                    final String serviceTypeName = StringModifier.getServiceTypeNameFromStateMethodName(methodStateType.getName());
                    final String obsInstName;

                    if (OntConfig.getOntologyModeHistoricData()) {
                        final String dateTimeNow = OffsetDateTime.now().toString();
                        obsInstName = OntConfig.OntPrefix.OBSERVATION.getName() + remoteUnitId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
                    } else {
                        obsInstName = OntConfig.OntPrefix.OBSERVATION.getName() + remoteUnitId + serviceTypeName;
                        delete.add(new RdfTriple(obsInstName, null, null));
                    }

                    final Timestamp timestamp = new Timestamp(TimestampJavaTimeTransform.transform(stateTimestamp));
                    final String timestampLiteral = StringModifier.convertToLiteral(dateFormat.format(timestamp), XsdType.DATE_TIME);
                    insertBuf.add(new RdfTriple(obsInstName, OntProp.TIME_STAMP.getName(), timestampLiteral));

                    //### add observation instance to observation class ###\\
                    insertBuf.add(new RdfTriple(obsInstName, OntExpr.IS_A.getName(), OntCl.OBSERVATION.getName()));

                    //### unitID triple ###\\
                    insertBuf.add(new RdfTriple(obsInstName, OntProp.UNIT_ID.getName(), remoteUnitId));

                    //### serviceType triple ###\\
                    services.add(OntConfig.SERVICE_NAME_MAP.get(serviceTypeName));
                    insertBuf.add(new RdfTriple(obsInstName, OntProp.PROVIDER_SERVICE.getName(), serviceTypeName));

                    //### stateValue triple ###\\
                    final int sizeBuf = insertBuf.size(); //TODO
                    insertBuf = addStateValue(OntConfig.SERVICE_NAME_MAP.get(serviceTypeName), stateType, obsInstName, insertBuf);

                    if (insertBuf.size() == sizeBuf) {
                        // incomplete observation instance. dropped...
                        insertBuf.clear();
                    }

                    // no exception produced: observation individual complete. add to main list
                    insert.addAll(insertBuf);
                }
            } catch (IllegalAccessException | InvocationTargetException | CouldNotPerformException ex) {
                // Could not collect all elements of observation instance
                ExceptionPrinter.printHistory("Could not get data from stateType " + methodStateType.getName() + " from unitRemote " + remoteUnitId
                        + ". Dropped.", ex, LOGGER, LogLevel.WARN);
            } catch (InterruptedException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.WARN);
            } catch (NoSuchElementException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
            insertBuf.clear();
        }

        final String sparql;

        if (OntConfig.getOntologyModeHistoricData()) {
            sparql = SparqlUpdateExpression.getSparqlInsertExpression(insert);
        } else {
            sparql = SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, insert, StaticSparqlExpression.getNullWhereExpression());
        }
        LOGGER.info(sparql);

        if (SparqlHttp.uploadSparqlRequest(sparql)) {
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

}
