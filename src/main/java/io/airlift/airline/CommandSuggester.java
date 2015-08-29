package io.airlift.airline;

import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.OptionMetadata;
import io.airlift.airline.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class CommandSuggester
        implements Suggester
{
    @Inject
    public CommandMetadata command;

    @Override
    public Iterable<String> suggest()
    {
        List<String> suggestions = CollectionUtils.asList(concat(transform(command.getCommandOptions(), OptionMetadata.optionsGetter())));

        if (command.getArguments() != null) {
            suggestions.add("--");
        }

        return suggestions;
    }
}
