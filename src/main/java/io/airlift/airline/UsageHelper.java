package io.airlift.airline;

import io.airlift.airline.model.ArgumentsMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.OptionMetadata;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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


            int result = option1.toLowerCase().compareTo(option2.toLowerCase());
            if(result == 0) {
                result = option2.compareTo(option1); // print lower case letters before upper case
                if(result == 0) {
                    result = Integer.valueOf(System.identityHashCode(o1)).compareTo(System.identityHashCode(o2));
                }
            }
            return result;
        }
    };
    public static final Comparator<CommandMetadata> DEFAULT_COMMAND_COMPARATOR = new Comparator<CommandMetadata>()
    {
        @Override
        public int compare(CommandMetadata o1, CommandMetadata o2)
        {
            int result = o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            if(result == 0) {
                result = o2.getName().compareTo(o1.getName()); // print lower case letters before upper case
                if(result == 0) {
                    result = Integer.valueOf(System.identityHashCode(o1)).compareTo(System.identityHashCode(o2));
                }
            }
            return result;
        }
    };

    public static String toDescription(OptionMetadata option)
    {
        Set<String> options = option.getOptions();

        String argumentString = formatArgumentString(option);

        Set<String> optionsFormatted = formatOptions(options, argumentString);
        return optionsFormatted.stream().collect(Collectors.joining(", "));
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

        String argumentString = formatArgumentString(option);

        Set<String> optionsFormatted = formatOptions(options, argumentString);
        stringBuilder.append(optionsFormatted.stream().collect(Collectors.joining(" | ")));

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
        return options.stream().filter(option -> !option.isHidden()).map(UsageHelper::toUsage).collect(Collectors.toList());
    }

    private static Set<String> formatOptions(Set<String> aOptions, String aArgumentString) {
        Set<String> optionsFormatted = aOptions;
        if(aArgumentString != null) {
            optionsFormatted = aOptions.stream().map(entry -> entry + ' ' + aArgumentString).collect(Collectors.toSet());
        }
        return optionsFormatted;
    }

    private static String formatArgumentString(OptionMetadata option) {
        final String argumentString;
        if (option.getArity() > 0) {
            argumentString = '<' + option.getTitle() + '>';
        } else {
            argumentString = null;
        }
        return argumentString;
    }
}
