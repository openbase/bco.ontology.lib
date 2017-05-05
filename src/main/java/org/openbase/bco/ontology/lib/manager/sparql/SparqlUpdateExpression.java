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
package org.openbase.bco.ontology.lib.manager.sparql;

import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 23.12.16.
 */
@SuppressWarnings({"PMD.UseStringBufferForStringAppends", "checkstyle:multiplestringliterals"})
public interface SparqlUpdateExpression {

    //TODO exception handling if null

    /**
     * Method creates a list with sparql update insert expressions. Each list element is an valid update.
     *
     * @param insertTriples The insert triple information (with or without namespace).
     * @return A list of strings, which are insert update expressions.
     * @throws IllegalArgumentException Exception is thrown, if whole triple is null or their elements are all null, to prevent a deletion of whole ontology.
     */
    static List<String> getSparqlUpdateInsertSingleExpr(final List<TripleArrayList> insertTriples) throws IllegalArgumentException {

        final List<String> expressionList = new ArrayList<>();

        for (final TripleArrayList triple : insertTriples) {
            final String updateExpression =
                    "PREFIX NS: <" + OntConfig.NS + "> "
                    + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
                    + "INSERT DATA { "
                        + getTripleCommand(triple)
                    + " } ";

            expressionList.add(updateExpression);
        }

        return expressionList;
    }

    /**
     * Method creates a single sparql update insert expression, which contains multiple triple commands. Atomicity.
     *
     * @param insertTriples The insert triple information (with or without namespace).
     * @return A single sparql update insert expression (bundle).
     * @throws IllegalArgumentException Exception is thrown, if whole triple is null or their elements are all null, to prevent a deletion of whole ontology.
     */
    static String getSparqlUpdateInsertBundleExpr(final List<TripleArrayList> insertTriples) throws IllegalArgumentException {

        // initial part of the large expression
        String multipleUpdateExpression =
                "PREFIX NS: <" + OntConfig.NS + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "INSERT DATA { ";

        for (final TripleArrayList triple : insertTriples) {
            // add triples to the large expression
            multipleUpdateExpression = multipleUpdateExpression + getTripleCommand(triple);
        }

        // close the large expression
        return multipleUpdateExpression + " } ";
    }

    static String getSparqlUpdateInsertWhereBundleExpr(final List<TripleArrayList> insertTriples, final List<TripleArrayList> whereTriples)
            throws IllegalArgumentException {

        // initial part of the large expression
        String multipleUpdateExpression =
                "PREFIX NS: <" + OntConfig.NS + "> "
                        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                        + "INSERT { ";

        for (final TripleArrayList triple : insertTriples) {
            // add triples to the large expression
            multipleUpdateExpression = multipleUpdateExpression + getTripleCommand(triple);
        }

        multipleUpdateExpression = multipleUpdateExpression + "} WHERE { ";

        for (final TripleArrayList triple : whereTriples) {
            multipleUpdateExpression = multipleUpdateExpression + getTripleCommand(triple);
        }

        // close the large expression
        return multipleUpdateExpression + " } ";
    }

    /**
     * Method creates an update sparql string with delete and insert triple(s). Subject, predicate and object can be selected by a "name" (string) or can be
     * placed as control variable by a "null" parameter in the tripleArrayList. The names of s, p, o keep the namespace or not. The order in the update string
     * is first delete and then insert. The whereExpr can be used to filter the triples, which should be deleted. Otherwise the statement is set to null.
     *
     * Example in deleteTriples: d930d217-02a8-4264-8d9f-240de7f0d0ca hasConnection null
     * leads to delete string: NS:d930d217-02a8-4264-8d9f-240de7f0d0ca NS:hasConnection ?object
     * and means: all triples with the named subject, named predicate and any object are deleted.
     *
     * @param deleteTriples The delete triple information (with or without namespace).
     * @param insertTriples The insert triple information (with or without namespace).
     * @param whereTriples Additional filter expression. Can be set to null, if not necessary.
     * @return A single sparql update delete and insert expression (bundle).
     * @throws IllegalArgumentException Exception is thrown, if whole triple is null or their elements are all null (triple! not list!), to prevent a deletion
     * of whole ontology.
     */
    static String getSparqlUpdateDeleteAndInsertBundleExpr(final List<TripleArrayList> deleteTriples, final List<TripleArrayList> insertTriples
            , final List<TripleArrayList> whereTriples) throws IllegalArgumentException {

        String multipleUpdateExpression =
                "PREFIX NS: <" + OntConfig.NS + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "DELETE { ";

        for (final TripleArrayList deleteTriple : deleteTriples) {
            multipleUpdateExpression = multipleUpdateExpression + getTripleCommand(deleteTriple);
        }

        multipleUpdateExpression = multipleUpdateExpression + "} INSERT { ";

        for (final TripleArrayList insertTriple : insertTriples) {
            multipleUpdateExpression = multipleUpdateExpression + getTripleCommand(insertTriple);
        }

        if (whereTriples == null) { // same triples as delete (functional reasons)
            multipleUpdateExpression = multipleUpdateExpression + "} WHERE { ";
            for (final TripleArrayList deleteTriple : deleteTriples) {
                multipleUpdateExpression = multipleUpdateExpression + getTripleCommand(deleteTriple);
            }

            multipleUpdateExpression = multipleUpdateExpression + "} ";
        } else {
            multipleUpdateExpression = multipleUpdateExpression + "} WHERE { ";
            for (final TripleArrayList whereTriple : whereTriples) {
                multipleUpdateExpression = multipleUpdateExpression + getTripleCommand(whereTriple);
            }
            multipleUpdateExpression = multipleUpdateExpression + "} ";
        }

        return multipleUpdateExpression;
    }

