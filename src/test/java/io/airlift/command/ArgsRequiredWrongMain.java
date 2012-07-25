package io.airlift.command;

@Command(name="ArgsRequiredWrongMain")
public class ArgsRequiredWrongMain
{
    @Arguments(required = true)
    public String[] file;
}
