package io.airlift.airline.args;

import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "Arity1")
public class Arity1
{
    @Option(arity = 1, name = "-inspect", description = "", required = false)
    public boolean inspect;
}
