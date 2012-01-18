package org.iq80.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.iq80.cli.GitLikeCommandParser.Builder;
import org.iq80.cli.model.CommandGroupMetadata;
import org.iq80.cli.model.CommandMetadata;
import org.testng.annotations.Test;

import java.util.List;

import static org.iq80.cli.OptionType.GLOBAL;

public class GalaxyCommandLineParser
{
    @Test
    public void test()
    {
        GitLikeCommandParser<?> parser = createParser();
        new GlobalUsage(119).usage("galaxy", parser.getMetadata());
        CommandUsage commandUsage = new CommandUsage(119);
        for (CommandMetadata command : parser.getMetadata().getDefaultGroupCommands()) {
            commandUsage.usage("galaxy", null, command);
        }
        for (CommandGroupMetadata commandGroup : parser.getMetadata().getCommandGroups()) {
            for (CommandMetadata command : commandGroup.getCommands()) {
                commandUsage.usage("galaxy", commandGroup.getName(), command);
            }
        }

        parse("--debug");
        parse("--debug", "show", "-u", "b2", "--state", "r");
        parse("--debug", "install", "com.proofpoint.discovery:discovery-server:1.1", "@discovery:general:1.0");
        parse("--debug", "upgrade", "-u", "b2", "1.1", "@1.0");
        parse("--debug", "upgrade", "-u", "b2", "1.1", "@1.0", "-s", "r");
        parse("--debug", "terminate", "-u", "b2");
        parse("--debug", "start", "-u", "b2");
        parse("--debug", "stop", "-u", "b2");
        parse("--debug", "restart", "-u", "b2");
        parse("--debug", "reset-to-actual", "-u", "b2");
        parse("--debug", "ssh", "-u", "b2", "--state", "r", "-x", "tail -F var/log/launcher.log");
        parse("--debug", "ssh", "-u", "b2", "--state", "r", "--", "tail", "-F", "var/log/launcher.log");
        parse("--debug", "agent");
        parse("--debug", "agent", "show");
        parse("--debug", "agent", "add", "--count", "4", "t1.micro");
    }

    private GitLikeCommandParser<?> createParser()
    {
        Builder<Object> builder = GitLikeCommandParser.parser("galaxy")
                .defaultCommand(ShowCommand.class)
                .addCommand(ShowCommand.class)
                .addCommand(InstallCommand.class)
                .addCommand(UpgradeCommand.class)
                .addCommand(TerminateCommand.class)
                .addCommand(StartCommand.class)
                .addCommand(StopCommand.class)
                .addCommand(RestartCommand.class)
                .addCommand(SshCommand.class)
                .addCommand(ResetToActualCommand.class);

        builder.addGroup("agent")
                .withDescription("Manage agents")
                .defaultCommand(AgentShowCommand.class)
                .addCommand(AgentShowCommand.class)
                .addCommand(AgentAddCommand.class);

        GitLikeCommandParser<?> galaxy = builder.build();
        return galaxy;
    }

    private void parse(String... args)
    {
        System.out.println(Joiner.on(" ").join(args));
        GitLikeCommandParser<?> parser = createParser();
        Object results = parser.parse(args);
        System.out.println(results);
        System.out.println();
    }

    public static class GlobalOptions
    {
        @Option(type = GLOBAL, options = "--debug", description = "Enable debug messages")
        public boolean debug = false;

        @Option(type = GLOBAL, options = "--coordinator", description = "Galaxy coordinator host (overrides GALAXY_COORDINATOR)")
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
        @Option(options = {"-b", "--binary"}, description = "Select slots with a given binary")
        public List<String> binary;

        @Option(options = {"-c", "--config"}, description = "Select slots with a given configuration")
        public List<String> config;

        @Option(options = {"-i", "--host"}, description = "Select slots on the given host")
        public List<String> host;

        @Option(options = {"-I", "--ip"}, description = "Select slots at the given IP address")
        public List<String> ip;

        @Option(options = {"-u", "--uuid"}, description = "Select slot with the given UUID")
        public List<String> uuid;

        @Option(options = {"-s", "--state"}, description = "Select 'r{unning}', 's{topped}' or 'unknown' slots")
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

    @Command(name = "show", description = "Show state of all slots")
    public static class ShowCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
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
    public static class InstallCommand
    {
        @Option(options = {"--count"}, description = "Number of instances to install")
        public int count = 1;

        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
        public final SlotFilter slotFilter = new SlotFilter();

        @Arguments(usage = "<groupId:artifactId[:packaging[:classifier]]:version> @<component:pools:version>",
                description = "The binary and @configuration to install.  The default packaging is tar.gz")
        public List<String> assignment = Lists.newArrayList();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("InstallCommand");
            sb.append("{count=").append(count);
            sb.append(", slotFilter=").append(slotFilter);
            sb.append(", assignment=").append(assignment);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "upgrade", description = "Upgrade software in a slot")
    public static class UpgradeCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
        public final SlotFilter slotFilter = new SlotFilter();

        @Arguments(usage = "[<binary-version>] [@<config-version>]",
                description = "Version of the binary and/or @configuration")
        public List<String> versions = Lists.newArrayList();

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
    public static class TerminateCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
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
    public static class StartCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
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
    public static class StopCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
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
    public static class RestartCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
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
    public static class ResetToActualCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
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
    public static class SshCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Options
        public final SlotFilter slotFilter = new SlotFilter();

        @Option(options = {"-x", "--ssh-command"}, description = "Command to execute")
        public String sshCommand;

        @Arguments(title = "ssh-arg", description = "Ssh command line arguments")
        public List<String> args = Lists.newArrayList();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("InstallCommand");
            sb.append("{slotFilter=").append(slotFilter);
            sb.append(", sshCommand='").append(sshCommand).append("'");
            sb.append(", args=").append(args);
            sb.append(", globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }

    @Command(name = "add", description = "Provision a new agent")
    public static class AgentAddCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Option(options = {"--count"}, description = "Number of agents to provision")
        public int count = 1;

        @Option(options = {"--availability-zone"}, description = "Availability zone to provision")
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
    public static class AgentShowCommand
    {
        @Options
        public GlobalOptions globalOptions = new GlobalOptions();

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("AgentShowCommand");
            sb.append("{globalOptions=").append(globalOptions);
            sb.append('}');
            return sb.toString();
        }
    }


}
