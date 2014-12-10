package io.airlift.airline.args;

import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "ArgsOutOfMemory")
public class ArgsOutOfMemory
{
    @Option(name = {"-p", "--pattern"},
            description = "pattern used by 'tail'. See http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout and http://logback.qos.ch/manual/layouts.html#AccessPatternLayout")
    public String pattern;

    @Option(name = "-q", description = "Filler arg")
    public String filler;
}
