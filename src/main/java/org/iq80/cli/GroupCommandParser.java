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
import org.iq80.cli.ParserUtil.OptionsMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.iq80.cli.ParserUtil.createInstance;

public class GroupCommandParser<C>
{
    private final String name;

    private final Class<?> groupOptionsType;

    private final CommandParser<C> defaultCommandParser;

    private final Map<String, CommandParser<C>> commandParserIndex;

    private final Map<String, OptionParser> groupOptionsIndex;

    public GroupCommandParser(String name, TypeConverter typeConverter, Iterable<CommandParser<C>> commandParsers)
    {
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");
        Preconditions.checkNotNull(commandParsers, "commands is null");

        this.name = name;

        CommandParser<C> defaultCommandParser = null;
        Set<Class<?>> groupOptionsTypes = newHashSet();
        ImmutableMap.Builder<String, CommandParser<C>> commandParserIndex = ImmutableMap.builder();
        for (CommandParser<C> commandParser : commandParsers) {
            commandParserIndex.put(commandParser.getName(), commandParser);
            if (commandParser.isDefaultCommand()) {
                if (defaultCommandParser == null) {
                    defaultCommandParser = commandParser;
                }
                else {
                    throw new ParseException("Group %s has multiple default commands: %s and %s", name, defaultCommandParser.getName(), commandParser.getName());
                }
            }
            if (commandParser.getGroupOptionsAccessor() != null) {
                groupOptionsTypes.add(commandParser.getGroupOptionsAccessor().getType());
            }
        }
        this.defaultCommandParser = defaultCommandParser;
        this.commandParserIndex = commandParserIndex.build();

        Preconditions.checkArgument(groupOptionsTypes.size() < 2, "Found multiple group options: %s", groupOptionsTypes);
        if (!groupOptionsTypes.isEmpty()) {
            this.groupOptionsType = groupOptionsTypes.iterator().next();
            OptionsMetadata optionsMetadata = ParserUtil.processAnnotations(groupOptionsType, typeConverter);
            if (optionsMetadata.getArgumentParser() != null) {
                throw new ParseException("Group options can not be annotated with @Arguments, found: %s", optionsMetadata.getArgumentParser().getPath());
            }
            if (optionsMetadata.getGroupOptionsAccessor() != null) {
                throw new ParseException("Group options can not be annotated with @Options(GROUP), found: %s", optionsMetadata.getGroupOptionsAccessor().getPath());
            }
            if (optionsMetadata.getGroupOptionsAccessor() != null) {
                throw new ParseException("Group options can not be annotated with @Options(GROUP), found: %s", optionsMetadata.getGroupOptionsAccessor().getPath());
            }

            ImmutableMap.Builder<String, OptionParser> groupOptionsIndex = ImmutableMap.builder();
            for (OptionParser groupOption : optionsMetadata.getOptions()) {
                for (String optionName : groupOption.getOptions()) {
                    groupOptionsIndex.put(optionName, groupOption);
                }
            }
            this.groupOptionsIndex = groupOptionsIndex.build();
        }
        else {
            this.groupOptionsType = null;
            this.groupOptionsIndex = ImmutableMap.of();
        }
    }

    public String getName()
    {
        return name;
    }

    public List<CommandParser<?>> getCommandParsers()
    {
        return new ArrayList<CommandParser<?>>(commandParserIndex.values());
    }

    public C parseInternal(Object globalOptionsInstance, boolean validate, List<String> parameters)
    {
        // create the group options instance
        Object groupOptionsInstance = createInstance(groupOptionsType);

        // process the arguments
        if (groupOptionsInstance != null) {
            parameters = ParserUtil.parseOptions(globalOptionsInstance, groupOptionsIndex, validate, true, parameters);
        }

        // select the command
        String commandName = Iterables.getFirst(parameters, null);
        CommandParser<C> commandParser;
        if (commandName != null) {
            commandParser = commandParserIndex.get(commandName);
            if (commandParser == null) {
                throw new ParseException("Unknown command %s", commandName);
            }
            parameters = parameters.subList(1, parameters.size());
        }
        else {
            commandParser = defaultCommandParser;
            if (commandParser == null) {
                if (name.isEmpty()) {
                    throw new ParseException("No command specified");
                }
                else {
                    throw new ParseException("Unknown command %s", name);
                }
            }
        }

        C commandResult = commandParser.parseInternal(globalOptionsInstance, groupOptionsInstance, validate, parameters);
        return commandResult;
    }
}
