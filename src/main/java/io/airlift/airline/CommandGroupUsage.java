package io.airlift.airline;

import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.OptionMetadata;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static io.airlift.airline.UsageHelper.DEFAULT_COMMAND_COMPARATOR;
import static io.airlift.airline.UsageHelper.DEFAULT_OPTION_COMPARATOR;

public class CommandGroupUsage
{
    private final int columnSize;
    private final boolean hideGlobalOptions;
    private final Comparator<? super OptionMetadata> optionComparator;
    private final Comparator<? super CommandMetadata> commandComparator = DEFAULT_COMMAND_COMPARATOR;

    public CommandGroupUsage()
    {
        this(79, false, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandGroupUsage(int columnSize)
    {
        this(columnSize, false, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandGroupUsage(int columnSize, boolean hideGlobalOptions)
    {
        this(columnSize, hideGlobalOptions, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandGroupUsage(int columnSize, boolean hideGlobalOptions, @Nullable Comparator<? super OptionMetadata> optionComparator)
    {
        checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
        this.hideGlobalOptions = hideGlobalOptions;
        this.optionComparator = optionComparator;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(@Nullable GlobalMetadata global, CommandGroupMetadata group)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(global, group, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(@Nullable GlobalMetadata global, CommandGroupMetadata group, StringBuilder out)
    {
        usage(global, group, new UsagePrinter(out, columnSize));
    }

    public void usage(@Nullable GlobalMetadata global, CommandGroupMetadata group, UsagePrinter out)
    {
        //
        // NAME
        //
        out.append("NAME").newline();

        out.newIndentedPrinter(8)
                .append(global.getName())
                .append(group.getName())
                .append("-")
                .append(group.getDescription())
                .newline()
                .newline();

        //
        // SYNOPSIS
        //
        out.append("SYNOPSIS").newline();
        UsagePrinter synopsis = out.newIndentedPrinter(8).newPrinterWithHangingIndent(8);

        List<CommandMetadata> commands = new ArrayList<>(group.getCommands());
        Collections.sort(commands, commandComparator);

        if (group.getDefaultCommand() != null) {
            CommandMetadata command = group.getDefaultCommand();
            if (global != null) {
                synopsis.append(global.getName());
                if (!hideGlobalOptions) {
                    synopsis.appendWords(UsageHelper.toSynopsisUsage(command.getGlobalOptions()));
                }
            }
            synopsis.append(group.getName()).appendWords(UsageHelper.toSynopsisUsage(command.getGroupOptions()));
            synopsis.newline();
        }
        for (CommandMetadata command : commands) {
            if (global != null) {
                synopsis.append(global.getName());
                if (!hideGlobalOptions) {
                    synopsis.appendWords(UsageHelper.toSynopsisUsage(command.getGlobalOptions()));
                }
            }
            synopsis.append(group.getName()).appendWords(UsageHelper.toSynopsisUsage(command.getGroupOptions()));
            synopsis.append(command.getName()).appendWords(UsageHelper.toSynopsisUsage(command.getCommandOptions()));
            synopsis.newline();
        }
        synopsis.newline();

        //
        // OPTIONS
        //
        List<OptionMetadata> options = new ArrayList<>();
        options.addAll(group.getOptions());
        if (global != null && !hideGlobalOptions) {
            options.addAll(global.getOptions());
        }
        if (options.size() > 0) {
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
        }

        //
        // COMMANDS
        //
        if (commands.size() > 0 || group.getDefaultCommand() != null) {
            out.append("COMMANDS").newline();
            UsagePrinter commandPrinter = out.newIndentedPrinter(8);

            if (group.getDefaultCommand() != null && group.getDefaultCommand().getDescription() != null) {
                commandPrinter.append("With no arguments,")
                        .append(group.getDefaultCommand().getDescription())
                        .newline()
                        .newline();
            }

            for (CommandMetadata command : group.getCommands()) {
                commandPrinter.append(command.getName()).newline();
                UsagePrinter descriptionPrinter = commandPrinter.newIndentedPrinter(4);

                descriptionPrinter.append(command.getDescription()).newline().newline();

                for (OptionMetadata option : command.getCommandOptions()) {
                    if (!option.isHidden() && option.getDescription() != null) {
                        descriptionPrinter.append("With")
                                .append(longest(option.getOptions()))
                                .append("option,")
                                .append(option.getDescription())
                                .newline()
                                .newline();
                    }
                }
            }
        }
    }

    private static String longest(Iterable<String> iterable)
    {
        String longest = "";
        for (String value : iterable) {
            if (value.length() > longest.length()) {
                longest = value;
            }
        }
        return longest;
    }
}
