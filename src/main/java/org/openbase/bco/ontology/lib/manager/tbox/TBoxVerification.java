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

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.commun.web.OntModelWeb;
import org.openbase.bco.ontology.lib.system.jp.JPTBoxDatabaseUri;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author agatting on 13.02.17.
 */
public interface TBoxVerification {

    /**
     * Method delivers all subclasses of the given ontClass via recursion.
     *
     * @param ontClassSet The (empty) set to itemize the ontClasses.
     * @param ontSuperClass The superclass.
     * @param inclusiveSuperclass If {@code true} the superclass is piece of the set of results. Otherwise not.
     * @return A set with class results.
     * @throws IllegalArgumentException Exception is thrown, if the superClass has no subClasses.
     */
    static Set<OntClass> listSubclassesOfOntSuperclass(Set<OntClass> ontClassSet, final OntClass ontSuperClass, final boolean inclusiveSuperclass) {

        if (ontClassSet == null) {
            ontClassSet = new HashSet<>();
        }
        // add initial superclass
        if (inclusiveSuperclass) {
            ontClassSet.add(ontSuperClass);
        }

        // get all subclasses of current superclass
        final ExtendedIterator ontClassExIt = ontSuperClass.listSubClasses();

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
     * Method verifies, if the input className is a valid class element of the ontology. Namespace prefix is automatically added, if missing. Consider case
     * sensitive of the class name! The ontology can be set as parameter or if {@code null} the actual ontology is downloaded.
     *
     * @param className The class name, which should be verified as OntClass (existing in ontology).
     * @param ontModel The ontology, which contains all elements. If {@code null}, the actual ontology is downloaded.
     * @return {@code true} if parameter is a valid ontClass name of the ontology. Otherwise {@code false}.
     * @throws JPServiceException Exception is thrown, if the uri to the tbox server can't be taken.
     * @throws IOException Exception is thrown, if their is no connection to the server.
     * @throws IllegalArgumentException Exception is thrown, if the className is null.
     */
    static boolean isOntClassExisting(final String className, OntModel ontModel) throws JPServiceException, IOException, IllegalArgumentException {

        // add namespace to className. Throw IllegalArgumentException if parameter is null
        final String classNameWithNS = OntologyToolkit.addNamespace(className);

        if (ontModel == null) {
            ontModel = OntModelWeb.getOntologyModel(JPService.getProperty(JPTBoxDatabaseUri.class).getValue());
        }
        return ontModel.getOntClass(classNameWithNS) != null;
    }

    /**
     * Method verifies, if the parameter is a valid property element of the ontology. Namespace prefix is automatically added, if missing. Consider case
     * sensitive of the property name! The ontology can be set as parameter or if {@code null} the actual ontology is downloaded.
     *
     * @param propertyName The property name, which should be verified as OntProperty (existing in ontology).
     * @param ontModel The ontology, which contains all elements. If {@code null}, the actual ontology is downloaded.
     * @return {@code true} if parameter is a valid ontProperty name of the ontology. Otherwise {@code false}.
     * @throws JPServiceException Exception is thrown, if the uri to the tbox server can't be taken.
     * @throws IOException Exception is thrown, if their is no connection to the server.
     * @throws IllegalArgumentException Exception is thrown, if the propertyName is null.
     */
    static boolean isOntPropertyExisting(final String propertyName, OntModel ontModel) throws IllegalArgumentException, IOException, JPServiceException {

        // add namespace to propertyName. Throw IllegalArgumentException if parameter is null
        final String propertyNameWithNS = OntologyToolkit.addNamespace(propertyName);

        if (ontModel == null) {
            ontModel = OntModelWeb.getOntologyModel(JPService.getProperty(JPTBoxDatabaseUri.class).getValue());
        }
        return ontModel.getOntProperty(propertyNameWithNS) != null;
    }

}
