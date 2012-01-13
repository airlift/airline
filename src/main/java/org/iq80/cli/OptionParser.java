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
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public class OptionParser
{
    private final String name;
    private final List<String> options;
    private final String description;
    private final int arity;
    private final boolean required;
    private final boolean hidden;
    private final Object defaultValue;
    private final Accessor accessor;

    public OptionParser(Option optionAnnotation, List<Field> path, TypeConverter typeConverter)
    {
        Preconditions.checkNotNull(optionAnnotation, "optionAnnotation is null");
        Preconditions.checkNotNull(path, "fields is null");
        Preconditions.checkArgument(!path.isEmpty(), "fields is empty");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");

        Field field = path.get(path.size() - 1);
        if (!optionAnnotation.name().isEmpty()) {
            this.name = optionAnnotation.name();
        } else {
            this.name = field.getName();
        }

        this.options = ImmutableList.copyOf(optionAnnotation.options());
        this.description = optionAnnotation.description();

        if (optionAnnotation.arity() != -1) {
            this.arity = optionAnnotation.arity();
        }
        else {
            Class<?> fieldType = field.getType();
            if (Boolean.class.isAssignableFrom(fieldType) || boolean.class.isAssignableFrom(fieldType)) {
                this.arity = 0;
            }
            else {
                this.arity = 1;
            }
        }

        this.required = optionAnnotation.required();
        this.hidden = optionAnnotation.hidden();

        this.defaultValue = null;

        accessor = new Accessor(name, path, typeConverter);
    }

    public String getName()
    {
        return name;
    }

    public List<String> getOptions()
    {
        return options;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isRequired()
    {
        return required;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }

    public String getPath()
    {
        return accessor.getPath();
    }

    public boolean isMultiOption()
    {
        return accessor.isMultiOption();
    }

    public void parseOption(Object instance, String currentArgument, Iterator<String> args)
    {
        if (arity == 0) {
            // this is a boolean argument
            accessor.addValue(instance, "true");
        }
        else {
            for (int i = 0; i < arity; i++) {
                if (!args.hasNext()) {
                    throw new ParseException("Expected %s values after %s",
                            arity == 0 ? "a value" : arity + " values ",
                            currentArgument);
                }
                accessor.addValue(instance, args.next());
            }
        }
    }

    @Override
    public String toString()
    {
        return "[OptionParser " + name + "]";
    }
}
