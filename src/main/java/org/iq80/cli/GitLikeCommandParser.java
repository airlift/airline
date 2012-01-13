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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import org.iq80.cli.ParserUtil.OptionsMetadata;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.iq80.cli.ParserUtil.createInstance;

public class GitLikeCommandParser<C>
{
    public static UntypedGlobalCommandParserBuilder builder(String name)
    {
        Preconditions.checkNotNull(name, "name is null");
        return new UntypedGlobalCommandParserBuilder(name, new TypeConverter(), null);
    }

    /**
     * Name of the global command.
     */
    private final String name;

    private final Class<?> globalOptionsType;

    private final Map<String, GroupCommandParser<C>> groupCommandParsers;

    private final Map<String, OptionParser> globalOptionsIndex;

    private final String optionSeparators;

    public GitLikeCommandParser(String name, TypeConverter typeConverter, String optionSeparators, Iterable<Class<? extends C>> commands)
    {
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");
        Preconditions.checkNotNull(commands, "commands is null");

        this.name = name;
        this.optionSeparators = optionSeparators;

        Set<Class<?>> globalOptionsTypes = newHashSet();
        ListMultimap<String, CommandParser<C>> commandsByGroup = ArrayListMultimap.create();
        for (Class<? extends C> command : commands) {
            CommandParser<C> commandParser = new CommandParser<C>(command, typeConverter, optionSeparators);
            commandsByGroup.put(commandParser.getGroup(), commandParser);
            if (commandParser.getGlobalOptionsAccessor() != null) {
                globalOptionsTypes.add(commandParser.getGlobalOptionsAccessor().getType());
            }
        }

        ImmutableMap.Builder<String, GroupCommandParser<C>> groupCommandParsers = ImmutableMap.builder();
        for (Entry<String, Collection<CommandParser<C>>> entry : commandsByGroup.asMap().entrySet()) {
            GroupCommandParser<C> groupCommandParser = new GroupCommandParser<C>(entry.getKey(), typeConverter, entry.getValue());
            groupCommandParsers.put(groupCommandParser.getName(), groupCommandParser);
        }
        this.groupCommandParsers = groupCommandParsers.build();

        Preconditions.checkArgument(globalOptionsTypes.size() < 2, "Found multiple global options: %s", globalOptionsTypes);
        if (!globalOptionsTypes.isEmpty()) {
            this.globalOptionsType = globalOptionsTypes.iterator().next();
            OptionsMetadata optionsMetadata = ParserUtil.processAnnotations(globalOptionsType, typeConverter);
            if (optionsMetadata.getArgumentParser() != null) {
                throw new ParseException("Global options can not be annotated with @Arguments, found: %s", optionsMetadata.getArgumentParser().getPath());
            }
            if (optionsMetadata.getGlobalOptionsAccessors() != null) {
                throw new ParseException("Global options can not be annotated with @Options(GLOBAL), found: %s", optionsMetadata.getGlobalOptionsAccessors().getPath());
            }
            if (optionsMetadata.getGroupOptionsAccessor() != null) {
                throw new ParseException("Global options can not be annotated with @Options(GROUP), found: %s", optionsMetadata.getGroupOptionsAccessor().getPath());
            }

            ImmutableMap.Builder<String, OptionParser> globalOptionsIndex = ImmutableMap.builder();
            for (OptionParser globalOption : optionsMetadata.getOptions()) {
                for (String optionName : globalOption.getOptions()) {
                    globalOptionsIndex.put(optionName, globalOption);
                }
            }
            this.globalOptionsIndex = globalOptionsIndex.build();
        }
        else {
            this.globalOptionsType = null;
            this.globalOptionsIndex = null;
        }
    }

    public String getName()
    {
        return name;
    }

    public List<GroupCommandParser<C>> getGroupCommandParsers()
    {
        return ImmutableList.copyOf(groupCommandParsers.values());
    }

