package org.iq80.cli;

public class TestUtil
{
    public static <T> GitLikeCli<T> singleCommandParser(Class<T> commandClass)
    {
         return GitLikeCli.buildCli("parser", commandClass)
                .withCommand(commandClass)
                .build();
    }
}
