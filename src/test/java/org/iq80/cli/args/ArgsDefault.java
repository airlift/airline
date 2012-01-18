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

package org.iq80.cli.args;

import com.google.common.collect.Lists;
import org.iq80.cli.Arguments;
import org.iq80.cli.Option;

import java.util.List;

public class ArgsDefault
{
    @Arguments
    public List<String> parameters = Lists.newArrayList();

    @Option(options = "-log", description = "Level of verbosity")
    public Integer log = 1;

    @Option(options = "-groups", description = "Comma-separated list of group names to be run")
    public String groups;

    @Option(options = "-debug", description = "Debug mode")
    public boolean debug = false;

    @Option(options = "-level", description = "A long number")
    public long level;

}