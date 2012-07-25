package org.iq80.cli;

import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.OptionMetadata;

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
