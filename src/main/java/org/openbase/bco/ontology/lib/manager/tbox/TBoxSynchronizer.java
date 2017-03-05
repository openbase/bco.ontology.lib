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
package org.openbase.bco.ontology.lib.manager.tbox;

import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.commun.web.ServerOntologyModel;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 20.02.17.
 */
public class TBoxSynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TBoxSynchronizer.class);
    private ScheduledFuture scheduledFutureTask;

    public TBoxSynchronizer() throws NotAvailableException {

        // ### Init ###
        initTBox();

    }

    private void initTBox() throws NotAvailableException {
        scheduledFutureTask = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

            try {
                if (!ServerOntologyModel.isOntModelOnServer(OntConfig.getTBoxDatabaseUri())) {
                    // server is empty - load and put ontology model (TBox) to first and second dataSets
                    final OntModel ontModel = TBoxLoader.loadOntModelFromFile(null);
                    ServerOntologyModel.addOntologyModel(ontModel, OntConfig.getTBoxDatabaseUri());
                    ServerOntologyModel.addOntologyModel(ontModel, OntConfig.getOntDatabaseUri());
                }

                if (ServerOntologyModel.isOntModelOnServer(OntConfig.getTBoxDatabaseUri())
                        && ServerOntologyModel.isOntModelOnServer(OntConfig.getOntDatabaseUri())) {
                    // tbox upload was successful
                    scheduledFutureTask.cancel(true);
                }
            } catch (CouldNotPerformException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            }
        }, 0, OntConfig.BIG_RETRY_PERIOD, TimeUnit.SECONDS);
    }

}
