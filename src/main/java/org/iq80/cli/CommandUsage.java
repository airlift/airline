package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import org.iq80.cli.model.ArgumentsMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.OptionMetadata;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class CommandUsage
{
    private final int columnSize;
    private final Comparator<? super OptionMetadata> optionComparator;

    private static final Comparator<OptionMetadata> DEFAULT_OPTION_COMPARATOR = new Comparator<OptionMetadata>()
    {
        @Override
        public int compare(OptionMetadata o1, OptionMetadata o2)
        {
            String option1 = o1.getOptions().iterator().next();
            option1 = option1.replaceFirst("^-+", "");

            String option2 = o2.getOptions().iterator().next();
            option2 = option2.replaceFirst("^-+", "");

            return ComparisonChain.start()
                    .compare(option1.toLowerCase(), option2.toLowerCase())
                    .compare(option2, option1) // print lower case letters before upper case
                    .compare(System.identityHashCode(o1), System.identityHashCode(o2))
                    .result();
        }
    };

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
        if (programName != null) {
            synopsis.append(programName).appendWords(toSynopsisUsage(command.getGlobalOptions()));
        }
        if (groupName != null) {
            synopsis.append(groupName).appendWords(toSynopsisUsage(command.getGroupOptions()));
        }
        synopsis.append(command.getName()).appendWords(toSynopsisUsage(command.getCommandOptions()));

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
        List<OptionMetadata> options = newArrayList(command.getAllOptions());
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

    private List<String> toSynopsisUsage(List<OptionMetadata> options)
    {
        List<String> commandArguments = newArrayList();
        commandArguments.addAll(Lists.transform(options, new Function<OptionMetadata, String>()
        {
            public String apply(OptionMetadata option)
            {
                return UsageHelper.toUsage(option);
            }
        }));
        return commandArguments;
    }
}
