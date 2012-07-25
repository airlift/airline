package io.airlift.command;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import io.airlift.command.model.ArgumentsMetadata;
import io.airlift.command.model.CommandGroupMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.GlobalMetadata;
import io.airlift.command.model.OptionMetadata;

import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;

public class Parser
{
    private static final Pattern GNU_SHORT_OPTIONS_PATTERN = Pattern.compile("-[^-]+");
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
            //
            // Try to parse next option(s) using different styles.  If code matches it returns
            // the next parser state, otherwise it returns null.

            // Parse a simple option
            ParseState nextState = parseSimpleOption(tokens, state, allowedOptions);
            if (nextState != null) {
                state = nextState;
                continue;
            }

            // Parse GNU getopt long-form: --option=value
            nextState = parseLongGnuGetOpt(tokens, state, allowedOptions);
            if (nextState != null) {
                state = nextState;
                continue;
            }

            // Handle classic getopt syntax: -abc
            nextState = parseShortGnuGetOpt(tokens, state, allowedOptions);
            if (nextState != null) {
                state = nextState;
                continue;
            }

            // did not match an option
            break;
        }

        return state;
    }

    private ParseState parseSimpleOption(PeekingIterator<String> tokens, ParseState state, List<OptionMetadata> allowedOptions)
    {
        OptionMetadata option = findOption(allowedOptions, tokens.peek());
        if (option == null) {
            return null;
        }

        tokens.next();
        state = state.pushContext(Context.OPTION).withOption(option);

        Object value;
        if (option.getArity() == 0) {
            state = state.withOptionValue(option, Boolean.TRUE).popContext();
        }
        else if (option.getArity() == 1) {
            if (tokens.hasNext()) {
                value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), tokens.next());
                state = state.withOptionValue(option, value).popContext();
            }
        }
        else {
            ImmutableList.Builder<Object> values = ImmutableList.builder();

            int count = 0;
            while (count < option.getArity() && tokens.hasNext()) {
                values.add(TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), tokens.next()));
                ++count;
            }

            if (count == option.getArity()) {
                state = state.withOptionValue(option, values.build()).popContext();
            }
        }
        return state;
    }

    private ParseState parseLongGnuGetOpt(PeekingIterator<String> tokens, ParseState state, List<OptionMetadata> allowedOptions)
    {
        List<String> parts = ImmutableList.copyOf(Splitter.on('=').limit(2).split(tokens.peek()));
        if (parts.size() != 2) {
            return null;
        }

        OptionMetadata option = findOption(allowedOptions, parts.get(0));
        if (option == null || option.getArity() != 1) {
            // TODO: this is not exactly correct. It should be an error condition
            return null;
        }

        // we have a match so consume the token
        tokens.next();

        // update state
        state = state.pushContext(Context.OPTION).withOption(option);
        Object value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), parts.get(1));
        state = state.withOption(option).withOptionValue(option, value).popContext();

        return state;
    }

    private ParseState parseShortGnuGetOpt(PeekingIterator<String> tokens, ParseState state, List<OptionMetadata> allowedOptions)
    {
        if (!GNU_SHORT_OPTIONS_PATTERN.matcher(tokens.peek()).matches()) {
            return null;
        }

        // remove leading dash from token
        String remainingToken = tokens.peek().substring(1);

        ParseState nextState = state;
        while (!remainingToken.isEmpty()) {
            char tokenCharacter = remainingToken.charAt(0);

            // is the current token character a single letter option?
            OptionMetadata option = findOption(allowedOptions, "-" + tokenCharacter);
            if (option == null) {
                return null;
            }

            nextState = nextState.pushContext(Context.OPTION).withOption(option);

            // remove current token character
            remainingToken = remainingToken.substring(1);

            // for no argument options, process the option and remove the character from the token
            if (option.getArity() == 0) {
                nextState = nextState.withOptionValue(option, Boolean.TRUE).popContext();
                continue;
            }

            if (option.getArity() == 1) {
                // we must, consume the current token so we can see the next token
                tokens.next();

                // if current token has more characters, this is the value; otherwise it is the next token
                if (!remainingToken.isEmpty()) {
                    Object value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), remainingToken);
                    nextState = nextState.withOptionValue(option, value).popContext();
                }
                else if (tokens.hasNext()) {
                    Object value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), tokens.next());
                    nextState = nextState.withOptionValue(option, value).popContext();
                }

                return nextState;
            }

            throw new UnsupportedOperationException("Short options style can not be used with option " + option.getAllowedValues());
        }

        // consume the current token
        tokens.next();

        return nextState;
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
