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
package org.openbase.bco.ontology.lib.manager.datapool;

import javafx.util.Pair;
import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.commun.web.OntModelWeb;
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.manager.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.tbox.TBoxSynchronizer;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntInstanceMapping;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntInstanceMappingImpl;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntPropertyMapping;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntPropertyMappingImpl;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableMessageMap;
import org.openbase.jul.extension.protobuf.ProtobufListDiff;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 18.01.17.
 */
public class UnitRegistrySynchronizer {

    //TODO if new unit type available: wait for tbox synch and confirmation to state observation
    //TODO set and list...standardize!

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRegistrySynchronizer.class);
    private ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> registryDiff;

    private Observer<UnitRegistryData> unitRegistryObserver;
    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableNewMessageMap;
    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableUpdatedMessageMap;
//    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableRemovedMessageMap;

    private final OntInstanceMapping ontInstanceMapping = new OntInstanceMappingImpl();
    private final OntPropertyMapping ontPropertyMapping = new OntPropertyMappingImpl();
    private UnitRegistryRemote unitRegistryRemote;
    private final TransactionBuffer transactionBufferImpl;
    private final Stopwatch stopwatch;
    private final TBoxSynchronizer tBoxSynchronizer;

    /**
     * Constructor for UnitRegistrySynchronizer.
     */
    public UnitRegistrySynchronizer(final TransactionBuffer transactionBuffer) throws InterruptedException, CouldNotPerformException, JPServiceException {

        this.transactionBufferImpl = transactionBuffer;
        this.registryDiff = new ProtobufListDiff<>();
        this.stopwatch = new Stopwatch();
        this.tBoxSynchronizer = new TBoxSynchronizer();

        // for init get the whole unitConfigList
        final List<UnitConfig> unitConfigList = getUnitConfigList();
        // start thread to synch tbox and abox initial
        startInitialization(unitConfigList);
        // start thread to synch tbox and abox changes by observer
        startUpdateObserver();
    }

    private void startInitialization(final List<UnitConfig> unitConfigList) throws InterruptedException, CouldNotPerformException, JPServiceException {

        // init: synchronize tbox based on server ontModel
        final OntModel ontModel = tBoxSynchronizer.extendTBoxViaServerModel(unitConfigList);
        // upload ontModel
        OntModelWeb.addOntModelViaRetry(ontModel);

        // fill abox initial with whole registry unitConfigs
        aBoxSynchInitUnits(unitConfigList, ontModel);
    }

    private List<UnitConfig> getUnitConfigList() throws NotAvailableException, InterruptedException {
        List<UnitConfig> unitConfigList = null;

        while (unitConfigList == null) {
            try {
                unitRegistryRemote = Registries.getUnitRegistry();
                unitRegistryRemote.waitForData(2, TimeUnit.SECONDS);

                unitConfigList = unitRegistryRemote.getUnitConfigs();
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory("Could not get UnitConfigs. Retry...", e, LOGGER, LogLevel.ERROR);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
        return unitConfigList;
    }

    private void startUpdateObserver() {
        this.unitRegistryObserver = (observable, unitRegistryData) -> GlobalCachedExecutorService.submit(() -> {

            final List<UnitConfig> unitConfigList = unitRegistryData.getUnitGroupUnitConfigList();
            final List<UnitConfig> unitConfigListBuf = new ArrayList<>();

            registryDiff.diff(unitConfigList);

            identifiableNewMessageMap = registryDiff.getNewMessageMap();
            identifiableUpdatedMessageMap = registryDiff.getUpdatedMessageMap();
//            identifiableRemovedMessageMap = registryDiff.getRemovedMessageMap();

            try {
                if (!identifiableNewMessageMap.isEmpty()) {
                    unitConfigListBuf.addAll(identifiableNewMessageMap.getMessages());
                    aBoxSynchNewUnits(unitConfigListBuf);
                    unitConfigListBuf.clear();
                    identifiableNewMessageMap.clear();
                }

                if (!identifiableUpdatedMessageMap.isEmpty()) {
                    unitConfigListBuf.addAll(identifiableUpdatedMessageMap.getMessages());
                    aBoxSynchUpdateUnits(unitConfigListBuf);
                    unitConfigListBuf.clear();
                    identifiableUpdatedMessageMap.clear();
                }
            } catch (InterruptedException | JPServiceException e) {
                //TODO
            }
//            if (!identifiableRemovedMessageMap.isEmpty()) {
//                unitConfigListBuf.addAll(identifiableRemovedMessageMap.getMessages());
//                aBoxSynchRemoveUnits(unitConfigListBuf);
//                unitConfigListBuf.clear();
//                identifiableRemovedMessageMap.clear();
//            }
        });

        this.unitRegistryRemote.addDataObserver(unitRegistryObserver);
    }

    private void aBoxSynchInitUnits(final List<UnitConfig> unitConfigList, final OntModel ontModel) throws JPServiceException {

        final List<TripleArrayList> insertTripleArrayLists = new ArrayList<>();

        // insert instances
        insertTripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfUnitsAfterInspection(ontModel, unitConfigList));
        insertTripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfStates(unitConfigList));
        insertTripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfProviderServices(ontModel));
        // insert properties
        insertTripleArrayLists.addAll(ontPropertyMapping.getPropertyTripleOfUnitConfigs(unitConfigList));

        // convert to sparql expression and upload...or save, if no server connection
        convertToSparqlExprAndUpload(null, insertTripleArrayLists);
    }

    private void aBoxSynchUpdateUnits(final List<UnitConfig> unitConfigList) throws InterruptedException, JPServiceException {

        final List<TripleArrayList> deleteTriples = new ArrayList<>();
        final List<TripleArrayList> insertTriples = new ArrayList<>();
        // get tbox of ontology (inspection doesn't necessary)
        final List<TripleArrayList> tboxTriples = tBoxSynchronizer.extendTBoxViaTriples(unitConfigList);

        // delete unit and states instances
        deleteTriples.addAll(ontInstanceMapping.getDeleteTripleOfUnitsAndStates(unitConfigList));
        // delete providerService instances
        //TODO delete providerService (?)
        // delete unit properties
        deleteTriples.addAll(ontPropertyMapping.getPropertyDeleteTripleOfUnitConfigs(unitConfigList));

        // insert tbox changes
        insertTriples.addAll(tboxTriples);
        // insert instances
        insertTriples.addAll(ontInstanceMapping.getMissingUnitTriples(unitConfigList));
        insertTriples.addAll(ontInstanceMapping.getMissingOntTripleOfStates(unitConfigList));
        insertTriples.addAll(ontInstanceMapping.getMissingServiceTriples(unitConfigList));
        // insert properties
        insertTriples.addAll(ontPropertyMapping.getPropertyTripleOfUnitConfigs(unitConfigList));

        // convert to sparql expression and upload...or save, if no server connection
        convertToSparqlExprAndUpload(deleteTriples, insertTriples);
    }

    private void aBoxSynchNewUnits(final List<UnitConfig> unitConfigList) throws InterruptedException, JPServiceException {

        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();
        // get tbox of ontology (inspection doesn't necessary)
        final List<TripleArrayList> tboxTriples = tBoxSynchronizer.extendTBoxViaTriples(unitConfigList);

        // insert tbox changes
        tripleArrayLists.addAll(tboxTriples);
        // insert instances
        tripleArrayLists.addAll(ontInstanceMapping.getMissingUnitTriples(unitConfigList));
        tripleArrayLists.addAll(ontInstanceMapping.getMissingOntTripleOfStates(unitConfigList));
        tripleArrayLists.addAll(ontInstanceMapping.getMissingServiceTriples(unitConfigList));
        // insert properties
        tripleArrayLists.addAll(ontPropertyMapping.getPropertyTripleOfUnitConfigs(unitConfigList));

        // convert to sparql expression and upload...or save, if no server connection
        convertToSparqlExprAndUpload(null, tripleArrayLists);
    }

