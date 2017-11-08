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
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.MultiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author agatting on 15.09.17.
 */
public class OntProviderServices {

    private final HashMap<String, List<OntStateChange>> ontProviderServices;

    public OntProviderServices() {
        this.ontProviderServices = new HashMap<>();
    }

    public OntProviderServices(final String ontProviderService, final OntStateChange ontStateChange) throws MultiException {
        ExceptionStack exceptionStack = Preconditions.checkNotNull(ontProviderService, "Parameter ontProviderService is null!", null);
        exceptionStack = Preconditions.checkNotNull(ontStateChange, "Parameter ontStateChange is null!", exceptionStack);
        MultiException.checkAndThrow("Input is invalid.", exceptionStack);

        final List<OntStateChange> ontStateChanges = new ArrayList<OntStateChange>(){{add(ontStateChange);}};
        this.ontProviderServices = new HashMap<String, List<OntStateChange>>() {{put(ontProviderService, ontStateChanges);}};
    }

//    public OntProviderServices(final String ontProviderService, final List<OntStateChange> ontStateChanges) {
//        this.ontProviderServices = new HashMap<>() {{addStateChange(ontProviderService, ontStateChanges);}};
//    }

    public HashMap<String, List<OntStateChange>> getOntProviderServices() {
        return ontProviderServices;
    }

    public List<OntStateChange> getStateChanges(final String providerService) {
        return ontProviderServices.get(providerService);
    }

    public void addOntStateChange(final String ontProviderService, final OntStateChange ontStateChange) {
        if (ontProviderServices.containsKey(ontProviderService)) {
            ontProviderServices.get(ontProviderService).add(ontStateChange);
        } else {
            ontProviderServices.put(ontProviderService, new ArrayList<OntStateChange>(){{add(ontStateChange);}});
        }
    }

}
