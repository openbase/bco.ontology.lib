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

import org.openbase.bco.ontology.lib.commun.monitor.HeartBeatCommunication;
import org.openbase.bco.ontology.lib.commun.rsb.RsbCommunication;
import org.openbase.bco.ontology.lib.manager.aggregation.AggregationTest;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBufferImpl;
import org.openbase.bco.ontology.lib.manager.datapool.UnitRegistrySynchronizer;
import org.openbase.bco.ontology.lib.manager.datapool.UnitRemoteSynchronizer;
import org.openbase.bco.ontology.lib.system.config.StaticSparqlExpression;
import org.openbase.bco.ontology.lib.system.jp.JPRsbScope;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;

/**
 * @author agatting on 20.10.16.
 */
public final class OntologyManagerController implements Launchable<Void>, VoidInitializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyManagerController.class);

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {

//        AggregationTest aggregationTest = new AggregationTest();

        try {
            if (JPService.getProperty(JPDebugMode.class).getValue()) {
                LOGGER.info("Debug Mode");
            }

            final RSBInformer<OntologyChange> rsbInformer = RsbCommunication.createRsbInformer(JPService.getProperty(JPRsbScope.class).getValue());
            final TransactionBuffer transactionBuffer = new TransactionBufferImpl();
            transactionBuffer.createAndStartQueue(rsbInformer);
            new UnitRegistrySynchronizer(transactionBuffer);
            new UnitRemoteSynchronizer(transactionBuffer, rsbInformer);
            new HeartBeatCommunication();

        } catch (JPServiceException e) {
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
//        try {
//            final OntConfig ontConfig = new OntConfig();
//            ontConfig.initialTestConfig();
//        } catch (JPServiceException e) {
//            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
//        }
    }
}
