package io.airlift.command.command;

import java.util.List;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Group;
import io.airlift.command.Option;

@Group(name = "singleGroup", description = "a single group", defaultCommand = CommandWithGroupAnnotation.class,commands = {CommandAdd.class})
@Command(name = "commandWithGroup", description = "A command with a group annotation")
public class CommandWithGroupAnnotation
{
    @Arguments(description = "Patterns of files to be added")
    public List<String> patterns;

    @Option(name = "-i")
    public Boolean interactive = false;
}
