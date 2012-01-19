Git-like-cli
============

Git-like-cli is a Java annotation-based framework for parsing Git like command line structures.

Here is a quick example:

```java
public class Git
{
    public static void main(String[] args)
    {
        Builder<Runnable> builder = parser("git", Runnable.class)
                .withDescription("the stupid content tracker")
                .defaultCommand(HelpCommand.class)
                .addCommand(SuggestCommand.class)
                .addCommand(HelpCommand.class)
                .addCommand(Add.class);

        builder.addGroup("remote")
                .withDescription("Manage set of tracked repositories")
                .defaultCommand(RemoteShow.class)
                .addCommand(RemoteShow.class)
                .addCommand(RemoteAdd.class);

        GitLikeCommandParser<Runnable> gitParser = builder.build();

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
```
