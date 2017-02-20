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
package org.openbase.bco.ontology.lib.tboxsynchronisation;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.OntologyEditCommands;
import org.openbase.bco.ontology.lib.webcommunication.ServerOntologyModel;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author agatting on 13.02.17.
 */
public class CompareOntClasses {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompareOntClasses.class);
    private Future taskFuture;
    private OntModel ontModel;

    public CompareOntClasses(final UnitConfig unitConfig) {

        try {
            taskFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(() -> {


                try {
                    ontModel = ServerOntologyModel.getOntologyModelTBox();

                    if (ontModel.isEmpty()) {
                        // server is empty - load and put ontology model (TBox)
                        ontModel = OntologyPreparation.loadOntModelFromFile(null);

                        ServerOntologyModel.putOntologyModel(ontModel);
                    }

                    taskFuture.cancel(true); //TODO position


                    if (!isUnitTypePresent(unitConfig)) {
                        // ontModel doesn't contain unitType of current unitConfig

                        final String unitType = OntologyEditCommands.convertWordToNounSyntax(unitConfig.getType().toString());

                        if (UnitConfigProcessor.isDalUnit(unitConfig)) {

                        } else {
//                            if (UnitConfigProcessor.isHostUnit())
                        }


                    }




                } catch (CouldNotPerformException e) {

                }
            }, 0, 1, TimeUnit.SECONDS);

        } catch (NotAvailableException e) {

        }


    }




    private boolean isUnitTypePresent(final UnitConfig unitConfig) {
        boolean unitTypePresent = false;

//        final OntModel ontModel = ServerOntologyModel.getOntologyModelTBox();
        // the ontSuperClass of the ontology to get all unit (sub)classes
        final OntClass ontClassUnit = ontModel
                .getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.UNIT.getName());

        Set<OntClass> ontClasses = new HashSet<>();
        ontClasses = OntTBoxInspectionCommands.listSubclassesOfOntSuperclass(ontClasses, ontClassUnit, true);

        String unitType = unitConfig.getType().toString().toLowerCase();
        unitType = unitType.replaceAll(ConfigureSystem.OntExpr.REMOVE.getName(), "");

        for (final OntClass ontClass : ontClasses) {
            final String ontClassName = ontClass.getLocalName().toLowerCase();

            if (unitType.equals(ontClassName)) {
                unitTypePresent = true;
                break;
            }
        }

        return unitTypePresent;
    }
}
