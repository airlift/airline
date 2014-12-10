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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Command(name = "Args1", description = "args1 description")
public class Args1
{
    @Arguments
    public List<String> parameters = Lists.newArrayList();

    @Option(name = {"-log", "-verbose"}, description = "Level of verbosity")
    public Integer verbose = 1;

    @Option(name = "-groups", description = "Comma-separated list of group names to be run")
    public String groups;

    @Option(name = "-debug", description = "Debug mode")
    public boolean debug = false;

    @Option(name = "-long", description = "A long number")
    public long l;

    @Option(name = "-double", description = "A double number")
    public double doub;

    @Option(name = "-float", description = "A float number")
    public float floa;

    @Option(name = "-bigdecimal", description = "A BigDecimal number")
    public BigDecimal bigd;

    @Option(name = "-date", description = "An ISO 8601 formatted date.")
    public Date date;
}
