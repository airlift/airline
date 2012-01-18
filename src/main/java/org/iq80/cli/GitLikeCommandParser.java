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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.GlobalMetadata;
import org.iq80.cli.model.MetadataLoader;
import org.iq80.cli.model.OptionMetadata;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class GitLikeCommandParser<C>
{

    public static Builder<Object> parser(String name)
    {
        Preconditions.checkNotNull(name, "name is null");
        return new Builder<Object>(name);
    }

    public static <T> Builder<T> parser(String name, Class<T> commandTypes)
    {
        Preconditions.checkNotNull(name, "name is null");
        return new Builder<T>(name);
    }

    private final GlobalMetadata metadata;

    private final List<OptionParser> globalOptions;
    private final CommandParser<C> defaultCommand;
    private final Map<String, CommandParser<C>> defaultGroupCommands;
    private final Map<String, CommandGroupParser<C>> commandGroups;

    private GitLikeCommandParser(String name,
            String description,
            TypeConverter typeConverter,
            Class<? extends C> defaultCommand,
            Iterable<Class<? extends C>> defaultGroupCommands,
            Iterable<CommandGroup<C>> groups)
    {
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");

        CommandMetadata defaultCommandMetadata = null;
        if (defaultCommand != null) {
            defaultCommandMetadata = MetadataLoader.loadCommand(defaultCommand);
        }

        List<CommandMetadata> defaultCommandGroup = MetadataLoader.loadCommands(defaultGroupCommands);

        List<CommandGroupMetadata> commandGroups = ImmutableList.copyOf(Iterables.transform(groups, new Function<CommandGroup<C>, CommandGroupMetadata>()
        {
            public CommandGroupMetadata apply(CommandGroup<C> group)
            {
                return MetadataLoader.loadCommandGroup(group.name, group.description, MetadataLoader.loadCommand(group.defaultCommand), MetadataLoader.loadCommands(group.commands));
            }
        }));

        this.metadata = MetadataLoader.loadGlobal(name, description, defaultCommandMetadata, defaultCommandGroup, commandGroups);



        this.globalOptions = OptionParser.from(typeConverter, metadata.getOptions());
        if (metadata.getDefaultCommand() != null) {
            this.defaultCommand = CommandParser.<C>create(typeConverter, metadata.getDefaultCommand());
        }
        else {
            this.defaultCommand = null;
        }
        this.defaultGroupCommands = CommandParser.createIndex(typeConverter, metadata.getDefaultGroupCommands());
        this.commandGroups = CommandGroupParser.createIndex(typeConverter, metadata.getCommandGroups());
    }

    public GlobalMetadata getMetadata()
    {
        return metadata;
    }

    public C parse(String... args)
    {
        Preconditions.checkNotNull(args, "args is null");
        return parse(true, ImmutableList.copyOf(args));
    }

    public C parse(boolean validate, Iterable<String> args)
    {
        // process global options
        ListMultimap<OptionMetadata, Object> parsedOptions = ArrayListMultimap.create();
        List<String> parameters = ParserUtil.parseOptions(this.globalOptions, validate, true, args, parsedOptions);

        // select the command group
        String name = Iterables.getFirst(parameters, null);
        CommandGroupParser<C> commandGroup = commandGroups.get(name);
        if (commandGroup != null) {
            // remove group name from parameters list
            parameters = parameters.subList(1, parameters.size());
            C commandResult = commandGroup.parseInternal(parsedOptions, validate, parameters);
            return commandResult;
        }

        // select a command in the default group
        CommandParser<C> command = defaultGroupCommands.get(name);
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

        C commandResult = command.parseInternal(parsedOptions, validate, parameters);
        return commandResult;
    }

    //
    // Builder Classes
    //

    public static class Builder<C>
    {
        protected final String name;
        protected String description;
        protected TypeConverter typeConverter = new TypeConverter();
        protected String optionSeparators;
        private Class<? extends C> defaultCommand;
        private final List<Class<? extends C>> defaultCommandGroupCommands = newArrayList();
        protected final Map<String, CommandGroup<C>> groups = newHashMap();

        public Builder(String name)
        {
            Preconditions.checkNotNull(name, "name is null");
            Preconditions.checkArgument(!name.isEmpty(), "name is empty");
            this.name = name;
        }

        public Builder<C> withDescription(String description)
        {
            Preconditions.checkNotNull(description, "description is null");
            Preconditions.checkArgument(!description.isEmpty(), "description is empty");
            this.description = description;
            return this;
        }

        public Builder<C> withTypeConverter(TypeConverter typeConverter)
        {
            Preconditions.checkNotNull(typeConverter, "typeConverter is null");
            this.typeConverter = typeConverter;
            return this;
        }

        public Builder<C> withOptionSeparators(String optionsSeparator)
        {
            Preconditions.checkNotNull(optionsSeparator, "optionsSeparator is null");
            this.optionSeparators = optionsSeparator;
            return this;
        }

        public Builder<C> defaultCommand(Class<? extends C> defaultCommand)
        {
            this.defaultCommand = defaultCommand;
            return this;
        }

        public Builder<C> addCommand(Class<? extends C> command)
        {
            this.defaultCommandGroupCommands.add(command);
            return this;
        }

        public CommandGroup<C> addGroup(String name)
        {
            Preconditions.checkNotNull(name, "name is null");
            Preconditions.checkArgument(!name.isEmpty(), "name is empty");
            Preconditions.checkArgument(!groups.containsKey(name), "Group %s has already been declared", name);

            CommandGroup<C> group = new CommandGroup<C>(name);
            groups.put(name, group);
            return group;
        }

        public GitLikeCommandParser<C> build()
        {
            return new GitLikeCommandParser<C>(name, description, typeConverter, defaultCommand, defaultCommandGroupCommands, groups.values());
        }
    }

    public static class CommandGroup<C>
    {
        private final String name;
        private String description;
        private Class<? extends C> defaultCommand;

        private final List<Class<? extends C>> commands = newArrayList();

        private CommandGroup(String name)
        {
            Preconditions.checkNotNull(name, "name is null");
            this.name = name;
        }

        public CommandGroup<C> withDescription(String description)
        {
            Preconditions.checkNotNull(description, "description is null");
            Preconditions.checkArgument(!description.isEmpty(), "description is empty");
            this.description = description;
            return this;
        }

        public CommandGroup<C> defaultCommand(Class<? extends C> defaultCommand)
        {
            Preconditions.checkNotNull(defaultCommand, "defaultCommand is null");
            this.defaultCommand = defaultCommand;
            return this;
        }

        public CommandGroup<C> addCommand(Class<? extends C> command)
        {
            Preconditions.checkNotNull(command, "command is null");
            commands.add(command);
            return this;
        }
    }
}
