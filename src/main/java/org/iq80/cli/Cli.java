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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.iq80.cli.model.ArgumentsMetadata;
import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.GlobalMetadata;
import org.iq80.cli.model.MetadataLoader;
import org.iq80.cli.model.OptionMetadata;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.iq80.cli.ParserUtil.createInstance;

public class Cli<C>
{

    public static CliBuilder<Object> buildCli(String name)
    {
        Preconditions.checkNotNull(name, "name is null");
        return new CliBuilder<Object>(name);
    }

    public static <T> CliBuilder<T> buildCli(String name, Class<T> commandTypes)
    {
        Preconditions.checkNotNull(name, "name is null");
        return new CliBuilder<T>(name);
    }

    private final GlobalMetadata metadata;

    private Cli(String name,
            String description,
            TypeConverter typeConverter,
            Class<? extends C> defaultCommand,
            Iterable<Class<? extends C>> defaultGroupCommands,
            Iterable<GroupBuilder<C>> groups)
    {
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");

        CommandMetadata defaultCommandMetadata = null;
        if (defaultCommand != null) {
            defaultCommandMetadata = MetadataLoader.loadCommand(defaultCommand);
        }

        List<CommandMetadata> defaultCommandGroup = MetadataLoader.loadCommands(defaultGroupCommands);

        List<CommandGroupMetadata> commandGroups = ImmutableList.copyOf(Iterables.transform(groups, new Function<GroupBuilder<C>, CommandGroupMetadata>()
        {
            public CommandGroupMetadata apply(GroupBuilder<C> group)
            {
                return MetadataLoader.loadCommandGroup(group.name, group.description, MetadataLoader.loadCommand(group.defaultCommand), MetadataLoader.loadCommands(group.commands));
            }
        }));

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
        Preconditions.checkNotNull(args, "args is null");
        
        Parser parser = new Parser(metadata);
        ParseState state = parser.parse(args);

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
                ImmutableMap.<Class<?>, Object>of(GlobalMetadata.class, metadata));
    }
    
    private void validate(ParseState state)
    {
        CommandMetadata command = state.getCommand();
        if (command == null) {
            throw new ParseException("No command specified");
        }

        ArgumentsMetadata arguments = command.getArguments();
        if (state.getParsedArguments().isEmpty() && arguments != null && arguments.isRequired()) {
            throw new ParseException("Required parameters are missing: %s", arguments.getTitle());
        }
        
        if (!state.getUnparsedInput().isEmpty()) {
            throw new ParseException("Found unexpected parameters: %s", state.getUnparsedInput());
        }

        if (state.getLocation() == Context.OPTION) {
            throw new ParseException("Required values for option '%s' not provided", state.getCurrentOption().getTitle());
        }

        for (OptionMetadata option : command.getAllOptions()) {
            if (option.isRequired() && !state.getParsedOptions().containsKey(option)) {
                throw new ParseException("Required option '%s' is missing", option.getOptions().iterator().next());
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
        private final List<Class<? extends C>> defaultCommandGroupCommands = newArrayList();
        protected final Map<String, GroupBuilder<C>> groups = newHashMap();

        public CliBuilder(String name)
        {
            Preconditions.checkNotNull(name, "name is null");
            Preconditions.checkArgument(!name.isEmpty(), "name is empty");
            this.name = name;
        }

        public CliBuilder<C> withDescription(String description)
        {
            Preconditions.checkNotNull(description, "description is null");
            Preconditions.checkArgument(!description.isEmpty(), "description is empty");
            this.description = description;
            return this;
        }

//        public CliBuilder<C> withTypeConverter(TypeConverter typeConverter)
//        {
//            Preconditions.checkNotNull(typeConverter, "typeConverter is null");
//            this.typeConverter = typeConverter;
//            return this;
//        }

//        public CliBuilder<C> withOptionSeparators(String optionsSeparator)
//        {
//            Preconditions.checkNotNull(optionsSeparator, "optionsSeparator is null");
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

        public CliBuilder<C> withCommands(Class<? extends C> command, Class<? extends C>... moreCommands)
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
            Preconditions.checkNotNull(name, "name is null");
            Preconditions.checkArgument(!name.isEmpty(), "name is empty");
            Preconditions.checkArgument(!groups.containsKey(name), "Group %s has already been declared", name);

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

        private final List<Class<? extends C>> commands = newArrayList();

        private GroupBuilder(String name)
        {
            Preconditions.checkNotNull(name, "name is null");
            this.name = name;
        }

        public GroupBuilder<C> withDescription(String description)
        {
            Preconditions.checkNotNull(description, "description is null");
            Preconditions.checkArgument(!description.isEmpty(), "description is empty");
            this.description = description;
            return this;
        }

        public GroupBuilder<C> withDefaultCommand(Class<? extends C> defaultCommand)
        {
            Preconditions.checkNotNull(defaultCommand, "defaultCommand is null");
            this.defaultCommand = defaultCommand;
            return this;
        }

        public GroupBuilder<C> withCommand(Class<? extends C> command)
        {
            Preconditions.checkNotNull(command, "command is null");
            commands.add(command);
            return this;
        }

        public GroupBuilder<C> withCommands(Class<? extends C> command, Class<? extends C>... moreCommands)
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
