package org.iq80.cli;

import com.google.common.base.Preconditions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TypeConverter
{
    public static TypeConverter newInstance()
    {
        return new TypeConverter();
    }

    public Object convert(String name, Class<?> type, String value)
    {
        Preconditions.checkNotNull(name, "name is null");
        Preconditions.checkNotNull(type, "type is null");
        Preconditions.checkNotNull(value, "value is null");

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

        // Look for a static valueOf(String) method (this covers enums which have a valueOf method)
        try {
            Method valueOf = type.getMethod("valueOf", String.class);
            if (valueOf.getReturnType().isAssignableFrom(type)) {
                return valueOf.invoke(null, value);
            }
        }
        catch (Throwable ignored) {
        }

        // Look for a constructor taking a string
        try {
            Constructor<?> constructor = type.getConstructor(String.class);
            return constructor.newInstance(value);
        }
        catch (Throwable ignored) {
        }

        throw new ParseException(String.format("%s: couldn't convert \"%s\" to a %s", name, value, type.getSimpleName()));
    }
}
