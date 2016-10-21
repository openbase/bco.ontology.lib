package org.openbase.bco.ontology.lib;

import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import rst.domotic.unit.UnitConfigType;

/**
 * Created by agatting on 20.10.16.
 */
public class Ontology {
    public static void main(String[] args) {

        UnitRegistry registry = null;
        try {
            registry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException ex) {
            ExceptionPrinter.printHistory("Could not start App", ex,System.err);
        }

        try {
            for(UnitConfigType.UnitConfig config : registry.getUnitConfigs()) {
                System.out.println(config.getLabel());
            }

        } catch (CouldNotPerformException e) {
            e.printStackTrace();
        }

    }
}
