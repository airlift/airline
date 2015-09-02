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
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.MetadataLoader;
import io.airlift.airline.model.OptionMetadata;
import io.airlift.airline.util.ArgumentChecker;
import io.airlift.airline.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

import static io.airlift.airline.ParserUtil.createInstance;

public class SingleCommand<C>
{
    public static <C> SingleCommand<C> singleCommand(Class<C> command)
    {
        return new SingleCommand<C>(command);
    }

    private final CommandMetadata commandMetadata;

    private SingleCommand(Class<C> command)
    {
        ArgumentChecker.checkNotNull(command, "command is null");

        commandMetadata = MetadataLoader.loadCommand(command);
    }

    public CommandMetadata getCommandMetadata()
    {
        return commandMetadata;
    }

    public C parse(String... args)
    {
        return parse(Arrays.asList(args));
    }
    
    public C parse(Iterable<String> args)
    {
        ArgumentChecker.checkNotNull(args, "args is null");
        
        Parser parser = new Parser();
        ParseState state = parser.parseCommand(commandMetadata, args);
        validate(state);

        CommandMetadata command = state.getCommand();

        return createInstance(command.getType(),
                command.getAllOptions(),
                state.getParsedOptions(),
                command.getArguments(),
                state.getParsedArguments(),
                command.getMetadataInjections(),
                CollectionUtils.<Class<?>, Object>asMap(CommandMetadata.class, commandMetadata));
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
}
