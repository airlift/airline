package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.GlobalMetadata;
import org.iq80.cli.model.OptionMetadata;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static org.iq80.cli.UsageHelper.toUsage;

public class GlobalUsageSummary
{
    private final int columnSize;

    public GlobalUsageSummary()
    {
        this(79);
    }

    public GlobalUsageSummary(int columnSize)
    {
        Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(GlobalMetadata global)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(global, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(GlobalMetadata global, StringBuilder out)
    {
        usage(global, new UsagePrinter(out, columnSize));
    }

    public void usage(GlobalMetadata global, UsagePrinter out)
    {
        //
        // Usage
        //

        // build arguments
        List<String> commandArguments = newArrayList();
        commandArguments.addAll(Collections2.transform(global.getOptions(), new Function<OptionMetadata, String>()
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
                .append(global.getName())
                .appendWords(commandArguments)
                .append("<command> [<args>]")
                .newline()
                .newline();

        //
        // Common commands
        //

        Map<String, String> commands = newTreeMap();
        for (CommandMetadata commandMetadata : global.getDefaultGroupCommands()) {
            if (!commandMetadata.isHidden()) {
                commands.put(commandMetadata.getName(), commandMetadata.getDescription());
            }
        }
        for (CommandGroupMetadata commandGroupMetadata : global.getCommandGroups()) {
            commands.put(commandGroupMetadata.getName(), commandGroupMetadata.getDescription());
        }

        out.append("The most commonly used ").append(global.getName()).append(" commands are:").newline();
        out.newIndentedPrinter(4).appendTable(Iterables.transform(commands.entrySet(), new Function<Entry<String, String>, Iterable<String>>()
        {
            public Iterable<String> apply(Entry<String, String> entry)
            {
                return ImmutableList.of(entry.getKey(), Objects.firstNonNull(entry.getValue(), ""));
            }
        }));
        out.newline();
        out.append("See").append("'" + global.getName()).append("help <command>' for more information on a specific command.").newline();
    }
}
