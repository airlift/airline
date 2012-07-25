package io.airlift.command;

import io.airlift.command.model.CommandGroupMetadata;
import io.airlift.command.model.CommandMetadata;
import io.airlift.command.model.GlobalMetadata;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.collect.Lists.newArrayList;

@Command(name = "help", description = "Display help information")
public class Help implements Runnable, Callable<Void>
{
    @Inject
    public GlobalMetadata global;

    @Arguments
    public List<String> command = newArrayList();

    @Override
    public void run()
    {
        help(global, command);
    }

    @Override
    public Void call()
    {
        run();
        return null;
    }

    public static void help(GlobalMetadata global, List<String> commandNames)
    {
        StringBuilder stringBuilder = new StringBuilder();
        help(global, commandNames, stringBuilder);
        System.out.println(stringBuilder.toString());
    }

    public static void help(GlobalMetadata global, List<String> commandNames, StringBuilder out)
    {
        if (commandNames.isEmpty()) {
            new GlobalUsageSummary().usage(global, out);
            return;
        }

        String name = commandNames.get(0);

        // main program?
        if (name.equals(global.getName())) {
            new GlobalUsage().usage(global, out);
            return;
        }

        // command in the default group?
        for (CommandMetadata command : global.getDefaultGroupCommands()) {
            if (name.equals(command.getName())) {
                new CommandUsage().usage(global.getName(), null, command, out);
                return;
            }
        }

        // command in a group?
        for (CommandGroupMetadata group : global.getCommandGroups()) {
            if (name.endsWith(group.getName())) {
                // general group help or specific command help?
                if (commandNames.size() == 1) {
                    new CommandGroupUsage().usage(global, group, out);
                    return;
                }
                else {
                    String commandName = commandNames.get(1);
                    for (CommandMetadata command : group.getCommands()) {
                        if (commandName.equals(command.getName())) {
                            new CommandUsage().usage(global.getName(), group.getName(), command, out);
                            return;
                        }
                    }
                    System.out.println("Unknown command " + name + " " + commandName);
                }
            }
        }

        System.out.println("Unknown command " + name);
    }
}
