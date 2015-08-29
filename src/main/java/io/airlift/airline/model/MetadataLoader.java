package io.airlift.airline.model;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import io.airlift.airline.*;
import io.airlift.airline.util.ArgumentChecker;
import io.airlift.airline.util.CollectionUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;

import static com.google.common.collect.Iterables.transform;

public class MetadataLoader
{
    public static GlobalMetadata loadGlobal(String name,
            String description,
            CommandMetadata defaultCommand,
            Iterable<CommandMetadata> defaultGroupCommands,
            Iterable<CommandGroupMetadata> groups)
    {
        List<OptionMetadata> globalOptionsBuilder = new ArrayList<>();
        if (defaultCommand != null) {
            globalOptionsBuilder.addAll(defaultCommand.getGlobalOptions());
        }
        for (CommandMetadata command : defaultGroupCommands) {
            globalOptionsBuilder.addAll(command.getGlobalOptions());
        }
        for (CommandGroupMetadata group : groups) {
            for (CommandMetadata command : group.getCommands()) {
                globalOptionsBuilder.addAll(command.getGlobalOptions());
            }
        }
        List<OptionMetadata> globalOptions = mergeOptionSet(globalOptionsBuilder);
        return new GlobalMetadata(name, description, globalOptions, defaultCommand, defaultGroupCommands, groups);
    }

    public static CommandGroupMetadata loadCommandGroup(String name, String description, CommandMetadata defaultCommand, Iterable<CommandMetadata> commands)
    {
        List<OptionMetadata> groupOptionsBuilder = new ArrayList<>();
        if (defaultCommand != null) {
            groupOptionsBuilder.addAll(defaultCommand.getGroupOptions());
        }
        for (CommandMetadata command : commands) {
            groupOptionsBuilder.addAll(command.getGroupOptions());
        }
        List<OptionMetadata> groupOptions = mergeOptionSet(groupOptionsBuilder);
        return new CommandGroupMetadata(name, description, groupOptions, defaultCommand, commands);
    }

    public static <T> List<CommandMetadata> loadCommands(Iterable<Class<? extends T>> defaultCommands)
    {
        return CollectionUtils.asList(Iterables.transform(defaultCommands, new Function<Class<?>, CommandMetadata>()
        {
            public CommandMetadata apply(Class<?> commandType) {
                return loadCommand(commandType);
            }
        }));
    }

    public static CommandMetadata loadCommand(Class<?> commandType)
    {
        Command command = null;
        for (Class<?> cls = commandType; command == null && !Object.class.equals(cls); cls = cls.getSuperclass()) {
            command = cls.getAnnotation(Command.class);
        }
        ArgumentChecker.checkCondition(command != null, "Command %s is not annotated with @Command", commandType.getName());
        String name = command.name();
        String description = command.description().isEmpty() ? null : command.description();
        boolean hidden = command.hidden();

        InjectionMetadata injectionMetadata = loadInjectionMetadata(commandType);

        CommandMetadata commandMetadata = new CommandMetadata(
                name,
                description,
                hidden, injectionMetadata.globalOptions,
                injectionMetadata.groupOptions,
                injectionMetadata.commandOptions,
                Iterables.getFirst(injectionMetadata.arguments, null),
                injectionMetadata.metadataInjections,
                commandType);

        return commandMetadata;

    }

    public static SuggesterMetadata loadSuggester(Class<? extends Suggester> suggesterClass)
    {
        InjectionMetadata injectionMetadata = loadInjectionMetadata(suggesterClass);
        return new SuggesterMetadata(suggesterClass, injectionMetadata.metadataInjections);
    }

    public static InjectionMetadata loadInjectionMetadata(Class<?> type)
    {
        InjectionMetadata injectionMetadata = new InjectionMetadata();
        loadInjectionMetadata(type, injectionMetadata, new ArrayList<Field>());
        injectionMetadata.compact();
        return injectionMetadata;
    }

