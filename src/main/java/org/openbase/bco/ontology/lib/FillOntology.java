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
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.UnitRemoteFactoryImpl;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType;
import rst.domotic.unit.UnitConfigType;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

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

    /**
     * Method fills the given ontology with information (instances).
     */
    public void fillUnitIndividuals() {
        LOGGER.info("Start filling ontology with individual units...");

        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException ex) {
            ExceptionPrinter.printHistory("Could not start App", ex, System.err);
        }

        try {
            // init: nur unitindividuen die enabled sind (disabled ignorieren)
            //TODO: reject disabled units
            //TODO: maybe a more efficient way of comparing?
            for (final UnitConfigType.UnitConfig unitConfig : registry.getUnitConfigs()) {
                final ExtendedIterator classIterator = ontModel.listClasses();
                String individualUnitName = unitConfig.getType().toString().toLowerCase();
                individualUnitName = individualUnitName.replaceAll("[^\\p{Alpha}]", "");

                while (classIterator.hasNext()) {
                    OntClass ontClass = (OntClass) classIterator.next();
                    final String className = ontClass.getLocalName().toLowerCase();

                    if (className.equals(individualUnitName)) {
                        try {
                            if (individualUnitName.equals("connection")) {
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
                            } else if (individualUnitName.equals("location")) {
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
                            ontModel.createIndividual(NAMESPACE + unitConfig.getId(), ontClass);

                        } catch (JenaException jenaException) {
                            LOGGER.error(jenaException.getMessage());
                        }
                        break;
                    }
                }

                fillProviderServiceIndividuals(unitConfig);
            }
            //TODO test, if all elements are used...
        } catch (CouldNotPerformException couldNotPerformException) {
            LOGGER.error(couldNotPerformException.getMessage());
        }
        fillStateValueIndividuals();
    }

    public void fillStateValueIndividuals() {
        LOGGER.info("Start filling ontology with individual state values...");

        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException ex) {
            ExceptionPrinter.printHistory("Could not start App", ex, System.err);
        }

        //Map<UnitTemplateType.UnitTemplate.UnitType, List<UnitRemote>> unitTypeListMap;


        ColorableLightRemote remote = new ColorableLightRemote();
        try {
            remote.initById("3249a1a5-52d1-4be1-910f-2063974b53f5");
            remote.activate();
            remote.waitForData();

            System.out.println(remote.getData().getPowerState().getValue());

        } catch (InterruptedException | CouldNotPerformException e) {
            e.printStackTrace();
        }

        try {
            for (final UnitConfigType.UnitConfig unitConfig : registry.getUnitConfigs()) {
                System.out.println(unitConfig.getType().toString().toLowerCase());
                if (unitConfig.getType().toString().toLowerCase().equals("dimmablelight")) {
                    //replaceAll("[^\\p{Alpha}]", "")
                    UnitRemote<?, UnitConfigType.UnitConfig> unitRemote =  UnitRemoteFactoryImpl.getInstance().newInitializedInstance(unitConfig);
                    System.out.println(unitRemote.getDataClass().getMethod("getPowerState").invoke(unitRemote.getData()));
                }
                //TODO


            }
        } catch (CouldNotPerformException | NoSuchMethodException | IllegalAccessException | InvocationTargetException
                | InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void fillProviderServiceIndividuals(UnitConfigType.UnitConfig unitConfig) {
            for (final ServiceConfigType.ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
                if (serviceConfig.getServiceTemplate().getPattern().toString().toLowerCase().equals("provider") ) {
                    OntClass ontClass = ontModel.getOntClass(NAMESPACE + "ProviderService");
                    ontModel.createIndividual(NAMESPACE + serviceConfig.getServiceTemplate().getType(), ontClass);
                }
        }
    }
}
