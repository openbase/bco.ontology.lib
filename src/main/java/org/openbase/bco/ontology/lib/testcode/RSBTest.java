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
import rsb.converter.NoSuchConverterException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            final List<String> list = new ArrayList<>();
            list.add("string");

            final RSBInformer<List<String>> synchronizedInformer = RSBFactoryImpl.getInstance()
                    .createSynchronizedInformer(SCOPE, (Class<List<String>>) (Class) List.class);
            synchronizedInformer.activate();

            final Future taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {

                        synchronizedInformer.publish(list);
                    } catch (CouldNotPerformException | InterruptedException e) {
                        ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                    }
                }
            }, 0, 5, TimeUnit.SECONDS);

            final RSBListener rsbListener = RSBFactoryImpl.getInstance().createSynchronizedListener(SCOPE);
            rsbListener.activate();
            rsbListener.addHandler(event -> {
                System.out.println("receive event: ");
                System.out.println(event.getData());
            }, false);

        } catch (InterruptedException | CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
    }

//        MultiException.ExceptionStack exceptionStack = null;
//        try {
//            for (final UnitConfig unitConfig : unitRegistry.getUnitConfigs()) {
//                try {
//                    throw new NotAvailableException("unit");
//                } catch (CouldNotPerformException e) {
//                    exceptionStack = MultiException.push(this, e, exceptionStack);
//                }
//            }
//            MultiException.checkAndThrow("Could not process all units!", exceptionStack);
//        } catch (CouldNotPerformException e) {
//            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
//        }
//
//
//        Future<Integer> taskFuture = GlobalCachedExecutorService.submit(() -> {
//            System.out.println("execute");
//            return 88;
//        });
//        Integer result = taskFuture.get(10, TimeUnit.MINUTES);
//
//    public void doSomeThing() throws CouldNotPerformException {
//        try {
//            LightRemote r = null;
//
//            r.getData();
//        } catch (CouldNotPerformException ex) {
//            throw new CouldNotPerformException("Could not do something.", ex);
//        }
//    }
}
