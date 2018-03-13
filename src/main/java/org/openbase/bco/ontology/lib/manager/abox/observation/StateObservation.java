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
import org.openbase.bco.ontology.lib.utility.rdf.RdfNodeObject;
import org.openbase.bco.ontology.lib.utility.sparql.QueryExpression;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.system.config.OntConfig.MethodRegEx;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntPrefix;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rst.processing.TimestampJavaTimeTransform;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.openbase.jul.schedule.RecurrenceEventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.timing.TimestampType;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class creates an instance of an observation, which is used for an unit. The observation sends the state data with timestamp to the ontology server.
 *
 * @param <T> is used to identify the unit data class.
 * @author agatting on 09.01.17.
 */
public class StateObservation<T> extends StateSources {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateObservation.class);
    private final SimpleDateFormat dateFormat;
    private final String unitRemoteId;
//    private final Stopwatch stopwatch;
    private final RSBInformer<OntologyChange> rsbInformer;
    private final UnitType unitType;
    private final ConnectionPhase connectionPhase;
    private T providerServiceObj;

    /**
     * Constructor initiates the observation of the input unitRemote to detect the state values and send it to the ontology server.
     *
     * @param unitRemote contains the state data, which should be observed.
     * @throws InstantiationException is thrown in case the observation could not be initiated, because at least one component failed.
     */
    public StateObservation(final UnitRemote unitRemote) throws InstantiationException {
        try {
            this.unitType = unitRemote.getUnitType();
            this.rsbInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(OntConfig.getOntologyRsbScope(), OntologyChange.class);
//            this.stopwatch = new Stopwatch();
            this.unitRemoteId = unitRemote.getId().toString();
            this.connectionPhase = new ConnectionPhase(unitRemote);
            this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());

            createAndAddObserver(unitRemote);

        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    /**
     * Method creates and adds the necessary observer for collecting time and system state information. The observer types are:
     * 1 connectionState: Keep the state of the unit remote connection to verify the correctness of the state changes.
     * N providerServices: Keep the state changes of the different provider services, which are set dependent on the unit remote.
     *
     * @param unitRemote is the unit remote to identify the provider services and their state changes.
     * @throws CouldNotPerformException is thrown in case the provider services could not be detected.
     */
    private void createAndAddObserver(final UnitRemote unitRemote) throws CouldNotPerformException {
        final Set<Method> methodsStateType =
                ReflectionUtility.detectMethods(unitRemote.getDataClass(), MethodRegEx.STATE_METHOD.getName(), Pattern.CASE_INSENSITIVE);

        for (final Method getGenericStateService : methodsStateType) {
            final String serviceTypeName = StringModifier.getServiceTypeNameFromStateMethodName(getGenericStateService.getName());
            final ServiceType serviceType = OntConfig.SERVICE_NAME_MAP.get(serviceTypeName);

            final RecurrenceEventFilter recurrenceEventFilter = new RecurrenceEventFilter(200) {
                @Override
                public void relay() {
                    serviceStateChangeProcessing(providerServiceObj, serviceType, serviceTypeName);
                }
            };

            final Observer<T> serviceStateObserver = (final Observable<T> observable, final T providerServiceData) -> {
                this.providerServiceObj = providerServiceData;
                recurrenceEventFilter.trigger();
            };

            unitRemote.addServiceStateObserver(OntConfig.SERVICE_NAME_MAP.get(serviceTypeName), serviceStateObserver);
        }

        final Observer<ConnectionState> unitRemoteConnectionObserver = (final Observable<ConnectionState> observable, final ConnectionState connectionState)
                    -> connectionPhase.identifyConnectionState(connectionState);
        unitRemote.addConnectionStateObserver(unitRemoteConnectionObserver);
    }

    /**
     * Method uses the providerService data to build the observation individual, which are used to store the state changes in the ontology. Furthermore, the
     * method observes the validity (e.g. valid timestamp) to ensure correct and complete data points in the ontology. In case of validity the triples are send
     * to the ontology server and, if successfully updated, a rsb notification is done.
     *
     * Current parts of an observation triple bundle are:
     * - is_a observation class
     * - unit id
     * - provider service
     * - timestamp
     * - state change (maybe contains multiple different values, because granularity of differentiation based on service state observer)
     *
     * @param providerServiceData are the data containing the state change information.
     * @param serviceType is the type of provider service, which the data belongs to.
     * @param serviceTypeName is the type of provider service in ontology string form.
     */
    private void serviceStateChangeProcessing(final T providerServiceData, final ServiceType serviceType, final String serviceTypeName) {
        final List<RdfTriple> insert = new ArrayList<>();
        final List<RdfTriple> delete = new ArrayList<>();

        try {
            final TimestampType.Timestamp stateTimestamp = (TimestampType.Timestamp) ReflectionUtility
                    .invokeMethod(providerServiceData, MethodRegEx.GET_TIMESTAMP.getName(), Pattern.CASE_INSENSITIVE);

            if (!stateTimestamp.hasTime() || stateTimestamp.getTime() == 0) {
                return; // no valid timestamp
            }

            final List<RdfNodeObject> stateSourcesResult = identifyStateType(OntConfig.SERVICE_NAME_MAP.get(serviceTypeName), providerServiceData);

            if (stateSourcesResult == null) {
                return; // incomplete observation instance, because could not identify state value
            }

            final Timestamp timestamp = new Timestamp(TimestampJavaTimeTransform.transform(stateTimestamp));
            final String timestampLiteral = StringModifier.convertToLiteral(dateFormat.format(timestamp), XsdType.DATE_TIME);

            for (final RdfNodeObject nodeObject : stateSourcesResult) {
                final String obsInstName;

                if (OntConfig.getOntologyManagerMode()) {
                    //--- in the moment (use of recurrenceEventFilter and max frequency time > 1) the time part of the nomenclature isn't necessary ---\\
//                    stopwatch.waitForStop(1); // wait one millisecond to guarantee, that observation instances are unique
//                    final String dateTimeNow = OffsetDateTime.now().toString();
//                    obsInstName = OntPrefix.OBSERVATION.getName() + unitRemoteId + dateTimeNow.substring(0, dateTimeNow.indexOf("+"));
                    obsInstName = OntPrefix.OBSERVATION.getName() + unitRemoteId + serviceTypeName;
                } else {
                    obsInstName = OntPrefix.OBSERVATION.getName() + unitRemoteId + serviceTypeName;
                    delete.add(new RdfTriple(obsInstName, null, null));
                }

                insert.add(new RdfTriple(obsInstName, OntExpr.IS_A.getName(), OntCl.OBSERVATION.getName()));
                insert.add(new RdfTriple(obsInstName, OntProp.UNIT_ID.getName(), unitRemoteId));
                insert.add(new RdfTriple(obsInstName, OntProp.TIME_STAMP.getName(), timestampLiteral));
                insert.add(new RdfTriple(obsInstName, OntProp.PROVIDER_SERVICE.getName(), serviceTypeName));

                for (final String stateValue : nodeObject.getStateValues()) {
                    // if value is literal (continuous value like hsb), the input string is correct.
                    // Otherwise (resource/discrete value ike ON, OFF, OPEN, ...) the naming convention of the ontology is performed
                    final String stateValueName = (nodeObject.isLiteral()) ? stateValue : StringModifier.firstCharToLowerCase(StringModifier.getCamelCaseName(stateValue));
                    insert.add(new RdfTriple(obsInstName, OntProp.STATE_VALUE.getName(), stateValueName));
                }
            }
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory("Dropped observation individual.", ex, LOGGER, LogLevel.ERROR);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not perform timestamp method via invocation. " + serviceTypeName + " from unitRemote " + unitRemoteId
                    + ". Dropped.", ex, LOGGER, LogLevel.ERROR);
        }

        try {
            final String sparql = (OntConfig.getOntologyManagerMode()) ? SparqlUpdateExpression.getSparqlInsertExpression(insert)
                    : SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, insert, QueryExpression.getNullWhereExpression());
            LOGGER.info(sparql);
            if (SparqlHttp.uploadSparqlRequest(sparql)) {
                rsbNotification(serviceType);
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    /**
     * After successfully updating of the state change(s) the method sends finally a rsb notification (to all listening trigger). The notification contains
     * the ontologyChange, which describes the kind of state change.
     *
     * @param serviceType is the type of service and part of the ontologyChange.
     * @throws InterruptedException is thrown in case the rsb notification failed by interrupting.
     * @throws CouldNotPerformException is thrown in case the rsb notification failed by processing the rsb instance.
     */
    private void rsbNotification(final ServiceType serviceType) throws InterruptedException, CouldNotPerformException {

        final OntologyChange ontologyChange = OntologyChange.newBuilder().addUnitType(unitType).addServiceType(serviceType).build();
        // publish notification via rsb
        rsbInformer.activate();
        rsbInformer.publish(ontologyChange);
        rsbInformer.deactivate();
    }

}
