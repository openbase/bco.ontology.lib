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

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
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
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType;
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
    private final OntModel ontModel;

    /**
     * Constructor for filling ontology model.
     *
     * @param ontModel the ontology model.
     */
    public FillOntology(final OntModel ontModel) {
        this.ontModel = ontModel;
    }

    //TODO take all units or dis-/enabled units as config choice...

    /**
     * Method fills the given ontology with unitType instances.
     * @param integrateProviderService instances of the providerService are integrated if true.
     */
    protected void integrateIndivUnitTypes(final boolean integrateProviderService) {
        LOGGER.info("Start integrate ontology with individual unitTypes...");

        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        try {
            //TODO: maybe a more efficient way of comparing?
            for (final UnitConfigType.UnitConfig unitConfig : registry.getUnitConfigs()) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)) {
                    final ExtendedIterator classIterator = ontModel.listClasses();
                    String indivUnitName = unitConfig.getType().toString().toLowerCase();
                    indivUnitName = indivUnitName.replaceAll("[^\\p{Alpha}]", "");

                    while (classIterator.hasNext()) {
                        OntClass ontClass = (OntClass) classIterator.next();
                        final String className = ontClass.getLocalName().toLowerCase();

                        try {
                            if (className.equals(indivUnitName) && "connection".equals(className)) {
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
                            } else if (className.equals(indivUnitName) && "location".equals(className)) {
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
                            if (className.equals(indivUnitName)) {
                                ontModel.createIndividual(NAMESPACE + unitConfig.getId(), ontClass);
                            }
                        } catch (JenaException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                    if (integrateProviderService) {
                        integrateIndivProviderServices(unitConfig);
                    }
                }
            }
            //TODO test, if all elements are used...
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * Integration of the individual stateValues into the ontology.
     */
    protected void integrateIndivStateValues() {
        LOGGER.info("Start integrate ontology with individual stateValues...");
        final OntClass ontClass = ontModel.getOntClass(NAMESPACE + "StateValue");

        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        // TODO get something like the group of units for generic behavior
        final ArrayList<UnitType> notDalUnits = new ArrayList<>();
        //AUTHORIZATION_GROUP, DISPLAY, RFID, TELEVISION
        notDalUnits.addAll(Arrays.asList(UnitType.AGENT, UnitType.APP, UnitType.AUDIO_SINK, UnitType.AUDIO_SOURCE,
                UnitType.AUTHORIZATION_GROUP, UnitType.CONNECTION, UnitType.DEVICE, UnitType.LOCATION, UnitType.SCENE,
                UnitType.USER, UnitType.VIDEO_DEPTH_SOURCE, UnitType.VIDEO_RGB_SOURCE));

        try {
            for (final UnitConfigType.UnitConfig unitConfig : registry.getUnitConfigs()) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)
                        && !notDalUnits.contains(unitConfig.getType())) {
                    //System.out.println(unitConfig.getLabel() + "---------" + unitConfig.getType());
                    final UnitRemote unitRemote = UnitRemoteFactoryImpl.getInstance()
                            .newInitializedInstance(unitConfig);
                    unitRemote.activate();
                    unitRemote.waitForData();

                    final Object object = findStateMethod(unitRemote);

                    if (object != null) {
                        ontModel.createIndividual(NAMESPACE + object, ontClass);
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
            if (Pattern.matches("get" + "[a-zA-Z]*" + "State", aMethod.getName())) {
                try {
                    final Object getState = unitRemote.getDataClass().getMethod(aMethod.getName())
                            .invoke(unitRemote.getData());
                    //final Object getName = getState.getClass().getName();
                    //System.out.println(getName);
                    return getState.getClass().getMethod("getValue")
                            .invoke(getState);
                } catch (IllegalAccessException | InvocationTargetException | NotAvailableException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                } catch (NoSuchMethodException e) {
                    LOGGER.warn(e + " - wrong State. Insignificant.");
                }
            }
        }
        return null;
    }

    /**
     * Integration of the individual providerServices into the ontology.
     * @param unitConfig the config list of the units.
     */
    protected void integrateIndivProviderServices(final UnitConfigType.UnitConfig unitConfig) {
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
