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
package io.airlift.airline;

import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Git.Add;
import io.airlift.airline.Git.RemoteAdd;
import io.airlift.airline.Git.RemoteShow;
import io.airlift.airline.args.*;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static io.airlift.airline.SingleCommand.singleCommand;
import static org.testng.Assert.assertEquals;

@Test
public class TestHelp
{
    //TODO some tests needed to get updated because the order of command options was changed by an upgrade to Java 8. It seems possible that Class#getDeclaredFields(...) was changed. The application shouldn't rely on that order...

    @Test
    public void testGit()
    {
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("git")
                .withDescription("the stupid content tracker")
                .withDefaultCommand(Help.class)
                .withCommand(Help.class)
                .withCommand(Add.class);

        builder.withGroup("remote")
                .withDescription("Manage set of tracked repositories")
                .withDefaultCommand(RemoteShow.class)
                .withCommand(RemoteShow.class)
                .withCommand(RemoteAdd.class);

        Cli<Runnable> gitParser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(gitParser.getMetadata(), new ArrayList<>(0), out);
        assertEquals(out.toString(), "usage: git [-v] <command> [<args>]\n" +
                "\n" +
                "The most commonly used git commands are:\n" +
                "    add      Add file contents to the index\n" +
                "    help     Display help information\n" +
                "    remote   Manage set of tracked repositories\n" +
                "\n" +
                "See 'git help <command>' for more information on a specific command.\n");

        out = new StringBuilder();
        Help.help(gitParser.getMetadata(), Arrays.asList("add"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        git add - Add file contents to the index\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        git [-v] add [-i] [--] [<patterns>...]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -i\n" +
                "            Add modified contents interactively.\n" +
                "\n" +
                "        -v\n" +
                "            Verbose mode\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options\n" +
                "\n" +
                "        <patterns>\n" +
                "            Patterns of files to be added\n" +
                "\n");

        out = new StringBuilder();
        Help.help(gitParser.getMetadata(), Arrays.asList("remote"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        git remote - Manage set of tracked repositories\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        git [-v] remote\n" +
                "        git [-v] remote add [-t <branch>]\n" +
                "        git [-v] remote show [-n]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -v\n" +
                "            Verbose mode\n" +
                "\n" +
                "COMMANDS\n" +
                "        With no arguments, Gives some information about the remote <name>\n" +
                "\n" +
                "        show\n" +
                "            Gives some information about the remote <name>\n" +
                "\n" +
                "            With -n option, Do not query remote heads\n" +
                "\n" +
                "        add\n" +
                "            Adds a remote\n" +
                "\n" +
                "            With -t option, Track only a specific branch\n" +
                "\n");
    }

    @Test
    public void testArgs1()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        Args1.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("Args1"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test Args1 - args1 description\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test Args1 [-bigdecimal <bigd>] [-date <date>] [-debug] [-double <doub>]\n" +
                "                [-float <floa>] [-groups <groups>]\n" +
                "                [(-verbose <verbose> | -log <verbose>)] [-long <l>] [--]\n" +
                "                [<parameters>...]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -bigdecimal <bigd>\n" +
                "            A BigDecimal number\n" +
                "\n" +
                "        -date <date>\n" +
                "            An ISO 8601 formatted date.\n" +
                "\n" +
                "        -debug\n" +
                "            Debug mode\n" +
                "\n" +
                "        -double <doub>\n" +
                "            A double number\n" +
                "\n" +
                "        -float <floa>\n" +
                "            A float number\n" +
                "\n" +
                "        -groups <groups>\n" +
                "            Comma-separated list of group names to be run\n" +
                "\n" +
                "        -verbose <verbose>, -log <verbose>\n" +
                "            Level of verbosity\n" +
                "\n" +
                "        -long <l>\n" +
                "            A long number\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options\n" +
                "\n" +
                "        <parameters>\n" +
                "\n" +
                "\n");
    }

    @Test
    public void testArgs2()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        Args2.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("Args2"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test Args2 -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test Args2 [-debug] [-groups <groups>] [-host <hosts>...]\n" +
                "                [(-verbose <verbose> | -log <verbose>)] [--] [<parameters>...]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -debug\n" +
                "            Debug mode\n" +
                "\n" +
                "        -groups <groups>\n" +
                "            Comma-separated list of group names to be run\n" +
                "\n" +
                "        -host <hosts>\n" +
                "            The host\n" +
                "\n" +
                "        -verbose <verbose>, -log <verbose>\n" +
                "            Level of verbosity\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options\n" +
                "\n" +
                "        <parameters>\n" +
                "            List of parameters\n" +
                "\n");
    }

