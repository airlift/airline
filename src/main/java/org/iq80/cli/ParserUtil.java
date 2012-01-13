package org.iq80.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class ParserUtil
{
    public static List<String> expandArgs(Iterable<String> originalArgs, String optionSeparators)
    {
        //
        // Expand separators
        //
        List<String> expandedArgs = newArrayList();
        for (String arg : originalArgs) {
            if (isOption(arg) && optionSeparators != null) {
                Iterables.addAll(expandedArgs, Splitter.onPattern("[" + optionSeparators + "]").limit(2).split(arg));
            }
            else {
                expandedArgs.add(arg);
            }
        }

        return ImmutableList.copyOf(expandedArgs);
    }

    public static <T> T createInstance(Class<T> type)
    {
        if (type != null) {
            try {
                return type.getConstructor().newInstance();
            }
            catch (Exception e) {
                throw new ParseException("Unable to create instance %s", type.getName());
            }
        }
        return null;
    }

    public static List<String> parseOptions(Object instance,
            Map<String, OptionParser> optionsIndex,
            boolean validate,
            boolean stopAtFirstUnusedArg,
            List<String> parameters)
    {
        Preconditions.checkNotNull(instance, "instance is null");
        Preconditions.checkNotNull(optionsIndex, "optionsIndex is null");
        Preconditions.checkNotNull(parameters, "parameters is null");

        // remember which options are used for validation
        List<OptionParser> usedOptions = newArrayList();

        List<String> unusedArguments = newArrayList();
        Iterator<String> iterator = parameters.iterator();
        while (iterator.hasNext()) {
            String arg = iterator.next();
            if (!stopAtFirstUnusedArg && arg.equals("--")) {
                Iterators.addAll(unusedArguments, iterator);
                break;
            }
            else if (ParserUtil.isOption(arg)) {
                OptionParser optionParser = optionsIndex.get(arg);
                if (optionParser == null) {
                    throw new ParseException("Unknown option: %s", arg);
                }

                optionParser.parseOption(instance, arg, iterator);

                usedOptions.add(optionParser);
            }
            else if (!ParserUtil.isStringEmpty(arg)) {
                unusedArguments.add(arg);
                if (stopAtFirstUnusedArg) {
                    Iterators.addAll(unusedArguments, iterator);
                    break;
                }
            }
        }

        if (validate) {
            validateOptions(optionsIndex.values(), usedOptions);
        }

        return unusedArguments;
    }

    public static OptionsMetadata processAnnotations(Class<?> type, TypeConverter typeConverter)
    {
        OptionsMetadata optionsMetadata = new OptionsMetadata();
        processAnnotations(type, typeConverter, optionsMetadata, Lists.<Field>newArrayList());
        optionsMetadata.validate();
        return optionsMetadata;
    }

    public static void processAnnotations(Class<?> type, TypeConverter typeConverter, OptionsMetadata optionsMetadata, List<Field> fields)
    {
        for (Class<?> cls = type; !Object.class.equals(cls); cls = cls.getSuperclass()) {
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                ImmutableList<Field> path = concat(fields, field);

                Options optionsAnnotation = field.getAnnotation(Options.class);
                if (optionsAnnotation != null) {
                    switch (optionsAnnotation.value()) {
                        case GLOBAL:
                            optionsMetadata.setGlobalOptionsAccessors(new Accessor(field.getName(), path, typeConverter));
                            break;
                        case GROUP:
                            optionsMetadata.setGroupOptionsAccessor(new Accessor(field.getName(), path, typeConverter));
                            break;
                        case COMMAND:
                            processAnnotations(field.getType(), typeConverter, optionsMetadata, path);
                            break;
                    }
                }

                Option optionAnnotation = field.getAnnotation(Option.class);
                if (optionAnnotation != null) {
                    OptionParser optionParser = new OptionParser(optionAnnotation, path, typeConverter);
                    for (String name : optionParser.getOptions()) {
                        for (OptionParser existingOption : optionsMetadata.getOptions()) {
                            if (existingOption.getOptions().contains(name)) {
                                throw new ParseException("Found the option %s multiple times", name);
                            }
                        }
                    }
                    optionsMetadata.getOptions().add(optionParser);
                }

                Arguments argumentsAnnotation = field.getAnnotation(Arguments.class);
                if (argumentsAnnotation != null) {
                    optionsMetadata.setArgumentParser(new ArgumentParser(argumentsAnnotation, path, typeConverter));
                }
            }
        }
    }

    public static ImmutableList<Field> concat(Iterable<Field> fields, Field field)
    {
        return ImmutableList.<Field>builder().addAll(fields).add(field).build();
    }

    public static boolean isStringEmpty(String s)
    {
        return s == null || "".equals(s);
    }

    public static boolean isOption(String arg)
    {
        return arg.charAt(0) == '-';
    }

    public static void validateOptions(Iterable<OptionParser> allOptions, Collection<OptionParser> usedOptions)
    {
        Set<OptionParser> missingOptions = newHashSet();
        for (OptionParser optionParser : allOptions) {
            if (optionParser.isRequired() && !usedOptions.contains(optionParser)) {
                missingOptions.add(optionParser);
            }
        }

        if (!missingOptions.isEmpty()) {
            StringBuilder missingFields = new StringBuilder();
            for (OptionParser optionParser : missingOptions) {
                missingFields.append(optionParser.getOptions().get(0)).append(" ");
            }
            throw new ParseException("The following options are required: %s", missingFields);
        }
    }

    public static class OptionsMetadata
    {
        private final List<Accessor> globalOptionsAccessors = newArrayList();
        private final List<Accessor> groupOptionsAccessors = newArrayList();
        private final Collection<OptionParser> options = newArrayList();
        private final List<ArgumentParser> argumentParsers = newArrayList();

        public Accessor getGlobalOptionsAccessors()
        {
            return Iterables.getFirst(globalOptionsAccessors, null);
        }

        public void setGlobalOptionsAccessors(Accessor globalOptionsAccessors)
        {
            this.globalOptionsAccessors.add(globalOptionsAccessors);
        }

        public Accessor getGroupOptionsAccessor()
        {
            return Iterables.getFirst(groupOptionsAccessors, null);
        }

        public void setGroupOptionsAccessor(Accessor groupOptionsAccessor)
        {
            this.groupOptionsAccessors.add(groupOptionsAccessor);
        }

        public Collection<OptionParser> getOptions()
        {
            return options;
        }

        public ArgumentParser getArgumentParser()
        {
            return Iterables.getFirst(argumentParsers, null);
        }

        public void setArgumentParser(ArgumentParser argumentParser)
        {
            this.argumentParsers.add(argumentParser);
        }

        public void validate()
        {
            if (argumentParsers.size() > 1) {
                throw new ParseException("Only one field can be annotated with @Arguments, found: %s", argumentParsers);
            }
            if (globalOptionsAccessors.size() > 1) {
                throw new ParseException("Only one field can be annotated with @Options(GLOBAL), found: %s", globalOptionsAccessors);
            }
            if (groupOptionsAccessors.size() > 1) {
                throw new ParseException("Only one field can be annotated with @Options(GROUP), found: %s", groupOptionsAccessors);
            }
        }
    }
}
