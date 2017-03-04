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

package org.openbase.bco.ontology.lib.testcode;

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
import org.openbase.bco.ontology.lib.config.OntConfig;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author agatting on 25.10.16.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public class FillOntology {

    private static final Logger LOGGER = LoggerFactory.getLogger(FillOntology.class);
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

    //TODO create lists with necessary classes for comparing (e.g. all subclasses of superclass "state")
    //TODO efficiency: e.g. getIndividual faster
    //TODO check iterator (if empty => nullPointer)
    //TODO getLocalName() not stable, if first char is a number...

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
            //UnitProcessor.
            //UnitConfigProcessor.
        }

        // test code
        /*BrightnessSensorRemote remote = new BrightnessSensorRemote();
        try {
            remote.initById("3249a1a5-52d1-4be1-910f-2063974b53f5");
            remote.activate();
            remote.waitForData();
            remote.getBrightnessState().getBrightness();

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
                                        ontClass = ontModel.getOntClass(OntConfig.NS + "Door");
                                        break;
                                    case "window":
                                        ontClass = ontModel.getOntClass(OntConfig.NS + "Window");
                                        break;
                                    case "passage":
                                        ontClass = ontModel.getOntClass(OntConfig.NS + "Passage");
                                        break;
                                    default:
                                        break;
                                }
                            } else if (className.equals(individualUnitName) && "location".equals(className)) {
                                switch (unitConfig.getLocationConfig().getType().toString().toLowerCase()) {
                                    case "region":
                                        ontClass = ontModel.getOntClass(OntConfig.NS + "Region");
                                        break;
                                    case "tile":
                                        ontClass = ontModel.getOntClass(OntConfig.NS + "Tile");
                                        break;
                                    case "zone":
                                        ontClass = ontModel.getOntClass(OntConfig.NS + "Zone");
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (className.equals(individualUnitName)) {
                                ontModel.createIndividual(OntConfig.NS + unitConfig.getId(), ontClass);
                            }
                        } catch (JenaException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                    if (integrateProviderService) { //TODO
                        integrateIndividualProviderServices(unitConfig);
                    }
                }
            }
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

        // add dataTypeProperties
        integrateDataTypeProperties(registry);

        try {
            for (final UnitConfig unitConfigLocation : registry.getUnitConfigs(UnitType.LOCATION)) {
                if (unitConfigLocation.getEnablingState().getValue().equals(State.ENABLED)) {
                    String locationTypeName = unitConfigLocation.getLocationConfig().getType().name().toLowerCase();
                    char[] charVar = locationTypeName.toCharArray();
                    charVar[0] = Character.toUpperCase(charVar[0]);
                    locationTypeName = new String(charVar);

                    // maybe without "Region"-class (no subLocation)
                    final ExtendedIterator individualIterator = ontModel.listIndividuals(ontModel
                            .getOntClass(OntConfig.NS + locationTypeName));

                    while (individualIterator.hasNext()) {
                        final Individual individual = (Individual) individualIterator.next();
                        // hint: getLocalName() doesn't work perfect, if the first characters of the id are numbers.
                        // Method expects the numbers as part of the namespace...
                        //TODO maybe error potential...
                        final String locationIdName = individual.getURI().substring(OntConfig.NS.length());
                        if (locationIdName.equals(unitConfigLocation.getId())) {
                            // property "hasSubLocation"
                            for (final String childId : unitConfigLocation.getLocationConfig().getChildIdList()) {
                                //TODO check Individual if null...
                                final Individual child = ontModel.getIndividual(OntConfig.NS + childId);
                                final ObjectProperty objectProperty = ontModel
                                        .getObjectProperty(OntConfig.NS + "hasSubLocation");
                                individual.addProperty(objectProperty, child);
                            }
                            // property "hasUnit"
                            for (final String unitId : unitConfigLocation.getLocationConfig().getUnitIdList()) {
                                //TODO check Individual if null...
                                final Individual unit = ontModel.getIndividual(OntConfig.NS + unitId);
                                final ObjectProperty objectProperty = ontModel
                                        .getObjectProperty(OntConfig.NS + "hasUnit");
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

                    final ObjectProperty objectProperty = ontModel.getObjectProperty(OntConfig.NS
                            + "hasConnection");
                    final ExtendedIterator individualIterator = ontModel.listIndividuals(ontModel
                            .getOntClass(OntConfig.NS + connectionTypeName));

                    while (individualIterator.hasNext()) {
                        final Individual connectionIndividual = (Individual) individualIterator.next();
                        final String connectionIdName = connectionIndividual.getURI()
                                .substring(OntConfig.NS.length());

                        if (connectionIdName.equals(unitConfigConnection.getId())) {
                            // property "hasConnection"
                            for (final String connectionTile : unitConfigConnection.getConnectionConfig()
                                    .getTileIdList()) {
                                final Individual tileIndividual = ontModel.getIndividual(OntConfig.NS
                                        + connectionTile);

                                tileIndividual.addProperty(objectProperty, connectionIndividual);
                            }
                        }
                    }
                }
            }

            for (final ServiceConfigType.ServiceConfig serviceConfig : registry.getServiceConfigs()) {
                final Individual stateIndividual = ontModel.getIndividual(OntConfig.NS
                        + serviceConfig.getUnitId());
                final ObjectProperty objectProperty = ontModel.getObjectProperty(OntConfig.NS + "hasState");
                final Individual serviceTypeIndividual = ontModel.getIndividual(OntConfig.NS + serviceConfig
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
                    .getOntClass(NAMESPACE + "STATE"));

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

    /**
     * Fill the ontology with dataTypeProperties.
     * @param registry the unit registry.
     */
    protected void integrateDataTypeProperties(final UnitRegistry registry) {
        try {
            final DatatypeProperty dataTypeHasLabel = ontModel.getDatatypeProperty(OntConfig.NS + "hasLabel");
            final DatatypeProperty dataTypeIsAvailable = ontModel.getDatatypeProperty(OntConfig.NS
                    + "isAvailable");

            registry.getUnitConfigs().stream().filter(unitConfig ->
                    unitConfig.getEnablingState().getValue().equals(State.ENABLED)).forEach(unitConfig -> {

                final String unitLabel = unitConfig.getLabel();
                final String unitId = unitConfig.getId();
                final Individual unitIndividual = ontModel.getIndividual(OntConfig.NS + unitId);

                // create dataTypeProperty hasLabel
                final Literal literal = ontModel.createTypedLiteral(unitLabel);
                unitIndividual.addLiteral(dataTypeHasLabel, literal);

                // create dataTypeProperty isAvailable
                unitIndividual.addLiteral(dataTypeIsAvailable, true);
            });
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    private void createObservationIndividual(final UnitConfig unitConfig, final ServiceType serviceType) {
        //TODO serviceType via unitConfig?
        observationNumber++;

        // create observation individual
        final Individual startIndividualObservation = ontModel.createIndividual(OntConfig
                .NS + "o" + observationNumber, ontModel.getOntClass(OntConfig.NS + "OBSERVATION"));

        // create objectProperty hasUnitId
        final Individual endIndividualUnit = ontModel.getIndividual(OntConfig.NS + unitConfig.getId());
        ObjectProperty objectProperty = ontModel.getObjectProperty(OntConfig.NS + "hasUnitId");
        startIndividualObservation.addProperty(objectProperty, endIndividualUnit);

        // create objectProperty hasProviderService
        if (serviceType != null) {
            final Individual endIndividualServiceType = ontModel.getIndividual(OntConfig.NS
                    + serviceType.toString());
            objectProperty = ontModel.getObjectProperty(OntConfig.NS + "hasProviderService");
            startIndividualObservation.addProperty(objectProperty, endIndividualServiceType);
        }

        // create property hasStateValue
        try {
            final UnitRemote unitRemote = UnitRemoteFactoryImpl.getInstance().newInitializedInstance(unitConfig);
            unitRemote.activate();
            unitRemote.waitForData();

            final Object objectState = findMethodByUnitRemote(unitRemote, OntConfig.GET_PATTERN_STATE);
            final Object objectStateValue = findMethodByObject(objectState, "getValue");

            objectProperty = ontModel.getObjectProperty(OntConfig.NS + "hasStateValue");

            //measure point of the unit has a dataTypeValue
            if (objectStateValue == null) {
                // whole string to lower case and delete substring "state"
                String state = objectState.getClass().getName().toLowerCase().replaceAll(OntConfig.STATE, "");
                // string has whole class path name. cut string at position of method name (starts with char "$")
                state = state.substring(state.lastIndexOf('$') + 1);
                final Object objectDataTypeStateValue = findMethodByObject(objectState, OntConfig.GET + state);

                if (objectDataTypeStateValue == null) {
                    LOGGER.error("No stateValue or dataTypeValue by unit: " + unitConfig.getId() + " is: "
                            + unitConfig.getType());
                } else {
                    //TODO literal format: stateValue + physical dataUnit
                    //get dataUnit
                    final Object objectDataUnit = findMethodByObject(objectState
                            , "dataUnit");
                    if (objectDataUnit == null) {
                        LOGGER.error("No dataUnit by unit: " + unitConfig.getId() + " is unitType: "
                                + unitConfig.getType());
                    } else {
                        final Literal stateValueLiteral = ontModel.createTypedLiteral(objectDataTypeStateValue
                                .toString() + " " + objectDataUnit.toString());
//                        RDFDatatype rdfDatatype = new BaseDatatype(OntConfig.NS + "fahrenheit");
//                        Literal literal = ontModel.createTypedLiteral(1.3, rdfDatatype);
                        // create dataTypeProperty "hasStateValue"
                        startIndividualObservation.addLiteral(objectProperty, stateValueLiteral);
                    }
                }
            } else {
                //measure point of the unit has a normal stateValue: create objectProperty "hasStateValue"
                final Individual endIndividualStateValue = ontModel.getIndividual(OntConfig.NS
                        + objectStateValue);
                startIndividualObservation.addProperty(objectProperty, endIndividualStateValue);
            }

            // create dataTypeProperty hasTimeStamp
            final Object objectTimeStamp = findMethodByObject(objectState, "getTimeStamp");
            final Literal literal = ontModel.createTypedLiteral(objectTimeStamp.toString());
            final DatatypeProperty datatypeProperty = ontModel.getDatatypeProperty(OntConfig.NS
                    + "hasTimeStamp");
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
        //TODO check all unitTypes and their stateValues (e.g. userActivity, userPresence)
        LOGGER.info("Start integrate ontology with individual stateValues...");
        final OntClass ontClassStateValue = ontModel.getOntClass(OntConfig.NS + "StateValue");

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

                    final Object objectState = findMethodByUnitRemote(unitRemote,
                            OntConfig.GET_PATTERN_STATE);
                    String objectStateName = objectState.getClass().getName().toLowerCase();
                    objectStateName = objectStateName.substring(objectStateName.
                            lastIndexOf(OntConfig.DOLLAR_SIGN) + 1);

                    final Object objectStateValue = findMethodByObject(objectState
                            , "getValue");

                    if (objectStateValue != null) {
                        ontModel.createIndividual(OntConfig.NS + objectStateValue, ontClassStateValue);
                    }

                    final Object objectId = findMethodByUnitRemote(unitRemote, "getId");
                    final ExtendedIterator classIterator = ontModel.listClasses();

                    while (classIterator.hasNext()) {
                        final OntClass ontClass = (OntClass) classIterator.next();
                        final String className = ontClass.getLocalName().toLowerCase();

                        //TODO swap out and a unit has (maybe) multiple states
                        // find correct state type class
                        if (objectId != null && objectStateName.contains(className)
                                && !className.equals(OntConfig.STATE)) {
                            ontModel.createIndividual(OntConfig.NS + objectId, ontClass);
                        }
                    }
                }
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * Get a method of an unknown java object.
     * @param object Source object (/class) of the method. Based on normal object to get class (compare with
     *               method: findMethodByUnitRemote(final UnitRemote unitRemote, String regex)).
     * @param regex Regular expression to find the method (method name). Better success if detailed.
     * @return Returns method-object by success, else null.
     */
    private Object findMethodByObject(final Object object, final String regex) {
        try {
            final String regexBuf = regex.toLowerCase(Locale.ENGLISH);
            final Method[] method = object.getClass().getMethods();

            for (final Method aMethod : method) {
                if (Pattern.matches(regexBuf, aMethod.getName().toLowerCase())) {
                    try {
                        return object.getClass().getMethod(aMethod.getName()).invoke(object);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        //TODO regex enum

        return null;
    }

    /**
     * Get a method of an unknown java object.
     * @param unitRemote Source object (/class) of the method. Based on the remote to get data class directly.
     * @param regex Regular expression to find the method (method name). Better success if detailed.
     * @return Returns method-object by success, else null.
     */
    private Object findMethodByUnitRemote(final UnitRemote unitRemote, final String regex) {
        final String regexBuf = regex.toLowerCase(Locale.ENGLISH);
        final Method[] method = unitRemote.getDataClass().getMethods();

        for (final Method aMethod : method) {
            if (Pattern.matches(regexBuf, aMethod.getName().toLowerCase())) {
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

    /**
     * Integration of the individual providerServices into the ontology.
     * @param unitConfig the config list of the units.
     */
    protected void integrateIndividualProviderServices(final UnitConfig unitConfig) {
        //LOGGER.info("Start integrate ontology with individual providerServices...");
        unitConfig.getServiceConfigList().stream().filter(serviceConfig -> serviceConfig.getServiceTemplate()
                .getPattern().toString().toLowerCase().equals("provider")).forEach(serviceConfig -> {
            final OntClass ontClass = ontModel.getOntClass(OntConfig.NS + "ProviderService");
            ontModel.createIndividual(OntConfig.NS + serviceConfig.getServiceTemplate().getType(), ontClass);
        });
    }

    /**
     * Observer.
     */
    protected void observer() {
        /**
         * Test example to create one observation individual.
         */
        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        try {
            for (final UnitConfig unitConfig : registry.getUnitConfigs(UnitType.BRIGHTNESS_SENSOR)) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)) {
                    final ServiceType serviceType = ServiceType.BRIGHTNESS_STATE_SERVICE;


                    /*PowerConsumptionSensorRemote remote = new PowerConsumptionSensorRemote();
                    try {
                        remote.initById("3249a1a5-52d1-4be1-910f-2063974b53f5");
                        remote.activate();
                        remote.waitForData();
                        remote.getPowerConsumptionState().

                        //System.out.println(remote.getData().getBrightnessState().getBrightnessDataUnit());
                    } catch (InterruptedException | CouldNotPerformException e) {
                            e.printStackTrace();
                    }*/

                    createObservationIndividual(unitConfig, serviceType);
                    break;
                }
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        /**
         * End of test example.
         */


        //TODO observer (beside objectProperties -> dataTypeProperty 'isAvailable')
    }
}
