package io.airlift.command.command;

import java.util.List;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;

@Command(name = "commandWithGroupNames", description = "A command with a group annotation", groupNames = {"singleGroup","singletonGroup"})
public class CommandWithGroupNames
{
    @Arguments(description = "Patterns of files to be added")
    public List<String> patterns;

    @Option(name = "-i")
    public Boolean interactive = false;
}
