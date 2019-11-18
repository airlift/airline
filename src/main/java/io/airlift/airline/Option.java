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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Option
{
    /**
     * Is this a command, group or global option
     */
    OptionType type() default OptionType.COMMAND;

    /**
     * Name used to identify the option value in documentation and error messages.
     */
    String title() default "";

    /**
     * An array of allowed command line parameters (e.g. "-n", "--name", etc...).
     */
    String[] name();

    /**
     * A description of this option.
     */
    String description() default "";

    /**
     * Whether this option is required.
     */
    boolean required() default false;

    /**
     * How many parameter values this option will consume. For example,
     * an arity of 2 will allow "-pair value1 value2".
     */
    int arity() default Integer.MIN_VALUE;

    /**
     * If true, this parameter won't appear in the usage().
     */
    boolean hidden() default false;

    String[] allowedValues() default {};
}
