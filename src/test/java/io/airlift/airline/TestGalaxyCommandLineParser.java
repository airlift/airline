package io.airlift.airline;

import io.airlift.airline.Cli.CliBuilder;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.airlift.airline.OptionType.GLOBAL;

public class TestGalaxyCommandLineParser
{
    @Test
    public void test()
    {
        parse();
        parse("help");
        parse("help", "galaxy");
        parse("help", "show");
        parse("help", "install");
        parse("help", "upgrade");
        parse("help", "upgrade");
        parse("help", "terminate");
        parse("help", "start");
        parse("help", "stop");
        parse("help", "restart");
        parse("help", "reset-to-actual");
        parse("help", "ssh");
        parse("help", "agent");
        parse("help", "agent", "show");
        parse("help", "agent", "add");

        parse("--debug", "show", "-u", "b2", "--state", "r");
        parse("--debug", "install", "com.proofpoint.discovery:discovery-server:1.1", "@discovery:general:1.0");
        parse("--debug", "upgrade", "-u", "b2", "1.1", "@1.0");
        parse("--debug", "upgrade", "-u", "b2", "1.1", "@1.0", "-s", "r");
        parse("--debug", "terminate", "-u", "b2");
        parse("--debug", "start", "-u", "b2");
        parse("--debug", "stop", "-u", "b2");
        parse("--debug", "restart", "-u", "b2");
        parse("--debug", "reset-to-actual", "-u", "b2");
        parse("--debug", "ssh");
        parse("--debug", "ssh", "-u", "b2", "--state", "r", "tail -F var/log/launcher.log");
        parse("--debug", "agent");
        parse("--debug", "agent", "show");
        parse("--debug", "agent", "add", "--count", "4", "t1.micro");
    }

    private Cli<GalaxyCommand> createParser()
    {
        CliBuilder<GalaxyCommand> builder = Cli.<GalaxyCommand>builder("galaxy")
                .withDescription("cloud management system")
                .withDefaultCommand(HelpCommand.class)
                .withCommand(HelpCommand.class)
                .withCommand(ShowCommand.class)
                .withCommand(InstallCommand.class)
                .withCommand(UpgradeCommand.class)
                .withCommand(TerminateCommand.class)
                .withCommand(StartCommand.class)
                .withCommand(StopCommand.class)
                .withCommand(RestartCommand.class)
                .withCommand(SshCommand.class)
                .withCommand(ResetToActualCommand.class);

        builder.withGroup("agent")
                .withDescription("Manage agents")
                .withDefaultCommand(AgentShowCommand.class)
                .withCommand(AgentShowCommand.class)
                .withCommand(AgentAddCommand.class)
                .withCommand(AgentTerminateCommand.class);

        return builder.build();
    }

    private void parse(String... args)
    {
        System.out.println("$ galaxy " + Arrays.asList(args).stream().collect(Collectors.joining(" ")));
        GalaxyCommand command = createParser().parse(args);
        command.execute();
        System.out.println();
    }

    private static class GlobalOptions
    {
        @Option(type = GLOBAL, name = "--debug", description = "Enable debug messages")
        public boolean debug;

        @Option(type = GLOBAL, name = "--coordinator", description = "Galaxy coordinator host (overrides GALAXY_COORDINATOR)")
        public String coordinator;

        private GlobalOptions() {
            debug = false;
            coordinator = System.getenv("GALAXY_COORDINATOR");
            if(coordinator == null) {
                coordinator = "http://localhost:64000";
            }
        }
    }

    public static class SlotFilter
    {
        @Option(name = {"-b", "--binary"}, description = "Select slots with a given binary")
        public List<String> binary;

        @Option(name = {"-c", "--config"}, description = "Select slots with a given configuration")
        public List<String> config;

        @Option(name = {"-i", "--host"}, description = "Select slots on the given host")
        public List<String> host;

