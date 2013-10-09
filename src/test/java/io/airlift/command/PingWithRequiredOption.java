package io.airlift.command;

import javax.inject.Inject;

@Command(name = "ping", description = "network test utility")
public class PingWithRequiredOption
{
    @Inject
    public HelpOption helpOption;

    @Option(name = {"-c", "--count"}, required=true, description = "Send count packets")
    public int count = 1;

    public static void main(String... args)
    {
        try{
            PingWithRequiredOption ping = SingleCommand.singleCommand(PingWithRequiredOption.class).parse(args);
            if (ping.helpOption.showHelpIfRequested()) {
                return;
            }

            ping.run();
        }catch(ParseException e){
            System.out.println(e.getMessage());
            return;
        }
    }

    public void run()
    {
        System.out.println("Ping count: " + count);
    }
}
