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

import javafx.util.Pair;
import org.joda.time.DateTime;
import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.ontology.lib.commun.web.SparqlUpdateWeb;
import org.openbase.bco.ontology.lib.manager.buffer.TransactionBuffer;
import org.openbase.bco.ontology.lib.manager.sparql.SparqlUpdateExpression;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
import org.openbase.bco.ontology.lib.system.config.OntConfig;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.Remote.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.ActivationStateType.ActivationState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author agatting on 16.03.17.
 */
public class ConnectionPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPhase.class);
//    private final SimpleDateFormat dateFormat;
    private final String remoteUnitId;
    private String subj_CurConnectionPhase;
    private boolean wasConnected;
    private final TransactionBuffer transactionBuffer;

    public ConnectionPhase(final UnitRemote unitRemote, final TransactionBuffer transactionBuffer) throws JPServiceException, NotAvailableException {

//        this.dateFormat = new SimpleDateFormat(OntConfig.DATE_TIME, Locale.getDefault());
        this.remoteUnitId = unitRemote.getId().toString();
        this.transactionBuffer = transactionBuffer;

        initConnectionState(unitRemote);

    }

    public void identifyConnection(final ConnectionState connectionState) throws JPServiceException {
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

    private void initConnectionState(final UnitRemote unitRemote) throws JPServiceException {
        // reduce connectionState to binary classification - connected and not connected
        if (unitRemote.getConnectionState().equals(ConnectionState.CONNECTED)) {
            wasConnected = true;
            updateConnectionPhase(ActivationState.State.ACTIVE);
        } else {
            wasConnected = false;
        }
    }

    private void updateConnectionPhase(final ActivationState.State activationState) throws JPServiceException {

        final String pred_IsA = OntConfig.OntExpr.A.getName();
        final String pred_HasFirstConnection = OntConfig.OntProp.FIRST_CONNECTION.getName();
        final String pred_HasLastConnection = OntConfig.OntProp.LAST_CONNECTION.getName();
        final String pred_HasConnectionPhase = OntConfig.OntProp.CONNECTION_PHASE.getName();
        final String obj_ConnectionPhase = OntConfig.OntCl.CONNECTION_PHASE.getName();
        final String obj_RecentHeartBeat = OntConfig.INSTANCE_RECENT_HEARTBEAT;

        final List<TripleArrayList> insertTriples = new ArrayList<>();
        final List<TripleArrayList> whereTriples = new ArrayList<>();

        if (activationState.equals(ActivationState.State.ACTIVE)) {
//            final Date now = new Date();
//            final String dateTime = dateFormat.format(now);
            final String dateTime = new DateTime().toString();
            subj_CurConnectionPhase = "connectionPhase" + remoteUnitId + dateTime.substring(0, dateTime.indexOf("+")); // must be the same at start and close!
            final String obj_Timestamp = "\"" + dateTime + "\"^^xsd:dateTime";

            insertTriples.add(new TripleArrayList(subj_CurConnectionPhase, pred_IsA, obj_ConnectionPhase));
            insertTriples.add(new TripleArrayList(remoteUnitId, pred_HasConnectionPhase, subj_CurConnectionPhase));
            insertTriples.add(new TripleArrayList(subj_CurConnectionPhase, pred_HasFirstConnection, obj_Timestamp));
            insertTriples.add(new TripleArrayList(subj_CurConnectionPhase, pred_HasLastConnection, obj_RecentHeartBeat));

            final String sparqlUpdate = SparqlUpdateExpression.getSparqlUpdateInsertBundleExpr(insertTriples);
            sendToServer(transactionBuffer, sparqlUpdate);

        } else if (activationState.equals(ActivationState.State.DEACTIVE)) {

            final String obj_Timestamp = "\"" + new DateTime().toString() + "\"^^xsd:dateTime";
//            insertTriple.add(new TripleArrayList(subj_CurConnectionPhase, pred_IsA, obj_ConnectionPhase));
//            insertTriple.add(new TripleArrayList(remoteUnitId, pred_HasConnectionPhase, subj_CurConnectionPhase));
            insertTriples.add(new TripleArrayList(subj_CurConnectionPhase, pred_HasLastConnection, obj_Timestamp));

            whereTriples.add(new TripleArrayList(subj_CurConnectionPhase, pred_HasFirstConnection, null));

            final String sparqlUpdate = SparqlUpdateExpression.getSparqlUpdateInsertWhereBundleExpr(insertTriples, whereTriples);
            sendToServer(transactionBuffer, sparqlUpdate);

        } else {
            LOGGER.warn("Method updateConnectionPhase is called with wrong ActivationState parameter.");
        }
    }

    boolean sendToServer(final TransactionBuffer transactionBuffer, final String sparqlUpdateExpr) throws JPServiceException {
        try {
            final boolean isHttpSuccess = SparqlUpdateWeb.sparqlUpdateToMainOntology(sparqlUpdateExpr, OntConfig.ServerServiceForm.UPDATE);

            if (!isHttpSuccess) {
                // could not send to server - insert sparql update expression to buffer queue
                transactionBuffer.insertData(new Pair<>(sparqlUpdateExpr, false));
            }
            return isHttpSuccess;
        } catch (CouldNotPerformException e) {
            // could not send to server - insert sparql update expression to buffer queue
            transactionBuffer.insertData(new Pair<>(sparqlUpdateExpr, false));
        }
        return false;
    }
}
