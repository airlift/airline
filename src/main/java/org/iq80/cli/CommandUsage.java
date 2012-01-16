package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
                    return toUsage(option);
                }
            }));
            arguments = commandParser.getArguments();
            if (arguments != null) {
                commandArguments.add("[--]");
                commandArguments.add(toUsage(arguments));
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
            optionPrinter.append(toDescription(option)).newline();

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
            optionPrinter.append(toDescription(arguments)).newline();

            // description
            descriptionPrinter.append(arguments.getDescription()).newline();
            descriptionPrinter.newline();
        }
    }

    private static String spaces(int count)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(" ");
        }
        return result.toString();
    }

    private String toDescription(OptionParser option)
    {
        List<String> options = option.getOptions();
        StringBuilder stringBuilder = new StringBuilder();

        final String argumentString = Joiner.on(" ").join(Lists.transform(ImmutableList.of(option.getName()), new Function<String, String>()
        {
            public String apply(@Nullable String argument)
            {
                return "<" + argument + ">";
            }
        }));

        Joiner.on(", ").appendTo(stringBuilder, Lists.transform(options, new Function<String, String>()
        {
            public String apply(@Nullable String option1)
            {
                return option1 + " " + argumentString;
            }
        }));

        return stringBuilder.toString();
    }

    private String toDescription(ArgumentParser arguments)
    {
        if (!arguments.getUsage().isEmpty()) {
            return arguments.getUsage();
        }

        return "<" + arguments.getName() + ">";

    }

    private String toUsage(OptionParser option)
    {
        List<String> options = option.getOptions();
        boolean required = option.isRequired();
        StringBuilder stringBuilder = new StringBuilder();
        if (!required) {
            stringBuilder.append('[');
        }

        if (options.size() > 1) {
            stringBuilder.append('(');
        }

        final String argumentString = Joiner.on(" ").join(Lists.transform(ImmutableList.of(option.getName()), new Function<String, String>()
        {
            public String apply(@Nullable String argument)
            {
                return "<" + argument + ">";
            }
        }));

        Joiner.on(" | ").appendTo(stringBuilder, Lists.transform(options, new Function<String, String>()
        {
            public String apply(@Nullable String option1)
            {
                return option1 + " " + argumentString;
            }
        }));

        if (option.isMultiOption()) {
            stringBuilder.append("...");
        }

        if (options.size() > 1) {
            stringBuilder.append(')');
        }

        if (!required) {
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    private String toUsage(ArgumentParser arguments)
    {
        if (!arguments.getUsage().isEmpty()) {
            return arguments.getUsage();
        }

        boolean required = arguments.isRequired();
        StringBuilder stringBuilder = new StringBuilder();
        if (!required) {
            stringBuilder.append('[');
        }

        stringBuilder.append("<").append(arguments.getName()).append(">");

        if (arguments.isMultiOption()) {
            stringBuilder.append("...");
        }

        if (!required) {
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    public static class UsagePrinter
    {
        private final StringBuilder out;
        private final int maxSize;
        private final int indent;
        private final AtomicInteger currentPosition;

        public UsagePrinter(StringBuilder out)
        {
            this(out, 79);
        }

        public UsagePrinter(StringBuilder out, int maxSize)
        {
            this(out, maxSize, 0, new AtomicInteger());
        }

        private UsagePrinter(StringBuilder out, int maxSize, int indent, AtomicInteger currentPosition)
        {
            this.out = out;
            this.maxSize = maxSize;
            this.indent = indent;
            this.currentPosition = currentPosition;
        }

        public UsagePrinter newIndentedPrinter(int size)
        {
            return new UsagePrinter(out, maxSize, indent + size, currentPosition);
        }

        public UsagePrinter newline()
        {
            out.append("\n");
            currentPosition.set(0);
            return this;
        }

        public UsagePrinter append(String value)
        {
            return appendWords(Splitter.onPattern("\\s+").omitEmptyStrings().trimResults().split(String.valueOf(value)));
        }

        public UsagePrinter appendWords(Iterable<String> words)
        {
            for (String word : words) {
                if (currentPosition.get() == 0) {
                    // beginning of line
                    out.append(spaces(indent));
                    currentPosition.getAndAdd((indent));
                }
                else if (word.length() > maxSize || currentPosition.get() + word.length() <= maxSize) {
                    // between words
                    out.append(" ");
                    currentPosition.getAndIncrement();
                }
                else {
                    // wrap line
                    out.append("\n").append(spaces(indent));
                    currentPosition.set(indent);
                }

                out.append(word);
                currentPosition.getAndAdd((word.length()));
            }
            return this;
        }
    }
}
