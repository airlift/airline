/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package io.airlift.command.args;

import io.airlift.command.Command;
import io.airlift.command.Option;

@Command(name="ArgsAllowedValues", description="ArgsAllowedValues description")
public class ArgsAllowedValues {

    @Option(name = "-mode", arity = 1, description = "A string from a restricted set of values", allowedValues = { "a", "b", "c" })
    public String mode;
}
