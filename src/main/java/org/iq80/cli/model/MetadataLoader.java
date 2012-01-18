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
import org.iq80.cli.Options;

import javax.annotation.Nullable;
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
        validateOptionsSet(globalOptions);
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
        validateOptionsSet(groupOptions);
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

        List<OptionMetadata> globalOptions = loadOptionsSet(commandType, OptionType.GLOBAL);
        List<OptionMetadata> groupOptions = loadOptionsSet(commandType, OptionType.GROUP);
        List<OptionMetadata> commandOptions = loadOptionsSet(commandType, OptionType.COMMAND);

        ArgumentsMetadata arguments = loadArguments(commandType);

        List<Accessor> metadataInjections = loadMetadataInjections(commandType, ImmutableList.<Field>of());

        CommandMetadata commandMetadata = new CommandMetadata(
                name,
                description,
                globalOptions,
                groupOptions,
                commandOptions,
                arguments,
                metadataInjections,
                commandType);
        return commandMetadata;
    }

    public static List<OptionMetadata> loadOptionsSet(Class<?> type, OptionType optionType)
    {
        Preconditions.checkNotNull(type, "type is null");
        Preconditions.checkNotNull(optionType, "optionType is null");
        List<OptionMetadata> options = loadOptionsSet(type, optionType, ImmutableList.<Field>of());
        options = mergeOptionSet(options);
        validateOptionsSet(options);
        return options;
    }

    private static List<OptionMetadata> loadOptionsSet(Class<?> type, OptionType optionType, List<Field> fields)
    {
        ImmutableList.Builder<OptionMetadata> optionsSet = ImmutableList.builder();
        for (Class<?> cls = type; !Object.class.equals(cls); cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                ImmutableList<Field> path = concat(fields, field);

                Options optionsAnnotation = field.getAnnotation(Options.class);
                if (optionsAnnotation != null) {
                    optionsSet.addAll(loadOptionsSet(field.getType(), optionType, path));
                }

                Option optionAnnotation = field.getAnnotation(Option.class);
                if (optionAnnotation != null && optionAnnotation.type() == optionType) {
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

                    optionsSet.add(new OptionMetadata(optionType, options, name, description, arity, required, hidden, path, allowedValues));
                }
            }
        }
        return optionsSet.build();
    }

    private static List<Accessor> loadMetadataInjections(Class<?> type, List<Field> fields)
    {
        ImmutableList.Builder<Accessor> metadataInjections = ImmutableList.builder();
        for (Class<?> cls = type; !Object.class.equals(cls); cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                ImmutableList<Field> path = concat(fields, field);

                Options optionsAnnotation = field.getAnnotation(Options.class);
                if (optionsAnnotation != null) {
                    if (field.getType().equals(GlobalMetadata.class)) {
                        metadataInjections.add(new Accessor(path));
                    }
                }
            }
        }
        return metadataInjections.build();
    }

    private static List<OptionMetadata> mergeOptionSet(Iterable<OptionMetadata> options)
    {
        ListMultimap<OptionMetadata, OptionMetadata> optionIndex = ArrayListMultimap.create();
        for (OptionMetadata option : options) {
            optionIndex.put(option, option);
        }

        return ImmutableList.copyOf(transform(optionIndex.asMap().values(), new Function<Collection<OptionMetadata>, OptionMetadata>()
        {
            @Override
            public OptionMetadata apply(@Nullable Collection<OptionMetadata> options)
            {
                return new OptionMetadata(options);
            }
        }));
    }

    private static void validateOptionsSet(Iterable<OptionMetadata> options)
    {
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
    }

    private static ArgumentsMetadata loadArguments(Class<?> type)
    {
        List<Field> argumentsFields = newArrayList();
        for (Class<?> cls = type; !Object.class.equals(cls); cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(Arguments.class)) {
                    argumentsFields.add(field);
                }
            }
        }
        if (argumentsFields.isEmpty()) {
            return null;
        }

        Preconditions.checkArgument(argumentsFields.size() < 2, "Multiple fields in type %s are annotated with @Arguments: %s", type.getName(), argumentsFields);

        Field field = argumentsFields.get(0);

        Arguments argumentsAnnotation = field.getAnnotation(Arguments.class);
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

        return new ArgumentsMetadata(title, description, usage, required, field);
    }

    private static <T> ImmutableList<T> concat(Iterable<T> iterable, T item)
    {
        return ImmutableList.<T>builder().addAll(iterable).add(item).build();
    }
}
