/**
 * Copyright (C) 2010 the original author or authors.
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

package org.iq80.cli;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.GlobalMetadata;
import org.iq80.cli.model.OptionMetadata;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class CommandGroupParser<C>
{
    public static <C> CommandGroupParser<C> create(TypeConverter typeConverter, CommandGroupMetadata commandGroup)
    {
        return new CommandGroupParser<C>(commandGroup, typeConverter);
    }

    public static <C> Map<String, CommandGroupParser<C>> createIndex(final TypeConverter typeConverter, List<CommandGroupMetadata> commandGroups)
    {
        ImmutableMap.Builder<String, CommandGroupParser<C>> index = ImmutableMap.builder();
        for (CommandGroupMetadata commandGroup : commandGroups) {
            index.put(commandGroup.getName(), CommandGroupParser.<C>create(typeConverter, commandGroup));
        }
        return index.build();
    }

    private final CommandGroupMetadata metadata;
    private final List<OptionParser> groupOptions;
    private final CommandParser<C> defaultCommand;
    private final Map<String, CommandParser<C>> commands;

    public CommandGroupParser(CommandGroupMetadata metadata, TypeConverter typeConverter)
    {
        Preconditions.checkNotNull(metadata, "metadata is null");
        this.metadata = metadata;
        this.groupOptions = OptionParser.from(typeConverter, metadata.getOptions());
        if (metadata.getDefaultCommand() != null) {
            this.defaultCommand = CommandParser.<C>create(typeConverter, metadata.getDefaultCommand());
        }
        else {
            this.defaultCommand = null;
        }
        this.commands = CommandParser.createIndex(typeConverter, metadata.getCommands());
    }

    public CommandGroupMetadata getMetadata()
    {
        return metadata;
    }

    public C parseInternal(@Nullable GlobalMetadata global, ListMultimap<OptionMetadata, Object> parsedOptions, boolean validate, List<String> args)
    {
        // process global options
        List<String> parameters = ParserUtil.parseOptions(this.groupOptions, validate, true, args, parsedOptions);

        // select a command
        String name = Iterables.getFirst(parameters, null);
        CommandParser<C> command = commands.get(name);
        if (command != null) {
            // remove command name from parameters list
            parameters = parameters.subList(1, parameters.size());
        }
        else {
            // use the default command
            command = defaultCommand;
        }

        if (command == null) {
            if (name == null) {
                throw new ParseException("No command specified");
            }
            else {
                throw new ParseException("Unknown command %s", name);
            }
        }

        C commandResult = command.parseInternal(global, parsedOptions, validate, parameters);
        return commandResult;
    }
}
