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
package org.openbase.bco.ontology.lib.trigger.sparql;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.utility.sparql.QueryExpression;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.ontology.OntologyChangeType.OntologyChange.Category;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 07.03.17.
 */
public class QueryParser {

    /**
     * Method creates an individual ontologyChange for the input query string (of the trigger with input label). The ontologyChange contains three types of
     * change values, which aggregate to (1) change categories, (2) service types and (3) unit types. Consider: the query string should not contain any negation
     * phrase, because of the parse complexity.
     *
     * @param triggerLabel is the label of the trigger.
     * @param triggerQuery is the query of the trigger.
     * @return the ontologyChange with change categories, service types and unit types.
     * @throws NotAvailableException is thrown in case the input label or query is null.
     * @throws MultiException is thrown in case the ontologyChange could not be parsed from the input query.
     */
    public OntologyChange getOntologyChange(final String triggerLabel, final String triggerQuery) throws NotAvailableException, MultiException {

        if (triggerLabel == null) {
            assert false;
            throw new NotAvailableException("Input label of trigger is null.");
        }

        if (triggerQuery == null) {
            assert false;
            throw new NotAvailableException("Input query of trigger is null.");
        }

        try {
            detectNegation(triggerQuery);
        } catch (MultiException ex) {
            throw new MultiException("Could not perform trigger with label \"" + triggerLabel + "\"", ex.getExceptionStack());
        }

        final ResultSet resultSet = getSPINResultSet(triggerQuery);
        final List<String> resources = getResourcesOfResultSet(resultSet);

        final List<UnitType> unitTypeChanges = getUnitTypeChanges(resources);
        final List<ServiceType> serviceTypeChanges = getServiceTypeChanges(resources);
        final List<Category> categoryChanges = getCategoryChanges(resources);

        if (unitTypeChanges.isEmpty() && serviceTypeChanges.isEmpty() && categoryChanges.isEmpty()) {
            throw new NotAvailableException("Could not identify any ontology changes for trigger with label \"" + triggerLabel + "\". Maybe wrong "
                    + "query string? Or select ontology change manually.");
        }

        return OntologyChange.newBuilder().addAllCategory(categoryChanges).addAllUnitType(unitTypeChanges).addAllServiceType(serviceTypeChanges).build();
    }

    private List<Category> getCategoryChanges(final List<String> resources) {

        final List<Category> categoryChanges = new ArrayList<>();

        //TODO

        return categoryChanges;
    }

    private List<UnitType> getUnitTypeChanges(final List<String> resources) {

        final List<UnitType> unitTypeChanges = new ArrayList<>();

        if (containsLocation(resources)) {
            unitTypeChanges.add(UnitType.LOCATION);
        }

        if (containsConnection(resources)) {
            unitTypeChanges.add(UnitType.CONNECTION);
        }

        for (final String resource : resources) {
            final UnitType unitType = OntConfig.UNIT_NAME_MAP.get(resource);

            if (unitType != null) {
                unitTypeChanges.add(unitType);
            }
        }
        return unitTypeChanges;
    }

    private boolean containsLocation(final List<String> resources) {

        for (final String location : OntConfig.LOCATION_CATEGORIES) {
            if (resources.contains(location)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsConnection(final List<String> resources) {

        for (final String connection : OntConfig.CONNECTION_CATEGORIES) {
            if (resources.contains(connection)) {
                return true;
            }
        }
        return false;
    }

    private List<ServiceType> getServiceTypeChanges(final List<String> resources) {

        final List<ServiceType> serviceTypeChanges = new ArrayList<>();

        for (final String resource : resources) {

            final ServiceType serviceType = OntConfig.SERVICE_NAME_MAP.get(resource);
            if (serviceType != null) {
                serviceTypeChanges.add(serviceType);
            }
        }
        return serviceTypeChanges;
    }

    private List<String> getResourcesOfResultSet(final ResultSet resultSet) {

        final List<QuerySolution> querySolutions = ResultSetFormatter.toList(resultSet);
        final List<String> resources = new ArrayList<>();

        for (final QuerySolution querySolution : querySolutions) {
            final String resultUri = querySolution.toString();
            String resourceResult = resultUri.substring(resultUri.indexOf(" <") + 1, resultUri.indexOf("> "));
            resourceResult = resourceResult.substring(OntConfig.NAMESPACE.length() + 1);

            resources.add(resourceResult);
        }
        return resources;
    }

    private ResultSet getSPINResultSet(final String askQuery) {
        // create query to ask uris from input query
        final Query queryUrisWithBcoNs = QueryFactory.create(QueryExpression.QUERY_URIS);
        final Model model = ModelFactory.createDefaultModel();

        // convert ask query to rdf spin
        final Query arqQuery = ARQFactory.get().createQuery(model, askQuery);
        final ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
        arq2SPIN.createQuery(arqQuery, null);

        // query all uris from beginning ask query
        final QueryExecution queryExecution = ARQFactory.get().createQueryExecution(queryUrisWithBcoNs, model);
        // resultSet contains all classes and instances of the query
        return queryExecution.execSelect();
    }

    private void detectNegation(final String askQuery) throws MultiException {
        // official w3c sparql negation forms
        final String[] negationForms = new String[] {"not exists", "!exists", "minus", "not in", "not bound"};
        MultiException.ExceptionStack exceptionStack = null;

        for (final String negationForm : negationForms) {
            try {
                if (askQuery.toLowerCase().contains(negationForm)) {
                    throw new NotAvailableException("Found negation keyword: " + negationForm);
                }
            } catch (NotAvailableException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }
        MultiException.checkAndThrow("Could not parse ontologyChange from query string, because of negation phrase in query string! Certain determination of "
                + "trigger changes criteria can't be guarantee. Select manual criteria for this trigger!", exceptionStack);
    }

}
