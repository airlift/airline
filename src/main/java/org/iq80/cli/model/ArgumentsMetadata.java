package org.iq80.cli.model;

import org.iq80.cli.Accessor;

import java.lang.reflect.Field;

public class ArgumentsMetadata
{
    private final String title;
    private final String description;
    private final String usage ;
    private final boolean required;
    private final Accessor accessor;

    public ArgumentsMetadata(String title, String description, String usage, boolean required, Field field)
    {
        this.title = title;
        this.description = description;
        this.usage = usage;
        this.required = required;
        this.accessor = new Accessor(field);
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public String getUsage()
    {
        return usage;
    }

    public boolean isRequired()
    {
        return required;
    }

    public Accessor getAccessor()
    {
        return accessor;
    }

    public boolean isMultiValued()
    {
        return accessor.isMultiValued();
    }

    public Class<?> getJavaType()
    {
        return accessor.getJavaType();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("ArgumentsMetadata");
        sb.append("{title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", usage='").append(usage).append('\'');
        sb.append(", required=").append(required);
        sb.append(", accessor=").append(accessor);
        sb.append('}');
        return sb.toString();
    }
}
