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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author agatting on 09.01.17.
 */
public interface RemotePool {

    //TODO exception handling

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(RemotePool.class);

    /**
     * Method provides the unitRemote, which is initialized by unitConfig.
     *
     * @param unitConfig The unitConfig.
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
     * Method provides all unitRemotes, which are initialized by all unitConfigs.
     *
     * @param unitConfigList The unitConfigs.
     * @return A list with unitRemotes.
     */
    static List<UnitRemote> getAllUnitRemotes(final List<UnitConfig> unitConfigList) {

        List<UnitRemote> unitRemoteList = new ArrayList<>();
        UnitRemote unitRemote;

        try {
            for (UnitConfig unitConfig : unitConfigList) {
                unitRemote = UnitRemoteFactoryImpl.getInstance().newInitializedInstance(unitConfig);
                unitRemote.activate();
                unitRemote.waitForData();

                unitRemoteList.add(unitRemote);
            }
        } catch (CouldNotPerformException | InterruptedException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return unitRemoteList;
    }

    /**
     * Method returns a set of methodObjects from an unknown java class by reflection. The set of methodObjects is
     * selected by matches with the delivered regular expression.
     *
     * @param unitRemote Source object (/class) of the methods. Based on the remote to get data class directly.
     * @param regex Regular expression to find the method (method name). Better success if detailed.
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

    /**
     * Method returns a set of methodObjects from an unknown java class by reflection. The set of methodObjects is
     * selected by matches with the delivered regular expression.
     *
     * @param object Source object (/class) of the method. Based on normal object to get class (compare with
     *               method: getMethodObjectsByUnitRemote(final UnitRemote unitRemote, String regex)).
     * @param regex Regular expression to find the method (method name). Better success if detailed.
     * @return HashSet of objects.
     */
    static Object getMethodByClassObject(final Object object, final String regex) {

        String regexBuf = "";

        try {
            if (regex == null) {
                throw new CouldNotPerformException("Regular expression is null! Cannot perform!");
            } else {
                regexBuf = regex.toLowerCase(Locale.ENGLISH);
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        MultiException.ExceptionStack exceptionStack = null;
        Set<Object> objectSet = new HashSet<>();

        try {
            if (object == null) {
                throw new ClassNotFoundException("Class object cannot be found!");
            } else {
                Method[] methodArray = object.getClass().getMethods();
                for (Method method : methodArray) {
                    if (Pattern.matches(regexBuf, method.getName().toLowerCase())) {
                        try {
                            Object objectMethod = object.getClass().getMethod(method.getName()).invoke(object);
                            objectSet.add(objectMethod);
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            exceptionStack = MultiException.push(null, e, exceptionStack);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            exceptionStack = MultiException.push(null, e, null);
        }

        try {
            MultiException.checkAndThrow("Could not process reflection correctly!", exceptionStack);
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return objectSet;
    }
}
