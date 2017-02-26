package org.openbase.bco.ontology.lib.trigger;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openbase.jul.pattern.Observable;
import rst.domotic.state.ActivationStateType;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TriggerConfig {

    public enum ChangeCategory {
        UNKNOWN,
        A,
        B,
        C
    }

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
        trigger.addObserver((Observable<ActivationStateType.ActivationState.State> source, ActivationStateType.ActivationState.State data) -> {
            // do useful stuff
        });
    }
}
