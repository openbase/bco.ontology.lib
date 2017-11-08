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
import org.openbase.jul.exception.NotAvailableException;

import java.util.HashMap;

/**
 * @author agatting on 15.09.17.
 */
public class OntUnits {

    private final HashMap<String, OntProviderServices> ontUnits;

    public OntUnits() {
        this.ontUnits = new HashMap<>();
    }

    public OntProviderServices getOntProviderServices(final String unitId) throws NotAvailableException {
        Preconditions.checkNotNull(unitId, "Parameter unitId is null!");
        return ontUnits.get(unitId);
    }

    public HashMap<String, OntProviderServices> getOntUnits() {
        return ontUnits;
    }

    public boolean addOntProviderService(final String unitId, final OntProviderServices ontProviderServices) throws MultiException {

        MultiException.checkAndThrow("Input is invalid.", Preconditions.checkNotNull(null, unitId, ontProviderServices));

        if (ontUnits.containsKey(unitId)) {
            return false;
        } else {
            ontUnits.put(unitId, ontProviderServices);
            return true;
        }
    }

//    public void addOntStateChange(final String unitId, final String ontProviderService, final OntStateChange ontStateChange) {
//        if (ontUnits.containsKey(unitId)) {
//            ontUnits.get(unitId).addOntStateChange(ontProviderService, ontStateChange);
//        } else {
//            final OntProviderServices ontProviderServices = new OntProviderServices(ontProviderService, ontStateChange);
//            ontUnits.put(unitId, ontProviderServices);
//        }
//    }

}
