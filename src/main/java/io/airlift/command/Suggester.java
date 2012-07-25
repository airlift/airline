package io.airlift.command;

public interface Suggester
{
    Iterable<String> suggest();
}