    public C parse(String... args)
    {
        Preconditions.checkNotNull(args, "args is null");
        return parse(true, ImmutableList.copyOf(args));
    }

    public C parse(boolean validate, Iterable<String> args)
    {
        // unroll the args
        List<String> parameters = ParserUtil.expandArgs(args, optionSeparators);

        // create the global options instance
        Object globalOptionsInstance = createInstance(globalOptionsType);

        // process the arguments
        if (globalOptionsInstance != null) {
            parameters = ParserUtil.parseOptions(globalOptionsInstance, globalOptionsIndex, validate, true, parameters);
        }

        // select the command group
        String groupName = Iterables.getFirst(parameters, null);
        GroupCommandParser<C> group = groupCommandParsers.get(groupName);
        if (group == null) {
            // send args to the default group
            group = groupCommandParsers.get("");
            if (group == null) {
                if (groupName == null) {
                    throw new ParseException("No command specified");
                }
                else {
                    throw new ParseException("Unknown command %s", groupName);
                }
            }
        } else {
            parameters = parameters.subList(1, parameters.size());
        }

        C commandResult = group.parseInternal(globalOptionsInstance, validate, parameters);
        return commandResult;
    }

    //
    // Builder Classes
    //

    public static class UntypedGlobalCommandParserBuilder extends TypedGlobalCommandParserBuilder<Object>
    {
        public UntypedGlobalCommandParserBuilder(String name,
                @Nullable TypeConverter typeConverter,
                @Nullable String optionSeparators)
        {
            super(name, typeConverter, optionSeparators, ImmutableList.<Class<?>>of());
        }

        public UntypedGlobalCommandParserBuilder withTypeConverter(TypeConverter typeConverter)
        {
            Preconditions.checkNotNull(typeConverter, "typeConverter is null");
            return new UntypedGlobalCommandParserBuilder(name, typeConverter, optionSeparators);
        }

        public UntypedGlobalCommandParserBuilder withOptionSeparators(String optionsSeparator)
        {
            Preconditions.checkNotNull(optionsSeparator, "optionsSeparator is null");
            return new UntypedGlobalCommandParserBuilder(name, typeConverter, optionsSeparator);
        }

        public <C> TypedGlobalCommandParserBuilder<C> withCommandType(Class<C> commandType)
        {
            Preconditions.checkNotNull(commandType, "commandType is null");
            return new TypedGlobalCommandParserBuilder<C>(name, typeConverter, optionSeparators, ImmutableList.<Class<? extends C>>of());
        }

        public TypedGlobalCommandParserBuilder<Object> addCommand(Class<?> command)
        {
            Preconditions.checkNotNull(command, "command is null");
            return new TypedGlobalCommandParserBuilder<Object>(
                    name,
                    typeConverter,
                    optionSeparators,
                    ImmutableList.<Class<?>>of(command));
        }
    }

    public static class TypedGlobalCommandParserBuilder<C>
    {
        protected final String name;
        protected final TypeConverter typeConverter;
        protected final String optionSeparators;
        protected final List<Class<? extends C>> commands;

        public TypedGlobalCommandParserBuilder(String name,
                @Nullable TypeConverter typeConverter,
                @Nullable String optionSeparators,
                Iterable<Class<? extends C>> commands)
        {
            Preconditions.checkNotNull(name, "name is null");
            this.name = name;
            this.typeConverter = typeConverter;
            this.optionSeparators = optionSeparators;
            this.commands = ImmutableList.copyOf(commands);
        }

        public TypedGlobalCommandParserBuilder<C> addCommand(Class<? extends C> command)
        {
            Preconditions.checkNotNull(command, "command is null");
            return new TypedGlobalCommandParserBuilder<C>(
                    name,
                    typeConverter,
                    optionSeparators,
                    ImmutableList.<Class<? extends C>>builder().addAll(commands).add(command).build());
        }

        public GitLikeCommandParser<C> build()
        {
            return new GitLikeCommandParser<C>(name, typeConverter, optionSeparators, commands);
        }
    }
}
