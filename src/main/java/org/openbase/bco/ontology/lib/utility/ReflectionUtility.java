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
package org.openbase.bco.ontology.lib.utility;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author agatting on 11.01.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public interface ReflectionUtility {

    /**
     * Method detects a method by input name and returns the invoked result of the method.
     *
     * @param object is the object, which the method is invoked from.
     * @param regEx is the regular expression, which should be matched with the method name.
     * @param patternFlag is the flag to configure the matching radius (e.g. Pattern.CASE_INSENSITIVE). Set to 0 if not necessary.
     * @return the invoked method object.
     * @throws CouldNotPerformException is thrown in case the method could not detected or the invocation of the detected method failed.
     */
    static Object invokeMethod(final Object object, final String regEx, final int patternFlag) throws CouldNotPerformException {
        try {
            Method method = detectMethod(object.getClass(), regEx, patternFlag);
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            throw new CouldNotPerformException("Invocation failed!", ex);
        }
    }

    /**
     * Method returns an invoked method set, which are matching with the regular expressions.
     *
     * @param object is the object, which the method is invoked from.
     * @param regEx is the regular expression, which should be matched with the method name.
     * @param patternFlag is the flag to configure the matching radius (e.g. Pattern.CASE_INSENSITIVE). Set to 0 if not necessary.
     * @return a set of invoked method objects.
     * @throws CouldNotPerformException is thrown in case the method could not detected or the invocation of the detected method failed.
     */
    static Set<Object> invokeMethods(final Object object, final String regEx, final int patternFlag) throws CouldNotPerformException {
        try {
            Set<Method> methods = detectMethods(object.getClass(), regEx, patternFlag);
            Set<Object> invokedObjects = new HashSet<>();

            for (Method method : methods) {
                invokedObjects.add(method.invoke(object));
            }
            return invokedObjects;
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            throw new CouldNotPerformException("Invocation failed!", ex);
        }
    }

    /**
     * Method detects the method of an object class by the input regular expression.
     *
     * @param inputClass is the class object, which contains the method.
     * @param regEx is the regular expression, which should be matched with the method name.
     * @param patternFlag is the flag to configure the matching radius (e.g. Pattern.CASE_INSENSITIVE). Set to 0 if not necessary.
     * @return the matched method object.
     * @throws CouldNotPerformException is thrown in case the input is null or the method could not explicit detected, because of no matching method or more
     * than one matching methods.
     */
    static Method detectMethod(final Class<?> inputClass, final String regEx, final int patternFlag) throws CouldNotPerformException {
        try {
            if (inputClass == null) {
                assert false;
                throw new NotAvailableException("Object class is null!");
            }

            if (regEx == null) {
                assert false;
                throw new NotAvailableException("Regular expression is null!");
            }

            List<Method> methods = new ArrayList<>();
            Pattern pattern = (patternFlag == 0) ? Pattern.compile(regEx) : Pattern.compile(regEx, patternFlag);

            for (Method method : inputClass.getMethods()) {
                Matcher matcher = pattern.matcher(method.getName());
                if (matcher.find()) {
                    methods.add(method);
                }
            }

            if (methods.size() == 1) {
                return methods.get(0);
            } else if (methods.size() >= 2) {
                String exceptionInfo = "There are more than one matching methods, which contains the regular expression: " + regEx + ". Method names are: ";

                for (Method method : methods) {
                    exceptionInfo += method.getName() + ", ";
                }
                throw new NoSuchMethodException(exceptionInfo);
            } else {
                throw new NoSuchMethodException("There is no matching method with regular expression: " + regEx);
            }
        } catch (NoSuchMethodException | NotAvailableException ex) {
            throw new CouldNotPerformException("Could not get method!", ex);
        }
    }

    /**
     * Method detects the methods of an object class by the input regular expression.
     *
     * @param inputClass is the class object, which contains the method.
     * @param regEx is the regular expression, which should be matched with the method name.
     * @param patternFlag is the flag to configure the matching radius (e.g. Pattern.CASE_INSENSITIVE). Set to 0 if not necessary.
     * @return a set of methods, which are matched with the regular expression.
     * @throws CouldNotPerformException is thrown in case the input parameter are null or there is no matching method.
     */
    static Set<Method> detectMethods(final Class<?> inputClass, final String regEx, final int patternFlag) throws CouldNotPerformException {
        try {
            if (inputClass == null) {
                assert false;
                throw new NotAvailableException("Class object is null!");
            }

            if (regEx == null) {
                assert false;
                throw new NotAvailableException("Regular expression is null!");
            }

            Set<Method> methods = new HashSet<>();
            Pattern pattern = (patternFlag == 0) ? Pattern.compile(regEx) : Pattern.compile(regEx, patternFlag);

            for (Method method : inputClass.getMethods()) {
                Matcher matcher = pattern.matcher(method.getName());

                if (matcher.find()) {
                    methods.add(method);
                }
            }
            if (methods.isEmpty()) {
                throw new NoSuchMethodException("There is no matching method with regular expression: " + regEx);
            }

            return methods;
        } catch (NotAvailableException | NoSuchMethodException ex) {
            throw new CouldNotPerformException("Could not get methods of dataClass!", ex);
        }
    }

}
