package io.airlift.command.model;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import io.airlift.command.Accessor;
import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Group;
import io.airlift.command.Groups;
import io.airlift.command.Option;
import io.airlift.command.OptionType;
import io.airlift.command.Suggester;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;
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
        if (commandType == null) {
            return null;
        }
        
        Command command = null;
        List<Group> groups = Lists.newArrayList();
        
        for (Class<?> cls = commandType; command == null && !Object.class.equals(cls); cls = cls.getSuperclass()) {
            command = cls.getAnnotation(Command.class);
            
            if(cls.isAnnotationPresent(Groups.class))
            {
                groups.addAll(Arrays.asList(cls.getAnnotation(Groups.class).value()));
            }
            if(cls.isAnnotationPresent(Group.class))
            {
                groups.add(cls.getAnnotation(Group.class));
            }
        }
        Preconditions.checkArgument(command != null, "Command %s is not annotated with @Command", commandType.getName());
        String name = command.name();
        String description = command.description().isEmpty() ? null : command.description();
        List<String> groupNames = Arrays.asList(command.groupNames());
        
        boolean hidden = command.hidden();

        InjectionMetadata injectionMetadata = loadInjectionMetadata(commandType);

        CommandMetadata commandMetadata = new CommandMetadata(
                name,
                description,
                command.discussion().isEmpty() ? null : command.discussion(),
                command.examples().length == 0 ? null : Lists.newArrayList(command.examples()),
                hidden, injectionMetadata.globalOptions,
                injectionMetadata.groupOptions,
                injectionMetadata.commandOptions,
                Iterables.getFirst(injectionMetadata.arguments, null),
                injectionMetadata.metadataInjections,
                commandType,
                groupNames,
                groups);

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
        if(type.isInterface())
        {
            return;
        }
        
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

    public static void loadCommandsIntoGroupsByAnnotation(List<CommandMetadata> allCommands, List<CommandGroupMetadata> commandGroups, List<CommandMetadata> defaultCommandGroup)
    {
        List<CommandMetadata> newCommands = new ArrayList<CommandMetadata>();

        // first, create any groups explicitly annotated
        createGroupsFromAnnotations(allCommands,newCommands,commandGroups,defaultCommandGroup);
        
        for (CommandMetadata command : allCommands) {
            boolean added = false;
            
            //now add the command to any groupNames specified in the Command annotation
            for(String groupName : command.getGroupNames())
            {
                CommandGroupMetadata group = find(commandGroups, compose(equalTo(groupName), CommandGroupMetadata.nameGetter()), null);
                if (group != null) {
                    group.addCommand(command);
                    added = true;
                }
                else
                {
                    ImmutableList.Builder<OptionMetadata> groupOptionsBuilder = ImmutableList.builder();
                    groupOptionsBuilder.addAll(command.getGroupOptions());
                    CommandGroupMetadata newGroup = loadCommandGroup(groupName,"",null, Collections.singletonList(command));
                    commandGroups.add(newGroup);
                    added = true;
                }
            }

            if(added && defaultCommandGroup.contains(command))
            {
                defaultCommandGroup.remove(command);
            }
        }
        
        allCommands.addAll(newCommands);
    }

    private static void createGroupsFromAnnotations(List<CommandMetadata> allCommands, List<CommandMetadata> newCommands, List<CommandGroupMetadata> commandGroups, List<CommandMetadata> defaultCommandGroup)
    {
        for (CommandMetadata command : allCommands) {
            boolean added = false;

            // first, create any groups explicitly annotated
            for(Group groupAnno : command.getGroups())
            {
                Class defaultCommandClass = null;
                CommandMetadata defaultCommand = null;

                //load default command if needed
                if(!groupAnno.defaultCommand().equals(Group.DEFAULT.class))
                {
                    defaultCommandClass = groupAnno.defaultCommand();
                    defaultCommand = find(allCommands, compose(equalTo(defaultCommandClass), CommandMetadata.typeGetter()), null);
                    if(null == defaultCommand)
                    {
                        defaultCommand = loadCommand(defaultCommandClass);
                        newCommands.add(defaultCommand);
                    }
                }

                //load other commands if needed
                List<CommandMetadata> groupCommands = new ArrayList<CommandMetadata>(groupAnno.commands().length);
                CommandMetadata groupCommand = null;
                for(Class commandClass : groupAnno.commands())
                {
                    groupCommand = find(allCommands, compose(equalTo(commandClass), CommandMetadata.typeGetter()), null);
                    if(null == groupCommand)
                    {
                        groupCommand = loadCommand(commandClass);
                        newCommands.add(groupCommand);
                        groupCommands.add(groupCommand);
                    }
                }

                CommandGroupMetadata groupMetadata = find(commandGroups, compose(equalTo(groupAnno.name()), CommandGroupMetadata.nameGetter()), null);
                if(null == groupMetadata)
                {
                    groupMetadata = loadCommandGroup(groupAnno.name(),groupAnno.description(),defaultCommand, groupCommands);
                    commandGroups.add(groupMetadata);
                }

                groupMetadata.addCommand(command);
                added = true;
            }

            if(added && defaultCommandGroup.contains(command))
            {
                defaultCommandGroup.remove(command);
            }
        }
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
