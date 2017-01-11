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
package org.openbase.bco.ontology.lib.datapool;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author agatting on 11.01.17.
 */
public interface ReflectObjectPool {

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(ReflectObjectPool.class);

    /**
     * * Method returns a set of methodObjects from an unknown java class by reflection. The set of methodObjects is
     * selected by matches with the delivered regular expression.
     *
     * @param object Source object (/class) of the method. Based on normal object to get the class.
     * @param regExStartsWith Beginning expression part of the method name.
     * @param regExEndsWith Ending expression part of the method name.
     * @return HashSet of objects.
     */
    static Set<Object> getMethodByClassObject(final Object object, final String regExStartsWith,
                                              final String regExEndsWith) {

        final String regExStartsWithBuf = convertRegExToLowerCase(regExStartsWith);
        final String regExEndsWithBuf = convertRegExToLowerCase(regExEndsWith);

        MultiException.ExceptionStack exceptionStack = null;
        Set<Object> objectSet = new HashSet<>();

        try {
            if (object == null) {
                throw new ClassNotFoundException("Parameter object is null!");
            } else {
                Method[] methodArray = object.getClass().getMethods();

                for (Method method : methodArray) {
                    final String methodName = method.getName().toLowerCase();

                    if (methodName.startsWith(regExStartsWithBuf) && methodName.endsWith(regExEndsWithBuf)) {
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

    /**
     * Method returns a set of methodObjects from an unknown java class by reflection. The set of methodObjects is
     * selected by matches with the delivered regular expression.
     *
     * @param object Source object (/class) of the method. Based on normal object to get the class.
     * @param regEx Regular expression to find the method (method name). Better success if detailed.
     * @return HashSet of objects.
     */
    static Set<Object> getMethodByClassObject(final Object object, final String regEx) {

        final String regExBuf = convertRegExToLowerCase(regEx);

        MultiException.ExceptionStack exceptionStack = null;
        Set<Object> objectSet = new HashSet<>();

        try {
            if (object == null) {
                throw new ClassNotFoundException("Parameter object is null!");
            } else {
                Method[] methodArray = object.getClass().getMethods();

                for (Method method : methodArray) {
                    if (Pattern.matches(regExBuf, method.getName().toLowerCase())) {
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

    /**
     * Methods converts a regEx string to lower case. If the parameter is null, an empty String is set.
     *
     * @param regEx Regular expression string.
     * @return The regular expression to lower case or an empty string if input is null.
     */
    static String convertRegExToLowerCase(final String regEx) {
        try {
            if (regEx == null) {
                throw new CouldNotPerformException("Regular expression is null! Cannot perform!");
            } else {
                return regEx.toLowerCase(Locale.ENGLISH);
            }
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }

        return "";
    }

}
