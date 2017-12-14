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
package org.openbase.bco.ontology.lib.manager.abox.observation;

import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.utility.sparql.RdfTriple;
import org.openbase.bco.ontology.lib.utility.StringModifier;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.system.config.OntConfig.XsdType;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntPrefix;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntExpr;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntCl;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntInst;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 16.03.17.
 */
public class ConnectionPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPhase.class);
    private final String unitId;
    private String connectionPhaseInst;
    private boolean isConnected;
    private boolean isSetConnectionPhaseSuccess;

    /**
     * Constructor initiates the connectionPhase of an unit.
     *
     * @param unitRemote is the unitRemote to identify the unit.
     * @throws NotAvailableException is thrown in case the id of the unitRemote is not available.
     */
    public ConnectionPhase(final UnitRemote unitRemote) throws NotAvailableException {
        this.unitId = unitRemote.getId().toString();
        this.isSetConnectionPhaseSuccess = false;
        this.connectionPhaseInst = null;

        initConnectionPhase(unitRemote);
    }

    private void initConnectionPhase(final UnitRemote unitRemote) {
        if (unitRemote.getConnectionState().equals(ConnectionState.CONNECTED)) {
            setConnectionPhase();
            this.isConnected = true;
        } else {
            this.isConnected = false;
        }
    }

    void identifyConnectionState(final ConnectionState connectionState) {
        switch (connectionState) {
            case CONNECTED:
                if (!isConnected) {
                    setConnectionPhase();
                    isConnected = true;
                }
                break;
            default:
                if (isConnected) {
                    closeConnectionPhase();
                    isConnected = false;
                }
        }
    }

    private void setConnectionPhase() {
        try {
            final List<RdfTriple> insert = new ArrayList<>();
            final LocalDateTime dateTime = LocalDateTime.now();
            final String dateTimeString = OffsetDateTime.of(dateTime, OffsetDateTime.now().getOffset()).toString();
            final String timestampLiteral = StringModifier.convertToLiteral(dateTimeString, XsdType.DATE_TIME);

            connectionPhaseInst = OntPrefix.CONNECTION_PHASE.getName() + unitId + dateTime.toString();

            insert.add(new RdfTriple(connectionPhaseInst, OntExpr.IS_A.getName(), OntCl.CONNECTION_PHASE.getName()));
            insert.add(new RdfTriple(connectionPhaseInst, OntProp.FIRST_CONNECTION.getName(), timestampLiteral));
            insert.add(new RdfTriple(connectionPhaseInst, OntProp.LAST_CONNECTION.getName(), OntInst.RECENT_HEARTBEAT.getName()));
            insert.add(new RdfTriple(unitId, OntProp.CONNECTION_PHASE.getName(), connectionPhaseInst));

            isSetConnectionPhaseSuccess = SparqlHttp.uploadSparqlRequest(SparqlUpdateExpression.getSparqlInsertExpression(insert));
        } catch (NotAvailableException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }

    private void closeConnectionPhase() {
        try {
            if (connectionPhaseInst == null) {
                assert false;
                throw new NotAvailableException("Tried to close a connectionPhase, which doesn't exist!");
            }

            final List<RdfTriple> delete = new ArrayList<>();
            final List<RdfTriple> insert = new ArrayList<>();
            final List<RdfTriple> where = new ArrayList<>();
            final String timestampLiteral = StringModifier.convertToLiteral(OffsetDateTime.now().toString(), XsdType.DATE_TIME);

            delete.add(new RdfTriple(connectionPhaseInst, OntProp.LAST_CONNECTION.getName(), OntInst.RECENT_HEARTBEAT.getName()));
            insert.add(new RdfTriple(connectionPhaseInst, OntProp.LAST_CONNECTION.getName(), timestampLiteral));
            where.add(new RdfTriple(connectionPhaseInst, OntProp.LAST_CONNECTION.getName(), OntInst.RECENT_HEARTBEAT.getName()));

            // there should be only an "close-update", if there was a "set-update". If "set-update" failed (no connection - transactionBuffer) the "close-update"
            // must be insert to the transactionBuffer too (order of updates).
            if (isSetConnectionPhaseSuccess) {
                SparqlHttp.uploadSparqlRequest(SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, insert, where));
            } else {
                TransactionBuffer.insertData(SparqlUpdateExpression.getConnectedSparqlUpdateExpression(delete, insert, where));
            }

            connectionPhaseInst = null;
        } catch (NotAvailableException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }
}
