package io.airlift.airline.args;

import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.OptionType;

import java.util.List;

/**
 * @author sstrohschein
 *         <br>Date: 01.09.15
 *         <br>Time: 19:55
 */
@Command(name = "command", description = "command description")
public class GlobalArgs
{
    @Option(type = OptionType.GLOBAL, name = "-noValue", description = "0 values")
    public boolean noValue;

    @Option(type = OptionType.GLOBAL, name = "-oneValue", description = "1 value")
    public int oneValue;

    @Option(type = OptionType.GLOBAL, name = "-twoValues", description = "2 values", arity = 2)
    public List<Integer> twoValues;
}
