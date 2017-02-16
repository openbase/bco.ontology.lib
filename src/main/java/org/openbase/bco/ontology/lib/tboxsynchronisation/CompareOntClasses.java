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
import org.openbase.bco.ontology.lib.webcommunication.ServerOntologyModel;
import org.openbase.jul.exception.CouldNotPerformException;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * @author agatting on 13.02.17.
 */
public class CompareOntClasses {

    public CompareOntClasses() {
        try {
            isOntologyServerEmpty();
        } catch (CouldNotPerformException e) {
            e.printStackTrace();
        }
    }

    public void blub(final UnitConfig unitConfig) {

//        try {
//            if (isUnitTypePresent(unitConfig)) {
//
//            }
//        } catch (CouldNotPerformException e) {
//
//        }


    }

    private boolean isOntologyServerEmpty() throws CouldNotPerformException {

        final OntModel ontModel = ServerOntologyModel.getOntologyModelTBox();

        System.out.println(ontModel.isEmpty()); //true wenn leer

        return false;
    }

    private boolean isUnitTypePresent(final UnitConfig unitConfig) throws CouldNotPerformException {
        boolean unitTypePresent = false;

        final OntModel ontModel = ServerOntologyModel.getOntologyModelTBox();
        // the ontSuperClass of the ontology to get all unit (sub)classes
        final OntClass ontClassUnit = ontModel
                .getOntClass(ConfigureSystem.NS + ConfigureSystem.OntClass.UNIT.getName());

        Set<OntClass> ontClasses = new HashSet<>();
        ontClasses = OntClassesInspection.listSubclassesOfOntSuperclass(ontClasses, ontClassUnit, true);

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
