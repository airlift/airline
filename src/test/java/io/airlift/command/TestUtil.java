package io.airlift.command;

public class TestUtil
{
    public static <T> Cli<T> singleCommandParser(Class<T> commandClass)
    {
         return Cli.buildCli("parser", commandClass)
                .withCommand(commandClass)
                .build();
    }
}
