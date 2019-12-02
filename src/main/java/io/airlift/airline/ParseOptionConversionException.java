/*
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

package io.airlift.airline;

public class ParseOptionConversionException
        extends ParseException
{
    private final String optionTitle;
    private final String value;
    private final String typeName;

    ParseOptionConversionException(String optionTitle, String value, String typeName)
    {
        this(optionTitle, value, typeName, null);
    }

    ParseOptionConversionException(String optionTitle, String value, String typeName, Throwable cause)
    {
        super(cause, "%s: can not convert \"%s\" to a %s", optionTitle, value, typeName);
        this.optionTitle = optionTitle;
        this.value = value;
        this.typeName = typeName;
    }

    public String getOptionTitle()
    {
        return optionTitle;
    }

    public String getValue()
    {
        return value;
    }

    public String getTypeName()
    {
        return typeName;
    }
}
