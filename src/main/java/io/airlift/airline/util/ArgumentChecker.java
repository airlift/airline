package io.airlift.airline.util;

import java.text.MessageFormat;

public final class ArgumentChecker
{
    private ArgumentChecker() {}

    public static void checkNotNull(Object reference, String errorMessage) {
        if(reference == null) {
            throw new NullPointerException(errorMessage);
        }
    }

    public static void checkCondition(boolean expression, String errorMessage, Object... errorMessageArguments) {
        if(!expression) {
            String errorMessageFormatted;
            if(errorMessageArguments != null && errorMessageArguments.length > 0) {
                errorMessageFormatted = MessageFormat.format(errorMessage, errorMessageArguments);
            } else {
                errorMessageFormatted = errorMessage;
            }
            throw new IllegalArgumentException(errorMessageFormatted);
        }
    }
}