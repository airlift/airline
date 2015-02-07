package io.airlift.airline.factory;

import io.airlift.airline.ParseException;

public class DefaultConstructorFactory implements CommandFactory {
	@Override
	public <T> T createInstance(Class<T> type) {
		if (type != null) {
			try {
				return type.getConstructor().newInstance();
			}
			catch (Exception e) {
				throw new ParseException(e, "Unable to create instance %s", type.getName());
			}
		}
		return null;
	}
}
