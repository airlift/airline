package io.airlift.airline;

import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.OptionMetadata;

import javax.inject.Inject;
import java.util.stream.Stream;

public class GlobalSuggester
    implements Suggester
{
    @Inject
    public GlobalMetadata metadata;

    @Override
    public Iterable<String> suggest()
    {
        return () -> Stream.concat(
                Stream.concat(
                        metadata.getCommandGroups().stream().map(CommandGroupMetadata::getName),
                        metadata.getDefaultGroupCommands().stream().map(CommandMetadata::getName)
                ),
                metadata.getOptions().stream().map(OptionMetadata::getOptions).flatMap(options -> options.stream())
        ).iterator();
    }
}
