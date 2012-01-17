package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;

public class UsageHelper
{
    public static String toDescription(OptionParser option)
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

    public static String toDescription(ArgumentParser arguments)
    {
        if (!arguments.getUsage().isEmpty()) {
            return arguments.getUsage();
        }

        return "<" + arguments.getName() + ">";

    }

    public static String toUsage(OptionParser option)
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

        final String argumentString;
        if (option.getArity() > 0) {
            argumentString = Joiner.on(" ").join(Lists.transform(ImmutableList.of(option.getName()), new Function<String, String>()
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

        Joiner.on(" | ").appendTo(stringBuilder, Lists.transform(options, new Function<String, String>()
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

    public static String toUsage(ArgumentParser arguments)
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
