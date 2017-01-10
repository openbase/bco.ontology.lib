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
package org.openbase.bco.ontology.lib.testcode;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 22.12.16.
 */
public class RSBTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSBTest.class);
    private static final String SCOPE = "/test/a3";

    /**
     * Constructor for RSBTest.
     */
    public RSBTest() {
        try {
            final RSBInformer<String> synchronizedInformer = RSBFactoryImpl.getInstance()
                    .createSynchronizedInformer(SCOPE, String.class);
            synchronizedInformer.activate();

            final Future taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        synchronizedInformer.publish("blub");
                    } catch (CouldNotPerformException | InterruptedException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }
                }
            }, 0, 5, TimeUnit.SECONDS);

            final RSBListener rsbListener = RSBFactoryImpl.getInstance().createSynchronizedListener(SCOPE);
            rsbListener.activate();
            rsbListener.addHandler(event -> LOGGER.info("receive event:" + event.getData()), false);

        } catch (InterruptedException | CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }


}
