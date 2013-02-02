package io.airlift.command.args;

import io.airlift.command.Command;
import io.airlift.command.Option;
import io.airlift.command.OptionType;

@Command(name="GlobalOptionsHidden")
public class GlobalOptionsHidden
{
    @Option(type = OptionType.GLOBAL, name = {"-hd", "--hidden"}, hidden = true)
    public boolean hiddenOption;

    @Option(type = OptionType.GLOBAL, name = {"-op" ,"--optional"}, hidden = false)
    public boolean optionalOption;
}
