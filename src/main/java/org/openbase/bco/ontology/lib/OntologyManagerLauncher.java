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
package org.openbase.bco.ontology.lib;

import org.openbase.bco.ontology.lib.jp.JPOntologyDBURL;
import org.openbase.bco.ontology.lib.jp.JPOntologyMode;
import org.openbase.bco.ontology.lib.jp.JPOntologyPingURL;
import org.openbase.bco.ontology.lib.jp.JPOntologyRSBScope;
import org.openbase.bco.ontology.lib.jp.JPOntologyURL;
import org.openbase.bco.registry.lib.BCO;
import org.openbase.jul.pattern.launch.AbstractLauncher;
import org.openbase.jps.core.JPService;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.InstantiationException;


/**
 * @author agatting on 10.01.17.
 */
public class OntologyManagerLauncher extends AbstractLauncher<OntologyManagerController> {

    /**
     * Constructor for OntologyManagerLauncher.
     *
     * @throws InstantiationException InstantiationException.
     */
    public OntologyManagerLauncher() throws InstantiationException {
        super(OntologyManager.class, OntologyManagerController.class);
    }

    /**
     * JP Environment.
     */
    @Override
    protected void loadProperties() {
        JPService.registerProperty(JPOntologyURL.class);
        JPService.registerProperty(JPOntologyDBURL.class);
        JPService.registerProperty(JPOntologyPingURL.class);
        JPService.registerProperty(JPOntologyRSBScope.class);
        JPService.registerProperty(JPOntologyMode.class);
        JPService.registerProperty(JPDebugMode.class);
    }

    /**
     * Main Method starting ontology application.
     *
     * @param args Arguments from commandline.
     * @throws Throwable Throwable.
     */
    public static void main(final String... args) throws Throwable {
        BCO.printLogo();
        main(args, OntologyManager.class, OntologyManagerLauncher.class);
    }

}
