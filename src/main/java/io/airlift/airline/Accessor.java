package io.airlift.airline;

import io.airlift.airline.util.ArgumentChecker;
import io.airlift.airline.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Accessor
{
    private final String name;
    private final Class<?> javaType;
    private final List<Field> path;
    private boolean multiValued;

    public Accessor(Field... path)
    {
        this(Arrays.asList(path));
    }

    public Accessor(Iterable<Field> path)
    {
        ArgumentChecker.checkNotNull(path, "path is null");
        ArgumentChecker.checkCondition(path.iterator().hasNext(), "path is empty");

        this.path = CollectionUtils.asList(path);

        this.name = this.path.get(0).getDeclaringClass().getSimpleName() + '.' + this.path.stream().map(Field::getName).collect(Collectors.joining("."));

        Field field = this.path.get(this.path.size() - 1);
        multiValued = Collection.class.isAssignableFrom(field.getType());
        javaType = getItemType(name, field.getGenericType());
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getJavaType()
    {
        return javaType;
    }

    public boolean isMultiValued()
    {
        return multiValued;
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
                Object nextInstance = intermediateField.get(instance);
                if (nextInstance == null) {
                    nextInstance = ParserUtil.createInstance(intermediateField.getType());
                    intermediateField.set(instance, nextInstance);
                }
                instance = nextInstance;
            }
            catch (Exception e) {
                throw new ParseException(e, "Error getting value of %s", pathName);
            }
        }
        return instance;
    }

    public void addValues(Object commandInstance, Iterable<?> values)
    {
        if (!values.iterator().hasNext()) {
            return;
        }

        // get the actual instance
        Object instance = getValue(commandInstance);

        Field field = path.get(path.size() - 1);
        field.setAccessible(true);

        final List<?> valueList = CollectionUtils.asList(values);
        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection<Object> collection = getOrCreateCollectionField(name, instance, field);
            collection.addAll(valueList);
        }
        else {
            try {
                if(valueList.isEmpty()) {
                    field.set(instance, null);
                } else {
                    field.set(instance, valueList.get(valueList.size() - 1));
                }
            }
            catch (Exception e) {
                throw new ParseException(e, "Error setting %s for argument %s", field.getName(), name);
            }
        }

    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Accessor accessor = (Accessor) o;

        if (!path.equals(accessor.path)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return path.hashCode();
    }

    @Override
    public String toString()
    {
        return name;
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
        catch (Exception ignored) {
        }

        throw new ParseException("Parameters of Collection type '%s' are not supported. Please use List or Set instead.", type.getSimpleName());
    }

    private static Collection<Object> getOrCreateCollectionField(String name, Object object, Field field)
    {
        Collection<Object> collection;
        try {
            collection = (Collection<Object>) field.get(object);
        }
        catch (Exception e) {
            throw new ParseException(e, "Error getting collection field %s for argument %s", field.getName(), name);
        }

        if (collection == null) {
            collection = newCollection(field.getType());
            try {
                field.set(object, collection);
            }
            catch (Exception e) {
                throw new ParseException(e, "Error setting collection field %s for argument %s", field.getName(), name);
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
