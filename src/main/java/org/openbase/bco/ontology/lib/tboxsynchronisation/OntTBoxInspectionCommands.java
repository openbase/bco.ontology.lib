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
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.OntologyEditCommands;
import org.openbase.bco.ontology.lib.webcommunication.ServerOntologyModel;
import org.openbase.jul.exception.CouldNotPerformException;

import java.util.Set;

/**
 * @author agatting on 13.02.17.
 */
public interface OntTBoxInspectionCommands {

    /**
     * Method delivers all subclasses of the given superclass via recursion.
     *
     * @param ontClassSet The (empty) set to itemize the ontClasses.
     * @param ontSuperClass The superclass.
     * @param inclusiveSuperclass Result list keeps superclass or not.
     *
     * @return The list with ontClasses.
     */
    static Set<OntClass> listSubclassesOfOntSuperclass(final Set<OntClass> ontClassSet, final OntClass
            ontSuperClass, final boolean inclusiveSuperclass) {

        // add initial superclass
        if (inclusiveSuperclass) {
            ontClassSet.add(ontSuperClass);
        }

        // get all subclasses of current superclass
        final ExtendedIterator ontClassExIt;
        ontClassExIt = ontSuperClass.listSubClasses();

        // add subclass(es) and if subclass has subclass(es) goto next layer via recursion
        while (ontClassExIt.hasNext()) {
            final OntClass ontClass = (OntClass) ontClassExIt.next();
            ontClassSet.add(ontClass);

            if (ontSuperClass.hasSubClass()) {
                listSubclassesOfOntSuperclass(ontClassSet, ontClass, false);
            }
        }
        return ontClassSet;
    }

    /**
     * Method verifies, if the parameter is a valid class element of the ontology. Namespace prefix is automatically
     * added, if missing. Consider upper and lower case of the class name! The ontology can be set as parameter or if
     * {@code null} the actual ontology is downloaded.
     *
     * @param className The class name, which should be verified (existing in ontology).
     * @param ontModel The ontology, which contains all elements. If {@code null}, the actual ontology is downloaded.
     * @return {@code true} if parameter is a valid class name of the ontology. Otherwise {@code false}.
     * @throws IllegalArgumentException IllegalArgumentException.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static boolean isOntClassExisting(final String className, OntModel ontModel) throws IllegalArgumentException
            , CouldNotPerformException {

        // add namespace to className. Throw IllegalArgumentException if parameter is null
        final String classNameWithNS = OntologyEditCommands.addNamespaceToOntElement(className);

//        try {
            if (ontModel == null) {
                ontModel = ServerOntologyModel.getOntologyModelFromServer(ConfigureSystem.getTBoxURIData());
            }
            return ontModel.getOntClass(classNameWithNS) != null;

//        } catch (CouldNotPerformException e) {
//            throw new CouldNotPerformException("Could not get ontology tbox model from server!", e);
//        }
    }

    /**
     * Method verifies, if the parameter is a valid property element of the ontology. Namespace prefix is automatically
     * added, if missing. Consider upper and lower case of the property name! The ontology can be set as parameter or if
     * {@code null} the actual ontology is downloaded.
     *
     * @param propertyName The property name, which should be verified (existing in ontology).
     * @param ontModel The ontology, which contains all elements. If {@code null}, the actual ontology is downloaded.
     * @return {@code true} if parameter is a valid property name of the ontology. Otherwise {@code false}.
     * @throws IllegalArgumentException IllegalArgumentException.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static boolean isOntPropertyExisting(final String propertyName, OntModel ontModel) throws IllegalArgumentException
            , CouldNotPerformException {

        // add namespace to propertyName. Throw IllegalArgumentException if parameter is null
        final String propertyNameWithNS = OntologyEditCommands.addNamespaceToOntElement(propertyName);

//        try {
            if (ontModel == null) {
                ontModel = ServerOntologyModel.getOntologyModelFromServer(ConfigureSystem.getTBoxURIData());
            }
            return ontModel.getOntProperty(propertyNameWithNS) != null;

//        } catch (CouldNotPerformException e) {
//            throw new CouldNotPerformException("Could not get ontology tbox model from server!", e);
//        }
    }

}
