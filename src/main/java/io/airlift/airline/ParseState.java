package io.airlift.airline;

import io.airlift.airline.model.CommandGroupMetadata;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.OptionMetadata;

import java.util.*;

public class ParseState
{
    private final List<Context> locationStack;
    private final CommandGroupMetadata group;
    private final CommandMetadata command;
    private final Map<OptionMetadata, List<Object>> parsedOptions;
    private final List<Object> parsedArguments;
    private final OptionMetadata currentOption;
    private final List<String> unparsedInput; 

    ParseState(CommandGroupMetadata group,
            CommandMetadata command,
            Map<OptionMetadata, List<Object>> parsedOptions,
            List<Context> locationStack,
            List<Object> parsedArguments,
            OptionMetadata currentOption,
            List<String> unparsedInput)
    {
        this.group = group;
        this.command = command;
        this.parsedOptions = parsedOptions;
        this.locationStack = locationStack;
        this.parsedArguments = parsedArguments;
        this.currentOption = currentOption;
        this.unparsedInput = unparsedInput;
    }

    public static ParseState newInstance()
    {
        return new ParseState(null, null, new HashMap<OptionMetadata, List<Object>>(), new ArrayList<Context>(), new ArrayList<>(), null, new ArrayList<String>());
    }

    public ParseState pushContext(Context location)
    {
        List<Context> locationStack = new ArrayList<>(this.locationStack);
        locationStack.add(location);

        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState popContext()
    {
        List<Context> locationStack = new ArrayList<>(this.locationStack.subList(0, this.locationStack.size() - 1));
        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState withOptionValue(OptionMetadata option, Object value)
    {
        Map<OptionMetadata, List<Object>> newOptions = new LinkedHashMap<>(parsedOptions);
        List<Object> existingValues = newOptions.get(option);
        if(existingValues != null) {
            existingValues.add(value);
        } else {
            newOptions.put(option, new ArrayList<>(Arrays.asList(value)));
        }

        return new ParseState(group, command, newOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState withGroup(CommandGroupMetadata group)
    {
        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState withCommand(CommandMetadata command)
    {
        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, unparsedInput);
    }

    public ParseState withOption(OptionMetadata option)
    {
        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, option, unparsedInput);
    }

    public ParseState withArgument(Object argument)
    {
        List<Object> newArguments = new ArrayList<>(parsedArguments);
        newArguments.add(argument);

        return new ParseState(group, command, parsedOptions, locationStack, newArguments, currentOption, unparsedInput);
    }


    public ParseState withUnparsedInput(String input)
    {
        List<String> newUnparsedInput = new ArrayList<>(unparsedInput);
        newUnparsedInput.add(input);

        return new ParseState(group, command, parsedOptions, locationStack, parsedArguments, currentOption, newUnparsedInput);
    }

    @Override
    public String toString()
    {
        return "ParseState{" +
                "locationStack=" + locationStack +
                ", group=" + group +
                ", command=" + command +
                ", parsedOptions=" + parsedOptions +
                ", parsedArguments=" + parsedArguments +
                ", currentOption=" + currentOption +
                ", unparsedInput=" + unparsedInput +
                '}';
    }

    public Context getLocation()
    {
        return locationStack.get(locationStack.size() - 1);
    }

    public CommandGroupMetadata getGroup()
    {
        return group;
    }

    public CommandMetadata getCommand()
    {
        return command;
    }

    public OptionMetadata getCurrentOption()
    {
        return currentOption;
    }

    public Map<OptionMetadata, List<Object>> getParsedOptions()
    {
        return parsedOptions;
    }

    public List<Object> getParsedArguments()
    {
        return parsedArguments;
    }

    public List<String> getUnparsedInput()
    {
        return unparsedInput;
    }
}
