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

package org.openbase.bco.ontology.lib;

/**
 * Created by agatting on 14.11.16.
 */

import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This java class configures the ontology-system and set different elements like namespace or superclasses of the
 * ontology. Furthermore a method tests the validity of them to roll an ExceptionHandling part out of the
 * ontology-processing-classes.
 */
public final class ConfigureSystem {

    /**
     * Namespace of the ontology.
     */
    public static final String NS = "http://www.openbase.org/bco/ontology#";

    /**
     * DateTime format.
     */
    public static final String DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * Ontology superclass name of Units.
     */
    public static final String UNIT_SUPERCLASS = "Unit";

    /**
     * Ontology superclass name of States.
     */
    public static final String STATE_SUPERCLASS = "State";

    /**
     * Ontology superclass name of ProviderServices.
     */
    public static final String PROVIDER_SERVICE_SUPERCLASS = "ProviderService";

    /**
     * UnitType Connection.
     */
    public static final String UNIT_TYPE_CONNECTION = "Connection";

    /**
     * UnitType Location.
     */
    public static final String UNIT_TYPE_LOCATION = "Location";

    /**
     * Pattern to remove all special signs in a string.
     */
    public static final String REMOVE_PATTERN = "[^\\p{Alpha}]";

    // -------------------------------

    /**
     * General string pattern (lower case only).
     */
    public static final String STRING_PATTERN = "[a-z]*";

    /**
     * Dollar sign.
     */
    public static final char DOLLAR_SIGN = '$';

    /**
     * state String.
     */
    public static final String STATE = "state";

    /**
     * get String.
     */
    public static final String GET = "get";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigureSystem.class);

    /**
     * Regular expressions for method searching.
     */
    //TODO interface not nice...
    public interface RegEx {
        /**
         * Regular expression "getid".
         */
        String GET_ID = "getId";
        /**
         * Regular expression "getValue".
         */
        String GET_VALUE = "getValue";
        /**
         * Regular expression "get" + Pattern + "dataUnit".
         */
        String GET_PATTERN_DATA_UNIT = GET + STRING_PATTERN + "dataUnit";
        /**
         * Regular expression "get" + Pattern + "State".
         */
        String GET_PATTERN_STATE = GET + STRING_PATTERN + STATE;
        /**
         * Regular expression "getTimeStamp".
         */
        String GET_TIME_STAMP = "getTimeStamp";

        /**
         * Empty method for pmd.
         * @return -
         */
        String emptyMethod();
    }

    /**
     * Private Constructor.
     */
    private ConfigureSystem() {
    }

    /**
     * Method tests configurations.
     *
     * @param ontModel The ontology model.
     */
    public void initialTestConfig(final OntModel ontModel) {

        //TODO if null -> list all classes
        //CHECKSTYLE.OFF: MultipleStringLiterals
        // test availability of ontology unit class
        if (ontModel.getOntClass(NS + UNIT_SUPERCLASS) == null) {
            LOGGER.warn("OntClass " + UNIT_SUPERCLASS
                    + " doesn't exist! Wrong String or missing class in Ontology-TBox!");
        }

        // test availability of ontology state class
        if (ontModel.getOntClass(NS + STATE_SUPERCLASS) == null) {
            LOGGER.warn("OntClass " + STATE_SUPERCLASS
                    + " doesn't exist! Wrong String or missing class in Ontology-TBox!");
        }

        // test availability of ontology providerService class
        if (ontModel.getOntClass(NS + PROVIDER_SERVICE_SUPERCLASS) == null) {
            LOGGER.warn("OntClass " + PROVIDER_SERVICE_SUPERCLASS
                    + " doesn't exist! Wrong String or missing class in Ontology-TBox!");
        }
        //CHECKSTYLE.ON: MultipleStringLiterals
    }
}