    public static void loadInjectionMetadata(Class<?> type, InjectionMetadata injectionMetadata, List<Field> fields)
    {
        for (Class<?> cls = type; !Object.class.equals(cls); cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                List<Field> path = concat(fields, field);

                Inject injectAnnotation = field.getAnnotation(Inject.class);
                if (injectAnnotation != null) {
                    if (field.getType().equals(GlobalMetadata.class) ||
                            field.getType().equals(CommandGroupMetadata.class) ||
                            field.getType().equals(CommandMetadata.class)) {
                        injectionMetadata.metadataInjections.add(new Accessor(path));
                    } else {
                        loadInjectionMetadata(field.getType(), injectionMetadata, path);
                    }
                }

                Option optionAnnotation = field.getAnnotation(Option.class);
                if (optionAnnotation != null) {
                    OptionType optionType = optionAnnotation.type();
                    String name;
                    if (!optionAnnotation.title().isEmpty()) {
                        name = optionAnnotation.title();
                    }
                    else {
                        name = field.getName();
                    }

                    List<String> options = Arrays.asList(optionAnnotation.name());
                    String description = optionAnnotation.description();

                    int arity = optionAnnotation.arity();
                    ArgumentChecker.checkCondition(arity >= 0 || arity == Integer.MIN_VALUE, "Invalid arity for option {1}", name);

                    if (optionAnnotation.arity() >= 0) {
                        arity = optionAnnotation.arity();
                    }
                    else {
                        Class<?> fieldType = field.getType();
                        if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
                            arity = 0;
                        }
                        else {
                            arity = 1;
                        }
                    }

                    boolean required = optionAnnotation.required();
                    boolean hidden = optionAnnotation.hidden();
                    List<String> allowedValues = Arrays.asList(optionAnnotation.allowedValues());
                    if (allowedValues.isEmpty()) {
                        allowedValues = null;
                    }

                    OptionMetadata optionMetadata = new OptionMetadata(optionType, options, name, description, arity, required, hidden, allowedValues, path);
                    switch (optionType) {
                        case GLOBAL:
                            injectionMetadata.globalOptions.add(optionMetadata);
                            break;
                        case GROUP:
                            injectionMetadata.groupOptions.add(optionMetadata);
                            break;
                        case COMMAND:
                            injectionMetadata.commandOptions.add(optionMetadata);
                            break;
                    }
                }

                Arguments argumentsAnnotation = field.getAnnotation(Arguments.class);
                if (field.isAnnotationPresent(Arguments.class)) {
                    String title;
                    if (!argumentsAnnotation.title().isEmpty()) {
                        title = argumentsAnnotation.title();
                    }
                    else {
                        title = field.getName();
                    }

                    String description = argumentsAnnotation.description();
                    String usage = argumentsAnnotation.usage();
                    boolean required = argumentsAnnotation.required();

                    injectionMetadata.arguments.add(new ArgumentsMetadata(title, description, usage, required, path));
                }
            }
        }
    }

    private static List<OptionMetadata> mergeOptionSet(List<OptionMetadata> options)
    {
        Map<OptionMetadata, List<OptionMetadata>> metadataIndex = new LinkedHashMap<>(options.size());
        for (OptionMetadata option : options) {
            List<OptionMetadata> values = metadataIndex.get(option);
            if(values == null) {
                values = new ArrayList<>();
                metadataIndex.put(option, values);
            }
            values.add(option);
        }

        options = CollectionUtils.asList(transform(metadataIndex.values(), new Function<Collection<OptionMetadata>, OptionMetadata>()
        {
            @Override
            public OptionMetadata apply(@Nullable Collection<OptionMetadata> options) {
                return new OptionMetadata(options);
            }
        }));

        Map<String, OptionMetadata> optionIndex = new HashMap<>();
        for (OptionMetadata option : options) {
            for (String optionName : option.getOptions()) {
                if (optionIndex.containsKey(optionName)) {
                    throw new IllegalArgumentException(String.format("Fields %s and %s have conflicting definitions of option %s",
                            optionIndex.get(optionName).getAccessors().iterator().next(),
                            option.getAccessors().iterator().next(),
                            optionName));
                }
                optionIndex.put(optionName, option);
            }
        }

        return options;
    }

    private static <T> List<T> concat(List<T> list, T item)
    {
        List<T> newList = new ArrayList<>(list);
        newList.add(item);
        return newList;
    }

    private static class InjectionMetadata
    {
        private List<OptionMetadata> globalOptions = new ArrayList<>();
        private List<OptionMetadata> groupOptions = new ArrayList<>();
        private List<OptionMetadata> commandOptions = new ArrayList<>();
        private List<ArgumentsMetadata> arguments = new ArrayList<>();
        private List<Accessor> metadataInjections = new ArrayList<>();

        private void compact()
        {
            globalOptions = mergeOptionSet(globalOptions);
            groupOptions = mergeOptionSet(groupOptions);
            commandOptions = mergeOptionSet(commandOptions);

            if (arguments.size() > 1) {
                arguments = Arrays.asList(new ArgumentsMetadata(arguments));
            }
        }
    }
}
