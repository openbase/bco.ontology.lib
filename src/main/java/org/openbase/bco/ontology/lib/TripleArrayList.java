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

/**
 * Created by agatting on 23.12.16.
 */
public class TripleArrayList {
    private final String subject;
    private final String predicate;
    private final String object;

    /**
     * Methods creates an array of objects in form of subject, predicate, object.
     *
     * @param subject SPARQL subject.
     * @param predicate SPARQL predicate.
     * @param object SPARQL object.
     */
    public TripleArrayList(final String subject, final String predicate, final String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * Getter for SPARQL subject.
     *
     * @return SPARQL subject.
     */
    public String getSubject() {
        return subject; }

    /**
     * Getter for SPARQL predicate.
     *
     * @return SPARQL predicate.
     */
    public String getPredicate() {
        return predicate; }

    /**
     * Getter for SPARQL object.
     *
     * @return SPARQL object.
     */
    public String getObject() {
        return object; }
}
