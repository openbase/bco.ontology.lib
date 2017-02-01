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
package org.openbase.bco.ontology.lib.sparql;

import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.webcommunication.WebInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 23.12.16.
 */
@SuppressWarnings({"PMD.UseStringBufferForStringAppends", "checkstyle:multiplestringliterals"})
public class SparqlUpdateExpression extends WebInterface {

    //TODO namespace: current check => "NS:" only...not whole namespace

    /**
     * Method creates a list with sparql update insert expressions. Each list element is an valid update.
     *
     * @param insertTripleArrayLists The insert triple information (with or without namespace).
     * @return A list of strings, which are update expressions.
     */
    protected List<String> getSparqlSingleUpdateInsertEx(final List<TripleArrayList> insertTripleArrayLists) {

        final List<String> expressionList = new ArrayList<>();

        for (final TripleArrayList triple : insertTripleArrayLists) {

            final String updateExpression =
                    "PREFIX NS: <" + ConfigureSystem.NS + "> "
                    + "INSERT DATA { "
                        + getInsertTripleCommand(triple)
                    + "} ";

            expressionList.add(updateExpression);
        }

        return expressionList;
    }

    /**
     * Method creates a single sparql update insert expression, which contains multiple triple commands. Atomicity.
     *
     * @param insertTripleArrayLists The insert triple information (with or without namespace).
     * @return A single sparql update insert expression (bundle).
     */
    protected String getSparqlBundleUpdateInsertEx(final List<TripleArrayList> insertTripleArrayLists) {

        // initial part of the large expression
        String multipleUpdateExpression =
                "PREFIX NS: <" + ConfigureSystem.NS + "> "
                + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
                + "INSERT DATA { ";

        for (final TripleArrayList triple : insertTripleArrayLists) {
            // add triples to the large expression
            multipleUpdateExpression = multipleUpdateExpression + getInsertTripleCommand(triple);
        }

        // close the large expression
        multipleUpdateExpression = multipleUpdateExpression + "} ";

        return multipleUpdateExpression;
    }

    /**
     * Method creates an update sparql string with delete and insert triple(s). Subject, predicate and object can be
     * selected by a "name" (string) or can be placed as control variable by a "null" parameter in the tripleArrayList.
     * The names of s, p, o keep the namespace or not. The order in the update string is first delete and then insert.
     * The whereExpr can be used to filter the triples, which should be deleted. Otherwise the statement is set to null.
     *
     * Example in deleteTripleArrayLists: d930d217-02a8-4264-8d9f-240de7f0d0ca hasConnection null
     * leads to delete string: NS:d930d217-02a8-4264-8d9f-240de7f0d0ca NS:hasConnection ?object
     * and means: all triples with the named subject, named predicate and any object are deleted.
     *
     * @param deleteTripleArrayLists The delete triple information (with or without namespace).
     * @param insertTripleArrayLists The insert triple information (with or without namespace).
     * @param whereExpr Additional filter expression. Can be set to null, if not necessary.
     * @return A single sparql update delete & insert expression (bundle).
     */
    protected String getSparqlBundleUpdateDeleteAndInsertEx(final List<TripleArrayList> deleteTripleArrayLists
            , final List<TripleArrayList> insertTripleArrayLists, final String whereExpr) {

        String multipleUpdateExpression =
                "PREFIX NS: <" + ConfigureSystem.NS + "> "
                + "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> "
                + "DELETE { ";

        for (final TripleArrayList deleteTriple : deleteTripleArrayLists) {
            multipleUpdateExpression = multipleUpdateExpression + getDeleteTripleCommand(deleteTriple);
        }

        multipleUpdateExpression = multipleUpdateExpression + "} INSERT { ";

        for (final TripleArrayList insertTriple : insertTripleArrayLists) {
            multipleUpdateExpression = multipleUpdateExpression + getInsertTripleCommand(insertTriple);
        }

        if (whereExpr == null) {
            multipleUpdateExpression = multipleUpdateExpression + "} WHERE { } ";
        } else {
            multipleUpdateExpression = multipleUpdateExpression + "} WHERE { " + whereExpr + "} ";
        }

        return multipleUpdateExpression;
    }

