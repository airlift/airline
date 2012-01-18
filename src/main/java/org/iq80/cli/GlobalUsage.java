package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.GlobalMetadata;
import org.iq80.cli.model.OptionMetadata;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static org.iq80.cli.UsageHelper.toUsage;

public class GlobalUsage
{
    private final int columnSize;

    public GlobalUsage()
    {
        this(79);
    }

    public GlobalUsage(int columnSize)
    {
        Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(@Nullable String programName, GlobalMetadata metadata)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(programName, metadata, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(@Nullable String programName, GlobalMetadata metadata, StringBuilder out)
    {
        usage(programName, metadata, new UsagePrinter(out, columnSize));
    }

    public void usage(@Nullable String programName, GlobalMetadata metadata, UsagePrinter out)
    {
        //
        // Usage
        //

        // build arguments
        List<String> commandArguments = newArrayList();
        commandArguments.addAll(Collections2.transform(metadata.getOptions(), new Function<OptionMetadata, String>()
        {
            public String apply(OptionMetadata option)
            {
                if (option.isHidden()) {
                    return null;
                }
                return toUsage(option);
            }
        }));
        out.newPrinterWithHangingIndent(8)
                .append("usage:")
                .append(metadata.getName())
                .appendWords(commandArguments)
                .append("<command> [<args>]")
                .newline()
                .newline();

        //
        // Common commands
        //

        Map<String, String> commands = newTreeMap();
        for (CommandMetadata commandMetadata : metadata.getDefaultGroupCommands()) {
            commands.put(commandMetadata.getName(), commandMetadata.getDescription());
        }
        for (CommandGroupMetadata commandGroupMetadata : metadata.getCommandGroups()) {
            commands.put(commandGroupMetadata.getName(), commandGroupMetadata.getDescription());
        }

        out.append("The most commonly used ").append(metadata.getName()).append(" commands are:").newline();
        out.newIndentedPrinter(4).appendTable(Iterables.transform(commands.entrySet(), new Function<Entry<String, String>, Iterable<String>>()
        {
            public Iterable<String> apply(Entry<String, String> entry)
            {
                return ImmutableList.of(entry.getKey(), entry.getValue());
            }
        }));
        out.newline();
        out.append("See '").append(metadata.getName()).append(" help <command>' for more information on a specific command.").newline();
    }
}
