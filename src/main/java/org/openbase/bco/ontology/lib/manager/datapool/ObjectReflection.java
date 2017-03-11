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
package org.openbase.bco.ontology.lib.manager.datapool;

import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author agatting on 11.01.17.
 */
@SuppressWarnings("checkstyle:multiplestringliterals")
public interface ObjectReflection {

    /**
     * Logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(ObjectReflection.class);

    /**
     * Method returns an invoked method of a class object by the matching name.
     *
     * @param object The class object, which contains the method.
     * @param methodName The name of the method. Ignores Case.
     * @return The reflecting method object.
     * @throws CouldNotPerformException Exception is thrown, if method could not be found or could not be invoked.
     */
    static Object getInvokedObject(final Object object, final String methodName) throws CouldNotPerformException {

        final Method method = getMethodByName(object.getClass(), methodName);

        try {
            return method.invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CouldNotPerformException("Could not invoke method!", e);
        }
    }

    /**
     * Method returns a single method object of the input dataClass by matched name.
     *
     * @param dataClass The dataClass, which contains the method.
     * @param methodName The name of the method. Ignores Case.
     * @return The matched method object.
     * @throws CouldNotPerformException Exception is thrown, if no matched method could be found.
     */
    static Method getMethodByName(final Class<?> dataClass, final String methodName) throws CouldNotPerformException {

        try {
            if (dataClass == null || methodName == null) {
                throw new IllegalArgumentException("Cause input parameter is null!");
            }

            for (final Method method : dataClass.getMethods()) {
                if (method.getName().equalsIgnoreCase(methodName)) {
                    return method;
                }
            }
            throw new NoSuchMethodException("Cause could not find method with name: " + methodName);

        } catch (NoSuchMethodException | IllegalArgumentException e) {
            throw new CouldNotPerformException("Could not get method of dataClass!", e);
        }
    }

    /**
     * Method returns a single method object of the input dataClass by matched ending regular expression.
     *
     * @param dataClass The dataClass, which contains the method.
     * @param regExEndsWith The name of the method suffix. Is set to lowercase.
     * @return The matched method object.
     * @throws CouldNotPerformException Exception is thrown, if a single method object could not be identified.
     */
    static Method getMethodByRegEx(final Class<?> dataClass, final String regExEndsWith) throws CouldNotPerformException {

        try {
            if (dataClass == null || regExEndsWith == null) {
                throw new IllegalArgumentException("Cause input parameter is null!");
            }

            final String methodNameBuf = regExEndsWith.toLowerCase();
            final List<Method> methodList = new ArrayList<>();

            for (final Method method : dataClass.getMethods()) {
                if (method.getName().toLowerCase().endsWith(methodNameBuf)) {
                    methodList.add(method);
                }
            }

            if (methodList.size() == 1) {
                return methodList.get(0);
            } else if (methodList.size() >= 2) {
                throw new NoSuchMethodException("Cause found multiple methods (" + methodList.size() + ") with suffix: " + regExEndsWith);
            } else {
                throw new NoSuchMethodException("Cause could not find method with suffix: " + regExEndsWith);
            }
        } catch (IllegalArgumentException | NoSuchMethodException e) {
            throw new CouldNotPerformException("Could not get methods of dataClass!", e);
        }
    }

