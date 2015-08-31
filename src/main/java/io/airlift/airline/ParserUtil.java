package io.airlift.airline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import io.airlift.airline.model.ArgumentsMetadata;
import io.airlift.airline.model.OptionMetadata;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;

public class ParserUtil
{
    public static <T> T createInstance(Class<T> type)
    {
        if (type != null) {
            try {
                return type.getConstructor().newInstance();
            }
            catch (Exception e) {
                throw new ParseException(e, "Unable to create instance %s", type.getName());
            }
        }
        return null;
    }

    public static <T> T createInstance(Class<?> type,
        Iterable<OptionMetadata> options,
        ListMultimap<OptionMetadata, Object> parsedOptions,
        ArgumentsMetadata arguments,
        Iterable<Object> parsedArguments,
        Iterable<Accessor> metadataInjection,
        Map<Class<?>, Object> bindings)
    {
        return createInstance(type, options, parsedOptions, arguments, parsedArguments, metadataInjection, bindings, new CommandFactoryDefault<T>());
    }
    
    
    public static <T> T injectOptions(T commandInstance,
        Iterable<OptionMetadata> options,
        ListMultimap<OptionMetadata, Object> parsedOptions,
        ArgumentsMetadata arguments,
        Iterable<Object> parsedArguments,
        Iterable<Accessor> metadataInjection,
        Map<Class<?>, Object> bindings)
    {      
        // inject options
        for (OptionMetadata option : options) {
            List<?> values = parsedOptions.get(option);
            if (option.getArity() > 1 && !values.isEmpty()) {
                // hack: flatten the collection
                values = ImmutableList.copyOf(concat((Iterable<Iterable<Object>>) values));
            }
            if (values != null && !values.isEmpty()) {
                for (Accessor accessor : option.getAccessors()) {
                    accessor.addValues(commandInstance, values);
                }
            }
        }
  
        // inject args
        if (arguments != null && parsedArguments != null) {
            for (Accessor accessor : arguments.getAccessors()) {
                accessor.addValues(commandInstance, parsedArguments);
            }
        }
  
        for (Accessor accessor : metadataInjection) {
            Object injectee = bindings.get(accessor.getJavaType());
  
            if (injectee != null) {
                accessor.addValues(commandInstance, ImmutableList.of(injectee));
            }
        }
  
        return commandInstance;
    }
    
    
    public static <T> T createInstance(Class<?> type,
            Iterable<OptionMetadata> options,
            ListMultimap<OptionMetadata, Object> parsedOptions,
            ArgumentsMetadata arguments,
            Iterable<Object> parsedArguments,
            Iterable<Accessor> metadataInjection,
            Map<Class<?>, Object> bindings,
            CommandFactory<T> commandFactory)
    {
        // create the command instance
        T commandInstance = (T) commandFactory.createInstance(type);

        return injectOptions(commandInstance, options, parsedOptions, arguments, parsedArguments, metadataInjection, bindings);        
    }
}
