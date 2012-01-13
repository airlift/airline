/**
 * Copyright (C) 2010 the original author or authors.
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

package org.iq80.cli;

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class ArgumentParser
{
    private final String name;
    private final String description;
    private final String usage;

    private final boolean required;
    private final Accessor accessor;

    public ArgumentParser(Arguments argumentsAnnotation, List<Field> path, TypeConverter typeConverter)
    {
        Preconditions.checkNotNull(argumentsAnnotation, "argumentsAnnotation is null");
        Preconditions.checkNotNull(path, "fields is null");
        Preconditions.checkArgument(!path.isEmpty(), "fields is empty");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");

        Field field = path.get(path.size() - 1);
        if (!argumentsAnnotation.name().isEmpty()) {
            this.name = argumentsAnnotation.name();
        }
        else {
            this.name = field.getName();
        }
        this.description = argumentsAnnotation.description();
        this.usage = argumentsAnnotation.usage();
        this.required = argumentsAnnotation.required();
        this.accessor = new Accessor(name, path, typeConverter);
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getUsage()
    {
        return usage;
    }

    public boolean isRequired()
    {
        return required;
    }

    public String getPath()
    {
        return accessor.getPath();
    }

    public boolean isMultiOption()
    {
        return accessor.isMultiOption();
    }

    public void addValue(Object instance, String value)
    {
        accessor.addValue(instance, value);
    }

    @Override
    public String toString()
    {
        return "[ArgumentParser " + name + "]";
    }
}
