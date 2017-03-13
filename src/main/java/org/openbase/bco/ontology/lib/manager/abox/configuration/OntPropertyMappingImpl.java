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
package org.openbase.bco.ontology.lib.manager.abox.configuration;

import org.openbase.bco.ontology.lib.manager.OntologyToolkit;
import org.openbase.bco.ontology.lib.system.config.OntConfig.OntProp;
import org.openbase.bco.ontology.lib.manager.sparql.TripleArrayList;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingPropertyTriples(final List<UnitConfig> unitConfigs) {

        final List<TripleArrayList> triples = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigs) {
            triples.addAll(getMissingPropertyTriples(unitConfig));
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getMissingPropertyTriples(final UnitConfig unitConfig) {

        List<TripleArrayList> triples = new ArrayList<>();

        if (unitConfig.getType().equals(UnitType.LOCATION)) {
            triples.addAll(getInsertTripleObjPropHasSubLocation(unitConfig));
            triples.addAll(getInsertTripleObjPropHasUnit(unitConfig));
        } else if (unitConfig.getType().equals(UnitType.CONNECTION)) {
            triples.addAll(getInsertTripleObjPropHasConnection(unitConfig));
        }

        triples.addAll(getInsertTripleObjPropHasState(unitConfig));
        triples.addAll(getInsertTripleDataTypePropHasLabel(unitConfig));
        triples.addAll(getTripleDataTypePropIsEnabled(unitConfig));

        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getDeletePropertyTriples(final List<UnitConfig> unitConfigs) {

        final List<TripleArrayList> triples = new ArrayList<>();

        for (final UnitConfig unitConfig : unitConfigs) {
            triples.addAll(getDeletePropertyTriples(unitConfig));
        }
        return triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TripleArrayList> getDeletePropertyTriples(final UnitConfig unitConfig) {

        List<TripleArrayList> triples = new ArrayList<>();

        if (unitConfig.getType().equals(UnitType.LOCATION)) {
            triples.add(getDeleteTripleObjPropHasSubLocation(unitConfig));
            triples.add(getDeleteTripleObjPropHasUnit(unitConfig));
        } else if (unitConfig.getType().equals(UnitType.CONNECTION)) {
            triples.add(getDeleteTripleObjPropHasConnection(unitConfig));
        }

        triples.add(getDeleteTripleObjPropHasState(unitConfig));
        triples.add(getDeleteTripleDataTypePropHasLabel(unitConfig));
        triples.add(getDeleteTripleDataTypePropIsEnabled(unitConfig));

        return triples;
    }

    private List<TripleArrayList> getInsertTripleObjPropHasSubLocation(final UnitConfig unitConfig) {

        final List<TripleArrayList> triples = new ArrayList<>();
        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = OntProp.SUB_LOCATION.getName();
        String object;

        // get all child IDs of the unit location
        for (final String childId : unitConfig.getLocationConfig().getChildIdList()) {
            object = childId;
            triples.add(new TripleArrayList(subject, predicate, object));
        }

        return triples;
    }

    private TripleArrayList getDeleteTripleObjPropHasSubLocation(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = OntProp.SUB_LOCATION.getName();

        return new TripleArrayList(subject, predicate, null);
    }

    private List<TripleArrayList> getInsertTripleObjPropHasUnit(final UnitConfig unitConfig) {

        final List<TripleArrayList> triples = new ArrayList<>();
        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = OntProp.UNIT.getName();

        // get all unit IDs, which can be found in the unit location
        for (final String obj_unitId : unitConfig.getLocationConfig().getUnitIdList()) {
            triples.add(new TripleArrayList(subject, predicate, obj_unitId));
        }

        return triples;
    }

    private TripleArrayList getDeleteTripleObjPropHasUnit(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = OntProp.UNIT.getName();

        return new TripleArrayList(subject, predicate, null);
    }

    private List<TripleArrayList> getInsertTripleObjPropHasConnection(final UnitConfig unitConfig) {

        final List<TripleArrayList> triples = new ArrayList<>();
        // s, p, o pattern
        final String predicate = OntProp.CONNECTION.getName();
        final String object = unitConfig.getId(); //get unit ID

        // get all tiles, which contains the connection unit
        for (final String subj_tileId : unitConfig.getConnectionConfig().getTileIdList()) {
            triples.add(new TripleArrayList(subj_tileId, predicate, object));
        }
        return triples;
    }

    private TripleArrayList getDeleteTripleObjPropHasConnection(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String predicate = OntProp.CONNECTION.getName();
        final String object = unitConfig.getId(); //get unit ID

        return new TripleArrayList(null, predicate, object);
    }

    private List<TripleArrayList> getInsertTripleObjPropHasState(final UnitConfig unitConfig) {

        final List<TripleArrayList> triples = new ArrayList<>();
        // s, p, o pattern
        final String predicate = OntProp.STATE.getName();
        final String object = unitConfig.getId();

        // get all serviceConfigs of the actual unit
        for (final ServiceConfig serviceConfig : unitConfig.getServiceConfigList()) {

            final String subject = OntologyToolkit.convertToNounSyntax(serviceConfig.getServiceTemplate().getType().name());
            triples.add(new TripleArrayList(subject, predicate, object));
        }
        return triples;
    }

    private TripleArrayList getDeleteTripleObjPropHasState(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String predicate = OntProp.STATE.getName();
        final String object = unitConfig.getId();

        return new TripleArrayList(null, predicate, object);
    }

    @SuppressWarnings("checkstyle:multiplestringliterals")
    private List<TripleArrayList> getInsertTripleDataTypePropHasLabel(final UnitConfig unitConfig) {

        final List<TripleArrayList> triples = new ArrayList<>();
        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = OntProp.LABEL.getName();
        final String object = "\"" + unitConfig.getLabel() + "\""; // dataTypes have quotation marks

        triples.add(new TripleArrayList(subject, predicate, object));

        return triples;
    }

    private TripleArrayList getDeleteTripleDataTypePropHasLabel(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = OntProp.LABEL.getName();

        return new TripleArrayList(subject, predicate, null);
    }

    private List<TripleArrayList> getTripleDataTypePropIsEnabled(final UnitConfig unitConfig) {

        final List<TripleArrayList> triples = new ArrayList<>();
        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = OntProp.IS_ENABLED.getName();
        final String object;

        if (unitConfig.getEnablingState().getValue().equals(EnablingStateType.EnablingState.State.ENABLED)) {
            object = "\"true\""; // dataTypes have quotation marks
        } else {
            object = "\"false\""; // dataTypes have quotation marks
        }

        triples.add(new TripleArrayList(subject, predicate, object));

        return triples;
    }

    private TripleArrayList getDeleteTripleDataTypePropIsEnabled(final UnitConfig unitConfig) {

        // s, p, o pattern
        final String subject = unitConfig.getId();
        final String predicate = OntProp.IS_ENABLED.getName();

        return new TripleArrayList(subject, predicate, null);
    }

}
