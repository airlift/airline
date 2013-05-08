/*
 * Copyright (C) 2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.command;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

        Optional<Object> converted = builtinValueOf(type, value)
                .or(fromString(type, value))
                .or(valueOf(type, value))
                .or(stringCtor(type, value));

        if (converted.isPresent())
            return converted.get();

        throw new ParseOptionConversionException(name, value, type.getSimpleName());
    }

    private Optional<Object> builtinValueOf(Class<?> type, String value) {
        Object converted = null;
        try {
            if (String.class.isAssignableFrom(type)) {
                converted = value;
            }
            else if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE.isAssignableFrom(type)) {
                converted = Boolean.valueOf(value);
            }
            else if (Byte.class.isAssignableFrom(type) || Byte.TYPE.isAssignableFrom(type)) {
                converted = Byte.valueOf(value);
            }
            else if (Short.class.isAssignableFrom(type) || Short.TYPE.isAssignableFrom(type)) {
                converted = Short.valueOf(value);
            }
            else if (Integer.class.isAssignableFrom(type) || Integer.TYPE.isAssignableFrom(type)) {
                converted = Integer.valueOf(value);
            }
            else if (Long.class.isAssignableFrom(type) || Long.TYPE.isAssignableFrom(type)) {
                converted = Long.valueOf(value);
            }
            else if (Float.class.isAssignableFrom(type) || Float.TYPE.isAssignableFrom(type)) {
                converted = Float.valueOf(value);
            }
            else if (Double.class.isAssignableFrom(type) || Double.TYPE.isAssignableFrom(type)) {
                converted = Double.valueOf(value);
            }
            return Optional.fromNullable(converted);
        }
        catch (RuntimeException ignored) {
            return Optional.absent();
        }
    }

    // Look for a static fromString(String) method
    private Optional<Object> fromString(Class<?> type, String value) {
        try {
            Method fromString = type.getMethod("fromString", String.class);
            if (fromString.getReturnType().isAssignableFrom(type)) {
                return Optional.of(fromString.invoke(null, value));
            }
            return Optional.absent();
        }
        catch (NoSuchMethodException e) {
            return Optional.absent();
        }
        catch (IllegalAccessException e) {
            return Optional.absent();
        }
        catch (InvocationTargetException e) {
            return Optional.absent();
        }
    }

    // Look for a static valueOf(String) method (this covers enums which have a valueOf method)
    private Optional<Object> valueOf(Class<?> type, String value) {
        try {
            Method valueOf = type.getMethod("valueOf", String.class);
            if (valueOf.getReturnType().isAssignableFrom(type)) {
                return Optional.of(valueOf.invoke(null, value));
            }
            return Optional.absent();
        }
        catch (NoSuchMethodException e) {
            return Optional.absent();
        }
        catch (IllegalAccessException e) {
            return Optional.absent();
        }
        catch (InvocationTargetException e) {
            return Optional.absent();
        }
    }

    // Look for a constructor taking a string
    private Optional<Object> stringCtor(Class<?> type, String value) {
        try {
            Constructor<?> constructor = type.getConstructor(String.class);
            return Optional.of(Object.class.cast(constructor.newInstance(value)));
        }
        catch (InstantiationException e) {
            return Optional.absent();
        }
        catch (IllegalAccessException e) {
            return Optional.absent();
        }
        catch (InvocationTargetException e) {
            return Optional.absent();
        }
        catch (NoSuchMethodException e) {
            return Optional.absent();
        }
    }
}
