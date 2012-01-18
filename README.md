Git-like-cli
============

Git-like-cli is a Java annotation-based framework for parsing Git like command line structures.

Here is a quick example:

```java
public class Git
{
    public static void main(String[] args)
    {
        Builder<GitCommand> builder = parser("git", GitCommand.class)
                .withDescription("the stupid content tracker")
                .addCommand(Help.class)
                .addCommand(Add.class)
                .addCommand(RemoteShow.class)
                .addCommand(RemoteAdd.class);

        builder.addGroup("remote")
                .withDescription("Manage set of tracked repositories")
                .defaultCommand(RemoteShow.class)
                .addCommand(RemoteAdd.class)
                .addCommand(RemoteShow.class);

        GitLikeCommandParser<GitCommand> gitParser = builder.build();

        gitParser.parse(args).execute();
    }

    public class GitCommand
    {
        @Option(options = "-v", description = "Verbose mode")
        public boolean verbose;

        public void execute()
        {
            System.out.println(getClass().getSimpleName());
        }
    }

    @Command(name = "help", description = "Show help")
    public class Help extends GitCommand
    {
    }

    @Command(name = "add", description = "Add file contents to the index")
    public class Add extends GitCommand
    {
        @Arguments(description = "Patterns of files to be added")
        public List<String> patterns;

        @Option(options = "-i")
        public boolean interactive;
    }

    @Command(name = "show", description = "Gives some information about the remote <name>")
    public class RemoteShow extends GitCommand
    {
        @Arguments(description = "Remote to show")
        public String remote;
    }

    @Command(name = "add", description = "Adds a remote")
    public class RemoteAdd extends GitCommand
    {
        @Arguments(description = "Remote repository to add")
        public List<String> remote;
    }
}
```
