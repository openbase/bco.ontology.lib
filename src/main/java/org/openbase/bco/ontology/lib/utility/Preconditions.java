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

import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface contains precondition methods to validity reference object(s) as one-liner.
 *
 * @author agatting on 14.09.17.
 */
public interface Preconditions {

    /**
     * Method ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference is the object, which should be checked.
     * @param errorMessage is the exception message to use if the check fails.
     * @param <T> is the data type of the reference object.
     * @return the non-null reference that was validated.
     * @throws NotAvailableException is thrown in case the parameter is null.
     */
    static <T> T checkNotNull(final T reference, final String errorMessage) throws NotAvailableException {
        if (reference == null) {
            throw new NotAvailableException((errorMessage == null) ? "Reference is null." : errorMessage);
        }
        return reference;
    }

    /**
     * Method ensures that an object reference passed as a parameter to the calling method is not null. Possible
     * NotAvailableException is stacked and returned.
     *
     * @param reference is the object, which should be checked.
     * @param source is the source of the reference. Can be ignored by NULL.
     * @param errorMessage is the exception message to use if the check fails.
     * @param exceptionStack is used to bundle possible exception. If NULL a new exceptionStack will be created.
     * @return the exceptionStack with an following entry if the check fails. Otherwise input stack is returned.
     */
    static ExceptionStack checkNotNull(final Object reference, final Object source,
                                       final String errorMessage, ExceptionStack exceptionStack) {
        try {
            exceptionStack = initializeIfNull(exceptionStack);
            checkNotNull(reference, errorMessage);
        } catch (NotAvailableException e) {
            return MultiException.push(source, e, exceptionStack);
        }
        return exceptionStack;
    }

    /**
     * Method ensures that one or multiple object reference(s) passed as a parameter to the calling method is/are not
     * null. Possible NotAvailableException is stacked and returned. By using one object reference use the single
     * version method to be able to specify the content if exception is thrown.
     *
     * @param source is the source of the reference(s). Can be ignored by NULL.
     * @param exceptionStack is used to bundle possible exception. If NULL a new exceptionStack will be created.
     * @param objects are the reference objects, which should be checked.
     * @return the exceptionStack with or without new exception entry.
     */
    static ExceptionStack multipleCheckNotNull(final Object source, ExceptionStack exceptionStack,
                                               final Object... objects) {
        exceptionStack = initializeIfNull(exceptionStack);

        for (int i = 0; i < objects.length; i++) {
            try {
                checkNotNull(objects[i], "Parameter " + i + " in argument array is null!");
            } catch (NotAvailableException e) {
                MultiException.push(source, e, exceptionStack);
            }
        }
        return exceptionStack;
    }

    /**
     * Method ensures that one or multiple object reference(s) passed as a parameter to the calling method is/are not
     * null. Throws Exception otherwise.
     *
     * @param source is the source of the reference(s). Can be ignored by NULL.
     * @param objects are the reference parameter, which should be checked.
     * @throws MultiException is thrown in case at least one reference parameter is NULL.
     */
    static void multipleCheckNotNullAndThrow(final Object source, final Object... objects) throws MultiException {
        final ExceptionStack exceptionStack = new ExceptionStack();

        for (int i = 0; i < objects.length; i++) {
            try {
                checkNotNull(objects[i], "Parameter " + i + " in argument array is null!");
            } catch (NotAvailableException e) {
                MultiException.push(source, e, exceptionStack);
            }
        }
        MultiException.checkAndThrow("Input is invalid.", exceptionStack);
    }

    /**
     * Method executes the input function (needs exactly one argument) and returns the result. If the input function
     * provokes an exception (cause e.g. wrong parameter), it will be stacked on the input exceptionStack and NULL will
     * be returned. Check the exceptionStack in this case.
     *
     * @param function represents a function with one argument and produces an result.
     * @param functionArgument is the single argument of the function.
     * @param source is the source of the function. Can be ignored by NULL.
     * @param exceptionStack is used to bundle possible exception. If NULL a new exceptionStack will be created.
     * @param <T> is the type of the argument to the function.
     * @param <R> is the type of the result to the function.
     * @return the result of the function with type R. Returns null if an exception occurs. Check exceptionStack.
     */
    static <T, R> R callFunction(final Function<T, R> function, final T functionArgument,
                                 final Object source, ExceptionStack exceptionStack) {
        try {
            exceptionStack = initializeIfNull(exceptionStack);
            return function.apply(functionArgument);
        } catch (Exception e) {
            MultiException.push(source, new Exception(e.toString()), exceptionStack);
        }
        return null;
    }

