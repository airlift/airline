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

package io.airlift.command.command;

import com.google.common.collect.Lists;
import io.airlift.command.Cli;
import io.airlift.command.model.CommandMetadata;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static io.airlift.command.TestUtil.singleCommandParser;

public class CommandTest
{
    @Test
    public void namedCommandTest1()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandAdd.class)
                .withCommand(CommandCommit.class)
                .build();

        Object command = parser.parse("add", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandAdd);
        CommandAdd add = (CommandAdd) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldComplainIfNoAnnotations()
    {
        singleCommandParser(String.class);
    }

    @Test
    public void commandTest2()
    {
        Cli<?> parser = Cli.builder("git")
                .withCommand(CommandAdd.class)
                .withCommand(CommandCommit.class)
                .build();
        parser.parse("-v", "commit", "--amend", "--author", "cbeust", "A.java", "B.java");

        Object command = parser.parse("-v", "commit", "--amend", "--author", "cbeust", "A.java", "B.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandCommit);
        CommandCommit commit = (CommandCommit) command;

        Assert.assertTrue(commit.commandMain.verbose);
        Assert.assertTrue(commit.amend);
        Assert.assertEquals(commit.author, "cbeust");
        Assert.assertEquals(commit.files, Arrays.asList("A.java", "B.java"));
    }

    @Test
    public void testExample() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemove.class)
            .build();

        final List<CommandMetadata> commandParsers = parser.getMetadata().getDefaultGroupCommands();

        Assert.assertEquals(1, commandParsers.size());

        CommandMetadata aMeta = commandParsers.get(0);

        Assert.assertEquals("remove", aMeta.getName());

        Assert.assertEquals(Lists.newArrayList("* The following is a usage example:",
                                               "\t$ git remove -i myfile.java"), aMeta.getExamples());
    }

    @Test
    public void testDiscussion() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandRemove.class)
            .build();

        final List<CommandMetadata> commandParsers = parser.getMetadata().getDefaultGroupCommands();

        Assert.assertEquals(1, commandParsers.size());

        CommandMetadata aMeta = commandParsers.get(0);

        Assert.assertEquals("remove", aMeta.getName());

        Assert.assertEquals("More details about how this removes files from the index.", aMeta.getDiscussion());
    }

    @Test
    public void testDefaultCommandInGroup() {
        Cli<?> parser = Cli.builder("git")
            .withCommand(CommandAdd.class)
            .withCommand(CommandCommit.class)
            .withDefaultCommand(CommandAdd.class)
            .build();

        Object command = parser.parse("-i", "A.java");

        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandAdd);
        CommandAdd add = (CommandAdd) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }
    
    @Test
    public void testCommandWithArgsSeparator() {
    	Cli<?> parser = Cli.builder("git")
    	                .withCommand(CommandHighArityOption.class)
    	                .build();

    	        Object command = parser.parse("-v", "cmd", "--option", "val1", "val2", "val3", "val4", "--", "arg1", "arg2", "arg3");
    	        Assert.assertNotNull(command, "command is null");
    	        Assert.assertTrue(command instanceof CommandHighArityOption);
    	        CommandHighArityOption cmdHighArity = (CommandHighArityOption) command;

    	        Assert.assertTrue(cmdHighArity.commandMain.verbose);
    	        Assert.assertEquals(cmdHighArity.option, Arrays.asList("val1", "val2", "val3", "val4"));
    	        Assert.assertEquals(cmdHighArity.args, Arrays.asList("arg1", "arg2", "arg3"));
    }
}
