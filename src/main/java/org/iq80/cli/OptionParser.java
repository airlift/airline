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


import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iq80.cli.model.OptionMetadata;

import java.util.Iterator;
import java.util.List;

public class OptionParser
{
    private final OptionMetadata metadata;
    private final TypeConverter typeConverter;

    public OptionParser(OptionMetadata metadata, TypeConverter typeConverter)
    {
        Preconditions.checkNotNull(metadata, "metadata is null");
        Preconditions.checkNotNull(typeConverter, "typeConverter is null");
        this.metadata = metadata;
        this.typeConverter = typeConverter;
    }

    public static List<OptionParser> from(final TypeConverter typeConverter, List<OptionMetadata> options)
    {
        return ImmutableList.copyOf(Lists.transform(options, new Function<OptionMetadata, OptionParser>()
        {
            @Override
            public OptionParser apply(OptionMetadata optionMetadata)
            {
                return new OptionParser(optionMetadata, typeConverter);
            }
        }));
    }

    public OptionMetadata getMetadata()
    {
        return metadata;
    }

    public boolean canParseOption(String currentArgument) {
        return metadata.getOptions().contains(currentArgument);
    }

    public List<Object> parseOption(String currentArgument, Iterator<String> args)
    {
        ImmutableList.Builder<Object> values = ImmutableList.builder();

        int arity = metadata.getArity();
        if (arity == 0) {
            // this is a boolean argument
            values.add(true);
        }
        else {
            for (int i = 0; i < arity; i++) {
                if (!args.hasNext()) {
                    throw new ParseException("Expected %s values after %s",
                            arity == 0 ? "a value" : arity + " values ",
                            currentArgument);
                }
                values.add(typeConverter.convert(metadata.getTitle(), metadata.getJavaType(), args.next()));
            }
        }

        return values.build();
    }

    @Override
    public String toString()
    {
        return "[OptionParser " + metadata + "]";
    }
}
