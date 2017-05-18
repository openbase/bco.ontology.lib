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

import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;

import java.util.List;

/**
 * @author agatting on 23.12.16.
 */
@SuppressWarnings({"PMD.UseStringBufferForStringAppends", "checkstyle:multiplestringliterals"})
public interface SparqlUpdateExpression {

    /**
     * Method creates an update sparql string with insert triple(s). Subject, predicate and object can be selected by a "name" (string). Missing namespace is
     * added automatically.
     *
     * @param insert is the insert triple list (with or without namespace).
     * @return a sparql update string to insert proper triples.
     * @throws NotAvailableException is thrown in case the rdf triple is null.
     */
    static String getSparqlUpdateExpression(final List<RdfTriple> insert) throws NotAvailableException {

        MultiException.ExceptionStack exceptionStack = null;
        String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "INSERT DATA { ";

        for (final RdfTriple triple : insert) {
            try {
                updateExpression = updateExpression + getTripleCommand(triple);
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(null, e, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("Some triple are null!", exceptionStack);
        } catch (MultiException e) {
            throw new NotAvailableException("Sparql update expression.");
        }
        return updateExpression + " } ";
    }

    /**
     * Method creates an update sparql string with insert triple(s). Subject, predicate and object can be selected by a "name" (string). Missing namespace is
     * added automatically. The where parameter can be used to specify the insert region. Otherwise the statement is set to null.
     *
     * @param insert is the insert triple list (with or without namespace).
     * @param where is an additional filter triple list to specify the insert region. Can be set to {@code null}, if not necessary.
     * @return a sparql update string to insert proper triples.
     * @throws NotAvailableException is thrown in case the rdf triple is null.
     */
    static String getSparqlUpdateExpression(final List<RdfTriple> insert, final List<RdfTriple> where) throws NotAvailableException {

        MultiException.ExceptionStack exceptionStack = null;
        String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                        + "INSERT { ";

        for (final RdfTriple triple : insert) {
            try {
                updateExpression = updateExpression + getTripleCommand(triple);
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(null, e, exceptionStack);
            }
        }

        updateExpression = updateExpression + "} WHERE { ";

        for (final RdfTriple triple : where) {
            try {
                updateExpression = updateExpression + getTripleCommand(triple);
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(null, e, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("Some triple are null!", exceptionStack);
        } catch (MultiException e) {
            throw new NotAvailableException("Sparql update expression.");
        }
        return updateExpression + " } ";
    }

    /**
     * Method creates an update sparql string with delete and insert triple(s). Subject, predicate and object can be selected by a "name" (string) or can be
     * placed as control variable by a "null" parameter in the rdfTriple. Missing namespace is added automatically. The order in the update string
     * is first delete and then insert. The where parameter can be used to filter the triples, which should be deleted. Otherwise the statement is set to null.
     *
     * Example in delete: "d930d217-02a8-4264-8d9f-240de7f0d0ca hasConnection null"
     * leads to delete string: "NS:d930d217-02a8-4264-8d9f-240de7f0d0ca NS:hasConnection ?object . "
     * and means: all triples with the named subject, named predicate and any object should be deleted.
     *
     * @param delete is the delete triple list (with or without namespace).
     * @param insert is the insert triple list (with or without namespace).
     * @param where is an additional filter triple list. Can be set to {@code null}, if not necessary.
     * @return a sparql update string to delete and insert proper triples.
     * @throws NotAvailableException is thrown in case the rdf triple is null or all triple elements are null (to prevent the deletion of whole ontology).
     */
    static String getSparqlUpdateExpression(final List<RdfTriple> delete, final List<RdfTriple> insert, final List<RdfTriple> where) throws NotAvailableException {

        MultiException.ExceptionStack exceptionStack = null;
        String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "DELETE { ";

        for (final RdfTriple triple : delete) {
            try {
                updateExpression = updateExpression + getTripleCommand(triple);
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(null, e, exceptionStack);
            }
        }

        updateExpression = updateExpression + "} INSERT { ";

        for (final RdfTriple triple : insert) {
            try {
                updateExpression = updateExpression + getTripleCommand(triple);
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(null, e, exceptionStack);
            }
        }

        updateExpression = updateExpression + "} WHERE { ";
        final List<RdfTriple> tripleBuf = (where == null) ? delete : where;

        for (final RdfTriple triple : tripleBuf) {
            try {
                updateExpression = updateExpression + getTripleCommand(triple);
            } catch (NotAvailableException e) {
                exceptionStack = MultiException.push(null, e, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("Some triple are null!", exceptionStack);
        } catch (MultiException e) {
            throw new NotAvailableException("Sparql update expression.");
        }
        return updateExpression + " } ";
    }

    /**
     * Method creates an update sparql string to delete triple(s). Subject, predicate and object can be selected by a "name" (string) or can be placed as
     * control variable by a "null" parameter in the rdfTriple. Missing namespace is added automatically. The where can be used to filter the
     * triples, which should be deleted. Otherwise the statement is set to null.
     *
     * Example in delete: "d930d217-02a8-4264-8d9f-240de7f0d0ca hasConnection null" leads to delete string:
     * "NS:d930d217-02a8-4264-8d9f-240de7f0d0ca NS:hasConnection ?object . "
     * and means: all triples with the named subject, named predicate and any object should be deleted.
     *
     * @param delete is the delete triple (with or without namespace).
     * @param where is an additional filter expression. Can be set to {@code null}, if not necessary.
     * @return a sparql update string to delete proper triples.
     * @throws NotAvailableException is thrown in case the rdf triple is null or all triple elements are null (to prevent the deletion of whole ontology).
     */
    static String getSparqlUpdateExpression(final RdfTriple delete, final String where) throws NotAvailableException {

        final String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "DELETE { " + getTripleCommand(delete);
        
        final String whereBlock = (where == null) ? getTripleCommand(delete) : where;
        return updateExpression + "} WHERE { " + whereBlock + " } ";
    }

    /**
     * Method builds the input rdf triple to a sparql update command (not complete expression). If an element (or multiple) of the rdf triple is null, it is a
     * sparql variable. Otherwise it is a specific ontology element and, if missing, the namespace is added.
     *
     * @param triple is the triple information: subject, predicate, object. If one ore two are {@code null} they are sparql variables.
     * @return a sparql command with following pattern: "NS:subject NS:predicate NS:object .".
     * @throws NotAvailableException is thrown in case the rdf triple is null or all triple elements are null (to prevent the deletion of whole ontology).
     */
    static String getTripleCommand(final RdfTriple triple) throws NotAvailableException {

        if (triple == null) {
            assert false;
            throw new NotAvailableException("Could not build delete triple command, because input triple is null!");
        }

        String subject = triple.getSubject();
        String predicate = triple.getPredicate();
        String object = triple.getObject();

        if (subject == null && predicate == null && object == null) {
            throw new NotAvailableException("Subject, predicate and object are null! Whole ontology can be deleted...!");
        }

        if (subject == null) {
            subject = "?subject";
        } else if (!subject.startsWith(OntExpr.NS.getName()) || !subject.startsWith(OntConfig.NAMESPACE)) {
            subject = OntExpr.NS.getName() + subject;
        }

        if (predicate == null) {
            predicate = "?predicate";
        } else if (!predicate.equalsIgnoreCase(OntExpr.A.getName()) && !predicate.startsWith(OntExpr.NS.getName()) && !predicate.startsWith(OntConfig.NAMESPACE)
                && !predicate.startsWith("owl:") && !predicate.startsWith("rdfs:")) {
            predicate = OntExpr.NS.getName() + predicate;
        }

        if (object == null) {
            object = "?object";
        } else if ((!object.startsWith(OntExpr.NS.getName()) || !object.startsWith(OntConfig.NAMESPACE)) && !object.startsWith("\"")) {
            object = OntExpr.NS.getName() + object;
        }
        return subject + " " + predicate + " " + object + " . ";
    }
}