    /**
     * Method executes the input function (needs exactly two arguments) and returns the result. If the input function
     * provokes an exception (cause e.g. wrong parameter), it will be stacked on the input exceptionStack and NULL will
     * be returned. Check the exceptionStack in this case.
     *
     * @param function represents a function with one argument and produces an result.
     * @param functionArgOne is the first argument of the function.
     * @param functionArgTwo is the second argument of the function.
     * @param source is the source of the function. Can be ignored by NULL.
     * @param exceptionStack is used to bundle possible exception. If NULL a new exceptionStack will be created.
     * @param <T> is the type of the first argument to the function.
     * @param <U> is the type of the second argument to the function.
     * @param <R> is the type of the result to the function.
     * @return the result of the function with type R. Returns null if an exception occurs. Check exceptionStack.
     */
    static <T, U, R> R callBiFunction(final BiFunction<T, U, R> function, final T functionArgOne,
                                      final U functionArgTwo, final Object source, ExceptionStack exceptionStack) {
        try {
            exceptionStack = initializeIfNull(exceptionStack);
            return function.apply(functionArgOne, functionArgTwo);
        } catch (Exception e) {
            MultiException.push(source, new Exception(e.toString()), exceptionStack);
        }
        return null;
    }

    /**
     * Method executes the input function without argument and returns the result. If the input function provokes an
     * exception it will be stacked on the input exceptionStack object and NULL will be returned. Check the
     * exceptionStack in this case.
     *
     * @param function represents a function without any argument and produces an result.
     * @param source of the function. Can be ignored by NULL.
     * @param exceptionStack is used to bundle possible exception. If NULL a new exceptionStack will be created.
     * @param <R> is the type of the result to the function.
     * @return the result of the function with type R. Returns null if an exception occurs. Check exceptionStack.
     */
    static <R> R callSupplier(final Supplier<R> function, final Object source, ExceptionStack exceptionStack) {
        try {
            exceptionStack = initializeIfNull(exceptionStack);
            return function.get();
        } catch (Exception e) {
            MultiException.push(source, new Exception(e.toString()), exceptionStack);
        }
        return null;
    }

    /**
     * Method executes the input function without argument and returns the result.
     *
     * @param function represents a function without any argument and produces an result.
     * @param <R> is the type of the result to the function.
     * @return the result of the function with type R.
     * @throws NotAvailableException is thrown in case the function throws an exception.
     */
    static <R> R callSupplier(final Supplier<R> function) throws NotAvailableException {
        try {
            return function.get();
        } catch (Exception e) {
            throw new NotAvailableException("Function invalid", e);
        }
    }

    /**
     * Method ensures that an string reference passed as a parameter to the calling method is not null or empty. The
     * reference will be returned if it has content. Otherwise an exception is thrown if NULL or empty.
     *
     * @param reference is the string, which should be checked.
     * @param errorMessage is the exception message to use if the check fails.
     * @return the input reference if it isn't empty.
     * @throws NotAvailableException is thrown in case the reference is null or empty.
     */
    static String checkHasContent(final String reference, final String errorMessage) throws NotAvailableException {
        checkNotNull(reference, errorMessage);

        if (reference.isEmpty()) {
            throw new NotAvailableException((errorMessage == null) ? "Reference string is empty." : errorMessage);
        }
        return reference;
    }

    /**
     * Method initializes the input exceptionStack if it's null.
     *
     * @param exceptionStack which should be initialized.
     * @return an initialized and empty exceptionStack if input is null. Otherwise the unchanged input exceptionStack.
     */
    static ExceptionStack initializeIfNull(ExceptionStack exceptionStack) {
        exceptionStack = (exceptionStack == null) ? new ExceptionStack() : exceptionStack;
        return exceptionStack;
    }

}
