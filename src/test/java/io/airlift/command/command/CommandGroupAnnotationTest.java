package io.airlift.command.command;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.airlift.command.Cli;
import io.airlift.command.ParseCommandUnrecognizedException;

public class CommandGroupAnnotationTest
{
    /*
        Tests for Groups -> Group annotations
     */
    @Test
    public void groupIsCreatedFromGroupsAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupsAnnotation.class)
                .build();

        Object command = parser.parse("groupInsideOfGroups", "commandWithGroupsAnno", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandWithGroupsAnnotation);
        CommandWithGroupsAnnotation add = (CommandWithGroupsAnnotation) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    @Test
    public void extraCommandsAreAddedFromGroupsAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupsAnnotation.class)
                .build();

        Object command = parser.parse("groupInsideOfGroups", "add", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandAdd);
        CommandAdd add = (CommandAdd) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    @Test(expectedExceptions = ParseCommandUnrecognizedException.class)
    public void commandRemovedFromDefaultGroupWithGroupsAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupsAnnotation.class)
                .build();

        Object command = parser.parse("commandWithGroupsAnno", "-i", "A.java");

    }

    /*
        Note: Disabling this test for now because there's a bug when the parser parses but doesn't find a command.
        It then properly uses the defaultCommand, however the input is still marked as unparsed which causes the default command to throw an exception.
        We need to fix the parser/CLI to re-parse the input after calling state.withCommand(defaultCommand)
     */
    @Test(enabled = false)
    public void defaultCommandIsAddedFromGroupsAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupsAnnotation.class)
                .build();

        Object command = parser.parse("groupInsideOfGroups", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandWithGroupsAnnotation);
        CommandWithGroupsAnnotation add = (CommandWithGroupsAnnotation) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    /*
        Tests for Group annotation
     */
    @Test
    public void groupIsCreatedFromGroupAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupAnnotation.class)
                .build();

        Object command = parser.parse("singleGroup", "commandWithGroup", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandWithGroupAnnotation);
        CommandWithGroupAnnotation add = (CommandWithGroupAnnotation) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    @Test
    public void extraCommandsAreAddedFromGroupAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupAnnotation.class)
                .build();

        Object command = parser.parse("singleGroup", "add", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandAdd);
        CommandAdd add = (CommandAdd) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    @Test(expectedExceptions = ParseCommandUnrecognizedException.class)
    public void commandRemovedFromDefaultGroupWithGroupAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupAnnotation.class)
                .build();

        Object command = parser.parse("commandWithGroup", "-i", "A.java");

    }

    /*
        Note: Disabling this test for now because there's a bug when the parser parses but doesn't find a command.
        It then properly uses the defaultCommand, however the input is still marked as unparsed which causes the default command to throw an exception.
        We need to fix the parser/CLI to re-parse the input after calling state.withCommand(defaultCommand)
     */
    @Test(enabled = false)
    public void defaultCommandIsAddedFromGroupAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupAnnotation.class)
                .build();

        Object command = parser.parse("singleGroup", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandWithGroupAnnotation);
        CommandWithGroupAnnotation add = (CommandWithGroupAnnotation) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    /*
       Tests for groupNames in Command annotation
    */
    @Test
    public void addedToGroupFromGroupAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommands(CommandWithGroupAnnotation.class,CommandWithGroupNames.class)
                .build();

        Object command = parser.parse("singleGroup", "commandWithGroupNames", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandWithGroupNames);
        CommandWithGroupNames add = (CommandWithGroupNames) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    @Test
    public void addedToSingletonGroupWithoutGroupAnnotation()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommands(CommandWithGroupNames.class)
                .build();

        Object command = parser.parse("singletonGroup", "commandWithGroupNames", "-i", "A.java");
        Assert.assertNotNull(command, "command is null");
        Assert.assertTrue(command instanceof CommandWithGroupNames);
        CommandWithGroupNames add = (CommandWithGroupNames) command;
        Assert.assertEquals(add.interactive.booleanValue(), true);
        Assert.assertEquals(add.patterns, Arrays.asList("A.java"));
    }

    @Test(expectedExceptions = ParseCommandUnrecognizedException.class)
    public void commandRemovedFromDefaultGroupWithGroupNames()
    {
        Cli<?> parser = Cli
                .buildCli("junk")
                .withCommand(CommandWithGroupNames.class)
                .build();

        Object command = parser.parse("commandWithGroupNames", "-i", "A.java");

    }

    
}
