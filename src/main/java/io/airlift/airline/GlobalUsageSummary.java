package io.airlift.airline;

import com.google.common.collect.ImmutableList;
import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;

public class GlobalUsageSummary
{
    private final int columnSize;

    public GlobalUsageSummary()
    {
        this(79);
    }

    public GlobalUsageSummary(int columnSize)
    {
        checkArgument(columnSize > 0, "columnSize must be greater than 0");
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
        List<String> commandArguments = global.getOptions().stream()
                .filter(option -> !option.isHidden())
                .map(UsageHelper::toUsage)
                .collect(toImmutableList());

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
        out.newIndentedPrinter(4).appendTable(commands.entrySet().stream()
                .map(entry -> ImmutableList.of(entry.getKey(), firstNonNull(entry.getValue(), "")))
                .collect(toList()));
        out.newline();
        out.append("See").append("'" + global.getName()).append("help <command>' for more information on a specific command.").newline();
    }
}
