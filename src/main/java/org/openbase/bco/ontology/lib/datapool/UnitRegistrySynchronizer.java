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
import org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.TransactionBuffer;
import org.openbase.bco.ontology.lib.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.webcommunication.ServerOntologyModel;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableMessageMap;
import org.openbase.jul.extension.protobuf.ProtobufListDiff;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 18.01.17.
 */
public class UnitRegistrySynchronizer extends SparqlUpdateExpression {

    //TODO handle situation: ontology server contains an old ontology with abox (comparison...(in the offline time
    // of the ontology, details of containing unitConfigs could be changed...)
    //TODO handle situation: no ontModel available
    //TODO if new unit type available: wait for tbox synch and confirmation
    //TODO during init phase: if no upload connection -> update observer (registry) wait and store updates
    //TODO approach: upload rdf file, which contains updates

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRegistrySynchronizer.class);
    private ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> registryDiff;

    private Observer<UnitRegistryData> unitRegistryObserver;
    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableNewMessageMap;
    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableUpdatedMessageMap;
    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableRemovedMessageMap;

    private final OntInstanceMapping ontInstanceMapping = new OntInstanceMappingImpl();
    private final OntPropertyMapping ontPropertyMapping = new OntPropertyMappingImpl();
    private UnitRegistryRemote unitRegistryRemote;
    private TransactionBuffer transactionBufferImpl;
    private Future taskFuture;

    /**
     * Constructor for UnitRegistrySynchronizer.
     */
    public UnitRegistrySynchronizer(final TransactionBuffer transactionBuffer) {

        this.transactionBufferImpl = transactionBuffer;

        // ### INIT ###
        try {
            taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    unitRegistryRemote = Registries.getUnitRegistry();
                    Registries.getUnitRegistry().waitForData();
                    List<UnitConfig> unitConfigListInit = unitRegistryRemote.getUnitConfigs();

                    // fill ontology initial with whole registry unitConfigs
                    aBoxSynchInitUnits(unitConfigListInit);


                    // ### UPDATE ###
                    registryDiff = new ProtobufListDiff<>();

                    unitRegistryObserver = (observable, unitRegistryData) -> GlobalCachedExecutorService.submit(() -> {

                        final List<UnitConfig> unitConfigList = unitRegistryData.getUnitGroupUnitConfigList();
                        List<UnitConfig> unitConfigListBuf = new ArrayList<>();

                        registryDiff.diff(unitConfigList);

                        identifiableNewMessageMap = registryDiff.getNewMessageMap();
                        identifiableUpdatedMessageMap = registryDiff.getUpdatedMessageMap();
                        identifiableRemovedMessageMap = registryDiff.getRemovedMessageMap();

                        if (!identifiableNewMessageMap.isEmpty()) {
                            unitConfigListBuf.addAll(identifiableNewMessageMap.getMessages());
                            aBoxSynchNewUnits(unitConfigListBuf);
                            identifiableNewMessageMap.clear();
                        }

                        if (!identifiableUpdatedMessageMap.isEmpty()) {
                            unitConfigListBuf.addAll(identifiableUpdatedMessageMap.getMessages());
                            aBoxSynchUpdateUnits(unitConfigListBuf);
                            identifiableUpdatedMessageMap.clear();
                        }

                        if (!identifiableRemovedMessageMap.isEmpty()) {
                            unitConfigListBuf.addAll(identifiableRemovedMessageMap.getMessages());
                            aBoxSynchRemoveUnits(unitConfigListBuf);
                            identifiableRemovedMessageMap.clear();
                        }

                        unitConfigListBuf.clear();
                    });

                    unitRegistryRemote.addDataObserver(unitRegistryObserver);


                    taskFuture.cancel(true);
                } catch (InterruptedException | CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
                    // retry via scheduled thread
                }
            }, 0, 1, TimeUnit.SECONDS);
        } catch (NotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            //TODO
        }

        //TODO start update after thread is canceled...

    }

    private void convertToSparqlExprAndUpload(final List<TripleArrayList> deleteTripleArrayLists
            , final List<TripleArrayList> insertTripleArrayLists) {

        String multiExprUpdate;

        if (deleteTripleArrayLists == null) {
            // convert triples to single sparql update expression (insert)
            multiExprUpdate = getSparqlBundleUpdateInsertEx(insertTripleArrayLists);
        } else if (insertTripleArrayLists == null) {
            // convert triples to single sparql update expression (delete)
            multiExprUpdate = getSparqlBundleUpdateDeleteAndInsertEx(deleteTripleArrayLists, null, null);
        } else {
            // convert triples to single sparql update expression (delete and insert)
            multiExprUpdate =
                    getSparqlBundleUpdateDeleteAndInsertEx(deleteTripleArrayLists, insertTripleArrayLists, null);
        }

        try {
            // upload to ontology server
            final int httpResponseCode = sparqlUpdate(multiExprUpdate);
            // check response code
            final boolean httpSuccess = httpRequestSuccess(httpResponseCode);

            if (!httpSuccess) {
                transactionBufferImpl.insertData(multiExprUpdate);
            } else {

            }
        } catch (IOException e) {
            transactionBufferImpl.insertData(multiExprUpdate);
        }
    }

    private void aBoxSynchInitUnits(List<UnitConfig> unitConfigList) {

        //TODO insert only data, which isn't in ontology yet

        try {
            // get whole ontology model for init phase (for comparing with individuals)
            final OntModel ontModel = ServerOntologyModel.getOntologyModel();
            final List<TripleArrayList> insertTripleArrayLists = new ArrayList<>();

            // insert instances
            insertTripleArrayLists.addAll(ontInstanceMapping
                    .getMissingOntTripleOfUnitsAfterInspection(ontModel, unitConfigList));
            insertTripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfStates(ontModel, unitConfigList));
            insertTripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfProviderServices(ontModel));
            // insert properties
            insertTripleArrayLists.addAll(ontPropertyMapping.getPropertyTripleOfUnitConfigs(unitConfigList));

            convertToSparqlExprAndUpload(null, insertTripleArrayLists);

            //TODO result
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    private void aBoxSynchUpdateUnits(final List<UnitConfig> unitConfigList) {

        try {
            // get tbox of ontology (inspection doesn't necessary)
            final OntModel ontModel = ServerOntologyModel.getOntologyModelTBox();
            final List<TripleArrayList> deleteTripleArrayLists = new ArrayList<>();
            final List<TripleArrayList> insertTripleArrayLists = new ArrayList<>();

            // delete unit and states instances
            deleteTripleArrayLists.addAll(ontInstanceMapping.getDeleteTripleOfUnitsAndStates(unitConfigList));
            // delete providerService instances
            //TODO delete providerService (?)
            // delete unit properties
            deleteTripleArrayLists.addAll(ontPropertyMapping.getPropertyDeleteTripleOfUnitConfigs(unitConfigList));

            // insert instances
            insertTripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfUnits(ontModel, unitConfigList));
            insertTripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfStates(ontModel, unitConfigList));
            insertTripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfProviderServices(ontModel));
            // insert properties
            insertTripleArrayLists.addAll(ontPropertyMapping.getPropertyTripleOfUnitConfigs(unitConfigList));

            convertToSparqlExprAndUpload(deleteTripleArrayLists, insertTripleArrayLists);

            //TODO result
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    private void aBoxSynchNewUnits(final List<UnitConfig> unitConfigList) {

        try {
            // get tbox of ontology (inspection doesn't necessary)
            final OntModel ontModel = ServerOntologyModel.getOntologyModelTBox();

            final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

            // insert instances
            tripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfUnits(ontModel, unitConfigList));
            tripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfStates(ontModel, unitConfigList));
            tripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfProviderServices(ontModel));
            // insert properties
            tripleArrayLists.addAll(ontPropertyMapping.getPropertyTripleOfUnitConfigs(unitConfigList));

            convertToSparqlExprAndUpload(null, tripleArrayLists);

            //TODO result
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

    private void aBoxSynchRemoveUnits(final List<UnitConfig> unitConfigList) {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        // delete unit and states instances
        tripleArrayLists.addAll(ontInstanceMapping.getDeleteTripleOfUnitsAndStates(unitConfigList));
        // delete providerService instances
        //TODO delete providerService (?)
        // delete unit properties
        tripleArrayLists.addAll(ontPropertyMapping.getPropertyDeleteTripleOfUnitConfigs(unitConfigList));

        convertToSparqlExprAndUpload(tripleArrayLists, null);

        //TODO result
    }

}
