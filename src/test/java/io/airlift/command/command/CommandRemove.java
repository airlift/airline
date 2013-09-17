/**
 * Copyright (C) 2013 the original author or authors.
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
package io.airlift.command.command;

import java.util.List;

import javax.inject.Inject;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @since 0
 * @version 0
 */
@Command(name = "remove",
         description = "Remove file contents to the index",
         discussion = "More details about how this removes files from the index.",
         examples = {"* The following is a usage example:",
                    "\t$ git remove -i myfile.java"})
public class CommandRemove {
    @Inject
    public CommandMain commandMain;

    @Arguments(description = "Patterns of files to be added")
    public List<String> patterns;

    @Option(name = "-i")
    public Boolean interactive = false;

}
