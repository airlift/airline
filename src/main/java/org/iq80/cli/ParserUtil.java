package org.iq80.cli;

public class ParserUtil
{
    public static <T> T createInstance(Class<T> type)
    {
        if (type != null) {
            try {
                return type.getConstructor().newInstance();
            }
            catch (Exception e) {
                throw new ParseException("Unable to create instance %s", type.getName());
            }
        }
        return null;
    }
}
