package io.airlift.command;

import io.airlift.command.model.CommandGroupMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.OptionMetadata;

import javax.inject.Inject;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class GroupSuggester
        implements Suggester
{
    @Inject
    public CommandGroupMetadata group;

    @Override
    public Iterable<String> suggest()
    {
        return concat(
                transform(group.getCommands(), CommandMetadata.nameGetter()),
                concat(transform(group.getOptions(), OptionMetadata.optionsGetter()))
        );
    }
}
