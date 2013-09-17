package io.airlift.command;

public class TestUtil
{
    public static <T> Cli<T> singleCommandParser(Class<T> commandClass)
    {
         return Cli.<T>builder("parser")
                .withCommand(commandClass)
                .build();
    }

    public static <T> Cli<T> singleCommandParserWithDefault(Class<T> commandClass)
    {
         return Cli.<T>builder("parser")
                .withCommand(commandClass)
                .withDefaultCommand(commandClass)
                .build();
    }

}
