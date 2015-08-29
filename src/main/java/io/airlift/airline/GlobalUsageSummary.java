package io.airlift.airline;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.OptionMetadata;
import io.airlift.airline.util.ArgumentChecker;

import java.util.*;
import java.util.Map.Entry;

import static io.airlift.airline.UsageHelper.toUsage;

public class GlobalUsageSummary
{
    private final int columnSize;

    public GlobalUsageSummary()
    {
        this(79);
    }

    public GlobalUsageSummary(int columnSize)
    {
        ArgumentChecker.checkCondition(columnSize > 0, "columnSize must be greater than 0");
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
        List<String> commandArguments = new ArrayList<>();
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

        Map<String, String> commands = new TreeMap<>();
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
                return Arrays.asList(entry.getKey(), Objects.firstNonNull(entry.getValue(), ""));
            }
        }));
        out.newline();
        out.append("See").append("'" + global.getName()).append("help <command>' for more information on a specific command.").newline();
    }
}
