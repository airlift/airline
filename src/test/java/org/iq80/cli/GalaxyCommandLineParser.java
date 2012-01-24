package org.iq80.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.iq80.cli.Cli.CliBuilder;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.iq80.cli.Cli.buildCli;
import static org.iq80.cli.OptionType.GLOBAL;

public class GalaxyCommandLineParser
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
        CliBuilder<GalaxyCommand> builder = buildCli("galaxy", GalaxyCommand.class)
                .withDescription("cloud management system")
                .withDefaultCommand(HelpCommand.class)
                .withCommands(HelpCommand.class,
                        ShowCommand.class,
                        InstallCommand.class,
                        UpgradeCommand.class,
                        TerminateCommand.class,
                        StartCommand.class,
                        StopCommand.class,
                        RestartCommand.class,
                        SshCommand.class,
                        ResetToActualCommand.class);

        builder.withGroup("agent")
                .withDescription("Manage agents")
                .withDefaultCommand(AgentShowCommand.class)
                .withCommands(AgentShowCommand.class,
                        AgentAddCommand.class,
                        AgentTerminateCommand.class);

        Cli<GalaxyCommand> galaxy = builder.build();
        return galaxy;
    }

    private void parse(String... args)
    {
        System.out.println("$ galaxy " + Joiner.on(" ").join(args));
        GalaxyCommand command = createParser().parse(args);
        command.execute();
        System.out.println();
    }

    public static class GlobalOptions
    {
        @Option(type = GLOBAL, name = "--debug", description = "Enable debug messages")
        public boolean debug = false;

        @Option(type = GLOBAL, name = "--coordinator", description = "Galaxy coordinator host (overrides GALAXY_COORDINATOR)")
        public String coordinator = Objects.firstNonNull(System.getenv("GALAXY_COORDINATOR"), "http://localhost:64000");

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("GlobalOptions");
            sb.append("{debug=").append(debug);
            sb.append(", coordinator='").append(coordinator).append('\'');
            sb.append('}');
            return sb.toString();
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

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Filter");
            sb.append("{binary=").append(binary);
            sb.append(", config=").append(config);
            sb.append(", host=").append(host);
            sb.append(", ip=").append(ip);
            sb.append(", uuid=").append(uuid);
            sb.append(", state=").append(state);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class AgentFilter
    {
        @Option(name = {"-i", "--host"}, description = "Select slots on the given host")
        public final List<String> host = newArrayList();

        @Option(name = {"-I", "--ip"}, description = "Select slots at the given IP address")
        public final List<String> ip = newArrayList();

        @Option(name = {"-u", "--uuid"}, description = "Select slot with the given UUID")
        public final List<String> uuid = newArrayList();

        @Option(name = {"-s", "--state"}, description = "Select 'r{unning}', 's{topped}' or 'unknown' slots")
        public final List<String> state = newArrayList();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Filter");
            sb.append("{host=").append(host);
            sb.append(", ip=").append(ip);
            sb.append(", uuid=").append(uuid);
            sb.append(", state=").append(state);
            sb.append('}');
            return sb.toString();
        }
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
    public static class HelpCommand extends GalaxyCommand
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
    public static class ShowCommand extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("ShowCommand");
            sb.append("{slotFilter=").append(slotFilter);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "install", description = "Install software in a new slot")
    public static class InstallCommand extends GalaxyCommand
    {
        @Option(name = {"--count"}, description = "Number of instances to install")
        public int count = 1;

        @Inject
        public final AgentFilter agentFilter = new AgentFilter();

        @Arguments(usage = "<groupId:artifactId[:packaging[:classifier]]:version> @<component:pools:version>",
                description = "The binary and @configuration to install.  The default packaging is tar.gz")
        public final List<String> assignment = Lists.newArrayList();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("InstallCommand");
            sb.append("{count=").append(count);
            sb.append(", agentFilter=").append(agentFilter);
            sb.append(", assignment=").append(assignment);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "upgrade", description = "Upgrade software in a slot")
    public static class UpgradeCommand extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Arguments(usage = "[<binary-version>] [@<config-version>]",
                description = "Version of the binary and/or @configuration")
        public final List<String> versions = Lists.newArrayList();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("UpgradeCommand");
            sb.append("{slotFilter=").append(slotFilter);
            sb.append(", versions=").append(versions);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "terminate", description = "Terminate (remove) a slot")
    public static class TerminateCommand extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("TerminateCommand");
            sb.append("{slotFilter=").append(slotFilter);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "start", description = "Start a server")
    public static class StartCommand extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("StartCommand");
            sb.append("{slotFilter=").append(slotFilter);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "stop", description = "Stop a server")
    public static class StopCommand extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("StopCommand");
            sb.append("{slotFilter=").append(slotFilter);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "restart", description = "Restart server")
    public static class RestartCommand extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("RestartCommand");
            sb.append("{slotFilter=").append(slotFilter);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "reset-to-actual", description = "Reset slot expected state to actual")
    public static class ResetToActualCommand extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("ResetToActualCommand");
            sb.append("{slotFilter=").append(slotFilter);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "ssh", description = "ssh to slot installation")
    public static class SshCommand extends GalaxyCommand
    {
        @Inject
        public final SlotFilter slotFilter = new SlotFilter();

        @Arguments(description = "Command to execute on the remote host")
        public String command;

        @Override
        public String toString()
        {
            return "SshCommand{" +
                    "slotFilter=" + slotFilter +
                    ", command='" + command + '\'' +
                    '}';
        }
    }

    @Command(name = "add", description = "Provision a new agent")
    public static class AgentAddCommand extends GalaxyCommand
    {
        @Option(name = {"--count"}, description = "Number of agents to provision")
        public int count = 1;

        @Option(name = {"--availability-zone"}, description = "Availability zone to provision")
        public String availabilityZone;

        @Arguments(usage = "[<instance-type>]", description = "Instance type to provision")
        public String instanceType;

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("AgentAddCommand");
            sb.append("{count=").append(count);
            sb.append(", availabilityZone='").append(availabilityZone).append('\'');
            sb.append(", instanceType=").append(instanceType);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "show", description = "Show agent details")
    public static class AgentShowCommand extends GalaxyCommand
    {
        @Inject
        public final AgentFilter agentFilter = new AgentFilter();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("AgentShowCommand");
            sb.append("{globalOptions=").append(globalOptions);
            sb.append(", agentFilter=").append(agentFilter);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "terminate", description = "Provision a new agent")
    public static class AgentTerminateCommand extends GalaxyCommand
    {
        @Arguments(title = "agent-id", description = "Agent to terminate", required = true)
        public String agentId;

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("AgentTerminateCommand");
            sb.append("{agentId='").append(agentId).append('\'');
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }
}
