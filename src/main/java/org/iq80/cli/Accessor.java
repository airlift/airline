package org.iq80.cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Accessor
{
    private final String name;
    private final List<Field> path;
    private final TypeConverter typeConverter;
    private final Class<?> type;

    public Accessor(String name, List<Field> path, TypeConverter typeConverter)
    {
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(path, "path is null");
        Preconditions.checkArgument(!path.isEmpty(), "path is empty");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");

        this.name = name;
        this.path = ImmutableList.copyOf(path);
        this.typeConverter = typeConverter;

        Field field = path.get(path.size() - 1);
        type = field.getType();
    }

    public Class<?> getType()
    {
        return type;
    }

    public String getPath()
    {
        return Joiner.on(',').join(Lists.transform(path, new Function<Field, String>()
        {
            public String apply(Field field)
            {
                return field.getName();
            }
        }));
    }

    public boolean isMultiOption()
    {
        return Collection.class.isAssignableFrom(type);
    }

    public Object getValue(Object instance)
    {
        StringBuilder pathName = new StringBuilder();
        for (Field intermediateField : path.subList(0, path.size() - 1)) {
            if (pathName.length() != 0) {
                pathName.append(".");
            }
            pathName.append(intermediateField.getName());

            try {
                instance = intermediateField.get(instance);
            }
            catch (IllegalAccessException e) {
                throw new ParseException(String.format("Error getting value of %s", pathName));
            }
            if (instance == null) {
                throw new ParseException(String.format("Field %s is null", pathName));
            }
        }
        return instance;
    }

    public void addValue(Object instance, Object value)
    {
        if (value instanceof String) {
            value = coerce((String) value);
        }

        // get the actual instance
        instance = getValue(instance);

        Field field = path.get(path.size() - 1);
        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection<Object> collection = getOrCreateCollectionField(name, instance, field);

            if (value instanceof Collection) {
                collection.addAll((Collection<?>) value);
            }
            else {
                collection.add(value);
            }
        }
        else {
            try {
                field.set(instance, value);
            }
            catch (IllegalAccessException e) {
                throw new ParseException(String.format("Error setting collection field %s for argument %s", field.getName(), name));
            }
        }
    }

    public Object coerce(String value)
    {
        Field field = path.get(path.size() - 1);
        Object convertedValue;
        if (Collection.class.isAssignableFrom(field.getType())) {
            convertedValue = typeConverter.convert(name, getRawType(getItemType(name, field.getGenericType())), value);
        }
        else {
            convertedValue = typeConverter.convert(name, getRawType(field.getType()), value);
        }
        return convertedValue;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("Accessor");
        sb.append("{path=").append(getPath());
        sb.append('}');
        return sb.toString();
    }

    //
    // Private reflection helper methods
    //

    @SuppressWarnings("unchecked")
    private static Collection<Object> newCollection(Class<?> type)
    {
        if (Collection.class.equals(type) || List.class.equals(type)) {
            return new ArrayList<Object>();
        }
        if (Set.class.equals(type)) {
            return new HashSet<Object>();
        }
        if (SortedSet.class.equals(type)) {
            return new TreeSet();
        }

        try {
            return (Collection<Object>) type.getConstructor().newInstance();
        }
        catch (Exception e) {
        }

        throw new ParseException("Parameters of Collection type '%s' are not supported. Please use List or Set instead.", type.getSimpleName());
    }

    private static Collection<Object> getOrCreateCollectionField(String name, Object object, Field field)
    {
        Collection<Object> collection;
        try {
            collection = (Collection<Object>) field.get(object);
        }
        catch (IllegalAccessException e) {
            throw new ParseException(String.format("Error getting collection field %s for argument %s", field.getName(), name));
        }

        if (collection == null) {
            collection = newCollection(field.getType());
            try {
                field.set(object, collection);
            }
            catch (IllegalAccessException e) {
                throw new ParseException(String.format("Error setting collection field %s for argument %s", field.getName(), name));
            }
        }
        return collection;
    }

    private static Class<?> getItemType(String name, Type type)
    {
        Class<?> rawClass = getRawType(type);
        if (rawClass == null) {
            throw new ParseException("Type of option %s be an exact type", name);
        }

        if (!Collection.class.isAssignableFrom(rawClass)) {
            return rawClass;
        }

        Type[] types = getTypeParameters(Collection.class, type);
        if ((types == null) || (types.length != 1)) {
            throw new ParseException("Unable to get item type of Collection option %s", name);
        }

        Type itemType = types[0];
        if (!(itemType instanceof Class)) {
            throw new ParseException("Collection type option %s must be an exact type", name);
        }

        return (Class<?>) itemType;
    }

    private static Class<?> getRawType(Type type)
    {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return getRawType(parameterizedType.getRawType());
        }
        return null;
    }

    private static Type[] getTypeParameters(Class<?> desiredType, Type type)
    {
        if (type instanceof Class) {
            Class<?> rawClass = (Class<?>) type;

            // if this is the collection class we're done
            if (desiredType.equals(type)) {
                return null;
            }

            for (Type iface : rawClass.getGenericInterfaces()) {
                Type[] collectionType = getTypeParameters(desiredType, iface);
                if (collectionType != null) {
                    return collectionType;
                }
            }

            return getTypeParameters(desiredType, rawClass.getGenericSuperclass());
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type rawType = parameterizedType.getRawType();
            if (desiredType.equals(rawType)) {
                return parameterizedType.getActualTypeArguments();
            }

            Type[] collectionTypes = getTypeParameters(desiredType, rawType);
            if (collectionTypes != null) {
                for (int i = 0; i < collectionTypes.length; i++) {
                    if (collectionTypes[i] instanceof TypeVariable) {
                        TypeVariable<?> typeVariable = (TypeVariable<?>) collectionTypes[i];
                        TypeVariable<?>[] rawTypeParams = ((Class<?>) rawType).getTypeParameters();
                        for (int j = 0; j < rawTypeParams.length; j++) {
                            if (typeVariable.getName().equals(rawTypeParams[j].getName())) {
                                collectionTypes[i] = parameterizedType.getActualTypeArguments()[j];
                            }
                        }
                    }
                }
            }
            return collectionTypes;
        }
        return null;
    }
}
