package io.airlift.airline;

import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.OptionMetadata;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class CommandSuggester
        implements Suggester
{
    @Inject
    public CommandMetadata command;

    @Override
    public Iterable<String> suggest()
    {
        List<String> suggestions = command.getCommandOptions().stream()
                .map(OptionMetadata::getOptions)
                .flatMap(options -> options.stream())
                .collect(Collectors.toList());

        if (command.getArguments() != null) {
            suggestions.add("--");
        }

        return suggestions;
    }
}
