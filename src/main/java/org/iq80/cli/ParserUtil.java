package org.iq80.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.iq80.cli.model.OptionMetadata;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class ParserUtil
{
    public static <T> T createInstance(Class<T> type)
    {
        if (type != null) {
            try {
                return type.getConstructor().newInstance();
            }
            catch (Exception e) {
                throw new ParseException("Unable to create instance %s", type.getName());
            }
        }
        return null;
    }

    public static List<String> parseOptions(
            List<OptionParser> options,
            boolean validate,
            boolean stopAtFirstUnusedArg,
            Iterable<String> parameters,
            ListMultimap<OptionMetadata, Object> parsedOptions)
    {
        Preconditions.checkNotNull(options, "options is null");
        Preconditions.checkNotNull(parameters, "parameters is null");
        Preconditions.checkNotNull(parsedOptions, "parsedOptions is null");

        // remember which options are used for validation
        List<OptionParser> usedOptions = newArrayList();

        List<String> unusedArguments = newArrayList();
        Iterator<String> iterator = parameters.iterator();
        while (iterator.hasNext()) {
            String arg = iterator.next();
            if (!stopAtFirstUnusedArg && arg.equals("--")) {
                Iterators.addAll(unusedArguments, iterator);
                break;
            }
            else if (ParserUtil.isOption(arg)) {
                OptionParser optionParser = null;
                for (OptionParser option : options) {
                    if (option.canParseOption(arg)) {
                        optionParser = option;
                        break;
                    }
                }
                if (optionParser == null) {
                    throw new ParseException("Unknown option: %s", arg);
                }

                parsedOptions.putAll(optionParser.getMetadata(), optionParser.parseOption(arg, iterator));

                usedOptions.add(optionParser);
            }
            else if (!ParserUtil.isStringEmpty(arg)) {
                unusedArguments.add(arg);
                if (stopAtFirstUnusedArg) {
                    Iterators.addAll(unusedArguments, iterator);
                    break;
                }
            }
        }

        if (validate) {
            validateOptions(options, usedOptions);
        }

        return unusedArguments;
    }

    public static boolean isStringEmpty(String s)
    {
        return s == null || "".equals(s);
    }

    public static boolean isOption(String arg)
    {
        return arg.charAt(0) == '-';
    }

    public static void validateOptions(Iterable<OptionParser> allOptions, Collection<OptionParser> usedOptions)
    {
        Set<OptionParser> missingOptions = newHashSet();
        for (OptionParser optionParser : allOptions) {
            if (optionParser.getMetadata().isRequired() && !usedOptions.contains(optionParser)) {
                missingOptions.add(optionParser);
            }
        }

        if (!missingOptions.isEmpty()) {
            StringBuilder missingFields = new StringBuilder();
            for (OptionParser optionParser : missingOptions) {
                missingFields.append(optionParser.getMetadata().getOptions().iterator().next()).append(" ");
            }
            throw new ParseException("The following options are required: %s", missingFields);
        }
    }
}