        @Option(name = {"-I", "--ip"}, description = "Select slots at the given IP address")
        public List<String> ip;

        @Option(name = {"-u", "--uuid"}, description = "Select slot with the given UUID")
        public List<String> uuid;

        @Option(name = {"-s", "--state"}, description = "Select 'r{unning}', 's{topped}' or 'unknown' slots")
        public List<String> state;
    }

    public static class AgentFilter
    {
        @Option(name = {"-i", "--host"}, description = "Select slots on the given host")
        public final List<String> host = new ArrayList<>();

        @Option(name = {"-I", "--ip"}, description = "Select slots at the given IP address")
        public final List<String> ip = new ArrayList<>();

        @Option(name = {"-u", "--uuid"}, description = "Select slot with the given UUID")
        public final List<String> uuid = new ArrayList<>();

        @Option(name = {"-s", "--state"}, description = "Select 'r{unning}', 's{topped}' or 'unknown' slots")
        public final List<String> state = new ArrayList<>();
    }

    public static abstract class GalaxyCommand
    {
        @Inject
        public GlobalOptions globalOptions = new GlobalOptions();

        public void execute()
        {
            System.out.println(this);
        }
    }

    @Command(name = "help", description = "Display help information about galaxy")
    public static class HelpCommand
            extends GalaxyCommand
    {
        @Inject
        public Help help;

        @Override
        public void execute()
        {
            help.call();
        }
    }

    @Command(name = "show", description = "Show state of all slots")
    public static class ShowCommand
            extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();
    }

    @Command(name = "install", description = "Install software in a new slot")
    public static class InstallCommand
            extends GalaxyCommand
    {
        @Option(name = {"--count"}, description = "Number of instances to install")
        public int count = 1;

        @Inject
        public final AgentFilter agentFilter = new AgentFilter();

        @Arguments(usage = "<groupId:artifactId[:packaging[:classifier]]:version> @<component:pools:version>",
                description = "The binary and @configuration to install.  The default packaging is tar.gz")
        public final List<String> assignment = new ArrayList<>();
    }

    @Command(name = "upgrade", description = "Upgrade software in a slot")
    public static class UpgradeCommand
            extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Arguments(usage = "[<binary-version>] [@<config-version>]",
                description = "Version of the binary and/or @configuration")
        public final List<String> versions = new ArrayList<>();
    }

    @Command(name = "terminate", description = "Terminate (remove) a slot")
    public static class TerminateCommand
            extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();
    }

    @Command(name = "start", description = "Start a server")
    public static class StartCommand
            extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();
    }

    @Command(name = "stop", description = "Stop a server")
    public static class StopCommand
            extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();
    }

    @Command(name = "restart", description = "Restart server")
    public static class RestartCommand
            extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();
    }

    @Command(name = "reset-to-actual", description = "Reset slot expected state to actual")
    public static class ResetToActualCommand
            extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();
    }

    @Command(name = "ssh", description = "ssh to slot installation")
    public static class SshCommand
            extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Arguments(description = "Command to execute on the remote host")
        public String command;
    }

    @Command(name = "add", description = "Provision a new agent")
    public static class AgentAddCommand
            extends GalaxyCommand
    {
        @Option(name = {"--count"}, description = "Number of agents to provision")
        public int count = 1;

        @Option(name = {"--availability-zone"}, description = "Availability zone to provision")
        public String availabilityZone;

        @Arguments(usage = "[<instance-type>]", description = "Instance type to provision")
        public String instanceType;
    }

    @Command(name = "show", description = "Show agent details")
    public static class AgentShowCommand
            extends GalaxyCommand
    {
        @Inject
        public final AgentFilter agentFilter = new AgentFilter();
    }

    @Command(name = "terminate", description = "Provision a new agent")
    public static class AgentTerminateCommand
            extends GalaxyCommand
    {
        @Arguments(title = "agent-id", description = "Agent to terminate", required = true)
        public String agentId;
    }
}
