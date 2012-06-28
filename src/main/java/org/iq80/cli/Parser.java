package org.iq80.cli;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.iq80.cli.model.ArgumentsMetadata;
import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.iq80.cli.model.GlobalMetadata;
import org.iq80.cli.model.OptionMetadata;

import java.util.List;

import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;

public class Parser
{
    private final GlobalMetadata metadata;

    public Parser(GlobalMetadata metadata)
    {
        this.metadata = metadata;
    }
    
    // global> (option value*)* (group (option value*)*)? (command (option value* | arg)* '--'? args*)?
    public ParseState parse(String... params)
    {
        return parse(ImmutableList.copyOf(params));
    }

    public ParseState parse(Iterable<String> params)
    {
        PeekingIterator<String> tokens = Iterators.peekingIterator(params.iterator());

        ParseState state = ParseState.newInstance().pushContext(Context.GLOBAL);

        // parse global options
        state = parseOptions(tokens, state, metadata.getOptions());

        // parse group
        if (tokens.hasNext()) {
            CommandGroupMetadata group = find(metadata.getCommandGroups(), compose(equalTo(tokens.peek()), CommandGroupMetadata.nameGetter()), null);
            if (group != null) {
                tokens.next();
                state = state.withGroup(group).pushContext(Context.GROUP);

                state = parseOptions(tokens, state, state.getGroup().getOptions());
            }
        }

        // parse command
        List<CommandMetadata> expectedCommands = metadata.getDefaultGroupCommands();
        if (state.getGroup() != null) {
            expectedCommands = state.getGroup().getCommands();
        }

        if (tokens.hasNext()) {
            CommandMetadata command = find(expectedCommands, compose(equalTo(tokens.peek()), CommandMetadata.nameGetter()), null);
            if (command != null) {
                tokens.next();
                state = state.withCommand(command).pushContext(Context.COMMAND);

                while (tokens.hasNext()) {
                    state = parseOptions(tokens, state, command.getCommandOptions());

                    state = parseArgs(state, tokens, command.getArguments());
                }
           }
        }

        return state;
    }

    private ParseState parseOptions(PeekingIterator<String> tokens, ParseState state, List<OptionMetadata> allowedOptions)
    {
        while (tokens.hasNext()) {
            OptionMetadata option = findOption(allowedOptions, tokens.peek());
            if (option == null) {
                break;
            }

            tokens.next();
            state = state.pushContext(Context.OPTION).withOption(option);

            Object value;
            if (option.getArity() == 0) {
                state = state.withOptionValue(option, Boolean.TRUE)
                        .popContext();
            }
            else if (option.getArity() == 1) {
                if (tokens.hasNext()) {
                    value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), tokens.next());
                    state = state.withOptionValue(option, value)
                            .popContext();
                }
            }
            else if (option.getArity() > 1) {
                ImmutableList.Builder<Object> values = ImmutableList.builder();

                int count = 0;
                while (count < option.getArity() && tokens.hasNext()) {
                    values.add(TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), tokens.next()));
                    ++count;
                }

                if (count == option.getArity()) {
                    state = state.withOptionValue(option, values.build())
                            .popContext();
                }
            }
            else {
                throw new UnsupportedOperationException("arity < 0 not yet supported");
            }
        }

        return state;
    }


    private ParseState parseArgs(ParseState state, PeekingIterator<String> tokens, ArgumentsMetadata arguments)
    {
        if (tokens.hasNext()) {
            if (tokens.peek().equals("--")) {
                state = state.pushContext(Context.ARGS);
                tokens.next();

                // consume all args
                while (tokens.hasNext()) {
                    state = parseArg(state, tokens, arguments);
                }
            }
            else {
                state = parseArg(state, tokens, arguments);
            }
        }

        return state;
    }

    private ParseState parseArg(ParseState state, PeekingIterator<String> tokens, ArgumentsMetadata arguments)
    {
        if (arguments != null) {
            state = state.withArgument(TypeConverter.newInstance().convert(arguments.getTitle(), arguments.getJavaType(), tokens.next()));
        }
        else {
            state = state.withUnparsedInput(tokens.next());
        }
        return state;
    }

    private OptionMetadata findOption(List<OptionMetadata> options, String param)
    {
        for (OptionMetadata optionMetadata : options) {
            if (optionMetadata.getOptions().contains(param)) {
                return optionMetadata;
            }
        }
        return null;
    }

}
