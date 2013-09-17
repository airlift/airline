package io.airlift.command;

public interface CommandFactory<T> {
  T createInstance(Class<?> type);
}
