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

    //TODO needs a refactoring regarding the token iterator handling!
    public ParseState parse(GlobalMetadata metadata, Iterable<String> params)
    {
        ParseState state = ParseState.newInstance().pushContext(Context.GLOBAL);

        Iterator<String> tokens = params.iterator();
        if(tokens.hasNext()) {
            String token = tokens.next();

            // parse global options
            List<OptionValue> optionValues;
            while((optionValues = parseOptions(token, tokens, state, metadata.getOptions())) != null) {
                state = pushOptionValues(state, optionValues);
                if(!tokens.hasNext()) {
                    return state;
                }
                token = tokens.next();
            }

            // parse group
            CommandGroupMetadata group = findCommandGroup(metadata, token);
            if (group != null) {
                state = parseCommandGroup(group, state, token, tokens);
                if(!tokens.hasNext()) {
                    return state;
                }
                token = tokens.next();
            }

            // parse command
            state = parseCommand(metadata, state, token, tokens);
        }
        return state;
    }

    public ParseState parseCommand(CommandMetadata command, Iterable<String> params)
    {
        Iterator<String> tokens = params.iterator();
        ParseState state = ParseState.newInstance().pushContext(Context.GLOBAL).withCommand(command);

        while (tokens.hasNext()) {
            String token = tokens.next();

            List<OptionValue> optionValues;
            while((optionValues = parseOptions(token, tokens, state, command.getCommandOptions())) != null) {
                state = pushOptionValues(state, optionValues);
                if(!tokens.hasNext()) {
                    return state;
                }
                token = tokens.next();
            }

            state = parseArgs(state, token, tokens, command.getArguments());
        }
        return state;
    }

    private ParseState parseCommand(GlobalMetadata metadata, ParseState state, String token, Iterator<String> tokens) {
        CommandMetadata command = findCommand(metadata, token, state);
        if (command == null) {
            state = state.withUnparsedInput(token);
            while (tokens.hasNext()) {
                state = state.withUnparsedInput(tokens.next());
            }
        }
        else {
            state = state.withCommand(command).pushContext(Context.COMMAND);
            if(!tokens.hasNext()) {
                return state;
            }
            token = tokens.next();

            List<OptionValue> optionValues;
            while((optionValues = parseOptions(token, tokens, state, command.getCommandOptions())) != null) {
                state = pushOptionValues(state, optionValues);
                if(!tokens.hasNext()) {
                    return state;
                }
                token = tokens.next();
            }
            state = parseArgs(state, token, tokens, command.getArguments());

            while (tokens.hasNext()) {
                token = tokens.next();
                while((optionValues = parseOptions(token, tokens, state, command.getCommandOptions())) != null) {
                    state = pushOptionValues(state, optionValues);
                    if(!tokens.hasNext()) {
                        return state;
                    }
                    token = tokens.next();
                }
                state = parseArgs(state, token, tokens, command.getArguments());
            }
        }
        return state;
    }

    private ParseState parseCommandGroup(CommandGroupMetadata group, ParseState state, String token, Iterator<String> tokens) {
        if (group != null) {
            state = state.withGroup(group).pushContext(Context.GROUP);

            List<OptionValue> optionValues;
            while((optionValues = parseOptions(token, tokens, state, state.getGroup().getOptions())) != null) {
                state = pushOptionValues(state, optionValues);
                if(tokens.hasNext()) {
                    token = tokens.next();
                }
            }
        }
        return state;
    }

    private List<OptionValue> parseOptions(String token, Iterator<String> tokens, ParseState state, List<OptionMetadata> allowedOptions)
    {
        // Try to parse next option(s) using different styles.  If code matches it returns
        // the option value(s), otherwise it returns null.

        // Parse a simple option
        final OptionValue optionValueSimple = parseSimpleOption(token, tokens, allowedOptions);
        if(optionValueSimple != null) {
            return Arrays.asList(optionValueSimple);
        }

        // Parse GNU getopt long-form: --option=value
        final OptionValue optionValueLongGnu = parseLongGnuGetOpt(token, allowedOptions);
        if (optionValueLongGnu != null) {
            return Arrays.asList(optionValueLongGnu);
        }

        // Handle classic getopt syntax: -abc
        return parseClassicGetOpt(token, tokens, allowedOptions);
    }

    private ParseState pushOptionValues(ParseState state, List<OptionValue> optionValues) {
        for(OptionValue optionValue: optionValues) {
            state = state.pushContext(Context.OPTION).withOptionValue(optionValue.getOption(), optionValue.getValue()).popContext();
        }
        return state;
    }

    private ParseState pushOptionValue(ParseState state, OptionValue optionValue) {
        return state.pushContext(Context.OPTION).withOptionValue(optionValue.getOption(), optionValue.getValue()).popContext();
    }

    private OptionValue parseSimpleOption(String token, Iterator<String> tokens, List<OptionMetadata> allowedOptions)
    {
        OptionMetadata option = findOption(allowedOptions, token);
        if (option == null) {
            return null;
        }

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

    private OptionValue parseLongGnuGetOpt(String token, List<OptionMetadata> allowedOptions)
    {
        List<String> parts = Arrays.asList(token.split("=")).stream().limit(2L).collect(Collectors.toList());
        if (parts.size() != 2) {
            return null;
        }

        OptionMetadata option = findOption(allowedOptions, parts.get(0));
        if (option == null || option.getArity() != 1) {
            // TODO: this is not exactly correct. It should be an error condition
            return null;
        }

        // determine option value
        Object value = TypeConverter.newInstance().convert(option.getTitle(), option.getJavaType(), parts.get(1));
        return new OptionValue(option, value);
    }

    private List<OptionValue> parseClassicGetOpt(String token, Iterator<String> tokens, List<OptionMetadata> allowedOptions)
    {
        if (!SHORT_OPTIONS_PATTERN.matcher(token).matches()) {
            return null;
        }

        // remove leading dash from token
        String remainingToken = token.substring(1);

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

        return optionValues;
    }

    private ParseState parseArgs(ParseState state, String token, Iterator<String> tokens, ArgumentsMetadata arguments)
    {
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

    private CommandMetadata findCommand(GlobalMetadata metadata, String token, ParseState state) {
        List<CommandMetadata> expectedCommands = metadata.getDefaultGroupCommands();
        if (state.getGroup() != null) {
            expectedCommands = state.getGroup().getCommands();
        }
        return expectedCommands.stream().filter(entry -> entry.getName().equals(token)).findFirst().orElse(null);
    }

    private CommandGroupMetadata findCommandGroup(GlobalMetadata metadata, String aTokenX) {
        return metadata.getCommandGroups().stream().filter(entry -> entry.getName().equals(aTokenX)).findFirst().orElse(null);
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
}
