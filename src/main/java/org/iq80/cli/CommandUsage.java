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
        usage(programName, groupName, commandParser, out, 0);
    }

    public void usage(@Nullable String programName, @Nullable String groupName, CommandParser<?> commandParser, StringBuilder out, int indent)
    {
        //
        // NAME
        //
        out.append(spaces(indent)).append("NAME\n");
        out.append(spaces(indent + 8));
        int currentPosition = indent + 8;
        if (commandParser.getGroup() != null) {
            out.append(commandParser.getGroup()).append(" ");
            currentPosition += commandParser.getGroup().length() + 1;
        }
        out.append(commandParser.getName()).append(" - ");
        currentPosition += commandParser.getName().length() + 3;
        wrap(commandParser.getDescription(), indent + 12, columnSize, out, currentPosition);
        out.append("\n\n");

        //
        // SYNOPSIS
        //
        out.append(spaces(indent)).append("SYNOPSIS\n");
        out.append(spaces(indent + 8));
        List<String> commandArguments = newArrayList();
        if (groupName != null && !programName.isEmpty()) {
            commandArguments.add(programName);
        }
        if (groupName != null && !groupName.isEmpty()) {
            commandArguments.add(groupName);
        }
        commandArguments.add(commandParser.getName());
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
        ArgumentParser arguments = commandParser.getArguments();
        if (arguments != null) {
            commandArguments.add("[--]");
            commandArguments.add(toUsage(arguments));
        }
        wrap(commandArguments, indent + 16, columnSize, out, indent + 8);
        out.append("\n\n");

        //
        // OPTIONS
        //
        List<OptionParser> options = new ArrayList<OptionParser>(commandParser.getOptions());
        if (optionComparator != null) {
            Collections.sort(options, optionComparator);
        }

        if (options.size() > 0 || arguments != null) {
            out.append(spaces(indent)).append("OPTIONS\n");
        }

        for (OptionParser option : options) {
            // option names
            out.append(spaces(indent + 8)).append(toDescription(option));

            // description
            out.append("\n").append(spaces(indent + 12));
            wrap(option.getDescription(), indent + 12, columnSize, out, indent + 12);
            out.append("\n");

            // default value
            Object defaultValue = option.getDefaultValue();
            if (defaultValue != null) {
                out.append("\n").append(spaces(indent + 12)).append("Default: ").append(defaultValue);
            }

            out.append("\n");
        }

        if (arguments != null) {
            // "--" option
            out.append(spaces(indent + 8)).append("--");

            // description
            out.append("\n").append(spaces(indent + 12));
            wrap("This option can be used to separate command-line options from the list of argument, (useful when arguments might be mistaken for command-line options",
                    indent + 12,
                    columnSize,
                    out,
                    indent + 12);

            out.append("\n\n");

            // arguments name
            out.append(spaces(indent + 8)).append(toDescription(arguments));

            // description
            out.append("\n").append(spaces(indent + 12));
            wrap(arguments.getDescription(), indent + 12, columnSize, out, indent + 12);
            out.append("\n");

            out.append("\n");

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

    private void wrap(String description, int indent, int maxSize, StringBuilder out, int currentPosition)
    {
        Iterable<String> words = Splitter.onPattern("\\W+").omitEmptyStrings().trimResults().split(description);
        wrap(words, indent, maxSize, out, currentPosition);
    }

    private void wrap(Iterable<String> words, int indent, int maxSize, StringBuilder out, int currentPosition)
    {
        boolean isFirst = true;
        for (String word : words) {
            if (word.length() > maxSize || currentPosition + word.length() <= maxSize) {
                if (!isFirst) {
                    out.append(" ");
                }
                currentPosition++;
            }
            else {
                out.append("\n").append(spaces(indent));
                currentPosition = indent;
            }
            out.append(word);
            currentPosition += word.length();
            isFirst = false;
        }
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
}
