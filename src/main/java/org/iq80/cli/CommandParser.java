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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.iq80.cli.ParserUtil.OptionsMetadata;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static org.iq80.cli.ParserUtil.createInstance;
import static org.iq80.cli.ParserUtil.expandArgs;

public class CommandParser<T>
{
    public static <T> CommandParser<T> create(Class<? extends T> commandType)
    {
        return new CommandParser<T>(commandType);
    }

    private final Class<? extends T> commandType;

    private final String name;

    /**
     * Group to which this command belongs.
     */
    private final String group;

    private final String description;

    /**
     * Is this the default command for the group?
     */
    private final boolean defaultCommand;

    private final List<OptionParser> options;

    private final Map<String, OptionParser> optionIndex;

    private final ArgumentParser arguments;
    private final Accessor globalOptionsAccessor;
    private final Accessor groupOptionsAccessor;

    private final String optionSeparators;

    private CommandParser(Class<? extends T> commandType)
    {
        this(commandType, new TypeConverter(), null);
    }

    public CommandParser(Class<? extends T> commandType, TypeConverter typeConverter, @Nullable String optionSeparators)
    {
        Preconditions.checkNotNull(commandType, "commandType is null");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");

        this.commandType = commandType;
        this.optionSeparators = optionSeparators;

        Command command = null;
        for (Class<?> cls = commandType; command == null && !Object.class.equals(cls); cls = cls.getSuperclass()) {
            command = cls.getAnnotation(Command.class);
        }
        Preconditions.checkArgument(command != null, "Command %s is not annotated with @Command", commandType.getName());
        this.name = command.name();
        this.group = command.group();
        this.description = command.description().isEmpty() ? null : command.description();
        this.defaultCommand = command.defaultCommand();

        OptionsMetadata optionsMetadata = ParserUtil.processAnnotations(commandType, typeConverter);
        this.arguments = optionsMetadata.getArgumentParser();
        this.options = ImmutableList.copyOf(optionsMetadata.getOptions());
        this.globalOptionsAccessor = optionsMetadata.getGlobalOptionsAccessors();
        this.groupOptionsAccessor = optionsMetadata.getGroupOptionsAccessor();

        ImmutableMap.Builder<String, OptionParser> optionsIndex = ImmutableMap.builder();
        for (OptionParser option : options) {
            for (String name : option.getOptions()) {
                optionsIndex.put(name, option);
            }
        }
        this.optionIndex = optionsIndex.build();
    }

    public String getName()
    {
        return name;
    }

    public String getGroup()
    {
        return group;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isDefaultCommand()
    {
        return defaultCommand;
    }

    public Accessor getGlobalOptionsAccessor()
    {
        return globalOptionsAccessor;
    }

    public Accessor getGroupOptionsAccessor()
    {
        return groupOptionsAccessor;
    }

    public List<OptionParser> getOptions()
    {
        return options;
    }

    public ArgumentParser getArguments()
    {
        return arguments;
    }

    public T parse(String... args)
    {
        // unroll the args
        List<String> unrolledArgs = expandArgs(ImmutableList.copyOf(args), optionSeparators);

        Preconditions.checkNotNull(args, "args is null");
        return parseInternal(null, null, true, unrolledArgs);
    }

    public T parseInternal(@Nullable Object globalOptions, @Nullable Object groupOptions, boolean validate, List<String> parameters)
    {
        // create the command instance
        T commandInstance = createInstance(commandType);

        // process the parameters
        List<String> commandArgs = ParserUtil.parseOptions(commandInstance, optionIndex, validate, false, parameters);
        if (validate && arguments != null && arguments.isRequired() && commandArgs.isEmpty()) {
            throw new ParseException("%s are required", arguments.getName());
        }

        // set command arguments
        if (!commandArgs.isEmpty()) {
            if (arguments == null) {
                throw new ParseException("You can not pass arguments to %s", name);
            }
            for (String commandArg : commandArgs) {
                arguments.addValue(commandInstance, commandArg);
            }
        }

        // set the global and group options
        if (globalOptions != null && globalOptionsAccessor != null) {
            globalOptionsAccessor.addValue(commandInstance, globalOptions);
        }
        if (groupOptions != null && groupOptionsAccessor != null) {
            groupOptionsAccessor.addValue(commandInstance, groupOptions);
        }

        return commandInstance;
    }
}
