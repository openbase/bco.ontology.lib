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

import org.openbase.bco.ontology.lib.commun.monitor.HeartbeatPhase;
import org.openbase.bco.ontology.lib.commun.web.OntModelHttp;
import org.openbase.bco.ontology.lib.manager.datasource.UnitRegistrySynchronizer;
import org.openbase.bco.ontology.lib.manager.datasource.UnitRemoteSynchronizer;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.utility.ontology.OntModelHandler;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableMessageMap;
import org.openbase.jul.extension.protobuf.ProtobufListDiff;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 20.10.16.
 */
public final class OntologyManagerController implements Launchable<Void>, VoidInitializable {

    /**
     * Informs observer about new units.
     */
    public static final ObservableImpl<List<UnitConfig>> NEW_UNIT_CONFIG_OBSERVABLE = new ObservableImpl<>();

    /**
     * Informs observer about updated units.
     */
    public static final ObservableImpl<List<UnitConfig>> UPDATED_UNIT_CONFIG_OBSERVABLE = new ObservableImpl<>();

    /**
     * Informs observer about removed units.
     */
    public static final ObservableImpl<List<UnitConfig>> REMOVED_UNIT_CONFIG_OBSERVABLE = new ObservableImpl<>();

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(OntologyChange.newBuilder().build()));
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyManagerController.class);

    private ProtobufListDiff<String, UnitConfig, UnitConfig.Builder> registryDiff;
    private UnitRegistryRemote unitRegistryRemote;
    private Observer<UnitRegistryData> unitRegistryObserver;

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        try {
            this.registryDiff = new ProtobufListDiff<>();

            if (JPService.getProperty(JPDebugMode.class).getValue()) {
                LOGGER.info("Debug Mode");
            }

            //TEST
//            OffsetDateTime from = OffsetDateTime.parse("2017-04-21T19:00:00.000+02:00");
//            OffsetDateTime until = OffsetDateTime.parse("2017-04-21T19:00:10.000+02:00");
//            DataProviding dataProviding = new DataProviding(from, until);

            new HeartbeatPhase();
            new UnitRegistrySynchronizer();
            new UnitRemoteSynchronizer();

            final List<UnitConfig> unitConfigs = getUnitConfigs();
            NEW_UNIT_CONFIG_OBSERVABLE.notifyObservers(unitConfigs);

            this.unitRegistryObserver = (observable, unitRegistryData) -> startUpdateObserver(unitRegistryData);
            this.unitRegistryRemote.addDataObserver(unitRegistryObserver);

        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        try {
            //TEST
//            Registries.waitForData();
//            UnitRemote colorableLightRemote = Units.getUnitByScope("//bad/colorablelight/deckenlampe_2/", true);
//            new StateObservation(colorableLightRemote);
//            MultiException.checkAndThrow("Input is invalid.", Preconditions.checkNotNull(null, null, ""));

            //upload (add) ontModel
            OntModelHttp.addModelToServer(OntModelHandler.loadOntModelFromFile(null, null), OntConfig.getOntologyDbUrl(), 0);
        } catch (NotAvailableException ex) {
            throw new InitializationException("Could not upload ontology model!", ex);
        }

//        try {
//            final OntConfig ontConfig = new OntConfig();
//            ontConfig.initialTestConfig();
//        } catch (JPServiceException ex) {
//            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
//        }
    }

    private List<UnitConfig> getUnitConfigs() throws InterruptedException {

        final Stopwatch stopwatch = new Stopwatch();

        while (true) {
            try {
                if (Registries.isDataAvailable()) {
                    unitRegistryRemote = Registries.getUnitRegistry();
                    unitRegistryRemote.waitForData(OntConfig.SMALL_RETRY_PERIOD_SECONDS, TimeUnit.SECONDS);
                }

                if (unitRegistryRemote != null && unitRegistryRemote.isDataAvailable()) {
                    return unitRegistryRemote.getUnitConfigs();
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not get unitConfigs. Retry...", ex, LOGGER, LogLevel.ERROR);
                stopwatch.waitForStart(OntConfig.SMALL_RETRY_PERIOD_MILLISECONDS);
            }
        }
    }

    private void startUpdateObserver(final UnitRegistryData unitRegistryData) {

        registryDiff.diff(unitRegistryData.getUnitGroupUnitConfigList());

        IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableNewMessageMap = registryDiff.getNewMessageMap();
        IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableUpdatedMessageMap = registryDiff.getUpdatedMessageMap();
        IdentifiableMessageMap<String, UnitConfig, UnitConfig.Builder> identifiableRemovedMessageMap = registryDiff.getRemovedMessageMap();

        try {
            if (!identifiableNewMessageMap.isEmpty()) {
                final List<UnitConfig> unitConfigs = new ArrayList<>(identifiableNewMessageMap.getMessages());
                NEW_UNIT_CONFIG_OBSERVABLE.notifyObservers(unitConfigs);
            }

            if (!identifiableUpdatedMessageMap.isEmpty()) {
                final List<UnitConfig> unitConfigs = new ArrayList<>(identifiableUpdatedMessageMap.getMessages());
                UPDATED_UNIT_CONFIG_OBSERVABLE.notifyObservers(unitConfigs);
            }
            if (!identifiableRemovedMessageMap.isEmpty()) {
                final List<UnitConfig> unitConfigs = new ArrayList<>(identifiableRemovedMessageMap.getMessages());
                REMOVED_UNIT_CONFIG_OBSERVABLE.notifyObservers(unitConfigs);
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }
}
