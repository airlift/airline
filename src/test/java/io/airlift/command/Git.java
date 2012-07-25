package org.iq80.cli;

import org.iq80.cli.Cli.CliBuilder;

import java.util.List;

import static org.iq80.cli.Cli.buildCli;
import static org.iq80.cli.OptionType.GLOBAL;

public class Git
{
    public static void main(String... args)
    {
        CliBuilder<Runnable> builder = buildCli("git", Runnable.class)
                .withDescription("the stupid content tracker")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        Add.class);

        builder.withGroup("remote")
                .withDescription("Manage set of tracked repositories")
                .withDefaultCommand(RemoteShow.class)
                .withCommands(RemoteShow.class,
                        RemoteAdd.class);

        Cli<Runnable> gitParser = builder.build();

        gitParser.parse(args).run();
    }

    public static class GitCommand implements Runnable
    {
        @Option(type = GLOBAL, name = "-v", description = "Verbose mode")
        public boolean verbose;

        public void run()
        {
            System.out.println(getClass().getSimpleName());
        }
    }

    @Command(name = "add", description = "Add file contents to the index")
    public static class Add extends GitCommand
    {
        @Arguments(description = "Patterns of files to be added")
        public List<String> patterns;

        @Option(name = "-i", description = "Add modified contents interactively.")
        public boolean interactive;
    }

    @Command(name = "show", description = "Gives some information about the remote <name>")
    public static class RemoteShow extends GitCommand
    {
        @Option(name = "-n", description = "Do not query remote heads")
        public boolean noQuery;

        @Arguments(description = "Remote to show")
        public String remote;
    }

    @Command(name = "add", description = "Adds a remote")
    public static class RemoteAdd extends GitCommand
    {
        @Option(name = "-t", description = "Track only a specific branch")
        public String branch;

        @Arguments(description = "Remote repository to add")
        public List<String> remote;
    }
}
