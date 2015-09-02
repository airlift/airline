package io.airlift.airline;

public interface CommandFactory<T> {
  T createInstance(Class<?> type);
}
