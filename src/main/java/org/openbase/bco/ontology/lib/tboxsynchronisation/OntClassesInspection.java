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
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Set;

/**
 * @author agatting on 13.02.17.
 */
public interface OntClassesInspection {

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

}
