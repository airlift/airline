package io.airlift.airline;

import io.airlift.airline.model.CommandMetadata;

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

    /**
     * Run the runnable if there are no parse errors and if help was not requested
     */
    public void runOrShowHelp(ParseResult parseResult, Runnable runnable) {
        if (parseResult.hasErrors()) {
            help = true;
            System.out.println(parseResult.getErrorMessage());
        }

        if (help) {
            Help.help(commandMetadata);
            return;
        }

        runnable.run();
    }

    /**
     * Run the runnable if help was not requested
     */
    public void runOrShowHelp(Runnable runnable) {
        runOrShowHelp(new ParseResult(), runnable);
    }
}
