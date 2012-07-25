package io.airlift.command.args;

import io.airlift.command.Command;
import io.airlift.command.Option;

@Command(name="Arity1")
public class Arity1
{
    @Option(arity = 1, name = "-inspect", description = "", required = false)
    public boolean inspect;
}
