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
    static String getSparqlInsertExpression(final List<RdfTriple> insert) throws NotAvailableException {

        MultiException.ExceptionStack exceptionStack = null;
        String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "INSERT DATA { ";

        for (RdfTriple triple : insert) {
            try {
                updateExpression += getTripleCommand(triple, false);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("Some triple are null!", exceptionStack);
        } catch (MultiException ex) {
            throw new NotAvailableException("Sparql update expression.", ex);
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

        for (RdfTriple triple : insert) {
            try {
                updateExpression += getTripleCommand(triple, false);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        updateExpression += "} WHERE { ";

        if (where != null) {
            for (RdfTriple triple : where) {
                try {
                    updateExpression += getTripleCommand(triple, false);
                } catch (NotAvailableException ex) {
                    exceptionStack = MultiException.push(null, ex, exceptionStack);
                }
            }
        }

        try {
            MultiException.checkAndThrow("Some triple are null!", exceptionStack);
        } catch (MultiException ex) {
            throw new NotAvailableException("Sparql update expression.", ex);
        }
        return updateExpression + " } ";
    }

    /**
     * Method creates an update sparql string with delete and insert triple(s). Subject, predicate and object can be selected by a "name" (string) or can be
     * placed as control variable by a "null" parameter in the rdfTriple. Missing namespace is added automatically. The order in the update string
     * is first delete and then insert. The where parameter can be used to filter the triples, which should be deleted. Otherwise the where parameter is set to
     * null, which leads to the same parameter like the delete parameter in the method. Beware: This sparql expressions separates delete/where and insert, which
     * means the sparql expression inserts triples as well there is NO match of the (delete/)where parameter! To insert triples dependent on the delete/where
     * parameter use another method.
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
    static String getSeparatedSparqlUpdateExpression(final List<RdfTriple> delete, final List<RdfTriple> insert, final List<RdfTriple> where) throws NotAvailableException {
        if (delete == null) {
            assert false;
            throw new NotAvailableException("Delete triple list is null.");
        }

        if (insert == null) {
            assert false;
            throw new NotAvailableException("Insert triple list is null.");
        }

        MultiException.ExceptionStack exceptionStack = null;
        String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <" + OntConfig.XSD + "> "
                + "PREFIX rdfs: <" + OntConfig.RDFS + "> "
                + "DELETE { ";

        for (RdfTriple triple : delete) {
            try {
                updateExpression += getTripleCommand(triple, false);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        updateExpression += "} WHERE { ";
        List<RdfTriple> whereExpression = (where == null) ? delete : where;

        for (RdfTriple triple : whereExpression) {
            try {
                updateExpression += getTripleCommand(triple, true);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        updateExpression += "} ; INSERT DATA { ";

        for (RdfTriple triple : insert) {
            try {
                updateExpression += getTripleCommand(triple, false);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("Some triple are null!", exceptionStack);
        } catch (MultiException ex) {
            throw new NotAvailableException("Sparql update expression.", ex);
        }
        return updateExpression + " } ";
    }

    /**
     * Method creates an update sparql string with delete and insert triple(s). Subject, predicate and object can be selected by a "name" (string) or can be
     * placed as control variable by a "null" parameter in the rdfTriple. Missing namespace is added automatically. The order in the update string
     * is first delete and then insert. The where parameter can be used to filter the triples, which should be deleted. Otherwise the where parameter is set to
     * null, which leads to the same parameter like the delete parameter in the method. Beware: This sparql expressions connects all parameter, which means if
     * there is no match with the where parameter (or rather delete parameter if where is set to null), the sparql expression doesn't insert any triples! To
     * insert triples independent on the delete/where parameter use another method.
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
    static String getConnectedSparqlUpdateExpression(final List<RdfTriple> delete, final List<RdfTriple> insert, final List<RdfTriple> where) throws NotAvailableException {
        if (delete == null) {
            assert false;
            throw new NotAvailableException("Delete triple list is null.");
        }

        if (insert == null) {
            assert false;
            throw new NotAvailableException("Insert triple list is null.");
        }

        MultiException.ExceptionStack exceptionStack = null;
        String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <" + OntConfig.XSD + "> "
                + "PREFIX rdfs: <" + OntConfig.RDFS + "> "
                + "DELETE { ";

        for (RdfTriple triple : delete) {
            try {
                updateExpression += getTripleCommand(triple, false);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        updateExpression += "} INSERT { ";

        for (RdfTriple triple : insert) {
            try {
                updateExpression += getTripleCommand(triple, false);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        updateExpression += "} WHERE { ";
        List<RdfTriple> whereExpression = (where == null) ? delete : where;

        for (RdfTriple triple : whereExpression) {
            try {
                updateExpression += getTripleCommand(triple, true);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("Some triple are null!", exceptionStack);
        } catch (MultiException ex) {
            throw new NotAvailableException("Sparql update expression.", ex);
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

        String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <" + OntConfig.XSD + "> "
                + "PREFIX rdfs: <" + OntConfig.RDFS + "> "
                + "DELETE { " + getTripleCommand(delete, false);
        String whereExpression = (where == null) ? getTripleCommand(delete, false) : where;

        return updateExpression + "} WHERE { " + whereExpression + " } ";
    }

    /**
     * Method creates an update sparql string with delete triple(s). Subject, predicate and object can be selected by a "name" (string). Missing namespace is
     * added automatically. The where parameter is used to specify the triples, which should be deleted. Can be set to null, if not necessary.
     *
     * @param delete is the delete triple list (with or without namespace).
     * @param where is an additional filter triple list. Can be set to {@code null}, if not necessary.
     * @return a sparql update string to delete proper triples.
     * @throws NotAvailableException is thrown in case the rdf triple is null or all triple elements are null (to prevent the deletion of whole ontology).
     */
    static String getSparqlDeleteExpression(final List<RdfTriple> delete, List<RdfTriple> where) throws NotAvailableException {

        if (delete == null) {
            assert false;
            throw new NotAvailableException("Delete triple list is null.");
        }

        MultiException.ExceptionStack exceptionStack = null;
        String updateExpression =
                "PREFIX NS: <" + OntConfig.NAMESPACE + "> "
                + "PREFIX xsd: <" + OntConfig.XSD + "> "
                + "PREFIX rdfs: <" + OntConfig.RDFS + "> "
                + "DELETE { ";

        for (RdfTriple triple : delete) {
            try {
                updateExpression += getTripleCommand(triple, false);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        updateExpression += "} WHERE { ";
        List<RdfTriple> whereExpression = (where == null) ? delete : where;

        for (RdfTriple triple : whereExpression) {
            try {
                updateExpression += getTripleCommand(triple, false);
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        try {
            MultiException.checkAndThrow("Some triple are null!", exceptionStack);
        } catch (MultiException ex) {
            throw new NotAvailableException("Sparql update expression.", ex);
        }
        return updateExpression + " } ";
    }

    /**
     * Method builds the input rdf triple to a sparql update command (not complete expression). If an element (or multiple) of the rdf triple is null, it is a
     * sparql variable. Otherwise it is a specific ontology element and, if missing, the namespace is added.
     *
     * @param triple is the triple information: subject, predicate, object. If one ore two are {@code null} they are sparql variables.
     * @param ignoreSafeguard is used to avoid an exception, if all elements of the triple are null. Beware! Maybe whole ontology can be deleted!
     * @return a sparql command with following pattern: "NS:subject NS:predicate NS:object .".
     * @throws NotAvailableException is thrown in case the rdf triple is null or all triple elements are null (to prevent the deletion of whole ontology).
     */
    static String getTripleCommand(final RdfTriple triple, final boolean ignoreSafeguard) throws NotAvailableException {

        if (triple == null) {
            assert false;
            throw new NotAvailableException("Could not build delete triple command, because input triple is null!");
        }

        String subject = triple.getSubject();
        String predicate = triple.getPredicate();
        String object = triple.getObject();

        if (subject == null && predicate == null && object == null && !ignoreSafeguard) {
            throw new NotAvailableException("Subject, predicate and object are null! Whole ontology can be deleted...!");
        }

        if (subject == null) {
            subject = "?subject";
        } else if (!subject.startsWith(OntExpr.NS.getName()) || !subject.startsWith(OntConfig.NAMESPACE)) {
            subject = OntExpr.NS.getName() + subject;
        }

        if (predicate == null) {
            predicate = "?predicate";
        } else if (!predicate.equalsIgnoreCase(OntExpr.IS_A.getName()) && !predicate.startsWith(OntExpr.NS.getName()) && !predicate.startsWith(OntConfig.NAMESPACE)
                && !predicate.startsWith("owl:") && !predicate.startsWith("rdfs:")) {
            predicate = OntExpr.NS.getName() + predicate;
        }

        if (object == null) {
            object = "?object";
        } else if (!object.startsWith(OntExpr.NS.getName()) && !object.startsWith(OntConfig.NAMESPACE) && !object.startsWith("\"")) {
            object = OntExpr.NS.getName() + object;
        }
        return subject + " " + predicate + " " + object + " . ";
    }
}
