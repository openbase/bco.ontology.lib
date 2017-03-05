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
import org.openbase.bco.ontology.lib.manager.OntologyEditCommands;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 13.02.17.
 */
public class CompareOntClasses {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompareOntClasses.class);
    private Future taskFuture;
    private boolean ontClassExist;

    public CompareOntClasses(final UnitConfig unitConfig) {

        try {
            taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {


//                try {


                    taskFuture.cancel(true); //TODO position


//                    if (!isUnitTypePresent(unitConfig)) {
//                        // ontModel doesn't contain unitType of current unitConfig
//
//                        final String unitType = OntologyEditCommands.convertWordToNounSyntax(unitConfig.getType().toString());
//
//                        if (UnitConfigProcessor.isDalUnit(unitConfig)) {
//
//                        } else {
////                            if (UnitConfigProcessor.isHostUnit())
//                        }
//
//
//                    }




//                } catch (CouldNotPerformException e) {
//
//                }
            }, 0, 1, TimeUnit.SECONDS);

        } catch (NotAvailableException e) {

        }


    }




    private boolean isUnitTypePresent(final UnitConfig unitConfig, final OntModel ontModel) {

        // get unitType in noun syntax
        final String unitName = OntologyEditCommands.convertWordToNounSyntax(unitConfig.getType().toString());
        ontClassExist = false;

        try {
            taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {

                try {
                    ontClassExist = TBoxVerificationResource.isOntClassExisting(unitName, ontModel);
                    taskFuture.cancel(true);
                } catch (CouldNotPerformException e) {
                    //retry
                    ExceptionPrinter.printHistory("Could not compare unitType with ontology. Retry in "
                            + OntConfig.SMALL_RETRY_PERIOD + " seconds.", e, LOGGER, LogLevel.WARN);
                } catch (IllegalArgumentException e) {
                    // could not compare, cause string is null. Reject comparing and upload unitType to ontology, as
                    // well ontClass is already existing in ontology
                    ontClassExist = false;
                }
            }, 0, OntConfig.SMALL_RETRY_PERIOD, TimeUnit.SECONDS);
        } catch (NotAvailableException e) {
            //TODO
        }

        return ontClassExist;
    }
}
