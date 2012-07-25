package org.iq80.cli;

import com.google.common.collect.ImmutableList;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.OptionMetadata;

import javax.inject.Inject;

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
        ImmutableList.Builder<String> suggestions = ImmutableList.<String>builder()
                .addAll(concat(transform(command.getCommandOptions(), OptionMetadata.optionsGetter())));

        if (command.getArguments() != null) {
            suggestions.add("--");
        }

        return suggestions.build();
    }
}
