/*
 * Copyright (C) 2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.command;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.airlift.command.model.ArgumentsMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.OptionMetadata;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static io.airlift.command.model.OptionMetadata.isHiddenPredicate;

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
                public String apply(String argument)
                {
                    return "<" + argument + ">";
                }
            }));
        } else {
            argumentString = null;
        }

        Joiner.on(", ").appendTo(stringBuilder, transform(options, new Function<String, String>()
        {
            public String apply(String option)
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
            argumentString = Joiner.on(" ").join(transform(ImmutableList.of(option.getTitle()), new Function<String, String>()
            {
                public String apply(String argument)
                {
                    return "<" + argument + ">";
                }
            }));
        }
        else {
            argumentString = null;
        }

        Joiner.on(" | ").appendTo(stringBuilder, transform(options, new Function<String, String>()
        {
            public String apply(String option)
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
        return ImmutableList.copyOf(transform(filter(options, isHiddenPredicate()), new Function<OptionMetadata, String>()
        {
            @Override
            public String apply(@Nonnull OptionMetadata option)
            {
                return toUsage(option);
            }
        }));
    }
}
