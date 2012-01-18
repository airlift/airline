package org.iq80.cli;

public class TestUtil
{
    public static <T> GitLikeCommandParser<T> singleCommandParser(Class<T> commandClass)
    {
         return GitLikeCommandParser.parser("parser", commandClass)
                .addCommand(commandClass)
                .build();
    }
}
