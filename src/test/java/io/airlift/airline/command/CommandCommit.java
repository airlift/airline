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

package io.airlift.airline.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import javax.inject.Inject;

import java.util.List;

@Command(name = "commit", description = "Record changes to the repository")
public class CommandCommit
{
    @Inject
    public CommandMain commandMain;

    @Arguments(description = "List of files")
    public List<String> files;

    @Option(name = "--amend", description = "Amend")
    public Boolean amend = false;

    @Option(name = "--author")
    public String author;
}
