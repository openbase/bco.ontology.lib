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
package org.openbase.bco.ontology.lib.datapool;

import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.aboxsynchronisation.configuration.OntInstanceMapping;
import org.openbase.bco.ontology.lib.aboxsynchronisation.configuration.OntInstanceMappingImpl;
import org.openbase.bco.ontology.lib.aboxsynchronisation.configuration.OntPropertyMapping;
import org.openbase.bco.ontology.lib.aboxsynchronisation.configuration.OntPropertyMappingImpl;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.webcommunication.ServerOntologyModel;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.List;

/**
 * @author agatting on 18.01.17.
 */
public class UnitRegistryObserver implements Observer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRegistryObserver.class);

    public UnitRegistryObserver() {

        UnitRegistryRemote unitRegistryRemote;

        while (true) {
            try {
                unitRegistryRemote = Registries.getUnitRegistry();
                unitRegistryRemote.addDataObserver(this);

                break;
            } catch (NotAvailableException | InterruptedException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    ExceptionPrinter.printHistory(e1, LOGGER, LogLevel.WARN);
                }
            }
        }

        OntInstanceMapping ontInstanceMapping = new OntInstanceMappingImpl();
        OntPropertyMapping ontPropertyMapping = new OntPropertyMappingImpl();
    }

    private void initABoxSynch(final UnitRegistryRemote unitRegistryRemote, final OntInstanceMapping ontInstanceMapping
            , final OntPropertyMapping ontPropertyMapping) {

        try {
            final List<UnitConfig> unitConfigList = unitRegistryRemote.getUnitConfigs();

            // get whole ontology model for init phase (for comparing with individuals)
            final OntModel ontModel = ServerOntologyModel.getOntologyModel();

            // instances
            final List<TripleArrayList> tripleArrayListsUnits = ontInstanceMapping
                    .getMissingOntTripleOfUnitsAfterInspection(ontModel, unitConfigList);
            final List<TripleArrayList> tripleArrayListsStates = ontInstanceMapping
                    .getMissingOntTripleOfStates(ontModel, unitConfigList);
            final List<TripleArrayList> tripleArrayListsServices = ontInstanceMapping
                    .getMissingOntTripleOfProviderServices(ontModel);

            // properties
            final List<TripleArrayList> tripleArrayListsProps = ontPropertyMapping
                    .getPropertyTripleOfUnitConfigs(unitConfigList);

            //TODO update...

        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    private void updateABoxSynch(final List<UnitConfig> unitConfigList, final OntInstanceMapping ontInstanceMapping
            , final OntPropertyMapping ontPropertyMapping) {

        // get TBox ontology model for update phase (without comparing)
        final OntModel ontModel = ServerOntologyModel.getOntologyModelTBox();

        //TODO need access to detailed update data...
    }

    @Override
    public void update(Observable observable, Object registryData) throws Exception {
        GlobalCachedExecutorService.submit(() -> {

            //TODO get notify about units/states/providerServices, which are changed, instead of whole unitRegistry
            while (true) {
                try {
                    final List<UnitConfig> unitConfigList = ((UnitRegistryRemote) registryData).getUnitConfigs();

                    break;
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        ExceptionPrinter.printHistory(e1, LOGGER, LogLevel.WARN);
                    }
                }
            }

            return null;
        });
    }

}
