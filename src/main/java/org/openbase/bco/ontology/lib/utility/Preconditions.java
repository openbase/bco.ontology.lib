package org.openbase.bco.ontology.lib.utility;

import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;

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
     * @return the non-null reference that was validated
     * @throws NotAvailableException is thrown in case the parameter is null.
     */
    static <T> T checkNotNull(final T reference, final String errorMessage) throws NotAvailableException {

        if (reference == null) {
            throw new NotAvailableException(errorMessage);
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
            throw new InvalidStateException(errorMessage);
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null. Possible NotAvailableException is stacked and returned.
     * Can be used to avoid large exception handling or rather describe as one-liner.
     *
     * @param reference is the object, which should be checked.
     * @param errorMessage is the exception message to use if the check fails.
     * @param exceptionStack is the exception stack to bundle possible exception. If null a new exceptionStack will be created.
     * @return the exceptionStack with an following entry if the check fails. Otherwise input stack is returned.
     */
    static ExceptionStack checkNotNull(final Object reference, final String errorMessage, final ExceptionStack exceptionStack) {
        try {
            checkNotNull(reference, errorMessage);
        } catch (NotAvailableException e) {
            return MultiException.push(null, e, exceptionStack);
        }
        return exceptionStack;
    }

    /**
     * Ensures that one or multiple object reference(s) passed as a parameter to the calling method is/are not null. Possible NotAvailableException is stacked
     * and returned. By using one object reference use the single version method to be able to specify the content if exception is thrown.
     *
     * @param exceptionStack is the exception stack to bundle possible exception. If null a new exceptionStack will be created.
     * @param objects are the reference objects, which should be checked.
     * @return the exceptionStack with an following entry if the check fails. Otherwise input exceptionStack is returned.
     */
    static ExceptionStack checkNotNull(ExceptionStack exceptionStack, final Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            exceptionStack = checkNotNull(objects[i], "Parameter " + i + " in argument array is null!", exceptionStack);
        }
        return exceptionStack;
    }

}
