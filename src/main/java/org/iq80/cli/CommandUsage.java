package org.iq80.cli;

import com.google.common.base.Preconditions;
import org.iq80.cli.model.ArgumentsMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.OptionMetadata;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.iq80.cli.UsageHelper.DEFAULT_COMMAND_COMPARATOR;
import static org.iq80.cli.UsageHelper.DEFAULT_OPTION_COMPARATOR;
import static org.iq80.cli.UsageHelper.toSynopsisUsage;

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
        Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
        this.optionComparator = optionComparator;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(@Nullable String programName, @Nullable String groupName, CommandMetadata command)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(programName, groupName, command, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(@Nullable String programName, @Nullable String groupName, CommandMetadata command, StringBuilder out)
    {
        usage(programName, groupName, command, new UsagePrinter(out, columnSize));
    }

    public void usage(@Nullable String programName, @Nullable String groupName, CommandMetadata command, UsagePrinter out)
    {
        //
        // NAME
        //
        out.append("NAME").newline();

        out.newIndentedPrinter(8)
                .append(programName)
                .append(groupName)
                .append(command.getName())
                .append("-")
                .append(command.getDescription())
                .newline()
                .newline();

        //
        // SYNOPSIS
        //
        out.append("SYNOPSIS").newline();
        UsagePrinter synopsis = out.newIndentedPrinter(8).newPrinterWithHangingIndent(8);
        List<OptionMetadata> options = newArrayList();
        if (programName != null) {
            List<OptionMetadata> globalOptions = new ArrayList<OptionMetadata>(command.getGlobalOptions());
            if (optionComparator != null) {
                  Collections.sort(globalOptions, optionComparator);
            }
            synopsis.append(programName).appendWords(toSynopsisUsage(globalOptions));
            options.addAll(command.getGlobalOptions());
        }
        if (groupName != null) {
            List<OptionMetadata> groupOptions = new ArrayList<OptionMetadata>(command.getGroupOptions());
            if (optionComparator != null) {
                  Collections.sort(groupOptions, optionComparator);
            }
            synopsis.append(groupName).appendWords(toSynopsisUsage(groupOptions));
            options.addAll(command.getGroupOptions());
        }
        List<OptionMetadata> commandOptions = new ArrayList<OptionMetadata>(command.getCommandOptions());
        if (optionComparator != null) {
              Collections.sort(commandOptions, optionComparator);
        }
        synopsis.append(command.getName()).appendWords(toSynopsisUsage(commandOptions));
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
            if (optionComparator != null) {
                Collections.sort(options, optionComparator);
            }

            out.append("OPTIONS").newline();

            for (OptionMetadata option : options) {
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
}