    @Test
    public void testArgsAritySting()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsArityString.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("ArgsArityString"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test ArgsArityString -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsArityString [-pairs <pairs>...] [--] [<rest>...]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -pairs <pairs>\n" +
                "            Pairs\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options\n" +
                "\n" +
                "        <rest>\n" +
                "            Rest\n" +
                "\n");
    }

    @Test
    public void testArgsBooleanArity()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsBooleanArity.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("ArgsBooleanArity"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test ArgsBooleanArity -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsBooleanArity [-debug <debug>]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -debug <debug>\n" +
                "\n" +
                "\n");
    }

    @Test
    public void testArgsInherited()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsInherited.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("ArgsInherited"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test ArgsInherited -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsInherited [-child <child>] [-debug] [-groups <groups>]\n" +
                "                [-level <level>] [-log <log>] [--] [<parameters>...]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -child <child>\n" +
                "            Child parameter\n" +
                "\n" +
                "        -debug\n" +
                "            Debug mode\n" +
                "\n" +
                "        -groups <groups>\n" +
                "            Comma-separated list of group names to be run\n" +
                "\n" +
                "        -level <level>\n" +
                "            A long number\n" +
                "\n" +
                "        -log <log>\n" +
                "            Level of verbosity\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options\n" +
                "\n" +
                "        <parameters>\n" +
                "\n" +
                "\n");
    }

    @Test
    public void testArgsRequired()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsRequired.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("ArgsRequired"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test ArgsRequired -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test ArgsRequired [--] <parameters>...\n" +
                "\n" +
                "OPTIONS\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options\n" +
                "\n" +
                "        <parameters>\n" +
                "            List of files\n" +
                "\n");
    }

    @Test
    public void testOptionsRequired()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        OptionsRequired.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("OptionsRequired"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test OptionsRequired -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test OptionsRequired [--optional <optionalOption>]\n" +
                "                --required <requiredOption>\n" +
                "\n" +
                "OPTIONS\n" +
                "        --optional <optionalOption>\n" +
                "\n" +
                "\n" +
                "        --required <requiredOption>\n" +
                "\n" +
                "\n");
    }

    @Test
    public void testOptionsHidden()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        OptionsHidden.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("OptionsHidden"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test OptionsHidden -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test OptionsHidden [--optional <optionalOption>]\n" +
                "\n" +
                "OPTIONS\n" +
                "        --optional <optionalOption>\n" +
                "\n" +
                "\n");
    }

    @Test
    public void testCommandHidden()
    {
        CliBuilder<Object> builder = Cli.builder("test")
                .withDescription("Test commandline")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        ArgsRequired.class, CommandHidden.class);

        Cli<Object> parser = builder.build();

        StringBuilder out = new StringBuilder();
        Help.help(parser.getMetadata(), new ArrayList<>(0), out);
        assertEquals(out.toString(), "usage: test <command> [<args>]\n" +
                "\n" +
                "The most commonly used test commands are:\n" +
                "    ArgsRequired\n" +
                "    help           Display help information\n" +
                "\n" +
                "See 'test help <command>' for more information on a specific command.\n");

        out = new StringBuilder();
        Help.help(parser.getMetadata(), Arrays.asList("CommandHidden"), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test CommandHidden -\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test CommandHidden [--optional <optionalOption>]\n" +
                "\n" +
                "OPTIONS\n" +
                "        --optional <optionalOption>\n" +
                "\n" +
                "\n");
    }

    @Test
    public void testSingleCommandArgs1()
    {
        SingleCommand<Args1> command = singleCommand(Args1.class);

        StringBuilder out = new StringBuilder();
        new CommandUsage().usage(null, null, "test", command.getCommandMetadata(), out);
        assertEquals(out.toString(), "NAME\n" +
                "        test - args1 description\n" +
                "\n" +
                "SYNOPSIS\n" +
                "        test [-bigdecimal <bigd>] [-date <date>] [-debug] [-double <doub>]\n" +
                "                [-float <floa>] [-groups <groups>]\n" +
                "                [(-verbose <verbose> | -log <verbose>)] [-long <l>] [--]\n" +
                "                [<parameters>...]\n" +
                "\n" +
                "OPTIONS\n" +
                "        -bigdecimal <bigd>\n" +
                "            A BigDecimal number\n" +
                "\n" +
                "        -date <date>\n" +
                "            An ISO 8601 formatted date.\n" +
                "\n" +
                "        -debug\n" +
                "            Debug mode\n" +
                "\n" +
                "        -double <doub>\n" +
                "            A double number\n" +
                "\n" +
                "        -float <floa>\n" +
                "            A float number\n" +
                "\n" +
                "        -groups <groups>\n" +
                "            Comma-separated list of group names to be run\n" +
                "\n" +
                "        -verbose <verbose>, -log <verbose>\n" +
                "            Level of verbosity\n" +
                "\n" +
                "        -long <l>\n" +
                "            A long number\n" +
                "\n" +
                "        --\n" +
                "            This option can be used to separate command-line options from the\n" +
                "            list of argument, (useful when arguments might be mistaken for\n" +
                "            command-line options\n" +
                "\n" +
                "        <parameters>\n" +
                "\n" +
                "\n");
    }
}
