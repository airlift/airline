package org.iq80.cli.model;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.iq80.cli.Accessor;
import org.iq80.cli.OptionType;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class OptionMetadata
{
    private final OptionType optionType;
    private final Set<String> options;
    private final String title;
    private final String description;
    private final int arity;
    private final boolean required;
    private final boolean hidden;
    private final Set<Accessor> accessors;
    private final Set<String> allowedValues;

    public OptionMetadata(OptionType optionType, Iterable<String> options, String title, String description, int arity, boolean required, boolean hidden, Iterable<Field> path, Iterable<String> allowedValues)
    {
        this.optionType = optionType;
        this.options = ImmutableSet.copyOf(options);
        this.title = title;
        this.description = description;
        this.arity = arity;
        this.required = required;
        this.hidden = hidden;
        this.accessors = ImmutableSet.of(new Accessor(path));

        if (allowedValues != null) {
            this.allowedValues = ImmutableSet.copyOf(allowedValues);
        }
        else {
            this.allowedValues = null;
        }
    }

    public OptionMetadata(Iterable<OptionMetadata> options)
    {
        Preconditions.checkNotNull(options, "options is null");
        Preconditions.checkArgument(!Iterables.isEmpty(options), "options is empty");

        OptionMetadata option = options.iterator().next();

        this.optionType = option.optionType;
        this.options = option.options;
        this.title = option.title;
        this.description = option.description;
        this.arity = option.arity;
        this.required = option.required;
        this.hidden = option.hidden;

        Set<Accessor> accessors = newHashSet();
        for (OptionMetadata other : options) {
            Preconditions.checkArgument(option.equals(other),
                    "Conflicting options definitions: %s, %s", option, other);

            accessors.addAll(other.getAccessors());
        }
        this.accessors = ImmutableSet.copyOf(accessors);
        
        if (option.allowedValues != null) {
            this.allowedValues = ImmutableSet.copyOf(option.allowedValues);
        }
        else {
            this.allowedValues = null;
        }
    }

    public OptionType getOptionType()
    {
        return optionType;
    }

    public Set<String> getOptions()
    {
        return options;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public int getArity()
    {
        return arity;
    }

    public boolean isRequired()
    {
        return required;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public boolean isMultiValued()
    {
        return accessors.iterator().next().isMultiValued();
    }

    public Class<?> getJavaType()
    {
        return accessors.iterator().next().getJavaType();
    }

    public Set<Accessor> getAccessors()
    {
        return accessors;
    }

    public Set<String> getAllowedValues()
    {
        return allowedValues;
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

        OptionMetadata that = (OptionMetadata) o;

        if (arity != that.arity) {
            return false;
        }
        if (hidden != that.hidden) {
            return false;
        }
        if (required != that.required) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (optionType != that.optionType) {
            return false;
        }
        if (!options.equals(that.options)) {
            return false;
        }
        if (!title.equals(that.title)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = optionType.hashCode();
        result = 31 * result + options.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + arity;
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (hidden ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("OptionMetadata");
        sb.append("{optionType=").append(optionType);
        sb.append(", options=").append(options);
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", arity=").append(arity);
        sb.append(", required=").append(required);
        sb.append(", hidden=").append(hidden);
        sb.append(", accessors=").append(accessors);
        sb.append('}');
        return sb.toString();
    }

    public static Function<OptionMetadata, Set<String>> optionsGetter()
    {
        return new Function<OptionMetadata, Set<String>>()
        {
            public Set<String> apply(OptionMetadata input)
            {
                return input.getOptions();
            }
        };
    }
}
