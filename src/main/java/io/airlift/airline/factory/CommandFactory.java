package io.airlift.airline.factory;

public interface CommandFactory {
	<T> T createInstance(Class<T> instanceClass);
}
