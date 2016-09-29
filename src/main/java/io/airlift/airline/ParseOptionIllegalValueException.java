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

package io.airlift.airline;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Exception thrown when the value for an option is not in the list of allowed
 * values
 */
public class ParseOptionIllegalValueException extends ParseException {
    private static final long serialVersionUID = 810812151673279427L;
    
    private final String optionTitle, illegalValue;
    private final Set<String> allowedValues;

    ParseOptionIllegalValueException(String optionTitle, String value, Set<String> allowedValues) {
        super("Value for option '%s' was given as '%s' which is not in the list of allowed values: %s", optionTitle,
                value, allowedValues);
        this.optionTitle = optionTitle;
        this.illegalValue = value;
        this.allowedValues = ImmutableSet.copyOf(allowedValues);
    }

    /**
     * Gets the option title
     * 
     * @return Option title
     */
    public String getOptionTitle() {
        return optionTitle;
    }

    /**
     * Gets the illegal value
     * 
     * @return Illegal value
     */
    public String getIllegalValue() {
        return illegalValue;
    }

    /**
     * Gets the set of allowed values
     * 
     * @return Allowed values
     */
    public Set<String> getAllowedValues() {
        return allowedValues;
    }
}
