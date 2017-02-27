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
package org.openbase.bco.ontology.lib.trigger;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openbase.bco.ontology.lib.config.CategoryConfig.ChangeCategory;
import org.openbase.jul.pattern.Observable;
import rst.domotic.state.ActivationStateType.ActivationState;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TriggerConfig {

    private final String label;
    private final List<ChangeCategory> changeCategoryList;
    private final String query;

    public TriggerConfig(String label, String query, Collection<ChangeCategory> changeCategoryList) {
        this.label = label;
        this.changeCategoryList = new ArrayList(changeCategoryList);
        this.query = query;
    }

    public String getLabel() {
        return label;
    }

    public List<ChangeCategory> getChangeCategory() {
        return Collections.unmodifiableList(changeCategoryList);
    }

    public String getQuery() {
        return query;
    }

    public void testTrigger() throws Exception {
        TriggerImpl trigger = new TriggerImpl(null);
        trigger.init(null);
        trigger.addObserver((Observable<ActivationState.State> source, ActivationState.State data) -> {
            // do useful stuff
        });
    }
}
