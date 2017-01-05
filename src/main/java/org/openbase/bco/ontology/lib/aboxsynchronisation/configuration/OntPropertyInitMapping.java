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
package org.openbase.bco.ontology.lib.aboxsynchronisation.configuration;

import org.openbase.bco.ontology.lib.ConfigureSystem;
import org.openbase.bco.ontology.lib.TripleArrayList;
import rst.domotic.service.ServiceConfigType.ServiceConfig;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by agatting on 21.12.16.
 */
public class OntPropertyInitMapping extends OntInstanceInspection {

    /**
     * Constructor for OntPropertyInitMapping.
     */
    public OntPropertyInitMapping() {
        super();

        ontPropInitMapping();
    }

    private void ontPropInitMapping() {

        List<TripleArrayList> tripleArrayInsertLists = new ArrayList<>();
        TripleArrayList tripleArrayDeleteList;

        for (final UnitConfig unitConfig : getUnitConfigListByUnitType(UnitType.LOCATION)) {
            //TODO delete triple expression (s?)
//            tripleArrayDeleteList = new TripleArrayList(unitConfig.getId(), ConfigureSystem.OntProp.SUB_LOCATION.getName(), null);
            tripleArrayInsertLists = getTripleObjPropHasSubLocation(tripleArrayInsertLists, unitConfig);
            //TODO delete triple expression (s?)
            tripleArrayInsertLists = getTripleObjPropHasUnit(tripleArrayInsertLists, unitConfig);
        }

        for (final UnitConfig unitConfig : getUnitConfigListByUnitType(UnitType.CONNECTION)) {
            //TODO delete triple expression (o?)
            tripleArrayInsertLists = getTripleObjPropHasConnection(tripleArrayInsertLists, unitConfig);

        }

        for (final UnitConfig unitConfig : getUnitConfigList()) {
            //TODO delete triple expression (s?)
            tripleArrayInsertLists = getTripleObjPropHasState(tripleArrayInsertLists, unitConfig);
            //TODO delete triple expression (s?)
            tripleArrayInsertLists= getTripleDataTypePropHasLabel(tripleArrayInsertLists, unitConfig);
        }


    }

    private List<TripleArrayList> getTripleObjPropHasSubLocation(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.SUB_LOCATION.getName();
        String object;

        // get all child IDs of the unit location
        for (final String childId : unitConfig.getLocationConfig().getChildIdList()) {
            object = childId;
            tripleArrayLists.add(new TripleArrayList(subject, predicate, object));
        }

        return tripleArrayLists;
    }

    private List<TripleArrayList> getTripleObjPropHasUnit(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.UNIT.getName();
        String object;

        // get all unit IDs, which can be found in the unit location
        for (final String unitId : unitConfig.getLocationConfig().getUnitIdList()) {
            object = unitId;
            tripleArrayLists.add(new TripleArrayList(subject, predicate, object));
        }

        return tripleArrayLists;
    }

    private List<TripleArrayList> getTripleObjPropHasConnection(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        // s, p, o pattern
        String subject;
        final String predicate = ConfigureSystem.OntProp.CONNECTION.getName();
        final String object = unitConfig.getId(); //get unit ID

        // get all tiles, which contains the connection unit
        for (final String tileId : unitConfig.getConnectionConfig().getTileIdList()) {
            subject = tileId;
            tripleArrayLists.add(new TripleArrayList(subject, predicate, object));
        }

        return tripleArrayLists;
    }

    private List<TripleArrayList> getTripleObjPropHasState(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        // s, p, o pattern
        String subject;
        final String predicate = ConfigureSystem.OntProp.STATE.getName();
        final String object = unitConfig.getId();

        // get all serviceConfigs of the actual unit
        for (final ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {
            subject = serviceConfig.getServiceTemplate().getType().toString();
            tripleArrayLists.add(new TripleArrayList(subject, predicate, object));
        }

        return tripleArrayLists;
    }

    private List<TripleArrayList> getTripleDataTypePropHasLabel(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        // s, p, o pattern
        String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.LABEL.getName();
        final String object = "\"" + unitConfig.getLabel() + "\""; // dataTypes have quotation marks

        tripleArrayLists.add(new TripleArrayList(subject, predicate, object));

        return tripleArrayLists;
    }

    //TODO isAvailable
    private List<TripleArrayList> getTripleDataTypePropIsAvailable(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        // s, p, o pattern
        String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.IS_AVAILABLE.getName();
        final String object = "\"true\""; // dataTypes have quotation marks

        tripleArrayLists.add(new TripleArrayList(subject, predicate, object));

        return tripleArrayLists;
    }

}
