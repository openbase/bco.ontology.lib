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
package org.openbase.bco.ontology.lib.jp;

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPString;

/**
 * @author agatting on 01.03.17.
 */
public class JPOntologyPingURL extends AbstractJPString {

    /**
     * Command line argument strings.
     */
    public static final String[] COMMAND_IDENTIFIERS = {"--ontology-server-ping-url"};

    /**
     * Constructor for the JPServerPingURL class.
     */
    public JPOntologyPingURL() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected String getPropertyDefaultValue() throws JPNotAvailableException {
        return JPService.getProperty(JPOntologyURL.class).getValue() + "/$/ping";
    }

    @Override
    public String getDescription() {
        return "ServerPingURL property is used to set the ping URL of the ontology server, which has to be reached.";
    }
}
