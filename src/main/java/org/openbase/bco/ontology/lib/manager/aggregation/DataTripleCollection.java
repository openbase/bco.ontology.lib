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
package org.openbase.bco.ontology.lib.manager.aggregation;

import org.joda.time.DateTime;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ObservationDataCollection;
import org.openbase.bco.ontology.lib.manager.aggregation.datatype.ServiceDataCollection;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author agatting on 01.04.17.
 */
public class DataTripleCollection extends DataAssignation {

    private DateTime dateTimeFrom;
    private DateTime dateTimeUntil;

    public DataTripleCollection(final DateTime dateTimeFrom, final DateTime dateTimeUntil) {
        super(dateTimeFrom, dateTimeUntil);
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeUntil = dateTimeUntil;


        collect();
    }

    private void collect() {
        final DataProviding dataProviding = new DataProviding(dateTimeFrom, dateTimeUntil);

        final HashMap<String, Long> connTimeEachUnit = dataProviding.getConnectionTimeForEachUnit();
        final HashMap<String, List<ObservationDataCollection>> observationsEachUnit = dataProviding.getObservationsForEachUnit();
        final List<TripleArrayList> triples = new ArrayList<>();

        triples.addAll(relateDataForEachUnit(connTimeEachUnit, observationsEachUnit));
        //TODO
    }

    private List<TripleArrayList> relateDataForEachUnit(final HashMap<String, Long> connectionTimePerUnit
            , final HashMap<String, List<ObservationDataCollection>> observationsPerUnit) {

        final List<TripleArrayList> triples = new ArrayList<>();

        for (final String unitId : connectionTimePerUnit.keySet()) {
            final long connectionTimeMilli = connectionTimePerUnit.get(unitId);
            final List<ObservationDataCollection> obsDataCollList = observationsPerUnit.get(unitId);

            triples.addAll(relateDataForEachProviderServiceOfEachUnit(unitId, connectionTimeMilli, obsDataCollList));
        }

        return triples;
    }

    private List<TripleArrayList> relateDataForEachProviderServiceOfEachUnit(final String unitId, final long connectionTimeMilli
            , final List<ObservationDataCollection> obsDataCollList) {

        final HashMap<String, List<ServiceDataCollection>> serviceDataColl = new HashMap<>();

        for (final ObservationDataCollection tripleObs : obsDataCollList) {

            final ServiceDataCollection serviceDataCollection = new ServiceDataCollection(tripleObs.getStateValue(), tripleObs.getDataType(), tripleObs.getTimestamp());

            if (serviceDataColl.containsKey(tripleObs.getProviderService())) {
                // there is an entry: add data
                final List<ServiceDataCollection> arrayList = serviceDataColl.get(tripleObs.getProviderService());
                arrayList.add(serviceDataCollection);
                serviceDataColl.put(tripleObs.getProviderService(), arrayList);
            } else {
                // there is no entry: put data
                final List<ServiceDataCollection> arrayList = new ArrayList<>();
                arrayList.add(serviceDataCollection);
                serviceDataColl.put(tripleObs.getProviderService(), arrayList);
            }
        }

        identifyServiceType(serviceDataColl, connectionTimeMilli, unitId);


        return null; //TODO
    }
}
