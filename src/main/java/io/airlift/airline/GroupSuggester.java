package io.airlift.airline;

import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.OptionMetadata;

import javax.inject.Inject;
import java.util.stream.Stream;

public class GroupSuggester
        implements Suggester
{
    @Inject
    public CommandGroupMetadata group;

    @Override
    public Iterable<String> suggest()
    {
        return () -> Stream.concat(
            group.getCommands().stream().map(CommandMetadata::getName),
            group.getOptions().stream().map(OptionMetadata::getOptions).flatMap(options -> options.stream())
        ).iterator();
    }
}
