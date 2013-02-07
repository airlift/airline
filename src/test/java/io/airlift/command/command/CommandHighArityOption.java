package io.airlift.command.command;

import java.util.List;

import javax.inject.Inject;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;

@Command(name = "cmd", description = "A command with an option that has a high arity option")
public class CommandHighArityOption {
	@Inject
	public CommandMain commandMain;
	
	@Option(name = "--option", description = "An option with high arity", arity = 5)
	public List<String> option;
	
	@Arguments(description = "The rest of arguments")
	public List<String> args;
}
