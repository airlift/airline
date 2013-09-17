// Copyright (c) 2010 - 2013, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

package io.airlift.command;

import static com.google.common.collect.Lists.newArrayList;
import static io.airlift.command.UsageHelper.DEFAULT_OPTION_COMPARATOR;
import static io.airlift.command.UsageHelper.toSynopsisUsage;
import io.airlift.command.model.ArgumentsMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.OptionMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class CommandUsage
{
    private final int columnSize;
    private final Comparator<? super OptionMetadata> optionComparator;

    public CommandUsage()
    {
        this(79, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandUsage(int columnSize)
    {
        this(columnSize, DEFAULT_OPTION_COMPARATOR);
    }

    public CommandUsage(int columnSize, @Nullable Comparator<? super OptionMetadata> optionComparator)
    {
        Preconditions.checkArgument(columnSize > 0, "columnSize must be greater than 0");
        this.columnSize = columnSize;
        this.optionComparator = optionComparator;
    }

    /**
     * Display the help on System.out.
     */
    public void usage(@Nullable String programName, @Nullable String groupName, CommandMetadata command)
    {
        StringBuilder stringBuilder = new StringBuilder();
        usage(programName, groupName, command, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    /**
     * Store the help in the passed string builder.
     */
    public void usage(@Nullable String programName, @Nullable String groupName, CommandMetadata command, StringBuilder out)
    {
        usage(programName, groupName, command, new UsagePrinter(out, columnSize));
    }

    public void usage(@Nullable String programName, @Nullable String groupName, CommandMetadata command, UsagePrinter out) {
        //
        // NAME
        //
        out.append("NAME").newline();

        out.newIndentedPrinter(8)
                .append(programName)
                .append(groupName)
                .append(command.getName())
                .append("-")
                .append(command.getDescription())
                .newline()
                .newline();

        //
        // SYNOPSIS
        //
        out.append("SYNOPSIS").newline();
        UsagePrinter synopsis = out.newIndentedPrinter(8).newPrinterWithHangingIndent(8);
        List<OptionMetadata> options = newArrayList();
        if (programName != null) {
            synopsis.append(programName).appendWords(toSynopsisUsage(sortOptions(command.getGlobalOptions())));
            options.addAll(command.getGlobalOptions());
        }
        if (groupName != null) {
            synopsis.append(groupName).appendWords(toSynopsisUsage(sortOptions(command.getGroupOptions())));
            options.addAll(command.getGroupOptions());
        }
        synopsis.append(command.getName()).appendWords(toSynopsisUsage(sortOptions(command.getCommandOptions())));
        options.addAll(command.getCommandOptions());

        // command arguments (optional)
        ArgumentsMetadata arguments = command.getArguments();
        if (arguments != null) {
            synopsis.append("[--]")
                    .append(UsageHelper.toUsage(arguments));
        }
        synopsis.newline();
        synopsis.newline();

        //
        // OPTIONS
        //
        if (options.size() > 0 || arguments != null) {
            options = sortOptions(options);

            out.append("OPTIONS").newline();

            for (OptionMetadata option : options) {
                // skip hidden options
                if (option.isHidden()) {
                    continue;
                }

                // option names
                UsagePrinter optionPrinter = out.newIndentedPrinter(8);
                optionPrinter.append(UsageHelper.toDescription(option)).newline();

                // description
                UsagePrinter descriptionPrinter = optionPrinter.newIndentedPrinter(4);
                descriptionPrinter.append(option.getDescription()).newline();

                descriptionPrinter.newline();
            }

            if (arguments != null) {
                // "--" option
                UsagePrinter optionPrinter = out.newIndentedPrinter(8);
                optionPrinter.append("--").newline();

                // description
                UsagePrinter descriptionPrinter = optionPrinter.newIndentedPrinter(4);
                descriptionPrinter.append("This option can be used to separate command-line options from the " +
                        "list of argument, (useful when arguments might be mistaken for command-line options").newline();
                descriptionPrinter.newline();

                // arguments name(s)
                optionPrinter.append(UsageHelper.toDescription(arguments)).newline();

                // description
                descriptionPrinter.append(arguments.getDescription()).newline();
                descriptionPrinter.newline();
            }
        }

        if (command.getDiscussion() != null) {
            out.append("DISCUSSION").newline();
            UsagePrinter disc = out.newIndentedPrinter(8);

            disc.append(command.getDiscussion())
                .newline()
                .newline();
        }

        if (command.getExamples() != null && !command.getExamples().isEmpty()) {
            out.append("EXAMPLES").newline();
            UsagePrinter ex = out.newIndentedPrinter(8);

            ex.appendTable(Iterables.partition(command.getExamples(), 1));
        }
    }

    private List<OptionMetadata> sortOptions(List<OptionMetadata> options) {
        if (optionComparator != null) {
            options = new ArrayList<OptionMetadata>(options);
            Collections.sort(options, optionComparator);
        }
        return options;
    }

    public String usageHTML(@Nullable String programName, @Nullable String groupName, CommandMetadata command) {
        final StringBuilder aBuilder = new StringBuilder("");

        final String NEWLINE = "<br/>\n";
        // TODO need boostrap css

        // todo close this
        aBuilder.append("<html>\n");
        aBuilder.append("<head>\n");
        aBuilder.append("<link href=\"css/bootstrap.min.css\" rel=\"stylesheet\" media=\"screen\">\n");
        aBuilder.append("</head>\n");
        aBuilder.append("<style>\n" +
                        "    body { margin: 50px; }\n" +
                        "</style>\n");
        aBuilder.append("<body>\n");

        aBuilder.append("<hr/>\n");
        aBuilder.append("<h1 class=\"text-info\">").append(programName).append(" ").append(groupName).append(" ").append(command.getName()).append(" Manual Page\n");
        aBuilder.append("<hr/>\n");

        aBuilder.append("<h1 class=\"text-info\">NAME</h1>\n").append(NEWLINE);

        aBuilder.append("<div class=\"row\">");
        aBuilder.append("<div class=\"span8 offset1\">");
        aBuilder.append(programName).append(" ");
        aBuilder.append(groupName).append(" ");
        aBuilder.append(command.getName()).append(" ");
        aBuilder.append("&mdash;");
        aBuilder.append(htmlize(command.getDescription()));
        aBuilder.append("</div>\n");
        aBuilder.append("</div>\n");

        aBuilder.append(NEWLINE);
        aBuilder.append("<h1 class=\"text-info\">SYNOPSIS</h1>\n").append(NEWLINE);

        List<OptionMetadata> options = newArrayList();
        aBuilder.append("<div class=\"row\">\n");
        aBuilder.append("<div class=\"span8 offset1\">\n");

        if (programName != null) {
            aBuilder.append(programName).append(" ").append(htmlize(Joiner.on(" ").join(toSynopsisUsage(sortOptions(command.getGlobalOptions())))));
            options.addAll(command.getGlobalOptions());
        }
        if (groupName != null) {
            aBuilder.append(groupName).append(" ").append(htmlize(Joiner.on(" ").join(toSynopsisUsage(sortOptions(command.getGroupOptions())))));
            options.addAll(command.getGroupOptions());
        }
        aBuilder.append(command.getName()).append(" ").append(htmlize(Joiner.on(" ").join(toSynopsisUsage(sortOptions(command.getCommandOptions())))));
        options.addAll(command.getCommandOptions());

        // command arguments (optional)
        ArgumentsMetadata arguments = command.getArguments();
        if (arguments != null) {
            aBuilder.append(" [--] ")
                    .append(htmlize(UsageHelper.toUsage(arguments)));
        }

        aBuilder.append("</div>\n");
        aBuilder.append("</div>\n");


        //
        // OPTIONS
        //
        if (options.size() > 0 || arguments != null) {
            options = sortOptions(options);

            aBuilder.append(NEWLINE);
            aBuilder.append("<h1 class=\"text-info\">OPTIONS</h1>\n").append(NEWLINE);

            for (OptionMetadata option : options) {
                // skip hidden options
                if (option.isHidden()) {
                    continue;
                }

                // option names
                aBuilder.append("<div class=\"row\">\n");
                aBuilder.append("<div class=\"span8 offset1\">\n");
                aBuilder.append(htmlize(UsageHelper.toDescription(option)));
                aBuilder.append("</div>\n");
                aBuilder.append("</div>\n");

                // description
                aBuilder.append("<div class=\"row\">\n");
                aBuilder.append("<div class=\"span8 offset2\">\n");
                aBuilder.append(htmlize(option.getDescription()));
                aBuilder.append("</div>\n");
                aBuilder.append("</div>\n");
            }

            if (arguments != null) {
                // "--" option
                aBuilder.append("<div class=\"row\">\n");
                aBuilder.append("<div class=\"span8 offset1\">\n");

                aBuilder.append("--\n");

                aBuilder.append("</div>\n");
                aBuilder.append("</div>\n");

                // description
                aBuilder.append("<div class=\"row\">\n");
                aBuilder.append("<div class=\"span8 offset2\">\n");

                aBuilder.append("This option can be used to separate command-line options from the " +
                                          "list of argument, (useful when arguments might be mistaken for command-line options\n");

                aBuilder.append("</div>\n");
                aBuilder.append("</div>\n");

                // arguments name
                aBuilder.append("<div class=\"row\">\n");
                aBuilder.append("<div class=\"span8 offset1\">\n");

                aBuilder.append(htmlize(UsageHelper.toDescription(arguments)));

                aBuilder.append("</div>\n");
                aBuilder.append("</div>\n");

                // description
                aBuilder.append("<div class=\"row\">\n");
                aBuilder.append("<div class=\"span8 offset2\">\n");

                aBuilder.append(htmlize(arguments.getDescription()));

                aBuilder.append("</div>\n");
                aBuilder.append("</div>\n");
            }
        }

        if (command.getDiscussion() != null) {
            aBuilder.append(NEWLINE);
            aBuilder.append("<h1 class=\"text-info\">DISCUSSION</h1>\n").append(NEWLINE);

            aBuilder.append("<div class=\"row\">\n");
            aBuilder.append("<div class=\"span8 offset1\">\n");

            aBuilder.append(htmlize(command.getDiscussion()));

            aBuilder.append("</div>\n");
            aBuilder.append("</div>\n");
        }

        if (command.getExamples() != null && !command.getExamples().isEmpty()) {
            aBuilder.append(NEWLINE);
            aBuilder.append("<h1 class=\"text-info\">EXAMPLES</h1>\n").append(NEWLINE);

            aBuilder.append("<div class=\"row\">\n");
            aBuilder.append("<div class=\"span12 offset1\">\n");

            // this will only work for "well-formed" examples
            for (int i = 0; i < command.getExamples().size(); i+=3) {
                String aText = command.getExamples().get(i).trim();
                String aEx = htmlize(command.getExamples().get(i+1));

                if (aText.startsWith("*")) {
                    aText = aText.substring(1).trim();
                }

                aBuilder.append("<p>\n");
                aBuilder.append(aText);
                aBuilder.append("</p>\n");

                aBuilder.append("<pre>\n");
                aBuilder.append(aEx);
                aBuilder.append("</pre>\n");
            }

            aBuilder.append("</div>\n");
            aBuilder.append("</div>\n");
        }

        aBuilder.append("</body>\n");
        aBuilder.append("</html>\n");

        return aBuilder.toString();
    }

    private static final String htmlize(final String theStr) {
        return theStr.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>");
    }
}