    /**
     * Method identifies, if the dataClass contain a method with the ending regular expression.
     *
     * @param dataClass The dataClass, which contains the method.
     * @param regExEndsWith The name of the method suffix. Is set to lowercase.
     * @return True, if match successful, false otherwise.
     */
    static boolean hasMethodByRegEx(final Class<?> dataClass, final String regExEndsWith) {

        if (dataClass == null || regExEndsWith == null) {
            throw new IllegalArgumentException("Cause input parameter is null!");
        }

        final String methodNameBuf = regExEndsWith.toLowerCase();

        for (final Method method : dataClass.getMethods()) {
            if (method.getName().toLowerCase().endsWith(methodNameBuf)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method returns an invoked method set, which are matching with the regular expressions.
     *
     * @param object The class object, which contains the method(s) and data.
     * @param regExStartsWith Beginning expression part of the method name. Is set to lowercase.
     * @param regExEndsWith Ending expression part of the method name. Is set to lowercase.
     * @return Set of invoked objects.
     * @throws CouldNotPerformException Exception is thrown, if the identified methods could not be invoked, cause of wrong parameters or missing regex match.
     */
    static Set<Object> getInvokedObjSet(final Object object, final String regExStartsWith, final String regExEndsWith) throws CouldNotPerformException {

        final Set<Method> methodSet = getMethodSetByRegEx(object.getClass(), regExStartsWith, regExEndsWith);
        final Set<Object> objectSet = new HashSet<>();

        try {
            for (final Method method : methodSet) {
                objectSet.add(method.invoke(object));
            }
            return objectSet;

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CouldNotPerformException("Could not invoke methods! Bad object?", e);
        }
    }

    /**
     * Method returns a set of method(s) of a dataClass. The set of methodObject(s) is selected by matches with the delivered regular expressions.
     *
     * @param dataClass The class data, which contains the method(s).
     * @param regExStartsWith Beginning expression part of the method name. Method sets to lowercase.
     * @param regExEndsWith Ending expression part of the method name. Method sets to lowercase.
     * @return Set of methods.
     * @throws CouldNotPerformException Exception is thrown, if methods could not be find, because of null parameter or there is no method with matching name.
     */
    static Set<Method> getMethodSetByRegEx(final Class<?> dataClass, final String regExStartsWith, final String regExEndsWith) throws CouldNotPerformException {

        try {
            if (dataClass == null || regExStartsWith == null || regExEndsWith == null) {
                throw new IllegalArgumentException("Cause input parameter is null!");
            }

            final String regExStartsWithBuf = regExStartsWith.toLowerCase();
            final String regExEndsWithBuf = regExEndsWith.toLowerCase();
            final Set<Method> methodSet = new HashSet<>();

            for (final Method method : dataClass.getMethods()) {
                final String methodName = method.getName().toLowerCase();

                if (methodName.startsWith(regExStartsWithBuf) && methodName.endsWith(regExEndsWithBuf)) {
                    final Method objectMethod = dataClass.getMethod(method.getName());
                    methodSet.add(objectMethod);
                }
            }
            return methodSet;

        } catch (IllegalArgumentException | NoSuchMethodException e) {
            throw new CouldNotPerformException("Could not get methods of dataClass!", e);
        }
    }

    /**
     * Method returns a set of method(s) of a class object (reflection). The set of methodObjects is
     * selected by matches with the delivered regular expression.
     *
     * @param dataClass The dataClass, which contains the method(s).
     * @param regEx Regular expression to find the method (method name). Method sets to lowercase.
     * @return Set of methods.
     * @throws CouldNotPerformException Exception is thrown, if methods could not be find, because of null parameter or there is no method with matching name.
     */
    static Set<Method> getMethodSetByRegEx(final Class<?> dataClass, final String regEx) throws CouldNotPerformException {

        try {
            if (dataClass == null || regEx == null) {
                throw new IllegalArgumentException("Cause parameter is null!");
            }

            final String regExBuf = regEx.toLowerCase();
            final Set<Method> methodSet = new HashSet<>();

            for (final Method method : dataClass.getMethods()) {
                if (Pattern.matches(regExBuf, method.getName().toLowerCase())) {
                    final Method objectMethod = dataClass.getMethod(method.getName());
                    methodSet.add(objectMethod);
                }
            }
            return methodSet;

        } catch (IllegalArgumentException | NoSuchMethodException e) {
            throw new CouldNotPerformException("Could not get methods of dataClass!", e);
        }
    }

}
