package io.airlift.airline;

import io.airlift.airline.model.ArgumentsMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.OptionMetadata;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static io.airlift.airline.UsageHelper.DEFAULT_OPTION_COMPARATOR;
import static io.airlift.airline.UsageHelper.toSynopsisUsage;

public class CommandUsage
{
    private final int columnSize;
    private final Comparator<? super OptionMetadata> optionComparator;

    public CommandUsage()
    {
        this(79, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandUsage(int columnSize)
    {
        this(columnSize, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandUsage(int columnSize, @Nullable Comparator<? super OptionMetadata> optionComparator)
    {
        checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
        this.optionComparator = optionComparator;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(@Nullable String programName, @Nullable String groupName, String commandName, CommandMetadata command)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(programName, groupName, commandName, command, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(@Nullable String programName, @Nullable String groupName, String commandName, CommandMetadata command, StringBuilder out)
    {
        usage(programName, groupName, commandName, command, new UsagePrinter(out, columnSize));
    }

    public void usage(@Nullable String programName, @Nullable String groupName, String commandName, CommandMetadata command, UsagePrinter out)
    {
        //
        // NAME
        //
        out.append("NAME").newline();

        out.newIndentedPrinter(8)
                .append(programName)
                .append(groupName)
                .append(commandName)
                .append("-")
                .append(command.getDescription())
                .newline()
                .newline();

        //
        // SYNOPSIS
        //
        out.append("SYNOPSIS").newline();
        UsagePrinter synopsis = out.newIndentedPrinter(8).newPrinterWithHangingIndent(8);
        List<OptionMetadata> options = new ArrayList<>();
        if (programName != null) {
            synopsis.append(programName).appendWords(toSynopsisUsage(sortOptions(command.getGlobalOptions())));
            options.addAll(command.getGlobalOptions());
        }
        if (groupName != null) {
            synopsis.append(groupName).appendWords(toSynopsisUsage(sortOptions(command.getGroupOptions())));
            options.addAll(command.getGroupOptions());
        }
        synopsis.append(commandName).appendWords(toSynopsisUsage(sortOptions(command.getCommandOptions())));
        options.addAll(command.getCommandOptions());

        // command arguments (optional)
        ArgumentsMetadata arguments = command.getArguments();
        if (arguments != null) {
            synopsis.append("[--]")
                    .append(UsageHelper.toUsage(arguments));
        }
        synopsis.newline();
        synopsis.newline();

        //
        // OPTIONS
        //
        if (options.size() > 0 || arguments != null) {
            options = sortOptions(options);

            out.append("OPTIONS").newline();

            for (OptionMetadata option : options) {
                // skip hidden options
                if (option.isHidden()) {
                    continue;
                }

                // option names
                UsagePrinter optionPrinter = out.newIndentedPrinter(8);
                optionPrinter.append(UsageHelper.toDescription(option)).newline();

                // description
                UsagePrinter descriptionPrinter = optionPrinter.newIndentedPrinter(4);
                descriptionPrinter.append(option.getDescription()).newline();

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

    private List<OptionMetadata> sortOptions(List<OptionMetadata> options)
    {
        if (optionComparator != null) {
            options = new ArrayList<OptionMetadata>(options);
            Collections.sort(options, optionComparator);
        }
        return options;
    }
}