    /**
     * Method creates an update sparql string to delete triple(s). Subject, predicate and object can be selected by a "name" (string) or can be placed as
     * control variable by a "null" parameter in the tripleArrayList. The names of s, p, o keep the namespace or not. The whereExpr can be used to filter the
     * triples, which should be deleted. Otherwise the statement is set to null.
     *
     * Example in deleteTriple: d930d217-02a8-4264-8d9f-240de7f0d0ca hasConnection null leads to delete string:
     * NS:d930d217-02a8-4264-8d9f-240de7f0d0ca NS:hasConnection ?object
     * and means: all triples with the named subject, named predicate and any object are deleted.
     *
     * @param deleteTriple The delete triple information (with or without namespace).
     * @param whereExpr Additional filter expression. Can be set to null, if not necessary.
     * @return A sparql update string to delete a triple.
     * @throws IllegalArgumentException Exception is thrown, if whole triple is null or their elements are all null, to prevent a deletion of whole ontology.
     */
    static String getSparqlUpdateSingleDeleteExpr(final TripleArrayList deleteTriple, final String whereExpr) throws IllegalArgumentException {

        String singleUpdateExpression =
                "PREFIX NS: <" + OntConfig.NS + "> "
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
                + "DELETE { "
                    + getTripleCommand(deleteTriple);

        if (whereExpr == null) { // same triples as delete (functional reasons)
            singleUpdateExpression = singleUpdateExpression
                    + "} WHERE { "
                        + getTripleCommand(deleteTriple)
                    + "} ";
        } else {
            singleUpdateExpression = singleUpdateExpression + "} WHERE { " + whereExpr + " } ";
        }

        return singleUpdateExpression;
    }

    /**
     * Method builds a connected command of the input triple. In this case the sparql update frame is missing. If elements are null, they are sparql variables.
     * Otherwise the namespace is added, if necessary.
     *
     * @param triple The triple information: subject, predicate, object. If elements are null, then it is a sparql variable.
     * @return A triple command without sparql update frame.
     * @throws IllegalArgumentException Exception is thrown, if whole triple is null or their elements are all null, to prevent a deletion of whole ontology.
     */
    static String getTripleCommand(final TripleArrayList triple) throws IllegalArgumentException {

        if (triple == null) {
            throw new IllegalArgumentException("Could not build delete triple command, cause input triple is null!");
        }

        String subject = triple.getSubject();
        String predicate = triple.getPredicate();
        String object = triple.getObject();

        if (subject == null && predicate == null && object == null) {
            throw new IllegalArgumentException("Subject, predicate and object are null! Does not build delete command, cause command deletes whole ontology!");
        }

        if (subject == null) {
            subject = "?subject";
        } else if (!subject.startsWith(OntExpr.NS.getName()) || !subject.startsWith(OntConfig.NS)) {
            subject = OntExpr.NS.getName() + subject;
        }

        if (predicate == null) {
            predicate = "?predicate";
        } else if (!predicate.equalsIgnoreCase(OntExpr.A.getName()) && (!predicate.startsWith(OntExpr.NS.getName()) || !predicate.startsWith(OntConfig.NS))) {
            // if predicate isn't an "a" then it's an property with namespace needed.
            predicate = OntExpr.NS.getName() + predicate;
        }

        // dataTypes starts with \" doesn't have NS
        if (object == null) {
            object = "?object";
        } else if ((!object.startsWith(OntExpr.NS.getName()) || !object.startsWith(OntConfig.NS)) && !object.startsWith("\"")) {
            object = OntExpr.NS.getName() + object;
        }

        return subject + " " + predicate + " " + object + " . ";
    }
}
