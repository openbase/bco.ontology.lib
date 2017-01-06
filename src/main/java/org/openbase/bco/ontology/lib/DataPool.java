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

import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.UnitRemoteFactoryImpl;
import org.openbase.bco.registry.unit.lib.UnitRegistry;
import org.openbase.bco.registry.unit.remote.CachedUnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.state.EnablingStateType.EnablingState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by agatting on 19.12.16.
 */
public interface DataPool  {

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(Ontology.class);

    /**
     * Method provides the unitRegistry.
     *
     * @return unitRegistry.
     */
    static UnitRegistry getUnitRegistry() {

        UnitRegistry unitRegistry = null;

        try {
            unitRegistry = CachedUnitRegistryRemote.getRegistry();
            CachedUnitRegistryRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitRegistry;
    }

    /**
     * Method returns a list of all unitConfigs, which are actual enabled.
     *
     * @return A list of all enabled unitConfigs.
     */
    static List<UnitConfig> getUnitConfigList() {

        List<UnitConfig> unitConfigList = new ArrayList<>();

        try {
            for (UnitConfig unitConfig : getUnitRegistry().getUnitConfigs()) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)) {
                    unitConfigList.add(unitConfig);
                }
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitConfigList;
    }

    /**
     * Method returns a list of unitConfigs of a specific unitType, which are actual enabled.
     *
     * @param unitType The unitType to presort the list.
     *
     * @return A list of unitConfigs.
     */
    static List<UnitConfig> getUnitConfigListByUnitType(final UnitType unitType) {

        List<UnitConfig> unitConfigList = new ArrayList<>();

        try {
            for (UnitConfig unitConfig : getUnitRegistry().getUnitConfigs(unitType)) {
                if (unitConfig.getEnablingState().getValue().equals(State.ENABLED)) {
                    unitConfigList.add(unitConfig);
                }
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitConfigList;
    }

    /**
     * Method provides the unitRemote, which is initialized by unitConfig.
     *
     * @param unitConfig The unitConfig.
     *
     * @return The unitRemote.
     */
    static UnitRemote getUnitRemoteByUnitConfig(final UnitConfig unitConfig) {

        UnitRemote unitRemote = null;

        try {
            unitRemote = UnitRemoteFactoryImpl.getInstance().newInitializedInstance(unitConfig);
            unitRemote.activate();
            unitRemote.waitForData();
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitRemote;
    }

    /**
     * Method returns a set of methodObjects from an unknown java class by reflection. The set of methodObjects is
     * selected by matches with the delivered regular expression.
     *
     * @param unitRemote Source object (/class) of the methods. Based on the remote to get data class directly.
     * @param regex Regular expression to find the method (method name). Better success if detailed.
     *
     * @return HashSet of objects.
     */
    static Set<Object> getMethodObjectsByUnitRemote(final UnitRemote unitRemote, final String regex) {

        String regexBuf = regex.toLowerCase(Locale.ENGLISH);
        Method[] methodArray = unitRemote.getDataClass().getMethods();
        Set<Object> objectSet = new HashSet<>();

        for (Method method : methodArray) {
            if (Pattern.matches(regexBuf, method.getName().toLowerCase())) {
                try {
                    @SuppressWarnings("unchecked") Object objectMethod = unitRemote.getDataClass()
                            .getMethod(method.getName()).invoke(unitRemote.getData());
                    objectSet.add(objectMethod);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException
                        | NotAvailableException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
            }
        }
        return objectSet;
    }

//    public Remote getRemote() {
//        BrightnessSensorRemote remote = new BrightnessSensorRemote();
//        try {
//            remote.initById("3249a1a5-52d1-4be1-910f-2063974b53f5");
//            remote.activate();
//            remote.waitForData();
//            remote.getBrightnessState().getBrightness();
//
//            //System.out.println(remote.getData().getBrightnessState().getBrightnessDataUnit());
//        } catch (InterruptedException | CouldNotPerformException e) {
//                e.printStackTrace();
//        }
//    }
}
