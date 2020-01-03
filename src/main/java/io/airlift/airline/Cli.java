/*
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

package io.airlift.airline;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.airlift.airline.model.ArgumentsMetadata;
import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.MetadataLoader;
import io.airlift.airline.model.OptionMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static io.airlift.airline.ParserUtil.createInstance;
import static io.airlift.airline.ParserUtil.injectOptions;
import static io.airlift.airline.model.MetadataLoader.loadCommand;
import static io.airlift.airline.model.MetadataLoader.loadCommandGroup;
import static io.airlift.airline.model.MetadataLoader.loadCommands;
import static java.util.Objects.requireNonNull;

public class Cli<C>
{
    public static <T> CliBuilder<T> builder(String name)
    {
        requireNonNull(name, "name is null");
        return new CliBuilder<T>(name);
    }

    @Deprecated
    public static CliBuilder<Object> buildCli(String name)
    {
        return builder(name);
    }

    @Deprecated
    public static <T> CliBuilder<T> buildCli(String name, Class<T> commandTypes)
    {
        return builder(name);
    }

    private final GlobalMetadata metadata;

    private Cli(String name,
            String description,
            TypeConverter typeConverter,
            Class<? extends C> defaultCommand,
            Iterable<Class<? extends C>> defaultGroupCommands,
            Iterable<GroupBuilder<C>> groups)
    {
        requireNonNull(name, "name is null");
        requireNonNull(typeConverter, "typeConverter is null");

        CommandMetadata defaultCommandMetadata = null;
        if (defaultCommand != null) {
            defaultCommandMetadata = loadCommand(defaultCommand);
        }

        List<CommandMetadata> defaultCommandGroup = loadCommands(defaultGroupCommands);

        List<CommandGroupMetadata> commandGroups = stream(groups)
                .map(group -> loadCommandGroup(
                        group.name,
                        group.description,
                        Optional.ofNullable(group.defaultCommand)
                                .map(MetadataLoader::loadCommand)
                                .orElse(null),
                        loadCommands(group.commands)))
                .collect(toImmutableList());

        this.metadata = MetadataLoader.loadGlobal(name, description, defaultCommandMetadata, defaultCommandGroup, commandGroups);
    }

    public GlobalMetadata getMetadata()
    {
        return metadata;
    }

    public C parse(String... args)
    {
        return parse(ImmutableList.copyOf(args));
    }

    public C parse(Iterable<String> args)
    {
        return parse(new DefaultCommandFactory<C>(), args);
    }

    public C parse(CommandFactory<C> commandFactory, String... args)
    {
        return parse(commandFactory, ImmutableList.copyOf(args));
    }

    public C parse(CommandFactory<C> commandFactory, Iterable<String> args)
    {
        requireNonNull(args, "args is null");

        Parser parser = new Parser();
        ParseState state = parser.parse(metadata, args);

        if (state.getCommand() == null) {
            if (state.getGroup() != null) {
                state = state.withCommand(state.getGroup().getDefaultCommand());
            }
            else {
                state = state.withCommand(metadata.getDefaultCommand());
            }
        }

        validate(state);

        CommandMetadata command = state.getCommand();

        return createInstance(command.getType(),
                command.getAllOptions(),
                state.getParsedOptions(),
                command.getArguments(),
                state.getParsedArguments(),
                command.getMetadataInjections(),
                ImmutableMap.<Class<?>, Object>of(GlobalMetadata.class, metadata),
                commandFactory);
    }

    public C parse(C commandInstance, String... args)
    {
        requireNonNull(args, "args is null");

        Parser parser = new Parser();
        ParseState state = parser.parse(metadata, args);

        if (state.getCommand() == null) {
            if (state.getGroup() != null) {
                state = state.withCommand(state.getGroup().getDefaultCommand());
            }
            else {
                state = state.withCommand(metadata.getDefaultCommand());
            }
        }

        validate(state);

        CommandMetadata command = state.getCommand();

        return injectOptions(commandInstance,
                command.getAllOptions(),
                state.getParsedOptions(),
                command.getArguments(),
                state.getParsedArguments(),
                command.getMetadataInjections(),
                ImmutableMap.<Class<?>, Object>of(GlobalMetadata.class, metadata));
    }

    private void validate(ParseState state)
    {
        CommandMetadata command = state.getCommand();
        if (command == null) {
            List<String> unparsedInput = state.getUnparsedInput();
            if (unparsedInput.isEmpty()) {
                throw new ParseCommandMissingException();
            }
            else {
                throw new ParseCommandUnrecognizedException(unparsedInput);
            }
        }

        ArgumentsMetadata arguments = command.getArguments();
        if (state.getParsedArguments().isEmpty() && arguments != null && arguments.isRequired()) {
            throw new ParseArgumentsMissingException(arguments.getTitle());
        }

        if (!state.getUnparsedInput().isEmpty()) {
            throw new ParseArgumentsUnexpectedException(state.getUnparsedInput());
        }

        if (state.getLocation() == Context.OPTION) {
            throw new ParseOptionMissingValueException(state.getCurrentOption().getTitle());
        }

        for (OptionMetadata option : command.getAllOptions()) {
            if (option.isRequired() && !state.getParsedOptions().containsKey(option)) {
                throw new ParseOptionMissingException(option.getOptions().iterator().next());
            }
        }
    }

    //
    // Builder Classes
    //

    public static class CliBuilder<C>
    {
        protected final String name;
        protected String description;
        protected TypeConverter typeConverter = new TypeConverter();
        protected String optionSeparators;
        private Class<? extends C> defaultCommand;
        private final List<Class<? extends C>> defaultCommandGroupCommands = new ArrayList<>();
        protected final Map<String, GroupBuilder<C>> groups = new HashMap<>();

        public CliBuilder(String name)
        {
            requireNonNull(name, "name is null");
            checkArgument(!name.isEmpty(), "name is empty");
            this.name = name;
        }

        public CliBuilder<C> withDescription(String description)
        {
            requireNonNull(description, "description is null");
            checkArgument(!description.isEmpty(), "description is empty");
            this.description = description;
            return this;
        }

//        public CliBuilder<C> withTypeConverter(TypeConverter typeConverter)
//        {
//            Preconditions.requireNonNull(typeConverter, "typeConverter is null");
//            this.typeConverter = typeConverter;
//            return this;
//        }

//        public CliBuilder<C> withOptionSeparators(String optionsSeparator)
//        {
//            Preconditions.requireNonNull(optionsSeparator, "optionsSeparator is null");
//            this.optionSeparators = optionsSeparator;
//            return this;
//        }

        public CliBuilder<C> withDefaultCommand(Class<? extends C> defaultCommand)
        {
            this.defaultCommand = defaultCommand;
            return this;
        }

        public CliBuilder<C> withCommand(Class<? extends C> command)
        {
            this.defaultCommandGroupCommands.add(command);
            return this;
        }

        @SafeVarargs
        public final CliBuilder<C> withCommands(Class<? extends C> command, Class<? extends C>... moreCommands)
        {
            this.defaultCommandGroupCommands.add(command);
            this.defaultCommandGroupCommands.addAll(ImmutableList.copyOf(moreCommands));
            return this;
        }

        public CliBuilder<C> withCommands(Iterable<Class<? extends C>> commands)
        {
            this.defaultCommandGroupCommands.addAll(ImmutableList.copyOf(commands));
            return this;
        }

        public GroupBuilder<C> withGroup(String name)
        {
            requireNonNull(name, "name is null");
            checkArgument(!name.isEmpty(), "name is empty");

            if (groups.containsKey(name)) {
                return groups.get(name);
            }

            GroupBuilder<C> group = new GroupBuilder<C>(name);
            groups.put(name, group);
            return group;
        }

        public Cli<C> build()
        {
            return new Cli<C>(name, description, typeConverter, defaultCommand, defaultCommandGroupCommands, groups.values());
        }
    }

    public static class GroupBuilder<C>
    {
        private final String name;
        private String description;
        private Class<? extends C> defaultCommand;

        private final List<Class<? extends C>> commands = new ArrayList<>();

        private GroupBuilder(String name)
        {
            requireNonNull(name, "name is null");
            this.name = name;
        }

        public GroupBuilder<C> withDescription(String description)
        {
            requireNonNull(description, "description is null");
            checkArgument(!description.isEmpty(), "description is empty");
            Preconditions.checkState(this.description == null, "description is already set");
            this.description = description;
            return this;
        }

        public GroupBuilder<C> withDefaultCommand(Class<? extends C> defaultCommand)
        {
            requireNonNull(defaultCommand, "defaultCommand is null");
            Preconditions.checkState(this.defaultCommand == null, "defaultCommand is already set");
            this.defaultCommand = defaultCommand;
            return this;
        }

        public GroupBuilder<C> withCommand(Class<? extends C> command)
        {
            requireNonNull(command, "command is null");
            commands.add(command);
            return this;
        }

        @SafeVarargs
        public final GroupBuilder<C> withCommands(Class<? extends C> command, Class<? extends C>... moreCommands)
        {
            this.commands.add(command);
            this.commands.addAll(ImmutableList.copyOf(moreCommands));
            return this;
        }

        public GroupBuilder<C> withCommands(Iterable<Class<? extends C>> commands)
        {
            this.commands.addAll(ImmutableList.copyOf(commands));
            return this;
        }
    }
}
