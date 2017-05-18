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
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import rst.domotic.ontology.OntologyChangeType.OntologyChange;
import rst.domotic.ontology.OntologyChangeType.OntologyChange.Category;
import rst.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author agatting on 07.03.17.
 */
public class QueryParser {

    private final String triggerLabel;
    private final String triggerQuery;

    private static final String uriQuery =
            "PREFIX sp: <http://spinrdf.org/sp#> "
            + "PREFIX NAMESPACE: <http://www.openbase.org/bco/ontology#> "
            + "SELECT ?y WHERE { "
                + "{ "
                    + "?x sp:object ?y . "
                    + "} UNION { "
                    + "?x sp:subject ?y . "
                + "} "
                + "FILTER(isURI(?y)) . "
                + "FILTER (regex(str(?y), \"http://www.openbase.org/bco/ontology#\")) . "
                + "FILTER NOT EXISTS { ?x sp:predicate NAMESPACE:hasStateValue } "
                + "FILTER NOT EXISTS { ?x sp:object NAMESPACE:Observation } "
                + "FILTER NOT EXISTS { ?x sp:subject NAMESPACE:Observation } "
                    // more filter criteria can be placed here ...
            + "} ";

    public QueryParser(final String triggerLabel, final String triggerQuery) {
        //TODO test with query, which contains literal....
        this.triggerLabel = triggerLabel;
        this.triggerQuery = triggerQuery;
    }

    public OntologyChange getOntologyChange() throws IllegalArgumentException {

        // check possibility of query parsing
        if (containsNegation(triggerQuery)) {
            throw new IllegalArgumentException("Could not parse query, cause query contains a negation form! Certain determination of trigger " +
                    "changes criteria can't be guarantee. Select manual criteria for trigger " + triggerLabel);
        } else {
            final ResultSet resultSet = getSPINResultSet(triggerQuery);
            final List<String> resourceList = getResourcesOfResultSet(resultSet);
            final List<String> alignedResourceList = getAlignedResources(resourceList);

            final List<UnitType> unitTypeChanges = getUnitTypeChanges(alignedResourceList);
            final List<ServiceType> serviceTypeChanges = getServiceTypeChanges(alignedResourceList);
            final List<Category> categoryChanges = getCategoryChanges(alignedResourceList);

            if (unitTypeChanges.isEmpty() && serviceTypeChanges.isEmpty() && categoryChanges.isEmpty()) {
                throw new IllegalArgumentException("Could not identify ontology changes for trigger " + triggerLabel + ". Maybe bad " +
                        "query or select manual ontology changes.");
            }

            return OntologyChange.newBuilder().addAllCategory(categoryChanges).addAllUnitType(unitTypeChanges).addAllServiceType(serviceTypeChanges).build();
        }
    }

    private List<Category> getCategoryChanges(final List<String> alignedResourceList) {

        final List<Category> categoryChanges = new ArrayList<>();

        //TODO

        return categoryChanges;
    }

    private List<UnitType> getUnitTypeChanges(final List<String> alignedResourceList) {

        final List<UnitType> unitTypeChanges = new ArrayList<>();
        final Map<String, UnitType> alignedUnitTypes = TypeAlignment.getAlignedUnitTypes();

        if (containsLocation(alignedResourceList)) {
            unitTypeChanges.add(UnitType.LOCATION);
        }
        if (containsConnection(alignedResourceList)) {
            unitTypeChanges.add(UnitType.CONNECTION);
        }

        for (final String alignedResource : alignedResourceList) {

            final UnitType unitType = alignedUnitTypes.get(alignedResource);

            if (unitType != null) {
                unitTypeChanges.add(unitType);
            }
        }
        return unitTypeChanges;
    }

    private boolean containsLocation(final List<String> alignedResourceList) {

        for (final String location : TypeAlignment.locationCategories) {
            if (alignedResourceList.contains(location)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsConnection(final List<String> alignedResourceList) {

        for (final String connection : TypeAlignment.connectionCategories) {
            if (alignedResourceList.contains(connection)) {
                return true;
            }
        }
        return false;
    }

    private List<ServiceType> getServiceTypeChanges(final List<String> alignedResourceList) {

        final List<ServiceType> serviceTypeChanges = new ArrayList<>();
        final Map<String, ServiceType> alignedServiceTypes = TypeAlignment.getAlignedServiceTypes();

        for (final String alignedResource : alignedResourceList) {

            final ServiceType serviceType = alignedServiceTypes.get(alignedResource);

            if (serviceType != null) {
                serviceTypeChanges.add(serviceType);
            }
        }
        return serviceTypeChanges;
    }

    private List<String> getAlignedResources(final List<String> resourceList) {
        return resourceList.stream().map(resource -> resource.toLowerCase().replace("_", "")).collect(Collectors.toList());
    }

    private List<String> getResourcesOfResultSet(final ResultSet resultSet) {

        final List<QuerySolution> querySolutionList = ResultSetFormatter.toList(resultSet);
        final List<String> resourceList = new ArrayList<>();

        for (final QuerySolution querySolution : querySolutionList) {

            final String resultUri = querySolution.toString();
            String resourceResult = resultUri.substring(resultUri.indexOf(" <") + 1, resultUri.indexOf("> "));
            resourceResult = resourceResult.substring(OntConfig.NAMESPACE.length() + 1);

            resourceList.add(resourceResult);
        }
        return resourceList;
    }

    private ResultSet getSPINResultSet(final String askQuery) {

        // create query to ask uris from input query
        final Query queryUrisWithBcoNs = QueryFactory.create(uriQuery);
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

    private boolean containsNegation(final String askQuery) {

        // official w3c sparql negation forms
        final String[] negationForms = new String[] {"not exists", "!exists", "minus", "not in", "not bound"};

        for (final String negationForm : negationForms) {
            if (askQuery.toLowerCase().contains(negationForm)) {
                return true;
            }
        }
        return false;
    }

}
