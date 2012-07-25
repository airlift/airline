package org.iq80.cli.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class GlobalMetadata
{
    private final String name;
    private final String description;
    private final List<OptionMetadata> options;
    private final CommandMetadata defaultCommand;
    private final List<CommandMetadata> defaultGroupCommands;
    private final List<CommandGroupMetadata> commandGroups;

    public GlobalMetadata(String name,
            String description,
            Iterable<OptionMetadata> options,
            CommandMetadata defaultCommand,
            Iterable<CommandMetadata> defaultGroupCommands,
            Iterable<CommandGroupMetadata> commandGroups)
    {
        this.name = name;
        this.description = description;
        this.options = ImmutableList.copyOf(options);
        this.defaultCommand = defaultCommand;
        this.defaultGroupCommands = ImmutableList.copyOf(defaultGroupCommands);
        this.commandGroups = ImmutableList.copyOf(commandGroups);
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public List<OptionMetadata> getOptions()
    {
        return options;
    }

    public CommandMetadata getDefaultCommand()
    {
        return defaultCommand;
    }

    public List<CommandMetadata> getDefaultGroupCommands()
    {
        return defaultGroupCommands;
    }

    public List<CommandGroupMetadata> getCommandGroups()
    {
        return commandGroups;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("GlobalMetadata");
        sb.append("{name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", options=").append(options);
        sb.append(", defaultCommand=").append(defaultCommand);
        sb.append(", defaultGroupCommands=").append(defaultGroupCommands);
        sb.append(", commandGroups=").append(commandGroups);
        sb.append('}');
        return sb.toString();
    }
}
