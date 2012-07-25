package io.airlift.command;

import io.airlift.command.model.CommandGroupMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.GlobalMetadata;
import io.airlift.command.model.OptionMetadata;

import javax.inject.Inject;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class GlobalSuggester
    implements Suggester
{
    @Inject
    public GlobalMetadata metadata;

    @Override
    public Iterable<String> suggest()
    {
        return concat(
                transform(metadata.getCommandGroups(), CommandGroupMetadata.nameGetter()),
                transform(metadata.getDefaultGroupCommands(), CommandMetadata.nameGetter()),
                concat(transform(metadata.getOptions(), OptionMetadata.optionsGetter()))
        );
    }
}
