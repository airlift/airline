package org.iq80.cli.args;

import org.iq80.cli.Command;
import org.iq80.cli.Option;

@Command(name="Arity1")
public class Arity1
{
    @Option(arity = 1, name = "-inspect", description = "", required = false)
    public boolean inspect;
}
