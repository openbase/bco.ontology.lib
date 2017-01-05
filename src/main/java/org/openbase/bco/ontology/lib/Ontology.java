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

import org.openbase.bco.ontology.lib.aboxsynchronisation.configuration.OntInstanceMapping;
import org.openbase.bco.ontology.lib.aboxsynchronisation.configuration.OntPropertyInitMapping;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Created by agatting on 20.10.16.
 */
public final class Ontology {

    /**
     * App name.
     */
    private static final String APP_NAME = Ontology.class.getSimpleName() + "App";
    private static final Logger LOGGER = LoggerFactory.getLogger(Ontology.class);

    private Ontology() { }

    /**
     * Main Method starting ontology application.
     *
     * @param args Arguments from commandline.
     */
    public static void main(final String... args) {

        LOGGER.info("Start " + APP_NAME + " ...");

        final CreateOntology ontology = new CreateOntology();
        ontology.loadOntology("src/Ontology3.owl");

//        WebInterface webInterface = new WebInterface();
        final OntInstanceMapping ontInstancesMapping = new OntInstanceMapping(ontology.getModel());

        final OntPropertyInitMapping ontPropertyInitMapping = new OntPropertyInitMapping();
//        RSBTest rsbTest = new RSBTest();

//        ontology.cleanOntology();
//        final FillOntology fillOntology = new FillOntology(ontology.getModel());
//        fillOntology.integrateIndividualUnitTypes(true);
//        fillOntology.integrateIndividualStateValues();
//        fillOntology.integrateObjectProperties();
//        fillOntology.observer();
//        ontology.saveOntology();

//        final QueryOntology queryOntology = new QueryOntology(ontology.getModel());
//        queryOntology.queryModel();

        ontology.getModel().close();
        if (ontology.getModel().isClosed()) {
            LOGGER.info(APP_NAME + " finished!");
            System.exit(0);
        }
    }
}
