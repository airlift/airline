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

package io.airlift.airline.args;

import com.google.common.collect.Lists;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.util.List;

@Command(name = "ArgsSingleChar")
public class ArgsSingleChar
{
    @Arguments
    public List<String> parameters = Lists.newArrayList();

    @Option(name = {"-l"}, description = "Long")
    public boolean l = false;

    @Option(name = "-g", description = "Global")
    public boolean g = false;

    @Option(name = "-d", description = "Debug mode")
    public boolean d = false;

    @Option(name = "-s", description = "A string")
    public String s = null;

    @Option(name = "-p", description = "A path")
    public String p = null;

    @Option(name = "-n", description = "No action")
    public boolean n = false;

    @Option(name = "-2", description = "Two")
    public boolean two = false;

    @Option(name = "-f", description = "A filename")
    public String f = null;

    @Option(name = "-z", description = "Compress")
    public boolean z = false;

    @Option(name = "--D", description = "Directory")
    public String dir;
}
