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

import org.openbase.bco.ontology.lib.commun.web.OntModelHttp;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.utility.OntModelUtility;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntRelationMappingImpl;
import org.openbase.bco.ontology.lib.utility.RdfTriple;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.tbox.OntClassMapping;
import org.openbase.bco.ontology.lib.manager.tbox.OntClassMappingImpl;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntInstanceMapping;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntInstanceMappingImpl;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntRelationMapping;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableMessageMap;
import org.openbase.jul.extension.protobuf.ProtobufListDiff;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 18.01.17.
 */
public class UnitRegistrySynchronizer {

    public static final ObservableImpl<List<UnitConfig>> newUnitConfigObservable = new ObservableImpl<>();
    //TODO second observable for updated unitConfigs?

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRegistrySynchronizer.class);
    private final ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> registryDiff;

    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableNewMessageMapUnitConfig;
    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableUpdatedMessageMapUnitConfig;
//    private IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableRemovedMessageMapUnitConfig;

    private final OntClassMapping ontClassMapping;
    private final OntInstanceMapping ontInstanceMapping;
    private final OntRelationMapping ontRelationMapping;
    private UnitRegistryRemote unitRegistryRemote;
    private final Stopwatch stopwatch;

    /**
     * Constructor for UnitRegistrySynchronizer.
     *
     * @throws InterruptedException InterruptedException
     * @throws CouldNotPerformException CouldNotPerformException
     */
    public UnitRegistrySynchronizer() throws InterruptedException, CouldNotPerformException {
        this.registryDiff = new ProtobufListDiff<>();
        this.stopwatch = new Stopwatch();
        this.ontClassMapping = new OntClassMappingImpl();
        this.ontInstanceMapping = new OntInstanceMappingImpl();
        this.ontRelationMapping = new OntRelationMappingImpl();

        initSynchConfigData();

        // start thread to synch tbox and abox changes by observer
        startUpdateObserver();
        LOGGER.info("Uploaded config data successfully.");
    }

    private void initSynchConfigData() throws InstantiationException {
        try {
            // upload ontModel
            OntModelHttp.addModelToServer(OntModelUtility.loadOntModelFromFile(null, null), OntConfig.ONTOLOGY_DATABASE_URL, 0);

            final List<UnitConfig> unitConfigs = getUnitConfigList();
            final List<RdfTriple> insertTriples = new ArrayList<>();

            // insert tbox
            insertTriples.addAll(ontClassMapping.getUnitTypeClasses());
            // insert instances
            insertTriples.addAll(ontInstanceMapping.getInsertUnitInstances(unitConfigs));
            insertTriples.addAll(ontInstanceMapping.getInsertStateAndServiceAndValueInstances());
            // insert relations
            insertTriples.addAll(ontRelationMapping.getInsertUnitRelations(unitConfigs));
            insertTriples.addAll(ontRelationMapping.getInsertStateRelations(null));

            // convert to sparql expression and upload...or save in buffer, if no server connection
            transformAndSynchronize(null, insertTriples);
        } catch (InterruptedException | NotAvailableException e) {
            throw new InstantiationException(this, e);
        }
    }

    private List<UnitConfig> getUnitConfigList() throws InterruptedException {
        while (true) {
            try {
                if (Registries.isDataAvailable()) {
                    unitRegistryRemote = Registries.getUnitRegistry();
                    unitRegistryRemote.waitForData(OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
                }
                if (unitRegistryRemote != null && unitRegistryRemote.isDataAvailable()) {
                    return unitRegistryRemote.getUnitConfigs();
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory("Could not get unitConfigs. Retry...", e, LOGGER, LogLevel.ERROR);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
    }

    private void startUpdateObserver() {
        final Observer<UnitRegistryData> unitRegistryObserver = (observable, unitRegistryData) -> GlobalCachedExecutorService.submit(() -> {

            registryDiff.diff(unitRegistryData.getUnitGroupUnitConfigList());
            identifiableNewMessageMapUnitConfig = registryDiff.getNewMessageMap();
            identifiableUpdatedMessageMapUnitConfig = registryDiff.getUpdatedMessageMap();
//            identifiableRemovedMessageMap = registryDiff.getRemovedMessageMap();

            try {
                if (!identifiableNewMessageMapUnitConfig.isEmpty()) {
                    final List<UnitConfig> unitConfigsBuf = new ArrayList<>(identifiableNewMessageMapUnitConfig.getMessages());
                    aBoxSynchNewUnits(unitConfigsBuf);
                    newUnitConfigObservable.notifyObservers(unitConfigsBuf);
                }

                if (!identifiableUpdatedMessageMapUnitConfig.isEmpty()) {
                    final List<UnitConfig> unitConfigsBuf = new ArrayList<>(identifiableUpdatedMessageMapUnitConfig.getMessages());
                    aBoxSynchUpdateUnits(unitConfigsBuf);
                }
//            if (!identifiableRemovedMessageMap.isEmpty()) {
//                final List<UnitConfig> unitConfigsBuf = new ArrayList<>(identifiableRemovedMessageMap.getMessages());
//                aBoxSynchRemoveUnits(unitConfigsBuf);
//            }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
            }
        });

        this.unitRegistryRemote.addDataObserver(unitRegistryObserver);
    }

    private void aBoxSynchUpdateUnits(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> deleteTriples = new ArrayList<>();
        final List<RdfTriple> insertTriples = new ArrayList<>();

        // delete unit and states instances
        deleteTriples.addAll(ontInstanceMapping.getDeleteUnitInstances(unitConfigs));
        // delete unit properties
        deleteTriples.addAll(ontRelationMapping.getDeleteUnitRelations(unitConfigs));

        // insert tbox changes
        insertTriples.addAll(ontClassMapping.getUnitTypeClasses(unitConfigs));
        // insert instances
        insertTriples.addAll(ontInstanceMapping.getInsertConfigInstances(unitConfigs));
        // insert properties
        insertTriples.addAll(ontRelationMapping.getInsertUnitRelations(unitConfigs));

        // convert to sparql expression and upload...or save in buffer, if no server connection
        transformAndSynchronize(deleteTriples, insertTriples);
    }

    private void aBoxSynchNewUnits(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> triples = new ArrayList<>();

        // insert tbox changes
        triples.addAll(ontClassMapping.getUnitTypeClasses(unitConfigs));
        // insert instances
        triples.addAll(ontInstanceMapping.getInsertConfigInstances(unitConfigs));
        // insert properties
        triples.addAll(ontRelationMapping.getInsertUnitRelations(unitConfigs));

        // convert to sparql expression and upload...or save in buffer, if no server connection
        transformAndSynchronize(null, triples);
    }

//    private void aBoxSynchRemoveUnits(final List<UnitConfig> unitConfigList) {
//
//        final List<RdfTriple> tripleArrayLists = new ArrayList<>();
//
//        // delete unit and states instances
//        tripleArrayLists.addAll(ontInstanceMapping.getDeleteTripleOfUnitsAndStates(unitConfigList));
//        // delete providerService instances

//        // delete unit properties
//        tripleArrayLists.addAll(ontRelationMapping.getDeleteUnitRelations(unitConfigList));
//
//        //convert to sparql expression and upload...or save, if no server connection
//        transformAndSynchronize(tripleArrayLists, null);
//    }

    private void transformAndSynchronize(final List<RdfTriple> delete, final List<RdfTriple> insert) {
        String sparql = "";

        try {
            if (delete == null) {
                // convert triples to sparql update expression (insert)
                sparql = SparqlUpdateExpression.getSparqlUpdateExpression(insert);
            } else if (insert == null) {
                // convert triples to sparql update expression (delete)
                sparql = SparqlUpdateExpression.getSparqlUpdateExpression(delete, null, null);
            } else {
                // convert triples to sparql update expression (delete and insert)
                sparql = SparqlUpdateExpression.getSparqlUpdateExpression(delete, insert, null);
            }

            // upload to ontology server
            SparqlHttp.uploadSparqlRequest(sparql, OntConfig.ONTOLOGY_DATABASE_URL);
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        } catch (IOException e) {
            TransactionBuffer.insertData(sparql);
        }
    }

}
