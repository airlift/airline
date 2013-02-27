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

package io.airlift.command;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class as a command.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Command
{
    /**
     * Name of the command.  Command name is split on white space to form a multi-word name.
     */
    String name();

    /**
     * Description of the command.
     */
    String description() default "";

    /**
     * If true, this command won't appear in the usage().
     */
    boolean hidden() default false;

    /**
     * An array of lines of text to provide a series of example usages of the command.
     *
     * {@code
    examples = {"* Explain what the command example does",
    "    $ cli group cmd foo.txt file.json",
    "",
    "* Explain what this command example does",
    "    $ cli group cmd --non-standard-option value foo.txt"}
     }
     * Formatting and blank lines are preserved to give users leverage over how the examples are displayed in the usage.
     */
    String[] examples() default {};

    /**
     * Block of text that provides an extended discussion on the behavior of the command.  Should
     * supplement the shorter description which is more of a summary where discussion can get into
     * greater detail.
     */
    String discussion() default "";

    /**
     *  the group(s) this command should belong to.
     *  if left empty the command will belong to the default command group
     */
    String[] groupNames() default {};
}
