package io.airlift.airline;

public class DefaultCommandFactory<T>
        implements CommandFactory<T>
{
    @SuppressWarnings("unchecked")
    @Override
    public T createInstance(Class<?> type)
    {
        return (T) ParserUtil.createInstance(type);
    }
}
