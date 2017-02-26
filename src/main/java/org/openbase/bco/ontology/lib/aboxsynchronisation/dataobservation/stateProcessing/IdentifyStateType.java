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
package org.openbase.bco.ontology.lib.aboxsynchronisation.dataobservation.stateProcessing;

import javafx.util.Pair;
import org.openbase.bco.ontology.lib.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;
import rst.domotic.state.ColorStateType.ColorState;
import rst.domotic.state.PowerStateType.PowerState;

import java.util.List;
import java.util.Set;

/**
 * @author agatting on 22.02.17.
 */
public class IdentifyStateType extends ValueOfServiceType {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IdentifyStateType.class);

    // declaration of predicates and classes, which are static
    private static final String predicateIsA = OntExpr.A.getName();
    private static final String predicateHasStateValue = OntProp.STATE_VALUE.getName();
    private static final String stateValueClass = OntCl.STATE_VALUE.getName();

    protected List<TripleArrayList> addStateValue(final String serviceType, final Object stateObject, final String subjectObservation
            , final List<TripleArrayList> tripleArrayListsBuf) {

        final Pair<Set<String>, Boolean> stateTypeAndIsPhysicalTypePair = identifyState(serviceType, stateObject);

        if (stateTypeAndIsPhysicalTypePair != null) {
            for (final String stateValue : stateTypeAndIsPhysicalTypePair.getKey()) {
                // check if stateType based on physical type. If yes = literal, if no = stateValue instance.
                if (!stateTypeAndIsPhysicalTypePair.getValue()) { // stateValue instance
//                System.out.println(stateTypeAndIsPhysicalTypePair.getKey());
                    tripleArrayListsBuf.add(new TripleArrayList(stateValue, predicateIsA, stateValueClass)); //TODO: redundant. another possibility?
                } else { // literal

                }
                tripleArrayListsBuf.add(new TripleArrayList(subjectObservation, predicateHasStateValue, stateValue));
            }
        } else {
            // no matched stateService
            try {
                throw new NotAvailableException("Could not identify stateType. Please check implementation or rather integrate " + serviceType
                        + " to method identifyState");
            } catch (NotAvailableException e) {
                ExceptionPrinter.printHistory(e, LOGGER, LogLevel.WARN);
            }
        }

        return tripleArrayListsBuf;
    }

    private Pair<Set<String>, Boolean> identifyState(final String serviceType, final Object stateObject) {
        final String serviceTypeBuf = serviceType.toLowerCase().replaceAll(OntExpr.REMOVE.getName(), "");
        Pair<Set<String>, Boolean> stateAndPhysicalTypePair;
        final Set<String> setStateValues;

        switch (serviceTypeBuf) {
            case "powerstateservice":
                setStateValues = powerStateValue((PowerState) stateObject);
                stateAndPhysicalTypePair = new Pair<>(setStateValues, false);

                return stateAndPhysicalTypePair;
            case "colorstateservice":
                setStateValues =  colorStateValue((ColorState) stateObject);
                stateAndPhysicalTypePair = new Pair<>(setStateValues, true);

                return stateAndPhysicalTypePair;
            default:
                return null;
        }
    }
}
