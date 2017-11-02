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
package org.openbase.bco.ontology.lib.manager.datasource;

import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.ontology.lib.OntologyManagerController;
import org.openbase.bco.ontology.lib.manager.abox.observation.StateObservation;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author agatting on 07.02.17.
 */
public class UnitRemoteSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRemoteSynchronizer.class);

    private static final ObservableImpl<UnitConfig> UNIT_REMOTE_OBSERVABLE = new ObservableImpl<>();
    private final List<UnitRemote> loadedUnitRemotes;

    private int successfullyRemotesNum = 0;
    private int failedRemotesNum = 0;
    private int potentialRemotesNum = 0;

    /**
     * Constructor initiates the observers, which are used to create for each unit an observation of their state data.
     */
    public UnitRemoteSynchronizer() {
        this.loadedUnitRemotes = new ArrayList<>();

        final Observer<List<UnitConfig>> newUnitConfigObserver = (source, unitConfigs) -> loadUnitRemotes(unitConfigs);
        final Observer<List<UnitConfig>> removedUnitConfigObserver = (source, unitConfigs) -> removeUnitRemotes(unitConfigs);
        final Observer<UnitConfig> unitRemoteObserver = (source, unitConfig) -> setStateObservation(unitConfig);

        OntologyManagerController.NEW_UNIT_CONFIG_OBSERVABLE.addObserver(newUnitConfigObserver);
        OntologyManagerController.REMOVED_UNIT_CONFIG_OBSERVABLE.addObserver(removedUnitConfigObserver);
        UnitRemoteSynchronizer.UNIT_REMOTE_OBSERVABLE.addObserver(unitRemoteObserver);
    }

    private void loadUnitRemotes(final List<UnitConfig> unitConfigs) throws InterruptedException, CouldNotPerformException {
        this.potentialRemotesNum = 0;
        this.successfullyRemotesNum = 0;
        this.failedRemotesNum = 0;

        final List<UnitConfig> unitConfigsBuf = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigs) {
            if (unitConfig.getType() != UnitType.DEVICE && unitConfig.getEnablingState().getValue() == State.ENABLED) {
                unitConfigsBuf.add(unitConfig);
                potentialRemotesNum++;
            }
        }

        LOGGER.info("Try to set state observation(s) of " + potentialRemotesNum + " potential unit remote(s).");

        for (final UnitConfig unitConfig : unitConfigsBuf) {
            UNIT_REMOTE_OBSERVABLE.notifyObservers(unitConfig);
        }
    }

    private synchronized void addLoadedUnitRemote(final UnitRemote unitRemote) {
        loadedUnitRemotes.add(unitRemote);
    }

    private synchronized List<UnitRemote> getLoadedUnitRemotes() {
        return loadedUnitRemotes;
    }
    private synchronized void incrementFailedRemotesNum() {
        failedRemotesNum++;
    }

    private synchronized int getFailedRemotesNum() {
        return failedRemotesNum;
    }

    private synchronized void checkDone() {
        if (getSuccessfullyRemotesNum() + getFailedRemotesNum() == potentialRemotesNum) {
            LOGGER.info("UnitRemoteSynchronizer is finished with current unitConfigs! There are " + getFailedRemotesNum() + " failed unitRemotes.");
        }
    }

    private synchronized int getSuccessfullyRemotesNum() {
        return successfullyRemotesNum;
    }

    private synchronized int incrementSuccessfullyRemotesNum(final String unitLabel) {
        successfullyRemotesNum++;
        LOGGER.info(unitLabel + " is loaded. Number " + successfullyRemotesNum + " of " + potentialRemotesNum + " state observations successfully started.");
        return successfullyRemotesNum;
    }

    private void setStateObservation(final UnitConfig unitConfig) throws InterruptedException {
        try {
            final UnitRemote unitRemote = Units.getFutureUnit(unitConfig, true).get();

            addLoadedUnitRemote(unitRemote);
            identifyUnitRemote(unitRemote);
            incrementSuccessfullyRemotesNum(unitRemote.getLabel());
        } catch (ExecutionException | NotAvailableException | InstantiationException ex) {
            incrementFailedRemotesNum();
            LOGGER.warn("Could not get unitRemote of " + unitConfig.getLabel() + ". Dropped.");
        }
        checkDone();
    }

    private void removeUnitRemotes(final List<UnitConfig> unitConfigs) throws NotAvailableException {
        for (final UnitConfig unitConfig : unitConfigs) {
            for (final UnitRemote unitRemote : getLoadedUnitRemotes()) {
                if (unitRemote.getId().equals(unitConfig.getId())) {
                    unitRemote.shutdown();
                }
            }
        }
    }

    private void identifyUnitRemote(final UnitRemote unitRemote) throws InstantiationException, NotAvailableException {

        final UnitType unitType = unitRemote.getUnitType();

        //TODO currently problematic unitTypes...fix in future
        switch (unitType) {
            case AUDIO_SINK:
//                new StateObservation(unitRemote);
                return;
            case AUDIO_SOURCE:
//                new StateObservation(unitRemote);
                return;
            case CONNECTION:
//                new StateObservation(unitRemote);
                return;
            case DEVICE:
//                new StateObservation(unitRemote);
                return;
            case LOCATION:
//                new StateObservation(unitRemote);
                return;
            default:
                new StateObservation(unitRemote);
        }
    }
}
