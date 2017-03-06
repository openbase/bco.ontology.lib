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

import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBufferImpl;
import org.openbase.bco.ontology.lib.manager.datapool.UnitRegistrySynchronizer;
import org.openbase.bco.ontology.lib.manager.datapool.UnitRemoteSynchronizer;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.schedule.Stopwatch;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author agatting on 20.10.16.
 */
public final class OntologyManagerController implements Launchable<Void>, VoidInitializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyManagerController.class);

    private static final String QUERY =
            "PREFIX NS:   <http://www.openbase.org/bco/ontology#> "
                + "ASK { "
                + "?x a NS:Device . "
                + "} ";

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
//        HeartBeatCommunication heartBeatCommunication = new HeartBeatCommunication();
//        new TBoxSynchronizer();

        Stopwatch stopwatch = new Stopwatch();

        final TransactionBuffer transactionBuffer = new TransactionBufferImpl();
        transactionBuffer.createAndStartQueue();
        new UnitRegistrySynchronizer(transactionBuffer);

        stopwatch.waitForStart(2000);
        new UnitRemoteSynchronizer(transactionBuffer);

//        stopwatch.waitForStart(10000);
//        System.out.println("Erstelle Trigger...");
//
//        OntologyChange ontologyChange = OntologyChange.newBuilder().addCategory(OntologyChange.Category.UNKNOWN).build();
//        final TriggerConfig triggerConfig = TriggerConfig.newBuilder().setLabel("trigger0").setQuery(AskQueryExample.QUERY_0)
//                .setDependingOntologyChange(ontologyChange).build();
//
//        final TriggerFactory triggerFactory = new TriggerFactory();
//        final Trigger trigger = triggerFactory.newInstance(triggerConfig);
//        trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
//            System.out.println(trigger.getTriggerConfig().getLabel() + " is " + data);
//            // do useful stuff
//        });

//        WebInterface webInterface = new WebInterface();

//        final QueryOntology queryOntology = new QueryOntology(ontology.getModel());
//        queryOntology.queryModel();

//        ontology.getModel().close();
//        if (ontology.getModel().isClosed()) {
////            LOGGER.info(APP_NAME + " finished!");
//            System.exit(0);
//        }

        try {
            if (JPService.getProperty(JPDebugMode.class).getValue()) {
                LOGGER.info("Debug Mode");
            }
        } catch (JPNotAvailableException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
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
    }
}
