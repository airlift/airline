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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import io.airlift.command.model.CommandGroupMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.OptionMetadata;

import java.util.List;

public class ParseState
{
    private final List<Context> locationStack;
    private final CommandGroupMetadata group;
    private final CommandMetadata command;
    private final ListMultimap<OptionMetadata, Object> parsedOptions;
    private final List<Object> parsedArguments;
    private final OptionMetadata currentOption;
    private final List<String> unparsedInput; 

    ParseState(CommandGroupMetadata group,
            CommandMetadata command,
            ListMultimap<OptionMetadata, Object> parsedOptions,
            List<Context> locationStack,
            List<Object> parsedArguments,
            OptionMetadata currentOption,
            List<String> unparsedInput)
    {
        this.group = group;
        this.command = command;
        this.parsedOptions = parsedOptions;
        this.locationStack = locationStack;
        this.parsedArguments = parsedArguments;
        this.currentOption = currentOption;
        this.unparsedInput = unparsedInput;
    }

    public static ParseState newInstance()
    {
        return new ParseState(null, null, ArrayListMultimap.<OptionMetadata, Object>create(), ImmutableList.<Context>of(), ImmutableList.of(), null, ImmutableList.<String>of());
    }

    public ParseState pushContext(Context location)
    {
        ImmutableList<Context> locationStack = ImmutableList.<Context>builder()
                .addAll(this.locationStack)
                .add(location)
                .build();

        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState popContext()
    {
        ImmutableList<Context> locationStack = ImmutableList.copyOf(this.locationStack.subList(0, this.locationStack.size() - 1));
        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState withOptionValue(OptionMetadata option, Object value)
    {
        ImmutableListMultimap<OptionMetadata, Object> newOptions = ImmutableListMultimap.<OptionMetadata, Object>builder()
                .putAll(parsedOptions)
                .put(option, value)
                .build();

        return new ParseState(group, command, newOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState withGroup(CommandGroupMetadata group)
    {
        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState withCommand(CommandMetadata command)
    {
        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState withOption(OptionMetadata option)
    {
        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, option, unparsedInput);
    }

    public ParseState withArgument(Object argument)
    {
        ImmutableList<Object> newArguments = ImmutableList.<Object>builder()
                .addAll(parsedArguments)
                .add(argument)
                .build();

        return new ParseState(group, command, parsedOptions, locationStack, newArguments, currentOption, unparsedInput);
    }


    public ParseState withUnparsedInput(String input)
    {
        ImmutableList<String> newUnparsedInput = ImmutableList.<String>builder()
                .addAll(unparsedInput)
                .add(input)
                .build();

        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, newUnparsedInput);
    }

    @Override
    public String toString()
    {
        return "ParseState{" +
                "locationStack=" + locationStack +
                ", group=" + group +
                ", command=" + command +
                ", parsedOptions=" + parsedOptions +
                ", parsedArguments=" + parsedArguments +
                ", currentOption=" + currentOption +
                ", unparsedInput=" + unparsedInput +
                '}';
    }

    public Context getLocation()
    {
        return locationStack.get(locationStack.size() - 1);
    }

    public CommandGroupMetadata getGroup()
    {
        return group;
    }

    public CommandMetadata getCommand()
    {
        return command;
    }

    public OptionMetadata getCurrentOption()
    {
        return currentOption;
    }

    public ListMultimap<OptionMetadata, Object> getParsedOptions()
    {
        return parsedOptions;
    }

    public List<Object> getParsedArguments()
    {
        return parsedArguments;
    }

    public List<String> getUnparsedInput()
    {
        return unparsedInput;
    }
}
