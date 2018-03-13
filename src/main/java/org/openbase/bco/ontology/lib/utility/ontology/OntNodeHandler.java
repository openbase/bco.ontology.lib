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
package org.openbase.bco.ontology.lib.utility.ontology;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntAggregatedStateChange;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.OntStateChangeBuf;
import org.openbase.bco.ontology.lib.system.config.OntConfig.StateValueType;
import org.openbase.bco.ontology.lib.utility.Preconditions;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface contains methods to handle and filter ontology nodes (based on RDF) as to their content.
 *
 * @author agatting on 08.08.17.
 */
public interface OntNodeHandler {

    /**
     * Method filters the input state changes and returns state changes, which includes literals of the input data type.
     *
     * @param stateChanges are the state changes of type OntStateChangeBuf, which include the state values.
     * @param dataType is the ontology data type to filter the different state values.
     * @return a list of filtered (literals only) state changes.
     * @throws MultiException is thrown in case one important value is null.
     */
    static List<OntStateChangeBuf> getLiteralElements(final List<OntStateChangeBuf> stateChanges, final StateValueType dataType) throws MultiException {

        ExceptionStack exceptionStack = new ExceptionStack();

        Preconditions.multipleCheckNotNullAndThrow(null, stateChanges, dataType);

//        Preconditions.checkNotNull(stateChanges, null, "Input state changes can not be null.", exceptionStack);
//        exceptionStack = Preconditions.checkNotNull(null, dataType, "Input state value type can not be null.", exceptionStack);

//        MultiException.checkAndThrow("Input parameter invalid.", exceptionStack);
        final List<OntStateChangeBuf> stateChangeLiterals = new ArrayList<>();

        for (final OntStateChangeBuf stateChange : stateChanges) {
            boolean buf = false;

//            final RDFNode stateValue = stateChange.getStateValues().get(0);
//            final List<RDFNode> rdfNode = null;
//
//            final OntStateChange ontStateChange = new OntStateChange(true, null, null);
//            final OntProviderServices ontProviderService = new OntProviderServices("");
//            ontProviderService.ontStateChes.add(ontStateChange);




            for (final RDFNode stateValue : stateChange.getStateValues()) {
                try {
                    Preconditions.checkNotNull(stateValue, "RDFNode can not be null!");

                    if (stateValue.isLiteral() && StringModifier.getLocalName(stateValue.asLiteral().getDatatypeURI()).equalsIgnoreCase(dataType.toString())) {
                        buf = true;
                    }
                } catch (NotAvailableException ex) {
                    exceptionStack = MultiException.push(null, ex, exceptionStack);
                }
            }

            if (buf) {
                stateChangeLiterals.add(stateChange);
            }
        }

        MultiException.checkAndThrow("There is a /are literal/s with no name!", exceptionStack);
        return stateChangeLiterals;
    }

    /**
     * Method filters the input state changes and returns state changes, which includes literals of the input data type.
     *
     * @param stateChanges are the state changes of type OntAggregatedStateChange, which include the state values.
     * @param dataType is the ontology data type to filter the different state values.
     * @return a list of filtered state changes.
     * @throws MultiException is thrown in case some state values are null.
     */
    static List<OntAggregatedStateChange> getAggLiteralElements(final List<OntAggregatedStateChange> stateChanges, final StateValueType dataType)
            throws MultiException {

        final List<OntAggregatedStateChange> stateChangeLiterals = new ArrayList<>();
        MultiException.ExceptionStack exceptionStack = null;

        for (final OntAggregatedStateChange stateChange : stateChanges) {
            try {
                final RDFNode stateValue = stateChange.getStateValue();

                if (stateValue.isLiteral() && StringModifier.getLocalName(stateValue.asLiteral().getDatatypeURI()).equalsIgnoreCase(dataType.name())) {
                    stateChangeLiterals.add(stateChange);
                }
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(null, ex, exceptionStack);
            }
        }

        MultiException.checkAndThrow("There is a /are literal/s with no name!", exceptionStack);
        return stateChangeLiterals;
    }

    /**
     * Method filters the input state changes and returns state changes, which includes resources.
     *
     * @param stateChanges are the state changes of type OntStateChangeBuf, which include the state values.
     * @return a list of filtered state changes.
     */
    static List<OntStateChangeBuf> getResourceElements(final List<OntStateChangeBuf> stateChanges) {

        final List<OntStateChangeBuf> stateChangeLiterals = new ArrayList<>();

        for (final OntStateChangeBuf stateChange : stateChanges) {
            boolean buf = false;

            for (final RDFNode stateValue : stateChange.getStateValues()) {
                if (stateValue.isResource()) {
                    buf = true;
                }
            }

            if (buf) {
                stateChangeLiterals.add(stateChange);
            }
        }
        return stateChangeLiterals;
    }

    /**
     * Method filters the input state changes and returns state changes, which includes resources.
     *
     * @param stateChanges are the state changes of type OntAggregatedStateChange, which include the state values.
     * @return a list of filtered state changes.
     */
    static List<OntAggregatedStateChange> getAggResourceElements(final List<OntAggregatedStateChange> stateChanges) {

        final List<OntAggregatedStateChange> stateChangeLiterals = new ArrayList<>();

        for (final OntAggregatedStateChange stateChange : stateChanges) {
            if (stateChange.getStateValue().isResource()) {
                stateChangeLiterals.add(stateChange);
            }
        }
        return stateChangeLiterals;
    }

    /**
     * Method gets the rdfNode from the input querySolution based on input name and returns the lexical form.
     *
     * @param querySolution is the single answer of the SELECT query, which contains the searched rdfNode.
     * @param name of the searched rdfNode. It's the control variable from the sparql query (e.g. ?unit - without "?").
     * @return the lexical form of the searched rdfNode.
     * @throws NotAvailableException is thrown in case at least one input parameter is null or not present int the
     * query solution.
     */
    static String getRDFLocalName(final QuerySolution querySolution, final String name) throws NotAvailableException {
        Preconditions.checkNotNull(querySolution, "Input querySolution is null!");
        Preconditions.checkNotNull(name, "Input name is null!");

        final RDFNode rdfNode = querySolution.get(name);

        if (rdfNode == null) {
            throw new NotAvailableException("Variable isn't present in this query solution!");
        } else if (rdfNode.isLiteral()) {
            return querySolution.getLiteral(name).getLexicalForm();
        } else {
            return StringModifier.getLocalName(querySolution.getResource(name).toString());
        }
    }
}
