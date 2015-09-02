package io.airlift.airline.model;

import io.airlift.airline.Accessor;
import io.airlift.airline.OptionType;
import io.airlift.airline.util.ArgumentChecker;
import io.airlift.airline.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class OptionMetadata
{
    private final OptionType optionType;
    private final Set<String> options;
    private final String title;
    private final String description;
    private final int arity;
    private final boolean required;
    private final boolean hidden;
    private final Set<String> allowedValues;
    private final Set<Accessor> accessors;

    public OptionMetadata(OptionType optionType,
            Iterable<String> options,
            String title,
            String description,
            int arity,
            boolean required,
            boolean hidden,
            Iterable<String> allowedValues,
            Iterable<Field> path)
    {
        ArgumentChecker.checkNotNull(optionType, "optionType is null");
        ArgumentChecker.checkNotNull(options, "options is null");
        ArgumentChecker.checkCondition(options.iterator().hasNext(), "options is empty");
        ArgumentChecker.checkNotNull(title, "title is null");
        ArgumentChecker.checkNotNull(path, "path is null");
        ArgumentChecker.checkCondition(path.iterator().hasNext(), "path is empty");

        this.optionType = optionType;
        this.options = new HashSet<>(CollectionUtils.asList(options));
        this.title = title;
        this.description = description;
        this.arity = arity;
        this.required = required;
        this.hidden = hidden;

        if (allowedValues != null) {
            this.allowedValues = new HashSet<>(CollectionUtils.asList(allowedValues));
        }
        else {
            this.allowedValues = null;
        }

        this.accessors = CollectionUtils.asSet(new Accessor(path));
    }

    public OptionMetadata(Iterable<OptionMetadata> options)
    {
        ArgumentChecker.checkNotNull(options, "options is null");
        ArgumentChecker.checkCondition(options.iterator().hasNext(), "options is empty");

        OptionMetadata option = options.iterator().next();

        this.optionType = option.optionType;
        this.options = option.options;
        this.title = option.title;
        this.description = option.description;
        this.arity = option.arity;
        this.required = option.required;
        this.hidden = option.hidden;
        if (option.allowedValues != null) {
            this.allowedValues = new HashSet<>(CollectionUtils.asList(option.allowedValues));
        }
        else {
            this.allowedValues = null;
        }

        Set<Accessor> accessors = new HashSet<>();
        for (OptionMetadata other : options) {
            ArgumentChecker.checkCondition(option.equals(other),
                    "Conflicting options definitions: {1}, {2}", option, other);

            accessors.addAll(other.getAccessors());
        }
        this.accessors = new HashSet<>(accessors);
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
        if (allowedValues != null ? !allowedValues.equals(that.allowedValues) : that.allowedValues != null) {
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
        result = 31 * result + (allowedValues != null ? allowedValues.hashCode() : 0);
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
}
