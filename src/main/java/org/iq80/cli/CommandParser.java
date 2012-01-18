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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import org.iq80.cli.model.ArgumentsMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.MetadataLoader;
import org.iq80.cli.model.OptionMetadata;

import java.util.List;
import java.util.Map;

public class CommandParser<T>
{
    public static <T> CommandParser<T> create(Class<T> type)
    {
        return create(new TypeConverter(), MetadataLoader.loadCommand(type));
    }

    public static <T> CommandParser<T> create(TypeConverter typeConverter, CommandMetadata command)
    {
        return new CommandParser<T>(typeConverter, command);
    }

    public static <T> Map<String, CommandParser<T>> createIndex(final TypeConverter typeConverter, List<CommandMetadata> commands)
    {
        ImmutableMap.Builder<String, CommandParser<T>> index = ImmutableMap.builder();
        for (CommandMetadata command : commands) {
            index.put(command.getName(), CommandParser.<T>create(typeConverter, command));
        }
        return index.build();
    }

    private final CommandMetadata metadata;
    private final TypeConverter typeConverter;
    private final List<OptionParser> options;

    public CommandParser(TypeConverter typeConverter, CommandMetadata metadata)
    {
        this.metadata = metadata;
        this.typeConverter = typeConverter;

        options = OptionParser.from(typeConverter, metadata.getAllOptions());

    }

    public CommandMetadata getMetadata()
    {
        return metadata;
    }

    public T parse(String... args)
    {
        return parseInternal(ArrayListMultimap.<OptionMetadata, Object>create(), true, ImmutableList.copyOf(args));
    }

    public T parseInternal(ListMultimap<OptionMetadata, Object> parsedOptions,
            boolean validate,
            Iterable<String> parameters)
    {
        // process options
        List<String> commandArgs = ParserUtil.parseOptions(options, validate, false, parameters, parsedOptions);

        // process arguments
        ArgumentsMetadata arguments = metadata.getArguments();
        if (validate && arguments != null && arguments.isRequired() && commandArgs.isEmpty()) {
            throw new ParseException("%s are required", arguments.getTitle());
        }
        ImmutableList.Builder<Object> parsedArguments = ImmutableList.builder();
        if (!commandArgs.isEmpty()) {
            if (arguments == null) {
                throw new ParseException("You can not pass arguments to %s", metadata.getName());
            }
            for (String commandArg : commandArgs) {
                parsedArguments.add(typeConverter.convert(arguments.getTitle(), arguments.getJavaType(), commandArg));
            }
        }

        // build the instance
        return createInstance(metadata.getType(), metadata.getAllOptions(), parsedOptions, arguments, parsedArguments.build());
    }

    public static <T> T createInstance(Class<?> type,
            Iterable<OptionMetadata> options,
            ListMultimap<OptionMetadata, Object> parsedOptions,
            ArgumentsMetadata arguments,
            Iterable<Object> parsedArguments)
    {
        // create the command instance
        T commandInstance = (T) ParserUtil.createInstance(type);

        // inject options
        for (OptionMetadata option : options) {
            List<Object> values = parsedOptions.get(option);
            if (values != null && !values.isEmpty()) {
                for (Accessor accessor : option.getAccessors()) {
                    accessor.addValues(commandInstance, values);
                }
            }
        }

        // inject args
        if (arguments != null) {
            arguments.getAccessor().addValues(commandInstance, parsedArguments);
        }

        return commandInstance;
    }
}
