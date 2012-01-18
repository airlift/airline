package org.iq80.cli;

import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.GlobalMetadata;
import org.iq80.cli.model.OptionMetadata;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

public class GlobalSuggester
    implements Suggester
{
    @Options
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
