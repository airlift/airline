package io.airlift.airline.model;

import io.airlift.airline.Accessor;
import io.airlift.airline.Suggester;

import java.util.ArrayList;
import java.util.List;

public class SuggesterMetadata
{
    private final Class<? extends Suggester> suggesterClass;
    private final List<Accessor> metadataInjections;

    public SuggesterMetadata(Class<? extends Suggester> suggesterClass, List<Accessor> metadataInjections)
    {
        this.suggesterClass = suggesterClass;
        this.metadataInjections = new ArrayList<>(metadataInjections);
    }

    public Class<? extends Suggester> getSuggesterClass()
    {
        return suggesterClass;
    }

    public List<Accessor> getMetadataInjections()
    {
        return metadataInjections;
    }
}
