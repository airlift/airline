package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class CommandUsage
{
    private final int columnSize;
    private final Comparator<? super OptionParser> optionComparator;

    public CommandUsage()
    {
        this(79, null);
    }

    public CommandUsage(int columnSize)
    {
        this(columnSize, null);
    }

    public CommandUsage(int columnSize, @Nullable Comparator<? super OptionParser> optionComparator)
    {
        Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
        this.optionComparator = optionComparator;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(@Nullable String programName, @Nullable String groupName, CommandParser<?> commandParser)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(programName, groupName, commandParser, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(@Nullable String programName, @Nullable String groupName, CommandParser<?> commandParser, StringBuilder out)
    {
        usage(programName, groupName, commandParser, new UsagePrinter(out, columnSize));
    }

    public void usage(@Nullable String programName, @Nullable String groupName, CommandParser<?> commandParser, UsagePrinter out)
    {
        //
        // NAME
        //
        out.append("NAME").newline();

        out.newIndentedPrinter(8)
                .append(commandParser.getGroup())
                .append(commandParser.getName())
                .append("-")
                .append(commandParser.getDescription())
                .newline()
                .newline();

        //
        // SYNOPSIS
        //
        out.append("SYNOPSIS").newline();
        ArgumentParser arguments;
        {
            UsagePrinter sectionPrinter = out.newIndentedPrinter(8);
            sectionPrinter
                    .append(programName)
                    .append(groupName)
                    .append(commandParser.getName());

            // build arguments
            List<String> commandArguments = newArrayList();
            commandArguments.addAll(Lists.transform(commandParser.getOptions(), new Function<OptionParser, String>()
            {
                public String apply(OptionParser option)
                {
                    if (option.isHidden()) {
                        return null;
                    }
                    return UsageHelper.toUsage(option);
                }
            }));
            arguments = commandParser.getArguments();
            if (arguments != null) {
                commandArguments.add("[--]");
                commandArguments.add(UsageHelper.toUsage(arguments));
            }
            // indent again to create hanging indent effect
            // send arguments to printer as pre-parsed words to avoid splitting within an argument
            sectionPrinter.newIndentedPrinter(8)
                    .appendWords(commandArguments)
                    .newline()
                    .newline();
        }

        //
        // OPTIONS
        //
        List<OptionParser> options = new ArrayList<OptionParser>(commandParser.getOptions());
        if (optionComparator != null) {
            Collections.sort(options, optionComparator);
        }

        if (options.size() > 0 || arguments != null) {
            out.append("OPTIONS").newline();
        }

        for (OptionParser option : options) {
            // option names
            UsagePrinter optionPrinter = out.newIndentedPrinter(8);
            optionPrinter.append(UsageHelper.toDescription(option)).newline();

            // description
            UsagePrinter descriptionPrinter = optionPrinter.newIndentedPrinter(4);
            descriptionPrinter.append(option.getDescription()).newline();

            // default value
            Object defaultValue = option.getDefaultValue();
            if (defaultValue != null) {
                descriptionPrinter.append("Default:").append(String.valueOf(defaultValue)).newline();
            }
            descriptionPrinter.newline();
        }

        if (arguments != null) {
            // "--" option
            UsagePrinter optionPrinter = out.newIndentedPrinter(8);
            optionPrinter.append("--").newline();

            // description
            UsagePrinter descriptionPrinter = optionPrinter.newIndentedPrinter(4);
            descriptionPrinter.append("This option can be used to separate command-line options from the " +
                    "list of argument, (useful when arguments might be mistaken for command-line options").newline();
            descriptionPrinter.newline();

            // arguments name
            optionPrinter.append(UsageHelper.toDescription(arguments)).newline();

            // description
            descriptionPrinter.append(arguments.getDescription()).newline();
            descriptionPrinter.newline();
        }
    }
}
