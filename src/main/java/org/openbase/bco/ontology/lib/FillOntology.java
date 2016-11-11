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

package org.openbase.bco.ontology.lib;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.UnitRemoteFactoryImpl;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by agatting on 25.10.16.
 */
public class FillOntology {

    private static final Logger LOGGER = LoggerFactory.getLogger(FillOntology.class);
    private static final String NAMESPACE = "http://www.openbase.org/bco/ontology#";
    private static final String GET = "get";
    private static final String DATAUNIT = "dataunit";
    private static final String STATE = "state";
    private static final String PATTERN = "[a-z]*";
    private final OntModel ontModel;
    private long observationNumber = 0;

    /**
     * Constructor for filling ontology model.
     *
     * @param ontModel the ontology model.
     */
    public FillOntology(final OntModel ontModel) {
        this.ontModel = ontModel;
    }

    //TODO take all units or dis-/enabled units as config choice...
    //TODO create lists with necessary classes for comparing (e.g. all subclasses of superclass "state")
    //TODO efficiency: e.g. getIndividual faster
    //TODO check iterator (if empty => nullPointer)

    /**
     * Method fills the given ontology with unitType instances.
     * @param integrateProviderService instances of the providerService are integrated if true.
     */
    protected void integrateIndividualUnitTypes(final boolean integrateProviderService) {
        LOGGER.info("Start integrate ontology with individual unitTypes...");

        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        // test code
        /*TemperatureSensorRemote remote = new TemperatureSensorRemote();
        try {
            remote.initById("3249a1a5-52d1-4be1-910f-2063974b53f5");
            remote.activate();
            remote.waitForData();
            remote.getTemperatureState().getTemperatureDataUnit()

            //System.out.println(remote.getData().getBrightnessState().getBrightnessDataUnit());
        } catch (InterruptedException | CouldNotPerformException e) {
                e.printStackTrace();
        }*/

        try {
            //TODO: maybe a more efficient way of comparing?
            for (final UnitConfig unitConfig : registry.getUnitConfigs()) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)) {
                    final ExtendedIterator classIterator = ontModel.listClasses();
                    String individualUnitName = unitConfig.getType().toString().toLowerCase();
                    individualUnitName = individualUnitName.replaceAll("[^\\p{Alpha}]", "");

                    while (classIterator.hasNext()) {
                        OntClass ontClass = (OntClass) classIterator.next();
                        final String className = ontClass.getLocalName().toLowerCase();

                        try {
                            if (className.equals(individualUnitName) && "connection".equals(className)) {
                                switch (unitConfig.getConnectionConfig().getType().toString().toLowerCase()) {
                                    case "door":
                                        ontClass = ontModel.getOntClass(NAMESPACE + "Door");
                                        break;
                                    case "window":
                                        ontClass = ontModel.getOntClass(NAMESPACE + "Window");
                                        break;
                                    case "passage":
                                        ontClass = ontModel.getOntClass(NAMESPACE + "Passage");
                                        break;
                                    default:
                                        break;
                                }
                            } else if (className.equals(individualUnitName) && "location".equals(className)) {
                                switch (unitConfig.getLocationConfig().getType().toString().toLowerCase()) {
                                    case "region":
                                        ontClass = ontModel.getOntClass(NAMESPACE + "Region");
                                        break;
                                    case "tile":
                                        ontClass = ontModel.getOntClass(NAMESPACE + "Tile");
                                        break;
                                    case "zone":
                                        ontClass = ontModel.getOntClass(NAMESPACE + "Zone");
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (className.equals(individualUnitName)) {
                                ontModel.createIndividual(NAMESPACE + unitConfig.getId(), ontClass);
                            }
                        } catch (JenaException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                    if (integrateProviderService) {
                        integrateIndividualProviderServices(unitConfig);
                    }
                }
            }
            //TODO test, if all elements are used...
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * Fill the ontology with object properties.
     */
    protected void integrateObjectProperties() {
        //TODO need to check if there are individuals to link them
        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }


        try {
            for (final UnitConfig unitConfigLocation : registry.getUnitConfigs(UnitType.LOCATION)) {
                if (unitConfigLocation.getEnablingState().getValue().equals(State.ENABLED)) {
                    String locationTypeName = unitConfigLocation.getLocationConfig().getType().name().toLowerCase();
                    char[] charVar = locationTypeName.toCharArray();
                    charVar[0] = Character.toUpperCase(charVar[0]);
                    locationTypeName = new String(charVar);

                    // maybe without "Region"-class (no subLocation)
                    final ExtendedIterator individualIterator = ontModel.listIndividuals(ontModel
                            .getOntClass(NAMESPACE + locationTypeName));

                    while (individualIterator.hasNext()) {
                        final Individual individual = (Individual) individualIterator.next();
                        // hint: getLocalName() doesn't work perfect, if the first characters of the id are numbers.
                        // Method expects the numbers as part of the namespace...
                        //TODO maybe error potential...
                        final String locationIdName = individual.getURI().substring(NAMESPACE.length());
                        if (locationIdName.equals(unitConfigLocation.getId())) {
                            // property "hasSubLocation"
                            for (final String childId : unitConfigLocation.getLocationConfig().getChildIdList()) {
                                //TODO check Individual if null...
                                final Individual child = ontModel.getIndividual(NAMESPACE + childId);
                                final ObjectProperty objectProperty = ontModel
                                        .getObjectProperty(NAMESPACE + "hasSubLocation");
                                individual.addProperty(objectProperty, child);
                            }
                            // property "hasUnit"
                            for (final String unitId : unitConfigLocation.getLocationConfig().getUnitIdList()) {
                                //TODO check Individual if null...
                                final Individual unit = ontModel.getIndividual(NAMESPACE + unitId);
                                final ObjectProperty objectProperty = ontModel.getObjectProperty(NAMESPACE + "hasUnit");
                                individual.addProperty(objectProperty, unit);
                            }
                            break;
                        }
                    }
                }
            }

            for (final UnitConfig unitConfigConnection : registry.getUnitConfigs(UnitType.CONNECTION)) {
                if (unitConfigConnection.getEnablingState().getValue().equals(State.ENABLED)) {
                    String connectionTypeName = unitConfigConnection.getConnectionConfig().getType().name()
                            .toLowerCase();

                    char[] charVar = connectionTypeName.toCharArray();
                    charVar[0] = Character.toUpperCase(charVar[0]);
                    connectionTypeName = new String(charVar);

                    final ObjectProperty objectProperty = ontModel.getObjectProperty(NAMESPACE + "hasConnection");
                    final ExtendedIterator individualIterator = ontModel.listIndividuals(ontModel
                            .getOntClass(NAMESPACE + connectionTypeName));

                    while (individualIterator.hasNext()) {
                        final Individual connectionIndividual = (Individual) individualIterator.next();
                        final String connectionIdName = connectionIndividual.getURI().substring(NAMESPACE.length());

                        if (connectionIdName.equals(unitConfigConnection.getId())) {
                            // property "hasConnection"
                            for (final String connectionTile : unitConfigConnection.getConnectionConfig()
                                    .getTileIdList()) {
                                final Individual tileIndividual = ontModel.getIndividual(NAMESPACE + connectionTile);

                                tileIndividual.addProperty(objectProperty, connectionIndividual);
                            }
                        }
                    }
                }
            }

            for (final ServiceConfigType.ServiceConfig serviceConfig : registry.getServiceConfigs()) {
                final Individual stateIndividual = ontModel.getIndividual(NAMESPACE + serviceConfig.getUnitId());
                final ObjectProperty objectProperty = ontModel.getObjectProperty(NAMESPACE + "hasState");
                final Individual serviceTypeIndividual = ontModel.getIndividual(NAMESPACE + serviceConfig
                        .getServiceTemplate().getType());

                if (stateIndividual != null) {
                    serviceTypeIndividual.addProperty(objectProperty, stateIndividual);
                }

                //alternative code...test, if better performance later...
                /*final ObjectProperty objectProperty = ontModel.getObjectProperty(NAMESPACE + "hasState");

                //to do while service...
                final String serviceTypeName = serviceConfig.getServiceTemplate().getType().toString();
                final String stateIdNameReg = serviceConfig.getUnitId();

                final ExtendedIterator individualIterator = ontModel.listIndividuals(ontModel
                    .getOntClass(NAMESPACE + "State"));

                while (individualIterator.hasNext()) {
                    final Individual individual = (Individual) individualIterator.next();
                    final String stateIdNameOnt = individual.getURI().substring(NAMESPACE.length());

                    if (stateIdNameOnt.equals(stateIdNameReg)) {
                        final Individual serviceIndividual = ontModel.getIndividual(NAMESPACE + serviceTypeName);
                        serviceIndividual.addProperty(objectProperty, individual);
                    }
                }*/
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    private void createObservationIndivdual(final UnitConfig unitConfig
            , final ServiceTemplateType.ServiceTemplate.ServiceType serviceType) {
        //TODO serviceType via unitConfig
        observationNumber++;

        // create observation individual
        final Individual startIndividualObservation = ontModel.createIndividual(NAMESPACE + "o" + observationNumber
                , ontModel.getOntClass(NAMESPACE + "Observation"));

        // create objectProperty hasUnitId
        final Individual endIndividualUnit = ontModel.getIndividual(NAMESPACE + unitConfig.getId());
        ObjectProperty objectProperty = ontModel.getObjectProperty("hasUnitId");
        startIndividualObservation.addProperty(objectProperty, endIndividualUnit);

        // create objectProperty hasProviderService
        final Individual endIndividualServiceType = ontModel.getIndividual(NAMESPACE + serviceType.toString());
        objectProperty = ontModel.getObjectProperty(NAMESPACE + "hasProviderService");
        startIndividualObservation.addProperty(objectProperty, endIndividualServiceType);

        // create objectProperty hasStateValue (or hasStateValueLiteral with hasDataUnit)
        try {
            final UnitRemote unitRemote = UnitRemoteFactoryImpl.getInstance().newInitializedInstance(unitConfig);
            unitRemote.activate();
            unitRemote.waitForData();

            final Object objectState = findStateMethod(unitRemote);
            final Object objectStateValue = findGetValueMethod(objectState);

            if (objectStateValue == null) {
                //measure point of the unit has a dataTypeValue
                final Object objectDataTypeStateValue = findDataTypeStateValue(objectState);
                if (objectDataTypeStateValue == null) {
                    LOGGER.error("No stateValue or dataTypeValue by unit: " + unitConfig.getId() + " is: "
                            + unitConfig.getType());
                } else {
                    // create dataTypeProperty "hasStateValueLiteral
                    final Literal dataTypeValueLiteral = ontModel.createLiteral(objectDataTypeStateValue.toString());
                    final DatatypeProperty dataTypeProperty = ontModel
                            .getDatatypeProperty(NAMESPACE + "hasStateValueLiteral");
                    startIndividualObservation.addLiteral(dataTypeProperty, dataTypeValueLiteral);

                    //create objectProperty hasDataUnit
                    final Object objectDataUnit = findDataUnitMethod(objectState);
                    if (objectDataUnit == null) {
                        LOGGER.error("No dataUnit by unit: " + unitConfig.getId() + " is unitType: "
                                + unitConfig.getType());
                    } else {
                        final Individual endIndividualDataUnit = ontModel.getIndividual(NAMESPACE + objectState
                                .toString());
                        objectProperty = ontModel.getObjectProperty(NAMESPACE + "hasDataUnit");
                        startIndividualObservation.addProperty(objectProperty, endIndividualDataUnit);
                    }
                }
            } else {
                //measure point of the unit has a normal stateValue
                final Individual endIndividualStateValue = ontModel.getIndividual(NAMESPACE + objectStateValue);
                objectProperty = ontModel.getObjectProperty("hasStateValue");
                startIndividualObservation.addProperty(objectProperty, endIndividualStateValue);
            }

            // create dataTypeProperty hasTimeStamp
            final Object objectTimeStamp = findTimeStampMethod(objectState);
            final Literal literal = ontModel.createLiteral(objectTimeStamp.toString());
            final DatatypeProperty datatypeProperty = ontModel.getDatatypeProperty(NAMESPACE + "hasTimeStamp");
            startIndividualObservation.addLiteral(datatypeProperty, literal);

        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        //TODO check string output
    }

    /**
     * Integration of the individual stateValues into the ontology.
     */
    protected void integrateIndividualStateValues() {
        LOGGER.info("Start integrate ontology with individual stateValues...");
        final OntClass ontClassStateValue = ontModel.getOntClass(NAMESPACE + "StateValue");

        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        // TODO get something like the group of units for generic behavior
        // registry.getDalUnitConfigs....
        final ArrayList<UnitType> notDalUnits = new ArrayList<>();
        //AUTHORIZATION_GROUP, DISPLAY, RFID, TELEVISION
        notDalUnits.addAll(Arrays.asList(UnitType.AGENT, UnitType.APP, UnitType.AUDIO_SINK, UnitType.AUDIO_SOURCE,
                UnitType.AUTHORIZATION_GROUP, UnitType.CONNECTION, UnitType.DEVICE, UnitType.LOCATION, UnitType.SCENE,
                UnitType.USER, UnitType.VIDEO_DEPTH_SOURCE, UnitType.VIDEO_RGB_SOURCE));

        try {
            for (final UnitConfig unitConfig : registry.getUnitConfigs()) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)
                        && !notDalUnits.contains(unitConfig.getType())) {
                    final UnitRemote unitRemote = UnitRemoteFactoryImpl.getInstance()
                            .newInitializedInstance(unitConfig);
                    unitRemote.activate();
                    unitRemote.waitForData();

                    final Object objectState = findStateMethod(unitRemote);
                    final Object objectStateValue = findGetValueMethod(objectState);
                    final Object objectDataUnit = findDataUnitMethod(objectState);

                    if (objectStateValue != null) {
                        ontModel.createIndividual(NAMESPACE + objectStateValue, ontClassStateValue);
                    }

                    final Object objectId = findIdMethod(unitRemote);
                    final ExtendedIterator classIterator = ontModel.listClasses();

                    while (classIterator.hasNext()) {
                        final OntClass ontClass = (OntClass) classIterator.next();
                        final String className = ontClass.getLocalName().toLowerCase();

                        //TODO find a better way of comparing (getClass().getName() provides whole path...)
                        // find correct state type class
                        if (objectId != null && objectState.getClass().getName().toLowerCase().contains(className)
                                && !className.equals(STATE)) {
                            ontModel.createIndividual(NAMESPACE + objectId, ontClass);
                        }

                        // find correct data unit class
                        if (objectDataUnit != null && className.equals(DATAUNIT)) {
                            ontModel.createIndividual(NAMESPACE + objectDataUnit, ontClass);
                        }
                    }
                }
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    private Object findStateMethod(final UnitRemote unitRemote) {
        final Method[] method = unitRemote.getDataClass().getMethods();
        for (final Method aMethod : method) {
            if (Pattern.matches(GET + PATTERN + STATE, aMethod.getName().toLowerCase())) {
                try {
                    return unitRemote.getDataClass().getMethod(aMethod.getName()).invoke(unitRemote.getData());
                    //return getState.getClass().getMethod("getValue").invoke(getState);
                } catch (IllegalAccessException | InvocationTargetException | NotAvailableException
                        | NoSuchMethodException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
            }
        }
        return null;
    }

    private Object findGetValueMethod(final Object getState) {
        try {
            if (getState != null) {
                return getState.getClass().getMethod("getValue").invoke(getState);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        } catch (NoSuchMethodException e) {
            LOGGER.warn(e + " - wrong State. Insignificant.");
        }
        return null;
    }

    private Object findIdMethod(final UnitRemote unitRemote) {
        final Method[] method = unitRemote.getDataClass().getMethods();
        for (final Method aMethod : method) {
            if (Pattern.matches("getid", aMethod.getName().toLowerCase())) {
                try {
                    return unitRemote.getDataClass().getMethod(aMethod.getName()).invoke(unitRemote.getData());
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
                        | NotAvailableException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
            }
        }
        return null;
    }

    //TODO reduce find-methods to one generic method
    private Object findDataUnitMethod(final Object getState) {
        final Method[] method = getState.getClass().getMethods();
        for (final Method aMethod : method) {
            if (Pattern.matches(GET + PATTERN + DATAUNIT, aMethod.getName().toLowerCase())) {
                try {
                    return getState.getClass().getMethod(aMethod.getName()).invoke(getState);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
            }
        }
        return null;
    }

    private Object findTimeStampMethod(final Object getState) {
        final Method[] method = getState.getClass().getMethods();
        for (final Method aMethod : method) {
            if (Pattern.matches("gettimestamp", aMethod.getName().toLowerCase())) {
                try {
                    return getState.getClass().getMethod(aMethod.getName()).invoke(getState);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
            }
        }
        return null;
    }

    private Object findDataTypeStateValue(final Object getState) {
        final Method[] method = getState.getClass().getMethods();
        final String state = getState.getClass().getName().toLowerCase().replaceAll(STATE, "");
        for (final Method aMethod : method) {
            if (Pattern.matches(state, aMethod.getName().toLowerCase())) {
                try {
                    return getState.getClass().getMethod(aMethod.getName()).invoke(getState);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
            }
        }
        return null;
    }

    /**
     * Integration of the individual providerServices into the ontology.
     * @param unitConfig the config list of the units.
     */
    protected void integrateIndividualProviderServices(final UnitConfig unitConfig) {
        //LOGGER.info("Start integrate ontology with individual providerServices...");
        unitConfig.getServiceConfigList().stream().filter(serviceConfig -> serviceConfig.getServiceTemplate()
                .getPattern().toString().toLowerCase().equals("provider")).forEach(serviceConfig -> {
            final OntClass ontClass = ontModel.getOntClass(NAMESPACE + "ProviderService");
            ontModel.createIndividual(NAMESPACE + serviceConfig.getServiceTemplate().getType(), ontClass);
        });
    }

    /**
     * Observer.
     */
    protected void observer() {
        //TODO observer
    }
}
