package io.airlift.command;

import io.airlift.command.model.CommandMetadata;

import javax.inject.Inject;

public class HelpOption
{
    @Inject
    public CommandMetadata commandMetadata;

    @Option(name = {"-h", "--help"}, description = "Display help information")
    public Boolean help = false;

    public boolean showHelpIfRequested()
    {
        if (help) {
            Help.help(commandMetadata);
        }
        return help;
    }
}
