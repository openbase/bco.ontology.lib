package org.openbase.bco.ontology.lib.jp;

import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPBoolean;

/**
 * @author agatting on 06.06.17.
 */
public class JPOntologyMode extends AbstractJPBoolean {

    /**
     * Command line argument strings.
     */
    public static final String[] COMMAND_IDENTIFIERS = {"--ontology-mode"};

    /**
     * Constructor for the JPOntologyMode class.
     */
    public JPOntologyMode() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected Boolean getPropertyDefaultValue() throws JPNotAvailableException {
        return false;
    }

    @Override
    public String getDescription() {
        return "Mode property is used to set the mode, which affects the state value range of the ontology. If bool property is true, state " +
                "values over time are stored in the ontology. Otherwise (DEFAULT false) current state values are stored only.";
    }
}
