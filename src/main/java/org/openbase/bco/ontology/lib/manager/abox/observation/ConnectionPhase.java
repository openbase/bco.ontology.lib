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

import org.joda.time.DateTime;
import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.ontology.lib.commun.web.SparqlHttp;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.utility.RdfTriple;
import org.openbase.bco.ontology.lib.utility.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.ActivationStateType.ActivationState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 16.03.17.
 */
public class ConnectionPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPhase.class);
//    private final SimpleDateFormat dateFormat;
    private final String remoteUnitId;
    private String subj_CurConnectionPhase;
    private boolean wasConnected;

    public ConnectionPhase(final UnitRemote unitRemote) throws NotAvailableException {

//        this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());
        this.remoteUnitId = unitRemote.getId().toString();

        initConnectionState(unitRemote);
    }

    public void identifyConnection(final ConnectionState connectionState) throws NotAvailableException {
        if (connectionState.equals(ConnectionState.CONNECTED) && !wasConnected) {
            // was NOT connected and now is connected - start connection phase
            updateConnectionPhase(ActivationState.State.ACTIVE);
            wasConnected = !wasConnected;
        } else if (!connectionState.equals(ConnectionState.CONNECTED) && wasConnected){
            // was connected and now is NOT connected - close connection phase
            updateConnectionPhase(ActivationState.State.DEACTIVE);
            wasConnected = !wasConnected;
        }
    }

    private void initConnectionState(final UnitRemote unitRemote) throws NotAvailableException {
        // reduce connectionState to binary classification - connected and not connected
        if (unitRemote.getConnectionState().equals(ConnectionState.CONNECTED)) {
            wasConnected = true;
            updateConnectionPhase(ActivationState.State.ACTIVE);
        } else {
            wasConnected = false;
        }
    }

    private void updateConnectionPhase(final ActivationState.State activationState) throws NotAvailableException {

        final String pred_IsA = OntConfig.OntExpr.IS_A.getName();
        final String pred_HasFirstConnection = OntConfig.OntProp.FIRST_CONNECTION.getName();
        final String pred_HasLastConnection = OntConfig.OntProp.LAST_CONNECTION.getName();
        final String pred_HasConnectionPhase = OntConfig.OntProp.CONNECTION_PHASE.getName();
        final String obj_ConnectionPhase = OntConfig.OntCl.CONNECTION_PHASE.getName();
        final String obj_RecentHeartBeat = OntConfig.INSTANCE_RECENT_HEARTBEAT;

        final List<RdfTriple> insertTriples = new ArrayList<>();
        final List<RdfTriple> whereTriples = new ArrayList<>();

        if (activationState.equals(ActivationState.State.ACTIVE)) {
//            final Date now = new Date();
//            final String dateTime = dateFormat.format(now);
            final String dateTime = new DateTime().toString();
            subj_CurConnectionPhase = "connectionPhase" + remoteUnitId + dateTime.substring(0, dateTime.indexOf("+")); // must be the same at start and close!
            final String obj_Timestamp = "\"" + dateTime + "\"^^xsd:dateTime";

            insertTriples.add(new RdfTriple(subj_CurConnectionPhase, pred_IsA, obj_ConnectionPhase));
            insertTriples.add(new RdfTriple(remoteUnitId, pred_HasConnectionPhase, subj_CurConnectionPhase));
            insertTriples.add(new RdfTriple(subj_CurConnectionPhase, pred_HasFirstConnection, obj_Timestamp));
            insertTriples.add(new RdfTriple(subj_CurConnectionPhase, pred_HasLastConnection, obj_RecentHeartBeat));

            final String sparqlUpdate = SparqlUpdateExpression.getSparqlUpdateExpression(insertTriples);
            sendToServer(sparqlUpdate);

        } else if (activationState.equals(ActivationState.State.DEACTIVE)) {

            final String obj_Timestamp = "\"" + new DateTime().toString() + "\"^^xsd:dateTime";
//            insertTriple.add(new RdfTriple(subj_CurConnectionPhase, pred_IsA, obj_ConnectionPhase));
//            insertTriple.add(new RdfTriple(remoteUnitId, pred_HasConnectionPhase, subj_CurConnectionPhase));
            insertTriples.add(new RdfTriple(subj_CurConnectionPhase, pred_HasLastConnection, obj_Timestamp));

            whereTriples.add(new RdfTriple(subj_CurConnectionPhase, pred_HasFirstConnection, null));

            final String sparqlUpdate = SparqlUpdateExpression.getSparqlUpdateExpression(insertTriples, whereTriples);
            sendToServer(sparqlUpdate);

        } else {
            LOGGER.warn("Method updateConnectionPhase is called with wrong ActivationState parameter.");
        }
    }

    boolean sendToServer(final String sparql) {
        try {
            SparqlHttp.uploadSparqlRequest(sparql, OntConfig.ONTOLOGY_DB_URL);
            return true;
        } catch (IOException e) {
            // could not send to server - insert sparql update expression to buffer queue
            TransactionBuffer.insertData(sparql);
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory("At least one element is null or whole update string is bad!", e, LOGGER, LogLevel.ERROR);
        }
        return false;
    }
}