    /**
     * Method creates an update sparql string to delete triple(s). Subject, predicate and object can be selected by a
     * "name" (string) or can be placed as control variable by a "null" parameter in the tripleArrayList. The names of
     * s, p, o keep the namespace or not. The whereExpr can be used to filter the triples, which should be deleted.
     * Otherwise the statement is set to null.
     *
     * Example in deleteTripleArrayLists: d930d217-02a8-4264-8d9f-240de7f0d0ca hasConnection null
     * leads to delete string: NS:d930d217-02a8-4264-8d9f-240de7f0d0ca NS:hasConnection ?object
     * and means: all triples with the named subject, named predicate and any object are deleted.
     *
     * @param deleteTripleArrayLists The delete triple information (with or without namespace).
     * @param whereExpr Additional filter expression. Can be set to null, if not necessary.
     *
     * @return A sparql update string to delete a triple.
     */
    protected String getSparqlUpdateSingleDeleteExpr(final TripleArrayList deleteTripleArrayLists
            , final String whereExpr) {

        String singleUpdateExpression =
                "PREFIX NS: <" + ConfigureSystem.NS + "> "
                + "DELETE { "
                    + getDeleteTripleCommand(deleteTripleArrayLists)
                + "} ";

        if (whereExpr == null) {
            singleUpdateExpression = singleUpdateExpression + "} WHERE { } ";
        } else {
            singleUpdateExpression = singleUpdateExpression + "} WHERE { " + whereExpr + "} ";
        }

        return singleUpdateExpression;
    }

    private String getInsertTripleCommand(final TripleArrayList tripleArrayList) {

        String subject = tripleArrayList.getSubject();
        String predicate = tripleArrayList.getPredicate();
        String object = tripleArrayList.getObject();

        if (subject == null) {
            subject = "?subject";
        } else if (!subject.startsWith(ConfigureSystem.OntExpr.NS.getName())) {
            subject = ConfigureSystem.OntExpr.NS.getName() + tripleArrayList.getSubject();
        }

        // dataTypes starts with \" doesn't have NS
        if (object == null) {
            object = "?object";
        } else if (!object.startsWith(ConfigureSystem.OntExpr.NS.getName()) && !object.startsWith("\"")) {
            object = ConfigureSystem.OntExpr.NS.getName() + tripleArrayList.getObject();
        }

        // if predicate isn't an "a" then it's an property with namespace needed. Info: predicate "a" is used to
        // insert an individual to a class.
        if (predicate == null) {
            predicate = "?predicate";
        } else if (!predicate.equals(ConfigureSystem.OntExpr.A.getName())
                && !predicate.startsWith(ConfigureSystem.OntExpr.NS.getName())) {
            predicate = ConfigureSystem.OntExpr.NS.getName() + predicate;
        }

        return subject + " " + predicate + " " + object + " . ";
    }

    private String getDeleteTripleCommand(final TripleArrayList tripleArrayList) {

        //TODO maybe special safety handling, because if s, p, o are all null => delete whole triple store

        String subject = tripleArrayList.getSubject();
        String predicate = tripleArrayList.getPredicate();
        String object = tripleArrayList.getObject();

        if (subject == null) {
            subject = "?subject";
        } else if (!subject.startsWith(ConfigureSystem.OntExpr.NS.getName())) {
            subject = ConfigureSystem.OntExpr.NS.getName() + subject;
        }

        if (predicate == null) {
            predicate = "?predicate";
        } else if (!predicate.equals(ConfigureSystem.OntExpr.A.getName())
                && !predicate.startsWith(ConfigureSystem.OntExpr.NS.getName())) {
            // if predicate isn't an "a" then it's an property with namespace needed.
            predicate = ConfigureSystem.OntExpr.NS.getName() + predicate;
        }

        if (object == null) {
            object = "?object";
        } else if (!object.startsWith(ConfigureSystem.OntExpr.NS.getName())) {
            object = ConfigureSystem.OntExpr.NS.getName() + object;
        }

        return subject + " " + predicate + " " + object + " . ";
    }
}
