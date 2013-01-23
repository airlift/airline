package io.airlift.command.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import io.airlift.command.Accessor;

import javax.annotation.Nullable;
import java.util.List;

public class CommandMetadata
{
    private final String name;
    private final String description;
    private final boolean hidden;
    private final List<OptionMetadata> globalOptions;
    private final List<OptionMetadata> groupOptions;
    private final List<OptionMetadata> commandOptions;
    private final ArgumentsMetadata arguments;
    private final List<Accessor> metadataInjections;
    private final Class<?> type;

    private final List<String> examples;
    private final String discussion;

    public CommandMetadata(String name,
                           String description,
                           final String discussion,
                           final List<String> examples,
                           boolean hidden, Iterable<OptionMetadata> globalOptions,
                           Iterable<OptionMetadata> groupOptions,
                           Iterable<OptionMetadata> commandOptions,
                           ArgumentsMetadata arguments,
                           Iterable<Accessor> metadataInjections,
                           Class<?> type)
    {
        this.name = name;
        this.description = description;
        this.hidden = hidden;
        this.globalOptions = ImmutableList.copyOf(globalOptions);
        this.groupOptions = ImmutableList.copyOf(groupOptions);
        this.commandOptions = ImmutableList.copyOf(commandOptions);
        this.arguments = arguments;
        this.metadataInjections = ImmutableList.copyOf(metadataInjections);
        this.type = type;
        this.discussion = discussion;
        this.examples = examples;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public List<OptionMetadata> getAllOptions()
    {
        return ImmutableList.<OptionMetadata>builder().addAll(globalOptions).addAll(groupOptions).addAll(commandOptions).build();

    }

    public List<String> getExamples() {
        return examples;
    }

    public String getDiscussion() {
        return discussion;
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

    public List<Accessor> getMetadataInjections()
    {
        return metadataInjections;
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
        sb.append(", discussion='").append(discussion).append('\'');
        sb.append(", examples='").append(examples).append('\'');
        sb.append(", globalOptions=").append(globalOptions);
        sb.append(", groupOptions=").append(groupOptions);
        sb.append(", commandOptions=").append(commandOptions);
        sb.append(", arguments=").append(arguments);
        sb.append(", metadataInjections=").append(metadataInjections);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }

    public static Function<CommandMetadata, String> nameGetter()
    {
        return new Function<CommandMetadata, String>()
        {
            public String apply(CommandMetadata input)
            {
                return input.getName();
            }
        };
    }
}
