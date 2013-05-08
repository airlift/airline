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
import static io.airlift.command.UsageHelper.DEFAULT_COMMAND_COMPARATOR;
import static io.airlift.command.UsageHelper.DEFAULT_OPTION_COMPARATOR;

public class CommandGroupUsage
{
    private final int columnSize;
    private final boolean hideGlobalOptions;
    private final Comparator<? super OptionMetadata> optionComparator;
    private final Comparator<? super CommandMetadata> commandComparator = DEFAULT_COMMAND_COMPARATOR;

    public CommandGroupUsage()
    {
        this(79, false, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandGroupUsage(int columnSize)
    {
        this(columnSize, false, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandGroupUsage(int columnSize, boolean hideGlobalOptions)
    {
        this(columnSize, hideGlobalOptions, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandGroupUsage(int columnSize, boolean hideGlobalOptions, @Nullable Comparator<? super OptionMetadata> optionComparator)
    {
        Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
        this.hideGlobalOptions = hideGlobalOptions;
        this.optionComparator = optionComparator;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(GlobalMetadata global, CommandGroupMetadata group)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(global, group, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(GlobalMetadata global, CommandGroupMetadata group, StringBuilder out)
    {
        usage(global, group, new UsagePrinter(out, columnSize));
    }

    public void usage(GlobalMetadata global, CommandGroupMetadata group, UsagePrinter out)
    {
        //
        // NAME
        //
        out.append("NAME").newline();

        out.newIndentedPrinter(8)
                .append(global.getName())
                .append(group.getName())
                .append("-")
                .append(group.getDescription())
                .newline()
                .newline();

        //
        // SYNOPSIS
        //
        out.append("SYNOPSIS").newline();
        UsagePrinter synopsis = out.newIndentedPrinter(8).newPrinterWithHangingIndent(8);

        List<CommandMetadata> commands = newArrayList(group.getCommands());
        Collections.sort(commands, commandComparator);

        if (group.getDefaultCommand() != null) {
            CommandMetadata command = group.getDefaultCommand();
            synopsis.append(global.getName());
            if (!hideGlobalOptions) {
                synopsis.appendWords(UsageHelper.toSynopsisUsage(command.getGlobalOptions()));
            }
            synopsis.append(group.getName()).appendWords(UsageHelper.toSynopsisUsage(command.getGroupOptions()));
            synopsis.newline();
        }
        for (CommandMetadata command : commands) {
            synopsis.append(global.getName());
            if (!hideGlobalOptions) {
                synopsis.appendWords(UsageHelper.toSynopsisUsage(command.getGlobalOptions()));
            }
            synopsis.append(group.getName()).appendWords(UsageHelper.toSynopsisUsage(command.getGroupOptions()));
            synopsis.append(command.getName()).appendWords(UsageHelper.toSynopsisUsage(command.getCommandOptions()));
            synopsis.newline();
        }
        synopsis.newline();

        //
        // OPTIONS
        //
        List<OptionMetadata> options = newArrayList();
        options.addAll(group.getOptions());
        if (!hideGlobalOptions) {
            options.addAll(global.getOptions());
        }
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
        if (commands.size() > 0 || group.getDefaultCommand() != null) {
            out.append("COMMANDS").newline();
            UsagePrinter commandPrinter = out.newIndentedPrinter(8);

            if (group.getDefaultCommand() != null && group.getDefaultCommand().getDescription() != null) {
                commandPrinter.append("With no arguments,")
                        .append(group.getDefaultCommand().getDescription())
                        .newline()
                        .newline();
            }

            for (CommandMetadata command : group.getCommands()) {
                commandPrinter.append(command.getName()).newline();
                UsagePrinter descriptionPrinter = commandPrinter.newIndentedPrinter(4);

                descriptionPrinter.append(command.getDescription()).newline().newline();

                for (OptionMetadata option : command.getCommandOptions()) {
                    if (!option.isHidden() && option.getDescription() != null) {
                        descriptionPrinter.append("With")
                                .append(longest(option.getOptions()))
                                .append("option,")
                                .append(option.getDescription())
                                .newline()
                                .newline();
                    }
                }
            }
        }
    }

    private static String longest(Iterable<String> iterable)
    {
        String longest = "";
        for (String value : iterable) {
            if (value.length() > longest.length()) {
                longest = value;
            }
        }
        return longest;
    }
}
