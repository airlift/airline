package org.iq80.cli;

@Command(name="ArgsRequiredWrongMain")
public class ArgsRequiredWrongMain
{
    @Arguments(required = true)
    public String[] file;
}
