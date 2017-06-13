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
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.jul.exception.NotAvailableException;

import java.io.IOException;

/**
 * @author agatting on 13.02.17.
 */
public interface TBoxVerification {

    /**
     * Method verifies, if the input className is a valid class element of the ontology. Namespace prefix is automatically added, if missing. Consider case
     * sensitive of the class name! The ontology can be set as parameter or if {@code null} the actual ontology is downloaded.
     *
     * @param className The class name, which should be verified as OntClass (existing in ontology).
     * @param ontModel The ontology, which contains all elements. If {@code null}, the actual ontology is downloaded.
     * @return {@code true} if parameter is a valid ontClass name of the ontology. Otherwise {@code false}.
     * @throws IOException Exception is thrown, if their is no connection to the server.
     * @throws NotAvailableException Exception is thrown, if the className is null.
     */
    static boolean isOntClassExisting(final String className, OntModel ontModel) throws IOException, NotAvailableException {

        // add namespace to className. Throw IllegalArgumentException if parameter is null
        String classNameWithNS = StringModifier.addBcoNamespace(className, true);

//        if (ontModel == null) {
//            ontModel = OntModelHttp.downloadModelFromServer(JPService.getProperty(JPTBoxDatabaseURL.class).getValue());
//        }
        return ontModel.getOntClass(classNameWithNS) != null;
    }

    /**
     * Method verifies, if the parameter is a valid property element of the ontology. Namespace prefix is automatically added, if missing. Consider case
     * sensitive of the property name! The ontology can be set as parameter or if {@code null} the actual ontology is downloaded.
     *
     * @param propertyName The property name, which should be verified as OntProperty (existing in ontology).
     * @param ontModel The ontology, which contains all elements. If {@code null}, the actual ontology is downloaded.
     * @return {@code true} if parameter is a valid ontProperty name of the ontology. Otherwise {@code false}.
     * @throws IOException Exception is thrown, if their is no connection to the server.
     * @throws NotAvailableException Exception is thrown, if the propertyName is null.
     */
    static boolean isOntPropertyExisting(final String propertyName, OntModel ontModel) throws NotAvailableException, IOException {

        // add namespace to propertyName. Throw IllegalArgumentException if parameter is null
        String propertyNameWithNS = StringModifier.addBcoNamespace(propertyName, true);

//        if (ontModel == null) {
//            ontModel = OntModelHttp.downloadModelFromServer(JPService.getProperty(JPOntologyTBoxDatabaseURL.class).getValue());
//        }
        return ontModel.getOntProperty(propertyNameWithNS) != null;
    }

}
