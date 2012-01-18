package org.iq80.cli.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class CommandMetadata
{
    private final String name;
    private final String description;
    private final List<OptionMetadata> globalOptions;
    private final List<OptionMetadata> groupOptions;
    private final List<OptionMetadata> commandOptions;
    private final ArgumentsMetadata arguments;
    private final Class<?> type;

    public CommandMetadata(String name,
            String description,
            Iterable<OptionMetadata> globalOptions,
            Iterable<OptionMetadata> groupOptions,
            Iterable<OptionMetadata> commandOptions,
            ArgumentsMetadata arguments, Class<?> type)
    {
        this.name = name;
        this.description = description;
        this.globalOptions = ImmutableList.copyOf(globalOptions);
        this.groupOptions = ImmutableList.copyOf(groupOptions);
        this.commandOptions = ImmutableList.copyOf(commandOptions);
        this.arguments = arguments;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public List<OptionMetadata> getAllOptions()
    {
        return ImmutableList.<OptionMetadata>builder().addAll(globalOptions).addAll(groupOptions).addAll(commandOptions).build();

    }

    public List<OptionMetadata> getGlobalOptions()
    {
        return globalOptions;
    }

    public List<OptionMetadata> getGroupOptions()
    {
        return groupOptions;
    }

    public List<OptionMetadata> getCommandOptions()
    {
        return commandOptions;
    }

    public ArgumentsMetadata getArguments()
    {
        return arguments;
    }

    public Class<?> getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("CommandMetadata");
        sb.append("{name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", globalOptions=").append(globalOptions);
        sb.append(", groupOptions=").append(groupOptions);
        sb.append(", commandOptions=").append(commandOptions);
        sb.append(", arguments=").append(arguments);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
