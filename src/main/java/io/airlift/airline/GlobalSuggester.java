package io.airlift.airline;

import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.OptionMetadata;

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
                transform(metadata.getCommandGroups(), CommandGroupMetadata::getName),
                transform(metadata.getDefaultGroupCommands(), CommandMetadata::getName),
                concat(transform(metadata.getOptions(), OptionMetadata::getOptions)));
    }
}
