/*
 * Copyright (C) 2012 the original author or authors.
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

import com.google.common.base.Preconditions;
import io.airlift.command.model.CommandGroupMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.GlobalMetadata;
import io.airlift.command.model.OptionMetadata;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.airlift.command.UsageHelper.DEFAULT_OPTION_COMPARATOR;

public class GlobalUsage
{
    private final int columnSize;
    private final Comparator<? super OptionMetadata> optionComparator;

    public GlobalUsage()
    {
        this(79, DEFAULT_OPTION_COMPARATOR);
    }

    public GlobalUsage(int columnSize)
    {
        this(columnSize, DEFAULT_OPTION_COMPARATOR);
    }

    public GlobalUsage(int columnSize, @Nullable Comparator<? super OptionMetadata> optionComparator)
    {
        Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
        this.optionComparator = optionComparator;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(GlobalMetadata global)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(global, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(GlobalMetadata global, StringBuilder out)
    {
        usage(global, new UsagePrinter(out, columnSize));
    }

    public void usage(GlobalMetadata global, UsagePrinter out)
    {
        //
        // NAME
        //
        out.append("NAME").newline();

        out.newIndentedPrinter(8)
                .append(global.getName())
                .append("-")
                .append(global.getDescription())
                .newline()
                .newline();

        //
        // SYNOPSIS
        //
        out.append("SYNOPSIS").newline();
        out.newIndentedPrinter(8).newPrinterWithHangingIndent(8)
                .append(global.getName())
                .appendWords(UsageHelper.toSynopsisUsage(global.getOptions()))
                .append("<command> [<args>]")
                .newline()
                .newline();

        //
        // OPTIONS
        //
        List<OptionMetadata> options = newArrayList(global.getOptions());
        if (options.size() > 0) {
            if (optionComparator != null) {
                Collections.sort(options, optionComparator);
            }

            out.append("OPTIONS").newline();

            for (OptionMetadata option : options) {
                // option names
                UsagePrinter optionPrinter = out.newIndentedPrinter(8);
                optionPrinter.append(UsageHelper.toDescription(option)).newline();

                // description
                UsagePrinter descriptionPrinter = optionPrinter.newIndentedPrinter(4);
                descriptionPrinter.append(option.getDescription()).newline();

                descriptionPrinter.newline();
            }
        }

        //
        // COMMANDS
        //
        out.append("COMMANDS").newline();
        UsagePrinter commandPrinter = out.newIndentedPrinter(8);

        for (CommandMetadata command : global.getDefaultGroupCommands()) {
            printCommandDescription(commandPrinter, null, command);
        }
        for (CommandGroupMetadata group : global.getCommandGroups()) {
            for (CommandMetadata command : group.getCommands()) {
                printCommandDescription(commandPrinter, group, command);
            }
        }
    }

    private void printCommandDescription(UsagePrinter commandPrinter, @Nullable CommandGroupMetadata group, CommandMetadata command)
    {
        if (group != null) {
            commandPrinter.append(group.getName());
        }
        commandPrinter.append(command.getName()).newline();
        if (command.getDescription() != null) {
            commandPrinter.newIndentedPrinter(4).append(command.getDescription()).newline();
        }
        commandPrinter.newline();
    }
}
