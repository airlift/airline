package io.airlift.airline;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public class TypeConverter
{
    public static TypeConverter newInstance()
    {
        return new TypeConverter();
    }

    public Object convert(String name, Class<?> type, String value)
    {
        requireNonNull(name, "name is null");
        requireNonNull(type, "type is null");
        requireNonNull(value, "value is null");

        try {
            if (String.class.isAssignableFrom(type)) {
                return value;
            }
            else if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE.isAssignableFrom(type)) {
                return Boolean.valueOf(value);
            }
            else if (Byte.class.isAssignableFrom(type) || Byte.TYPE.isAssignableFrom(type)) {
                return Byte.valueOf(value);
            }
            else if (Short.class.isAssignableFrom(type) || Short.TYPE.isAssignableFrom(type)) {
                return Short.valueOf(value);
            }
            else if (Integer.class.isAssignableFrom(type) || Integer.TYPE.isAssignableFrom(type)) {
                return Integer.valueOf(value);
            }
            else if (Long.class.isAssignableFrom(type) || Long.TYPE.isAssignableFrom(type)) {
                return Long.valueOf(value);
            }
            else if (Float.class.isAssignableFrom(type) || Float.TYPE.isAssignableFrom(type)) {
                return Float.valueOf(value);
            }
            else if (Double.class.isAssignableFrom(type) || Double.TYPE.isAssignableFrom(type)) {
                return Double.valueOf(value);
            }
        }
        catch (Exception ignored) {
        }

        // Look for a static fromString(String) method
        try {
            Method valueOf = type.getMethod("fromString", String.class);
            if (valueOf.getReturnType().isAssignableFrom(type)) {
                return valueOf.invoke(null, value);
            }
        }
        catch (InvocationTargetException ex) {
            throw new ParseOptionConversionException(name, value, type.getSimpleName(), ex.getTargetException());
        }
        catch (IllegalAccessException | NoSuchMethodException ignored) {
        }

        // Look for a static valueOf(String) method (this covers enums which have a valueOf method)
        try {
            Method valueOf = type.getMethod("valueOf", String.class);
            if (valueOf.getReturnType().isAssignableFrom(type)) {
                return valueOf.invoke(null, value);
            }
        }
        catch (InvocationTargetException ex) {
            throw new ParseOptionConversionException(name, value, type.getSimpleName(), ex.getTargetException());
        }
        catch (IllegalAccessException | NoSuchMethodException ignored) {
        }

        // Look for a constructor taking a string
        try {
            Constructor<?> constructor = type.getConstructor(String.class);
            return constructor.newInstance(value);
        }
        catch (InvocationTargetException ex) {
            throw new ParseOptionConversionException(name, value, type.getSimpleName(), ex.getTargetException());
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException ignored) {
        }

        throw new ParseOptionConversionException(name, value, type.getSimpleName());
    }
}
