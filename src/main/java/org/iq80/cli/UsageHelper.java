package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.iq80.cli.model.ArgumentsMetadata;
import org.iq80.cli.model.OptionMetadata;

import javax.annotation.Nullable;
import java.util.Set;

public class UsageHelper
{
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
}
