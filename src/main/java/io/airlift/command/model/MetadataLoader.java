package org.iq80.cli.model;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import org.iq80.cli.Accessor;
import org.iq80.cli.Arguments;
import org.iq80.cli.Command;
import org.iq80.cli.Option;
import org.iq80.cli.OptionType;
import org.iq80.cli.Suggester;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class MetadataLoader
{
    public static GlobalMetadata loadGlobal(String name,
            String description,
            CommandMetadata defaultCommand,
            Iterable<CommandMetadata> defaultGroupCommands,
            Iterable<CommandGroupMetadata> groups)
    {
        ImmutableList.Builder<OptionMetadata> globalOptionsBuilder = ImmutableList.builder();
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
        List<OptionMetadata> globalOptions = mergeOptionSet(globalOptionsBuilder.build());
        return new GlobalMetadata(name, description, globalOptions, defaultCommand, defaultGroupCommands, groups);
    }

    public static CommandGroupMetadata loadCommandGroup(String name, String description, CommandMetadata defaultCommand, Iterable<CommandMetadata> commands)
    {
        ImmutableList.Builder<OptionMetadata> groupOptionsBuilder = ImmutableList.builder();
        if (defaultCommand != null) {
            groupOptionsBuilder.addAll(defaultCommand.getGroupOptions());
        }
        for (CommandMetadata command : commands) {
            groupOptionsBuilder.addAll(command.getGroupOptions());
        }
        List<OptionMetadata> groupOptions = mergeOptionSet(groupOptionsBuilder.build());
        return new CommandGroupMetadata(name, description, groupOptions, defaultCommand, commands);
    }

    public static <T> ImmutableList<CommandMetadata> loadCommands(Iterable<Class<? extends T>> defaultCommands)
    {
        return ImmutableList.copyOf(Iterables.transform(defaultCommands, new Function<Class<?>, CommandMetadata>()
        {
            public CommandMetadata apply(Class<?> commandType)
            {
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
        Preconditions.checkArgument(command != null, "Command %s is not annotated with @Command", commandType.getName());
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
        loadInjectionMetadata(type, injectionMetadata, ImmutableList.<Field>of());
        injectionMetadata.compact();
        return injectionMetadata;
    }

    public static void loadInjectionMetadata(Class<?> type, InjectionMetadata injectionMetadata, List<Field> fields)
    {
        for (Class<?> cls = type; !Object.class.equals(cls); cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                ImmutableList<Field> path = concat(fields, field);

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

                    List<String> options = ImmutableList.copyOf(optionAnnotation.name());
                    String description = optionAnnotation.description();

                    int arity = optionAnnotation.arity();
                    Preconditions.checkArgument(arity >= 0 || arity == Integer.MIN_VALUE, "Invalid arity for option %s", name);

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
                    List<String> allowedValues = ImmutableList.copyOf(optionAnnotation.allowedValues());
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
        ListMultimap<OptionMetadata, OptionMetadata> metadataIndex = ArrayListMultimap.create();
        for (OptionMetadata option : options) {
            metadataIndex.put(option, option);
        }

        options = ImmutableList.copyOf(transform(metadataIndex.asMap().values(), new Function<Collection<OptionMetadata>, OptionMetadata>()
        {
            @Override
            public OptionMetadata apply(@Nullable Collection<OptionMetadata> options)
            {
                return new OptionMetadata(options);
            }
        }));

        Map<String, OptionMetadata> optionIndex = newHashMap();
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

    private static <T> ImmutableList<T> concat(Iterable<T> iterable, T item)
    {
        return ImmutableList.<T>builder().addAll(iterable).add(item).build();
    }

    private static class InjectionMetadata
    {
        private List<OptionMetadata> globalOptions = newArrayList();
        private List<OptionMetadata> groupOptions = newArrayList();
        private List<OptionMetadata> commandOptions = newArrayList();
        private List<ArgumentsMetadata> arguments = newArrayList();
        private List<Accessor> metadataInjections = newArrayList();

        private void compact()
        {
            globalOptions = mergeOptionSet(globalOptions);
            groupOptions = mergeOptionSet(groupOptions);
            commandOptions = mergeOptionSet(commandOptions);

            if (arguments.size() > 1) {
                arguments = ImmutableList.of(new ArgumentsMetadata(arguments));
            }
        }
    }
}
