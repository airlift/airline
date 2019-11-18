package io.airlift.airline;

public final class TestingUtil
{
    private TestingUtil() {}

    public static <T> Cli<T> singleCommandParser(Class<T> commandClass)
    {
        return Cli.<T>builder("parser")
                .withCommand(commandClass)
                .build();
    }
}
