package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

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
    public void usage(@Nullable String programName, GitLikeCommandParser<?> parser)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(programName, parser, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(@Nullable String programName, GitLikeCommandParser<?> parser, StringBuilder out)
    {
        usage(programName, parser, new UsagePrinter(out, columnSize));
    }

    public void usage(@Nullable String programName, GitLikeCommandParser<?> parser, UsagePrinter out)
    {
        //
        // Usage
        //

        // build arguments
        List<String> commandArguments = newArrayList();
        commandArguments.addAll(Collections2.transform(parser.getGlobalOptions(), new Function<OptionParser, String>()
        {
            public String apply(OptionParser option)
            {
                if (option.isHidden()) {
                    return null;
                }
                return toUsage(option);
            }
        }));
        out.append("usage:")
                .append(parser.getName())
                .newIndentedPrinter(8) // hanging indent
                .appendWords(commandArguments)
                .append("<command> [<args>]")
                .newline()
                .newline();

        //
        // Common commands
        //

        Map<String, String> commands = newTreeMap();
        for (GroupCommandParser<?> groupCommandParser : parser.getGroupCommandParsers()) {
            if (groupCommandParser.getName().isEmpty()) {
                for (CommandParser<?> commandParser : groupCommandParser.getCommandParsers()) {
                    commands.put(commandParser.getName(), commandParser.getDescription());
                }
            }
            else {
                commands.put(groupCommandParser.getName(), "Manage " + groupCommandParser.getName() + "s");
            }
        }

        out.append("The most commonly used ").append(parser.getName()).append(" commands are:").newline();
        out.newIndentedPrinter(4).appendTable(Iterables.transform(commands.entrySet(), new Function<Entry<String, String>, Iterable<String>>()
        {
            public Iterable<String> apply(Entry<String, String> entry)
            {
                return ImmutableList.of(entry.getKey(), entry.getValue());
            }
        }));
        out.newline();
        out.append("See '").append(parser.getName()).append(" help <command>' for more information on a specific command.").newline();
    }
}
