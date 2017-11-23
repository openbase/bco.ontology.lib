package org.openbase.bco.ontology.lib.utility;

import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface contains precondition methods to validity reference object(s) as one-liner.
 *
 * @author agatting on 14.09.17.
 */
public interface Preconditions {

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference is the object, which should be checked.
     * @param errorMessage is the exception message to use if the check fails.
     * @param <T> is the data type of the reference object.
     * @return the non-null reference that was validated.
     * @throws NotAvailableException is thrown in case the parameter is null.
     */
    static <T> T checkNotNull(final T reference, final String errorMessage) throws NotAvailableException {

        if (reference == null) {
            throw new NotAvailableException(Optional.ofNullable(errorMessage).orElse(""));
        }
        return reference;
    }

    /**
     * Ensures that an object reference is equal to the object comparison. Furthermore, input parameters are checked to null.
     *
     * @param reference is the object, which should be checked.
     * @param comparisonObj is the object, which should be equal to the reference.
     * @param errorMessage is the exception message to use if the check fails.
     * @param <T> is the data type of the reference and comparison object.
     * @return the checked input reference that was compared.
     * @throws InvalidStateException is thrown in case an input is null or the parameters are not equal.
     */
    static <T> T checkEquality(final T reference, final T comparisonObj, final String errorMessage) throws InvalidStateException {

        if (!checkNotNull(reference, "Reference is null!").equals(checkNotNull(comparisonObj, "Comparison object is null!"))) {
            throw new InvalidStateException(Optional.ofNullable(errorMessage).orElse(""));
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null. Possible NotAvailableException is stacked and returned.
     * Can be used to avoid large exception handling or rather describe as one-liner.
     *
     * @param reference is the object, which should be checked.
     * @param source is the source of the reference. Can be ignored by NULL.
     * @param errorMessage is the exception message to use if the check fails.
     * @param exceptionStack is the exception stack to bundle possible exception. If NULL a new exceptionStack will be created.
     * @return the exceptionStack with an following entry if the check fails. Otherwise input stack is returned.
     */
    static ExceptionStack checkNotNull(final Object reference, final Object source, final String errorMessage, final ExceptionStack exceptionStack) {
        try {
            checkNotNull(reference, errorMessage);
        } catch (NotAvailableException e) {
            return MultiException.push(source, e, exceptionStack);
        }
        return (exceptionStack == null) ? new ExceptionStack() : exceptionStack;
    }

    /**
     * Ensures that one or multiple object reference(s) passed as a parameter to the calling method is/are not null. Possible NotAvailableException is stacked
     * and returned. By using one object reference use the single version method to be able to specify the content if exception is thrown.
     *
     * @param source is the source of the reference(s). Can be ignored by NULL.
     * @param exceptionStack is the exception stack to bundle possible exception. If NULL a new exceptionStack will be created.
     * @param objects are the reference objects, which should be checked.
     * @return the exceptionStack with an following entry if the check fails. Otherwise input exceptionStack is returned.
     */
    static ExceptionStack checkNotNull(final Object source, ExceptionStack exceptionStack, final Object... objects) {

        for (int i = 0; i < objects.length; i++) {
            final int index = i + 1;
            exceptionStack = checkNotNull(objects[i], source, "Parameter " + index + " in argument array is null!", exceptionStack);
        }
        return (exceptionStack == null) ? new ExceptionStack() : exceptionStack;
    }

    /**
     * Method executes the input function (needs exactly one argument) and returns the result. If the input function provokes an exception, cause e.g. wrong
     * parameter, it will be stacked on the input exceptionStack object and null will be returned. Check the exceptionStack in this case.
     *
     * @param function represents a function with one argument and produces an result.
     * @param functionArgument is the single argument of the function.
     * @param source is the source of the function. Can be ignored by NULL.
     * @param exceptionStack is the exception stack to bundle possible exception. If NULL a new exceptionStack will be created.
     * @param <T> is the type of the argument to the function.
     * @param <R> is the type of the result to the function.
     * @return the result of the function based on type R. If an exception occurs NULL will be returned. Check the exceptionStack.
     */
    static <T, R> R callFunction(final Function<T, R> function, final T functionArgument, final Object source, ExceptionStack exceptionStack) {
        try {
            exceptionStack = (exceptionStack == null) ? new ExceptionStack() : exceptionStack;
            return function.apply(functionArgument);
        } catch (Exception e) {
            MultiException.push(source, new Exception(e.toString()), exceptionStack);
        }
        return null;
    }

    /**
     * Method executes the input function without argument and returns the result. If the input function provokes an exception it will be stacked on the input
     * exceptionStack object and null will be returned. Check the exceptionStack in this case.
     *
     * @param function represents a function without any argument and produces an result. Be sure, that the method reference isn't NULL!
     * @param source is the source of the function. Can be ignored by NULL.
     * @param exceptionStack is the exception stack to bundle possible exception. If NULL a new exceptionStack will be created.
     * @param <R> is the type of the result to the function.
     * @return the result of the function based on type R. If an exception occurs NULL will be returned. Check the exceptionStack.
     */
    static <R> R callFunction(final Supplier<R> function, final Object source, ExceptionStack exceptionStack) {
        try {
            exceptionStack = (exceptionStack == null) ? new ExceptionStack() : exceptionStack;
            return function.get();
        } catch (Exception e) {
            MultiException.push(source, new Exception(e.toString()), exceptionStack);
        }
        return null;
    }

}
