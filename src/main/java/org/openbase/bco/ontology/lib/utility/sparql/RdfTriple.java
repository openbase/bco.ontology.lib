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
package org.openbase.bco.ontology.lib.utility.sparql;

/**
 * @author agatting on 23.12.16.
 */
public class RdfTriple {
    private final String subject;
    private final String predicate;
    private final String object;

    /**
     * Methods creates a triple with subject, predicate and object.
     *
     * @param subject is the SPARQL subject.
     * @param predicate is the SPARQL predicate.
     * @param object is the SPARQL object.
     */
    public RdfTriple(final String subject, final String predicate, final String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * Getter for SPARQL subject.
     *
     * @return the SPARQL subject.
     */
    public String getSubject() {
        return subject; }

    /**
     * Getter for SPARQL predicate.
     *
     * @return the SPARQL predicate.
     */
    public String getPredicate() {
        return predicate; }

    /**
     * Getter for SPARQL object.
     *
     * @return the SPARQL object.
     */
    public String getObject() {
        return object; }
}
