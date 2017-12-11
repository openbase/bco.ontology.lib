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
package org.openbase.bco.ontology.lib.manager.aggregation.datatype;

import org.openbase.bco.ontology.lib.utility.Preconditions;
import org.openbase.jul.exception.MultiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is part of a data structure to provide the bco ontology data (state values of sensors and actuators). The custom data type 'OntProviderServices'
 * expresses the middle-level of the data structure (middle granularity), which includes a quantity of elements (1:N - relation). Consider in addition the
 * data types {@link OntUnits} and {@link OntStateChange}.
 *
 *           1       :      N                       1       :      N
 * OntUnits --- (includes) --- OntProviderServices --- (includes) --- OntStateChange
 *
 * @author agatting on 15.09.17.
 */
public class OntProviderServices {

    private final HashMap<String, List<OntStateChange>> ontProviderServices;

    /**
     * Constructor creates a hashMap, which describes the providerServices(s)(key) and his related list of {@link OntStateChange} (value).
     */
    public OntProviderServices() {
        this.ontProviderServices = new HashMap<>();
    }

    /**
     * Constructor creates a hashMap, which describes the providerServices(s)(key) and his related list of {@link OntStateChange} (value).
     *
     * @param ontProviderService is the providerService extracted from the ontology.
     * @param ontStateChange is the concrete state change extracted from the ontology.
     * @throws MultiException is thrown in case at least one input parameter is null.
     */
    public OntProviderServices(final String ontProviderService, final OntStateChange ontStateChange) throws MultiException {
        MultiException.checkAndThrow("Input is invalid.", Preconditions.checkNotNull(this, null, ontProviderService, ontStateChange));

        final List<OntStateChange> ontStateChanges = new ArrayList<OntStateChange>(){ {add(ontStateChange);} };
        this.ontProviderServices = new HashMap<String, List<OntStateChange>>() { {put(ontProviderService, ontStateChanges);} };
    }

    /**
     * Method adds a hashMap entry based on the input parameter.
     *
     * @param ontProviderService is the providerService extracted from the ontology.
     * @param ontStateChange is the concrete state change extracted from the ontology.
     * @throws MultiException is thrown in case at least one input parameter is null.
     */
    public void addOntStateChange(final String ontProviderService, final OntStateChange ontStateChange) throws MultiException {
        MultiException.checkAndThrow("Input is invalid.", Preconditions.checkNotNull(this, null, ontProviderService, ontStateChange));

        if (ontProviderServices.containsKey(ontProviderService)) {
            ontProviderServices.get(ontProviderService).add(ontStateChange);
        } else {
            ontProviderServices.put(ontProviderService, new ArrayList<OntStateChange>(){ {add(ontStateChange);} });
        }
    }

}