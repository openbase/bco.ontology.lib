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

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType;

import java.util.Objects;

/**
 * Created by agatting on 25.10.16.
 */
public class FillOntology {

    private static final Logger LOGGER = LoggerFactory.getLogger(FillOntology.class);
    private static final String NAMESPACE = "http://www.openbase.org/bco/ontology#";
    private final OntModel ontModel;

    /**
     * Constructor for filling ontology model.
     *
     * @param ontModel the ontology model.
     */
    public FillOntology(final OntModel ontModel) {
        this.ontModel = ontModel;
    }

    /**
     * Method fills the given ontology with information (instances).
     */
    public void fillWithIndividuals() {
        LOGGER.info("Start filling ontology...");

        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException ex) {
            ExceptionPrinter.printHistory("Could not start App", ex, System.err);
        }

        try {
            for (final UnitConfigType.UnitConfig config : registry.getUnitConfigs()) {
                //System.out.println(config.getLabel());

                String registryName = config.getType().toString().toLowerCase();
                //TODO: check correct nomination (space character, etc. ... leads to wrong characters in ontology)
                registryName = registryName.replaceAll("_", "");
                //System.out.println(registryName);

                final ExtendedIterator classIterator = ontModel.listClasses();

                while (classIterator.hasNext()) {
                    final OntClass ontClass = (OntClass) classIterator.next();
                    final String className = ontClass.getLocalName().toString().toLowerCase();

                    //System.out.println(className);

                    if (Objects.equals(className, registryName)) {
                        try {
                            //TODO: Handling of not unique unit labels...
                            ontModel.createIndividual(NAMESPACE + config.getLabel(), ontClass);
                        } catch (JenaException jenaException) {
                            LOGGER.error(jenaException.getMessage());
                        }
                    }
                }
            }

        } catch (CouldNotPerformException couldNotPerformException) {
            LOGGER.error(couldNotPerformException.getMessage());
        }
    }
}
