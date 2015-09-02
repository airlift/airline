/*
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

import io.airlift.airline.Cli;
import org.testng.annotations.Test;

import java.util.Arrays;

import static io.airlift.airline.TestingUtil.singleCommandParser;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandAdd);
        CommandAdd add = (CommandAdd) command;
        assertEquals(add.interactive.booleanValue(), true);
        assertEquals(add.patterns, Arrays.asList("A.java"));
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
        assertNotNull(command, "command is null");
        assertTrue(command instanceof CommandCommit);
        CommandCommit commit = (CommandCommit) command;

        assertTrue(commit.commandMain.verbose);
        assertTrue(commit.amend);
        assertEquals(commit.author, "cbeust");
        assertEquals(commit.files, Arrays.asList("A.java", "B.java"));
    }
}
