package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.iq80.cli.model.ArgumentsMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.OptionMetadata;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

public class UsageHelper
{
    public static final Comparator<OptionMetadata> DEFAULT_OPTION_COMPARATOR = new Comparator<OptionMetadata>()
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
    public static final Comparator<CommandMetadata> DEFAULT_COMMAND_COMPARATOR = new Comparator<CommandMetadata>()
    {
        @Override
        public int compare(CommandMetadata o1, CommandMetadata o2)
        {
            return ComparisonChain.start()
                    .compare(o1.getName().toLowerCase(), o2.getName().toLowerCase())
                    .compare(o2.getName(), o1.getName()) // print lower case letters before upper case
                    .compare(System.identityHashCode(o1), System.identityHashCode(o2))
                    .result();
        }
    };

    public static String toDescription(OptionMetadata option)
    {
        Set<String> options = option.getOptions();
        StringBuilder stringBuilder = new StringBuilder();

        final String argumentString;
        if (option.getArity() > 0) {
            argumentString = Joiner.on(" ").join(Lists.transform(ImmutableList.of(option.getTitle()), new Function<String, String>()
            {
                public String apply(@Nullable String argument)
                {
                    return "<" + argument + ">";
                }
            }));
        } else {
            argumentString = null;
        }

        Joiner.on(", ").appendTo(stringBuilder, Iterables.transform(options, new Function<String, String>()
        {
            public String apply(@Nullable String option)
            {
                if (argumentString != null) {
                    return option + " " + argumentString;
                }
                return option;
            }
        }));

        return stringBuilder.toString();
    }

    public static String toDescription(ArgumentsMetadata arguments)
    {
        if (!arguments.getUsage().isEmpty()) {
            return arguments.getUsage();
        }

        return "<" + arguments.getTitle() + ">";

    }

    public static String toUsage(OptionMetadata option)
    {
        Set<String> options = option.getOptions();
        boolean required = option.isRequired();
        StringBuilder stringBuilder = new StringBuilder();
        if (!required) {
            stringBuilder.append('[');
        }

        if (options.size() > 1) {
            stringBuilder.append('(');
        }

        final String argumentString;
        if (option.getArity() > 0) {
            argumentString = Joiner.on(" ").join(Iterables.transform(ImmutableList.of(option.getTitle()), new Function<String, String>()
            {
                public String apply(@Nullable String argument)
                {
                    return "<" + argument + ">";
                }
            }));
        }
        else {
            argumentString = null;
        }

        Joiner.on(" | ").appendTo(stringBuilder, Iterables.transform(options, new Function<String, String>()
        {
            public String apply(@Nullable String option)
            {
                if (argumentString != null) {
                    return option + " " + argumentString;
                }
                else {
                    return option;
                }
            }
        }));

        if (options.size() > 1) {
            stringBuilder.append(')');
        }

        if (option.isMultiValued()) {
            stringBuilder.append("...");
        }

        if (!required) {
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    public static String toUsage(ArgumentsMetadata arguments)
    {
        if (!arguments.getUsage().isEmpty()) {
            return arguments.getUsage();
        }

        boolean required = arguments.isRequired();
        StringBuilder stringBuilder = new StringBuilder();
        if (!required) {
            stringBuilder.append('[');
        }

        stringBuilder.append("<").append(arguments.getTitle()).append(">");

        if (arguments.isMultiValued()) {
            stringBuilder.append("...");
        }

        if (!required) {
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    public static List<String> toSynopsisUsage(List<OptionMetadata> options)
    {
        List<String> commandArguments = newArrayList();
        commandArguments.addAll(Collections2.transform(options, new Function<OptionMetadata, String>()
        {
            public String apply(OptionMetadata option)
            {
                if (option.isHidden()) {
                    return null;
                }
                return toUsage(option);
            }
        }));
        return commandArguments;
    }
}
