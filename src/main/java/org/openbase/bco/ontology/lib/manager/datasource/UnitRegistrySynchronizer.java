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
package org.openbase.bco.ontology.lib.manager.datasource;

import org.openbase.bco.ontology.lib.OntologyManagerController;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntRelationMappingImpl;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.tbox.OntClassMapping;
import org.openbase.bco.ontology.lib.manager.tbox.OntClassMappingImpl;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntInstanceMapping;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntInstanceMappingImpl;
import org.openbase.bco.ontology.lib.manager.abox.configuration.OntRelationMapping;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 18.01.17.
 */
public class UnitRegistrySynchronizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitRegistrySynchronizer.class);

    private final OntClassMapping ontClassMapping;
    private final OntInstanceMapping ontInstanceMapping;
    private final OntRelationMapping ontRelationMapping;

    private final Observer<List<UnitConfig>> newUnitConfigObserver;
    private final Observer<List<UnitConfig>> updatedUnitConfigObserver;
    private final Observer<List<UnitConfig>> removedUnitConfigObserver;

    /**
     * Constructor for UnitRegistrySynchronizer.
     */
    public UnitRegistrySynchronizer() {
        this.ontClassMapping = new OntClassMappingImpl();
        this.ontInstanceMapping = new OntInstanceMappingImpl();
        this.ontRelationMapping = new OntRelationMappingImpl();

        this.newUnitConfigObserver = (source, unitConfigs) -> newUnitConfigData(unitConfigs);
        this.updatedUnitConfigObserver = (source, unitConfigs) -> updatedUnitConfigData(unitConfigs);
        this.removedUnitConfigObserver = (source, unitConfigs) -> removedUnitConfigData(unitConfigs);

        rstConfigData();

        OntologyManagerController.NEW_UNIT_CONFIG_OBSERVABLE.addObserver(newUnitConfigObserver);
        OntologyManagerController.UPDATED_UNIT_CONFIG_OBSERVABLE.addObserver(updatedUnitConfigObserver);
//        OntologyManagerController.REMOVED_UNIT_CONFIG_OBSERVABLE.addObserver(removedUnitConfigObserver);
    }

    private void rstConfigData() {
        final List<RdfTriple> insertTriples = new ArrayList<>();

        // rst information - insert one time
        // insert tbox
        insertTriples.addAll(ontClassMapping.getUnitTypeClasses());
        // insert instances
        insertTriples.addAll(ontInstanceMapping.getInsertStateAndServiceAndValueInstances());
        // insert relations
        insertTriples.addAll(ontRelationMapping.getInsertStateRelations(null));

        // convert to sparql expression and upload...or save in buffer, if no server connection
        transformAndSynchronize(null, insertTriples);
        LOGGER.info("Uploaded rst config data successfully.");
    }

    private void newUnitConfigData(final List<UnitConfig> unitConfigs) throws InstantiationException {

        final List<RdfTriple> insertTriples = new ArrayList<>();

        // insert instances
        insertTriples.addAll(ontInstanceMapping.getInsertUnitInstances(unitConfigs));
        // insert relations
        insertTriples.addAll(ontRelationMapping.getInsertUnitRelations(unitConfigs));

        // convert to sparql expression and upload...or save in buffer, if no server connection
        transformAndSynchronize(null, insertTriples);
        LOGGER.info("Uploaded unit config data successfully.");
    }

    private void updatedUnitConfigData(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> deleteTriples = new ArrayList<>();
        final List<RdfTriple> insertTriples = new ArrayList<>();

        // delete unit instances
        deleteTriples.addAll(ontInstanceMapping.getDeleteUnitInstances(unitConfigs));
        // delete unit properties
        deleteTriples.addAll(ontRelationMapping.getDeleteUnitRelations(unitConfigs));

        // insert instances
        insertTriples.addAll(ontInstanceMapping.getInsertConfigInstances(unitConfigs));
        // insert properties
        insertTriples.addAll(ontRelationMapping.getInsertUnitRelations(unitConfigs));

        // convert to sparql expression and upload...or save in buffer, if no server connection
        transformAndSynchronize(deleteTriples, insertTriples);
        LOGGER.info("Uploaded updated unit config data successfully.");
    }

    private void removedUnitConfigData(final List<UnitConfig> unitConfigs) {

        final List<RdfTriple> deleteTriples = new ArrayList<>();

        // delete unit instances
        deleteTriples.addAll(ontInstanceMapping.getDeleteUnitInstances(unitConfigs));
        // delete unit properties
        deleteTriples.addAll(ontRelationMapping.getDeleteUnitRelations(unitConfigs));

        //convert to sparql expression and upload...or save, if no server connection
        transformAndSynchronize(deleteTriples, null);
        LOGGER.info("Uploaded removed unit config data successfully.");
    }

    private void transformAndSynchronize(final List<RdfTriple> delete, final List<RdfTriple> insert) {
        String sparql = "";

        try {
            if (delete == null) {
                // convert triples to sparql update expression (insert)
                sparql = SparqlUpdateExpression.getSparqlInsertExpression(insert);
            } else if (insert == null) {
                // convert triples to sparql update expression (delete)
                sparql = SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, null, null);
            } else {
                // convert triples to sparql update expression (delete and insert)
                sparql = SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, insert, null);
            }

            // upload to ontology server
            SparqlHttp.uploadSparqlRequest(sparql, OntConfig.getOntologyDbUrl());
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        } catch (IOException ex) {
            TransactionBuffer.insertData(sparql);
        }
    }

}
