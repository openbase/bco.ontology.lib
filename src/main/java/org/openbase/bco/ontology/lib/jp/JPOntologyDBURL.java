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
 * @author agatting on 22.02.17.
 */
public class JPOntologyDBURL extends AbstractJPString {

    /**
     * Command line argument strings.
     */
    public static final String[] COMMAND_IDENTIFIERS = {"--ontology-db-url"};

    /**
     * Constructor for the JPOntologyDBURL class.
     */
    public JPOntologyDBURL() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected String getPropertyDefaultValue() throws JPNotAvailableException {
        return JPService.getProperty(JPOntologyURL.class).getValue() + "/bco.ontology/";
    }

    @Override
    public String getDescription() {
        return "OntologyDatabaseURL property is used to set the URL to the server, which contains the ontology database.";
    }

}
