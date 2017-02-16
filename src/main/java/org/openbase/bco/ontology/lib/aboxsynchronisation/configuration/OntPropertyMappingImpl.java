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
import org.openbase.bco.ontology.lib.sparql.TripleArrayList;
import rst.domotic.service.ServiceConfigType.ServiceConfig;
import rst.domotic.state.EnablingStateType;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author agatting on 21.12.16.
 */
public class OntPropertyMappingImpl implements OntPropertyMapping {

    //TODO exception handling
    //TODO reduce methods params: .addAll

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getPropertyTripleOfUnitConfigs(final List<UnitConfig> unitConfigList) {

        final List<TripleArrayList> tripleArrayInsertLists = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigList) {
            tripleArrayInsertLists.addAll(getPropertyTripleOfSingleUnitConfig(unitConfig));
        }

        return tripleArrayInsertLists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getPropertyTripleOfSingleUnitConfig(final UnitConfig unitConfig) {

        List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        if (unitConfig.getType().equals(UnitType.LOCATION)) {
            tripleArrayLists = getInsertTripleObjPropHasSubLocation(tripleArrayLists, unitConfig);
            tripleArrayLists = getInsertTripleObjPropHasUnit(tripleArrayLists, unitConfig);
        } else if (unitConfig.getType().equals(UnitType.CONNECTION)) {
            tripleArrayLists = getInsertTripleObjPropHasConnection(tripleArrayLists, unitConfig);
        }

        tripleArrayLists = getInsertTripleObjPropHasState(tripleArrayLists, unitConfig);
        tripleArrayLists = getInsertTripleDataTypePropHasLabel(tripleArrayLists, unitConfig);
        tripleArrayLists = getTripleDataTypePropIsEnabled(tripleArrayLists, unitConfig);

        return tripleArrayLists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getPropertyDeleteTripleOfUnitConfigs(final List<UnitConfig> unitConfigList) {

        final List<TripleArrayList> tripleArrayDeleteLists = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigList) {
            tripleArrayDeleteLists.addAll(getPropertyDeleteTripleOfSingleUnitConfig(unitConfig));
        }

        return tripleArrayDeleteLists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getPropertyDeleteTripleOfSingleUnitConfig(final UnitConfig unitConfig) {

        List<TripleArrayList> tripleArrayLists = new ArrayList<>();

        if (unitConfig.getType().equals(UnitType.LOCATION)) {
            tripleArrayLists.add(getDeleteTripleObjPropHasSubLocation(unitConfig));
            tripleArrayLists.add(getDeleteTripleObjPropHasUnit(unitConfig));
        } else if (unitConfig.getType().equals(UnitType.CONNECTION)) {
            tripleArrayLists.add(getDeleteTripleObjPropHasConnection(unitConfig));
        }

        tripleArrayLists.add(getDeleteTripleObjPropHasState(unitConfig));
        tripleArrayLists.add(getDeleteTripleDataTypePropHasLabel(unitConfig));
        tripleArrayLists.add(getDeleteTripleDataTypePropIsEnabled(unitConfig));

        return tripleArrayLists;
    }

    private List<TripleArrayList> getInsertTripleObjPropHasSubLocation(final List<TripleArrayList> tripleArrayLists
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

    private TripleArrayList getDeleteTripleObjPropHasSubLocation(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.SUB_LOCATION.getName();

        return new TripleArrayList(subject, predicate, null);
    }

    private List<TripleArrayList> getInsertTripleObjPropHasUnit(final List<TripleArrayList> tripleArrayLists
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

    private TripleArrayList getDeleteTripleObjPropHasUnit(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.UNIT.getName();

        return new TripleArrayList(subject, predicate, null);
    }

    private List<TripleArrayList> getInsertTripleObjPropHasConnection(final List<TripleArrayList> tripleArrayLists
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

    private TripleArrayList getDeleteTripleObjPropHasConnection(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String predicate = ConfigureSystem.OntProp.CONNECTION.getName();
        final String object = unitConfig.getId(); //get unit ID

        return new TripleArrayList(null, predicate, object);
    }

    private List<TripleArrayList> getInsertTripleObjPropHasState(final List<TripleArrayList> tripleArrayLists
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

    private TripleArrayList getDeleteTripleObjPropHasState(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String predicate = ConfigureSystem.OntProp.STATE.getName();
        final String object = unitConfig.getId();

        return new TripleArrayList(null, predicate, object);
    }

    @SuppressWarnings("checkstyle:multiplestringliterals")
    private List<TripleArrayList> getInsertTripleDataTypePropHasLabel(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.LABEL.getName();
        final String object = "\"" + unitConfig.getLabel() + "\""; // dataTypes have quotation marks

        tripleArrayLists.add(new TripleArrayList(subject, predicate, object));

        return tripleArrayLists;
    }

    private TripleArrayList getDeleteTripleDataTypePropHasLabel(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.LABEL.getName();

        return new TripleArrayList(subject, predicate, null);
    }

    //TODO isAvailable
    private List<TripleArrayList> getTripleDataTypePropIsEnabled(final List<TripleArrayList> tripleArrayLists
            , final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.IS_ENABLED.getName();
        final String object;

        if (unitConfig.getEnablingState().getValue().equals(EnablingStateType.EnablingState.State.ENABLED)) {
            object = "\"true\""; // dataTypes have quotation marks
        } else {
            object = "\"false\""; // dataTypes have quotation marks
        }

        tripleArrayLists.add(new TripleArrayList(subject, predicate, object));

        return tripleArrayLists;
    }

    private TripleArrayList getDeleteTripleDataTypePropIsEnabled(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = ConfigureSystem.OntProp.IS_ENABLED.getName();

        return new TripleArrayList(subject, predicate, null);
    }

}
