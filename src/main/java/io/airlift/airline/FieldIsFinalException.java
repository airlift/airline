package io.airlift.airline;

/**
 * Created by zhxiaog on 15-3-22.
 */
public class FieldIsFinalException
        extends RuntimeException
{
    public FieldIsFinalException(String clsName, String fieldName, String metadataType)
    {
        super(String.format("Found %s on final field %s#%s.", metadataType, clsName, fieldName));
    }
}
