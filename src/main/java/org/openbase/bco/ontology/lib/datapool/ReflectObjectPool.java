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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author agatting on 11.01.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public interface ReflectObjectPool {

    //TODO list instead of set, cause in case of multiple equal named methods...?

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(ReflectObjectPool.class);

    /**
     * Method returns an invoked method of a class object by the matching name.
     *
     * @param object The class object, which contains the method.
     * @param methodName The name of the method. Ignores Case.
     * @return The reflecting method object.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static Object getInvokedObj(final Object object, final String methodName) throws CouldNotPerformException {

        Method method = getMethodByName(object, methodName);

        try {
            return method.invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CouldNotPerformException("Could not invoke method!", e);
        }
    }

    /**
     * Method reflects a method of a class object by the matching name.
     *
     * @param object The class object, which contains the method.
     * @param methodName The name of the method. Ignores Case.
     * @return The reflecting method.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static Method getMethodByName(final Object object, final String methodName) throws CouldNotPerformException {

        try {
            if (object == null || methodName == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            Method[] methodArray = object.getClass().getMethods();

            for (Method method : methodArray) {
                if (method.getName().equalsIgnoreCase(methodName)) {
                    return method;
                }
            }
            throw new NoSuchMethodException("Cause cannot find method with name: " + methodName);
        } catch (NoSuchMethodException | IllegalArgumentException e) {
            throw new CouldNotPerformException("Cannot perform reflection!", e);
        }
    }

    /**
     * Method reflects a method of a class object by the suffix.
     *
     * @param object The class object, which contains the method.
     * @param regExEndsWith The name of the suffix.  Is set to lowercase.
     * @return The reflecting method.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static Method getMethodByRegEx(final Object object, final String regExEndsWith) throws CouldNotPerformException {

        try {
            if (object == null || regExEndsWith == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            String methodNameBuf = regExEndsWith.toLowerCase(Locale.ENGLISH);
            Method[] methodArray = object.getClass().getMethods();
            List<Method> methodList = new ArrayList<>();

            for (Method method : methodArray) {
                if (method.getName().toLowerCase().endsWith(methodNameBuf)) {
                    methodList.add(method);
                }
            }

            if (methodList.size() == 1) {
                return methodList.get(0);
            }

            throw new NoSuchMethodException("Cause cannot find method with suffix: "
                    + regExEndsWith + "or has found multiple matches!");

        } catch (NoSuchMethodException | IllegalArgumentException e) {
            throw new CouldNotPerformException("Cannot perform reflection!", e);
        }
    }

    /**
     * Method checks if a method with the ending regular expression is existing.
     *
     * @param object The class object, which contains the method.
     * @param regExEndsWith The name of the suffix. Is set to lowercase.
     * @return True, if match successful, false otherwise.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static boolean hasMethodByRegEx(final Object object, final String regExEndsWith) throws CouldNotPerformException {

        try {
            if (object == null || regExEndsWith == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            String methodNameBuf = regExEndsWith.toLowerCase(Locale.ENGLISH);
            Method[] methodArray = object.getClass().getMethods();

            for (Method method : methodArray) {
                if (method.getName().toLowerCase().endsWith(methodNameBuf)) {
                    return true;
                }
            }

            return false;

        } catch (IllegalArgumentException e) {
            throw new CouldNotPerformException("Cannot perform reflection!", e);
        }
    }

    /**
     * Method returns an invoked method set, which are matching with the regular expressions.
     *
     * @param object The class object, which contains the method(s).
     * @param regExStartsWith Beginning expression part of the method name. Is set to lowercase.
     * @param regExEndsWith Ending expression part of the method name. Is set to lowercase.
     * @return Set of invoked objects.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static Set<Object> getInvokedObjSet(final Object object, final String regExStartsWith,
                                        final String regExEndsWith) throws CouldNotPerformException {

        Set<Method> methodSet = getMethodSetByRegEx(object, regExStartsWith, regExEndsWith);
        Set<Object> objectSet = new HashSet<>();

        try {
            for (Method method : methodSet) {
                objectSet.add(method.invoke(object));
            }

            return objectSet;

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CouldNotPerformException("Could not invoke methods!", e);
        }
    }

    /**
     * Method returns a set of method(s) of a class object (reflection). The set of methodObject(s) is
     * selected by matches with the delivered regular expressions.
     *
     * @param object The class object, which contains the method(s).
     * @param regExStartsWith Beginning expression part of the method name. Is set to lowercase.
     * @param regExEndsWith Ending expression part of the method name. Is set to lowercase.
     * @return Set of methods.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static Set<Method> getMethodSetByRegEx(final Object object, final String regExStartsWith,
                                           final String regExEndsWith) throws CouldNotPerformException {

        try {
            if (object == null || regExStartsWith == null || regExEndsWith == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            String regExStartsWithBuf = regExStartsWith.toLowerCase(Locale.ENGLISH);
            String regExEndsWithBuf = regExEndsWith.toLowerCase(Locale.ENGLISH);
            Method[] methodArray = object.getClass().getMethods();
            Set<Method> methodSet = new HashSet<>();

            for (Method method : methodArray) {
                String methodName = method.getName().toLowerCase();

                if (methodName.startsWith(regExStartsWithBuf) && methodName.endsWith(regExEndsWithBuf)) {
                    Method objectMethod = object.getClass().getMethod(method.getName());
                    methodSet.add(objectMethod);
                }
            }

            return methodSet;

        } catch (IllegalArgumentException | NoSuchMethodException e) {
            throw new CouldNotPerformException("Cannot perform reflection!", e);
        }
    }

    /**
     * Method returns a set of method(s) of a class object (reflection). The set of methodObjects is
     * selected by matches with the delivered regular expression.
     *
     * @param object The class object, which contains the method(s).
     * @param regEx Regular expression to find the method (method name). Is set to lowercase.
     * @return Set of methods.
     * @throws CouldNotPerformException CouldNotPerformException.
     */
    static Set<Method> getMethodSetByRegEx(final Object object, final String regEx)
            throws CouldNotPerformException {

        try {
            if (object == null || regEx == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            String regExBuf = regEx.toLowerCase(Locale.ENGLISH);
            Method[] methodArray = object.getClass().getMethods();
            Set<Method> methodSet = new HashSet<>();

            for (Method method : methodArray) {
                if (Pattern.matches(regExBuf, method.getName().toLowerCase())) {
                    Method objectMethod = object.getClass().getMethod(method.getName());
                    methodSet.add(objectMethod);
                }
            }

            return methodSet;

        } catch (IllegalArgumentException | NoSuchMethodException e) {
            throw new CouldNotPerformException("Cannot perform reflection!", e);
        }
    }

}
