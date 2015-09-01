package io.airlift.airline;

import io.airlift.airline.model.ArgumentsMetadata;
import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.GlobalMetadata;
import io.airlift.airline.model.OptionMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser
{
    private static final Pattern SHORT_OPTIONS_PATTERN = Pattern.compile("-[^-].*");

    // global> (option value*)* (group (option value*)*)? (command (option value* | arg)* '--'? args*)?
    public ParseState parse(GlobalMetadata metadata, String... params)
    {
        return parse(metadata, Arrays.asList(params));
    }

    //TODO needs a refactoring regarding the tokens iterator handling and add a test!
    public ParseState parse(GlobalMetadata metadata, Iterable<String> params)
    {
        TokenIterator tokens = new TokenIterator(params.iterator());

        ParseState state = ParseState.newInstance().pushContext(Context.GLOBAL);
        if(!tokens.hasNext()) {
            return state;
        }

        // parse global options
        state = parseOptions(tokens, state, metadata.getOptions());

        // parse group
        if (tokens.hasNext()) {
            CommandGroupMetadata group = metadata.getCommandGroups().stream().filter(entry -> entry.getName().equals(tokens.peek())).findFirst().orElse(null);
            if (group != null) {
                tokens.next();
                state = state.withGroup(group).pushContext(Context.GROUP);

                state = parseOptions(tokens, state, state.getGroup().getOptions());
            }

            if (tokens.hasNext()) {
                // parse command
                String token = tokens.next();
                CommandMetadata command = parseCommand(metadata, token, state);
                if (command == null) {
                    state = state.withUnparsedInput(token);
                    while (tokens.hasNext()) {
                        state = state.withUnparsedInput(tokens.next());
                    }
                }
                else {
                    state = state.withCommand(command).pushContext(Context.COMMAND);

                    while (tokens.hasNext()) {
                        state = parseOptions(tokens, state, command.getCommandOptions());

                        state = parseArgs(state, tokens, command.getArguments());
                    }
                }
            }
        }

        return state;
    }

    public ParseState parseCommand(CommandMetadata command, Iterable<String> params)
    {
        TokenIterator tokens = new TokenIterator(params.iterator());
        ParseState state = ParseState.newInstance().pushContext(Context.GLOBAL).withCommand(command);

        while (tokens.hasNext()) {
            state = parseOptions(tokens, state, command.getCommandOptions());

            state = parseArgs(state, tokens, command.getArguments());
        }
        return state;
    }

    private ParseState parseOptions(TokenIterator tokens, ParseState state, List<OptionMetadata> allowedOptions)
    {
        List<OptionValue> allOptionValues = new ArrayList<>();
        while (tokens.hasNext()) {
            //
            // Try to parse next option(s) using different styles.  If code matches it returns
            // the next parser state, otherwise it returns null.

            // Parse a simple option
            final OptionValue optionValueSimple = parseSimpleOption(tokens, allowedOptions);
            if(optionValueSimple != null) {
                allOptionValues.add(optionValueSimple);
                continue;
            }

            // Parse GNU getopt long-form: --option=value
            final OptionValue optionValueLongGnu = parseLongGnuGetOpt(tokens, allowedOptions);
            if (optionValueLongGnu != null) {
                allOptionValues.add(optionValueLongGnu);
                continue;
            }

            // Handle classic getopt syntax: -abc
            final List<OptionValue> optionValuesClassic = parseClassicGetOpt(tokens, allowedOptions);
            if (optionValuesClassic != null) {
                allOptionValues.addAll(optionValuesClassic);
                continue;
            }

            // did not match an option
            break;
        }

        for(OptionValue optionValueClassic: allOptionValues) {
            state = state.pushContext(Context.OPTION).withOption(optionValueClassic.getOption());
            state = state.withOptionValue(optionValueClassic.getOption(), optionValueClassic.getValue()).popContext();
        }
        return state;
    }

    private OptionValue parseSimpleOption(TokenIterator tokens, List<OptionMetadata> allowedOptions)
    {
        OptionMetadata option = findOption(allowedOptions, tokens.peek());
        if (option == null) {
            return null;
        }

        tokens.next();

        Object value;
        if (option.getArity() == 0) {
            return new OptionValue(option, Boolean.TRUE);
        }
        else if (option.getArity() == 1) {
            if (tokens.hasNext()) {
                value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), tokens.next());
                return new OptionValue(option, value);
            }
            return new OptionValue(option, null);
        }
        else {
            List<Object> values = new ArrayList<>(option.getArity());

            int count = 0;
            while (count < option.getArity() && tokens.hasNext()) {
                values.add(TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), tokens.next()));
                ++count;
            }

            if (count != option.getArity()) {
                throw new ParseOptionMissingValueException(option.getTitle());
            }
            return new OptionValue(option, values);
        }
    }

    private OptionValue parseLongGnuGetOpt(TokenIterator tokens, List<OptionMetadata> allowedOptions)
    {
        List<String> parts = Arrays.asList(tokens.peek().split("=")).stream().limit(2L).collect(Collectors.toList());
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

        // determine option value
        Object value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), parts.get(1));
        return new OptionValue(option, value);
    }

    private List<OptionValue> parseClassicGetOpt(TokenIterator tokens, List<OptionMetadata> allowedOptions)
    {
        if (!SHORT_OPTIONS_PATTERN.matcher(tokens.peek()).matches()) {
            return null;
        }

        // remove leading dash from token
        String remainingToken = tokens.peek().substring(1);

        List<OptionValue> optionValues = new ArrayList<>();
        while (!remainingToken.isEmpty()) {
            char tokenCharacter = remainingToken.charAt(0);

            // is the current token character a single letter option?
            OptionMetadata option = findOption(allowedOptions, "-" + tokenCharacter);
            if (option == null) {
                return null;
            }

            // remove current token character
            remainingToken = remainingToken.substring(1);

            // for no argument options, process the option and remove the character from the token
            if (option.getArity() == 0) {
                OptionValue optionValue = new OptionValue(option, Boolean.TRUE);
                optionValues.add(optionValue);
                continue;
            }

            if (option.getArity() == 1) {
                // we must, consume the current token so we can see the next token
                tokens.next();

                // if current token has more characters, this is the value; otherwise it is the next token
                if (!remainingToken.isEmpty()) {
                    Object value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), remainingToken);
                    OptionValue optionValue = new OptionValue(option, value);
                    optionValues.add(optionValue);
                }
                else if (tokens.hasNext()) {
                    Object value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), tokens.next());
                    OptionValue optionValue = new OptionValue(option, value);
                    optionValues.add(optionValue);
                }

                return optionValues;
            }

            throw new UnsupportedOperationException("Short options style can not be used with option " + option.getAllowedValues());
        }

        // consume the current token
        tokens.next();

        return optionValues;
    }

    private ParseState parseArgs(ParseState state, TokenIterator tokens, ArgumentsMetadata arguments)
    {
        if (tokens.hasNext()) {
            String token = tokens.next();
            if (token.equals("--")) {
                state = state.pushContext(Context.ARGS);

                // consume all args
                while (tokens.hasNext()) {
                    token = tokens.next();
                    state = parseArg(state, token, arguments);
                }
            }
            else {
                state = parseArg(state, token, arguments);
            }
        }

        return state;
    }

    private ParseState parseArg(ParseState state, String token, ArgumentsMetadata arguments)
    {
        if (arguments != null) {
            state = state.withArgument(TypeConverter.newInstance().convert(arguments.getTitle(), arguments.getJavaType(), token));
        }
        else {
            state = state.withUnparsedInput(token);
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

    private CommandMetadata parseCommand(GlobalMetadata metadata, String token, ParseState state) {
        List<CommandMetadata> expectedCommands = metadata.getDefaultGroupCommands();
        if (state.getGroup() != null) {
            expectedCommands = state.getGroup().getCommands();
        }
        return expectedCommands.stream().filter(entry -> entry.getName().equals(token)).findFirst().orElse(null);
    }

    private static class OptionValue
    {
        private final OptionMetadata option;
        private Object value;

        private OptionValue(OptionMetadata option, Object value) {
            this.option = option;
            this.value = value;
        }

        public OptionMetadata getOption() {
            return option;
        }

        public Object getValue() {
            return value;
        }
    }

    /**
     * Temporary, fitted copy from guava, see deprecation comments.
     * @deprecated //TODO The iterator handling within the Parser class should be refactored! Remove this implementation after that!
     */
    @Deprecated
    private static class TokenIterator implements Iterator<String> {

        private final Iterator<String> iterator;
        private boolean hasPeeked;
        private String peekedElement;

        private TokenIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return hasPeeked || iterator.hasNext();
        }

        @Override
        public String next() {
            if (!hasPeeked) {
                return iterator.next();
            }
            String result = peekedElement;
            hasPeeked = false;
            peekedElement = null;
            return result;
        }

        public String peek() {
            if (!hasPeeked) {
                peekedElement = iterator.next();
                hasPeeked = true;
            }
            return peekedElement;
        }
    }
}
