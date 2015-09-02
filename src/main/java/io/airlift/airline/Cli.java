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

package io.airlift.airline;

import io.airlift.airline.model.ArgumentsMetadata;
import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.MetadataLoader;
import io.airlift.airline.model.OptionMetadata;
import io.airlift.airline.util.ArgumentChecker;
import io.airlift.airline.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.airlift.airline.ParserUtil.createInstance;

public class Cli<C>
{
    public static <T> CliBuilder<T> builder(String name)
    {
        ArgumentChecker.checkNotNull(name, "name is null");
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
        ArgumentChecker.checkNotNull(name, "name is null");
        ArgumentChecker.checkNotNull(typeConverter, "typeConverter is null");

        CommandMetadata defaultCommandMetadata = null;
        if (defaultCommand != null) {
            defaultCommandMetadata = MetadataLoader.loadCommand(defaultCommand);
        }

        List<CommandMetadata> defaultCommandGroup = MetadataLoader.loadCommands(defaultGroupCommands);

        List<CommandGroupMetadata> commandGroups = CollectionUtils.asList(groups).stream()
                .map(group -> MetadataLoader.loadCommandGroup(group.name, group.description, MetadataLoader.loadCommand(group.defaultCommand), MetadataLoader.loadCommands(group.commands)))
                .collect(Collectors.toList());

        this.metadata = MetadataLoader.loadGlobal(name, description, defaultCommandMetadata, defaultCommandGroup, commandGroups);
    }

    public GlobalMetadata getMetadata()
    {
        return metadata;
    }

    public C parse(String... args)
    {
        return parse(Arrays.asList(args));
    }
    
    public C parse(Iterable<String> args)
    {
        ArgumentChecker.checkNotNull(args, "args is null");
        
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
                CollectionUtils.<Class<?>, Object>asMap(GlobalMetadata.class, metadata));
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
            ArgumentChecker.checkNotNull(name, "name is null");
            ArgumentChecker.checkCondition(!name.isEmpty(), "name is empty");
            this.name = name;
        }

        public CliBuilder<C> withDescription(String description)
        {
            ArgumentChecker.checkNotNull(description, "description is null");
            ArgumentChecker.checkCondition(!description.isEmpty(), "description is empty");
            this.description = description;
            return this;
        }

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

        public CliBuilder<C> withCommands(Class<? extends C> command, Class<? extends C>... moreCommands)
        {
            this.defaultCommandGroupCommands.add(command);
            this.defaultCommandGroupCommands.addAll(Arrays.asList(moreCommands));
            return this;
        }

        public CliBuilder<C> withCommands(Iterable<Class<? extends C>> commands)
        {
            this.defaultCommandGroupCommands.addAll(CollectionUtils.asList(commands));
            return this;
        }

        public GroupBuilder<C> withGroup(String name)
        {
            ArgumentChecker.checkNotNull(name, "name is null");
            ArgumentChecker.checkCondition(!name.isEmpty(), "name is empty");

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
        private String description = null;
        private Class<? extends C> defaultCommand = null;

        private final List<Class<? extends C>> commands = new ArrayList<>();

        private GroupBuilder(String name)
        {
            ArgumentChecker.checkNotNull(name, "name is null");
            this.name = name;
        }

        public GroupBuilder<C> withDescription(String description)
        {
            ArgumentChecker.checkNotNull(description, "description is null");
            ArgumentChecker.checkCondition(!description.isEmpty(), "description is empty");
            ArgumentChecker.checkCondition(this.description == null, "description is already set");
            this.description = description;
            return this;
        }

        public GroupBuilder<C> withDefaultCommand(Class<? extends C> defaultCommand)
        {
            ArgumentChecker.checkNotNull(defaultCommand, "defaultCommand is null");
            ArgumentChecker.checkCondition(this.defaultCommand == null, "defaultCommand is already set");
            this.defaultCommand = defaultCommand;
            return this;
        }

        public GroupBuilder<C> withCommand(Class<? extends C> command)
        {
            ArgumentChecker.checkNotNull(command, "command is null");
            commands.add(command);
            return this;
        }

        public GroupBuilder<C> withCommands(Class<? extends C> command, Class<? extends C>... moreCommands)
        {
            this.commands.add(command);
            this.commands.addAll(Arrays.asList(moreCommands));
            return this;
        }

        public GroupBuilder<C> withCommands(Iterable<Class<? extends C>> commands)
        {
            this.commands.addAll(CollectionUtils.asList(commands));
            return this;
        }
    }
}
