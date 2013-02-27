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

package io.airlift.command;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import io.airlift.command.model.ArgumentsMetadata;
import io.airlift.command.model.CommandGroupMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.GlobalMetadata;
import io.airlift.command.model.MetadataLoader;
import io.airlift.command.model.OptionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static io.airlift.command.ParserUtil.createInstance;

public class Cli<C>
{
    public static <T> CliBuilder<T> builder(String name)
    {
        Preconditions.checkNotNull(name, "name is null");
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
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");

        CommandMetadata defaultCommandMetadata = null;
        if (defaultCommand != null) {
            defaultCommandMetadata = MetadataLoader.loadCommand(defaultCommand);
        }

        final List<CommandMetadata> allCommands = new ArrayList<CommandMetadata>();
        
        List<CommandMetadata> defaultCommandGroup = Lists.newArrayList(MetadataLoader.loadCommands(defaultGroupCommands));

        // currentlly the default command is required to be in the commands list. If that changes, we'll need to add it here and add checks for existence
        allCommands.addAll(defaultCommandGroup);
        
        List<CommandGroupMetadata> commandGroups = Lists.newArrayList(Iterables.transform(groups, new Function<GroupBuilder<C>, CommandGroupMetadata>()
        {
            public CommandGroupMetadata apply(GroupBuilder<C> group)
            {
                CommandMetadata groupDefault = MetadataLoader.loadCommand(group.defaultCommand);
                List<CommandMetadata> groupCommands = MetadataLoader.loadCommands(group.commands);

                // currentlly the default command is required to be in the commands list. If that changes, we'll need to add it here and add checks for existence
                allCommands.addAll(groupCommands);

                return MetadataLoader.loadCommandGroup(group.name, group.description, groupDefault, groupCommands);
            }
        }));

        // add commands to groups based on the value of groups in the @Command annotations
        // rather than change the entire way metadata is loaded, I figured just post-processing was an easier, yet uglier, way to go
        MetadataLoader.loadCommandsIntoGroupsByAnnotation(allCommands,commandGroups, defaultCommandGroup);
        
        this.metadata = MetadataLoader.loadGlobal(name, description, defaultCommandMetadata, ImmutableList.copyOf(defaultCommandGroup), ImmutableList.copyOf(commandGroups));
    }

    public GlobalMetadata getMetadata()
    {
        return metadata;
    }

    public C parse(CommandFactory<C> commandFactory, String... args)
    {
        return parse(commandFactory, ImmutableList.copyOf(args));
    }    
    
    public C parse(String... args)
    {
        return parse(new CommandFactoryDefault<C>(), ImmutableList.copyOf(args));
    }

    public C parse(Iterable<String> args) 
    {
        return parse(new CommandFactoryDefault<C>(), args);
    }

    public C parse(CommandFactory<C> commandFactory, Iterable<String> args)
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

        ImmutableMap.Builder<Class<?>, Object> bindings = ImmutableMap.<Class<?>, Object>builder().put(GlobalMetadata.class, metadata);

        if (state.getGroup() != null) {
            bindings.put(CommandGroupMetadata.class, state.getGroup());
        }

        if (state.getCommand() != null) {
            bindings.put(CommandMetadata.class, state.getCommand());
        }

        return createInstance(command.getType(),
                command.getAllOptions(),
                state.getParsedOptions(),
                command.getArguments(),
                state.getParsedArguments(),
                command.getMetadataInjections(),
                bindings.build(),
                commandFactory);
    }

    public C parse(C commandInstance, String... args)
    {
        Preconditions.checkNotNull(args, "args is null");
        
        Parser parser = new Parser(metadata);
        ParseState state = parser.parse(args);

        CommandMetadata command = MetadataLoader.loadCommand(commandInstance.getClass());

        state = state.withCommand(command);

        validate(state);


        ImmutableMap.Builder<Class<?>, Object> bindings = ImmutableMap.<Class<?>, Object>builder().put(GlobalMetadata.class, metadata);

        if (state.getGroup() != null) {
            bindings.put(CommandGroupMetadata.class, state.getGroup());
        }

        bindings.put(CommandMetadata.class, command);

        C c = (C) ParserUtil.injectOptions(commandInstance,
            command.getAllOptions(),
            state.getParsedOptions(),
            command.getArguments(),
            state.getParsedArguments(),
            command.getMetadataInjections(),
            bindings.build());
        
        return c;
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
        private final List<Class<? extends C>> defaultCommandGroupCommands = newArrayList();
        protected final Map<String, GroupBuilder<C>> groups = newHashMap();
        protected CommandFactory<C> commandFactory;

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
        
        public CliBuilder<C> withCommandFactory(CommandFactory<C> commandFactory) 
        {
            this.commandFactory = commandFactory;
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

        public GroupBuilder<C> getGroup(final String theName) {
            Preconditions.checkNotNull(theName, "name is null");
            Preconditions.checkArgument(!theName.isEmpty(), "name is empty");
            Preconditions.checkArgument(groups.containsKey(theName), "Group %s has not been declared", theName);

            return groups.get(theName);
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