//    private void aBoxSynchRemoveUnits(final List<UnitConfig> unitConfigList) {
//
//        final List<TripleArrayList> tripleArrayLists = new ArrayList<>();
//
//        // delete unit and states instances
//        tripleArrayLists.addAll(ontInstanceMapping.getDeleteTripleOfUnitsAndStates(unitConfigList));
//        // delete providerService instances
//        //TODO delete providerService (?)
//        // delete unit properties
//        tripleArrayLists.addAll(ontPropertyMapping.getPropertyDeleteTripleOfUnitConfigs(unitConfigList));
//
//        //convert to sparql expression and upload...or save, if no server connection
//        convertToSparqlExprAndUpload(tripleArrayLists, null);
//    }

    private void convertToSparqlExprAndUpload(final List<TripleArrayList> deleteTriple, final List<TripleArrayList> insertTriple) throws JPServiceException {
        String multiExprUpdate = "";

        try {
            if (deleteTriple == null) {
                // convert triples to single sparql update expression (insert)
                multiExprUpdate = SparqlUpdateExpression.getSparqlUpdateInsertBundleExpr(insertTriple);
            } else if (insertTriple == null) {
                // convert triples to single sparql update expression (delete)
                multiExprUpdate = SparqlUpdateExpression.getSparqlUpdateDeleteAndInsertBundleExpr(deleteTriple, null, null);
            } else {
                // convert triples to single sparql update expression (delete and insert)
                multiExprUpdate = SparqlUpdateExpression.getSparqlUpdateDeleteAndInsertBundleExpr(deleteTriple, insertTriple, null);
            }

            // upload to ontology server
            final boolean isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToAllDataBases(multiExprUpdate, OntConfig.ServerServiceForm.UPDATE);

            if (!isHttpSuccess) {
                transactionBufferImpl.insertData(new Pair<>(multiExprUpdate, true));
            } else {
                //TODO rsb notification
            }
        } catch (CouldNotPerformException e) {
            transactionBufferImpl.insertData(new Pair<>(multiExprUpdate, true));
        } catch (IllegalAccessException e) {
            ExceptionPrinter.printHistory("Defect sparql update expression! Dropped.", e, LOGGER, LogLevel.ERROR);
        }
    }

}
