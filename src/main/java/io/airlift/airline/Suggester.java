package io.airlift.airline;

public interface Suggester
{
    Iterable<String> suggest();
}
