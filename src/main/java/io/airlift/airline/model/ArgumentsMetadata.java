package io.airlift.airline.model;

import io.airlift.airline.Accessor;
import io.airlift.airline.util.ArgumentChecker;
import io.airlift.airline.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ArgumentsMetadata
{
    private final String title;
    private final String description;
    private final String usage;
    private final boolean required;
    private final Set<Accessor> accessors;

    public ArgumentsMetadata(String title, String description, String usage, boolean required, Iterable<Field> path)
    {
        ArgumentChecker.checkNotNull(title, "title is null");
        ArgumentChecker.checkNotNull(path, "path is null");
        ArgumentChecker.checkCondition(path.iterator().hasNext(), "path is empty");

        this.title = title;
        this.description = description;
        this.usage = usage;
        this.required = required;
        this.accessors = CollectionUtils.asSet(new Accessor(path));
    }

    public ArgumentsMetadata(Iterable<ArgumentsMetadata> arguments)
    {
        ArgumentChecker.checkNotNull(arguments, "arguments is null");
        ArgumentChecker.checkCondition(arguments.iterator().hasNext(), "arguments is empty");

        ArgumentsMetadata first = arguments.iterator().next();

        this.title = first.title;
        this.description = first.description;
        this.usage = first.usage;
        this.required = first.required;

        Set<Accessor> accessors = new HashSet<>();
        for (ArgumentsMetadata other : arguments) {
            ArgumentChecker.checkCondition(first.equals(other),
                    "Conflicting arguments definitions: {1}, {2}", first, other);

            accessors.addAll(other.getAccessors());
        }
        this.accessors = new HashSet<>(accessors);
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

    public Set<Accessor> getAccessors()
    {
        return accessors;
    }

    public boolean isMultiValued()
    {
        return accessors.iterator().next().isMultiValued();
    }

    public Class<?> getJavaType()
    {
        return accessors.iterator().next().getJavaType();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArgumentsMetadata that = (ArgumentsMetadata) o;

        if (required != that.required) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (!title.equals(that.title)) {
            return false;
        }
        if (usage != null ? !usage.equals(that.usage) : that.usage != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = title.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (usage != null ? usage.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        return result;
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
        sb.append(", accessors=").append(accessors);
        sb.append('}');
        return sb.toString();
    }
}
