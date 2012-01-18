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

package org.iq80.cli.command;

import org.iq80.cli.GitLikeCommandParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.iq80.cli.TestUtil.singleCommandParser;

public class CommandTest
{
    @Test
    public void namedCommandTest1()
    {
        GitLikeCommandParser<?> parser = GitLikeCommandParser
                .parser("git")
                .addCommand(CommandAdd.class)
                .addCommand(CommandCommit.class)
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
        GitLikeCommandParser<?> parser = GitLikeCommandParser
                .parser("git")
                .addCommand(CommandAdd.class)
                .addCommand(CommandCommit.class)
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
}
