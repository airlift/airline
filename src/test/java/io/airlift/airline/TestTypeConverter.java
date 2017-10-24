package io.airlift.airline;

import com.google.common.primitives.Primitives;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestTypeConverter
{
    private TypeConverter tc = new TypeConverter();

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "name is null")
    public void nullNameShouldThrowException()
    {
        tc.convert(null, Boolean.class, "true");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "type is null")
    public void nullTypeShouldThrowException()
    {
        valueTest(null, "true");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "value is null")
    public void nullValueShouldThrowException()
    {
        valueTest(Byte.class, null);
    }

    @Test
    public void stringNominal()
    {
        valueTest(String.class, "test");
    }

    @Test
    public void stringCorner()
    {
        valueTest(String.class, "");
    }

    @Test
    public void booleanNominalTrue()
    {
        valueTest(Boolean.class, "true");
    }

    @Test
    public void booleanNominalFalse()
    {
        valueTest(boolean.class, "false");
    }

    @Test
    public void booleanIncompatible()
    {
        Object obj = tc.convert("name", Boolean.class, "abc123!#%");
        assertTrue(obj instanceof Boolean);
        assertEquals((boolean) obj, false);
    }

    @Test
    public void byteNominalTest()
    {
        valueTest(Byte.class, "100");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"-129\" to a byte")
    public void byteBoundaryMinMinus1()
    {
        valueTest(byte.class, "-129");
    }

    @Test
    public void byteBoundaryMin()
    {
        valueTest(Byte.class, "-128");
    }

    @Test
    public void byteBoundaryMinPlus1()
    {
        valueTest(byte.class, "-127");
    }

    @Test
    public void byteBoundaryMaxMinus1()
    {
        valueTest(Byte.class, "126");
    }

    @Test
    public void byteBoundaryMax()
    {
        valueTest(byte.class, "127");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"128\" to a Byte")
    public void byteBoundaryMaxPlus1()
    {
        valueTest(Byte.class, "128");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"abc123!#%\" to a byte")
    public void byteIncompatible()
    {
        valueTest(byte.class, "abc123!#%");
    }

    @Test
    public void shortNominalTest()
    {
        valueTest(Short.class, "100");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"-32769\" to a short")
    public void shortBoundaryMinMinus1()
    {
        valueTest(short.class, "-32769");
    }

    @Test
    public void shortBoundaryMin()
    {
        valueTest(Short.class, "-32768");
    }

    @Test
    public void shortBoundaryMinPlus1()
    {
        valueTest(short.class, "-32767");
    }

    @Test
    public void shortBoundaryMaxMinus1()
    {
        valueTest(Short.class, "32766");
    }

    @Test
    public void shortBoundaryMax()
    {
        valueTest(short.class, "32767");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"32768\" to a Short")
    public void shortBoundaryMaxPlus1()
    {
        valueTest(Short.class, "32768");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"abc123!#%\" to a short")
    public void shortIncompatible()
    {
        valueTest(short.class, "abc123!#%");
    }

    @Test
    public void intNominalTest()
    {
        valueTest(Integer.class, "100");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"-2147483649\" to a int")
    public void intBoundaryMinMinus1()
    {
        valueTest(int.class, "-2147483649");
    }

    @Test
    public void intBoundaryMin()
    {
        valueTest(Integer.class, "-2147483648");
    }

    @Test
    public void intBoundaryMinPlus1()
    {
        valueTest(int.class, "-2147483647");
    }

    @Test
    public void intBoundaryMaxMinus1()
    {
        valueTest(Integer.class, "2147483646");
    }

    @Test
    public void intBoundaryMax()
    {
        valueTest(int.class, "2147483647");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"2147483648\" to a Integer")
    public void intBoundaryMaxPlus1()
    {
        valueTest(Integer.class, "2147483648");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"abc123!#%\" to a int")
    public void intIncompatible()
    {
        valueTest(int.class, "abc123!#%");
    }

    @Test
    public void longNominalTest()
    {
        valueTest(Long.class, "100");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"-9223372036854775809\" to a long")
    public void longBoundaryMinMinus1()
    {
        valueTest(long.class, "-9223372036854775809");
    }

    @Test
    public void longBoundaryMin()
    {
        valueTest(Long.class, "-9223372036854775808");
    }

    @Test
    public void longBoundaryMinPlus1()
    {
        valueTest(long.class, "-9223372036854775807");
    }

    @Test
    public void longBoundaryMaxMinus1()
    {
        valueTest(Long.class, "9223372036854775806");
    }

    @Test
    public void longBoundaryMax()
    {
        valueTest(long.class, "9223372036854775807");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"9223372036854775808\" to a Long")
    public void longBoundaryMaxPlus1()
    {
        valueTest(Long.class, "9223372036854775808");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"abc123!#%\" to a long")
    public void longIncompatible()
    {
        valueTest(long.class, "abc123!#%");
    }

    @Test
    public void floatNominalTest()
    {
        valueTest(Float.class, "3.141");
    }

    @Test
    public void floatBoundaryMinMinus1()
    {
        Object convertedType = tc.convert("name", float.class, "-1.4E-46");
        assertTrue(convertedType instanceof Float);
        assertEquals(String.valueOf(convertedType), "-0.0");
    }

    @Test
    public void floatBoundaryMin()
    {
        valueTest(Float.class, "-1.4E-45");
    }

    @Test
    public void floatBoundaryMinPlus1()
    {
        valueTest(float.class, "-1.4E-44");
    }

    @Test
    public void floatBoundaryMaxMinus1()
    {
        valueTest(Float.class, "3.4E37");
    }

    @Test
    public void floatBoundaryMax()
    {
        valueTest(float.class, "3.4E38");
    }

    @Test
    public void floatBoundaryMaxPlus1()
    {
        Object convertedType = tc.convert("name", Float.class, "3.4E39");
        assertTrue(convertedType instanceof Float);
        assertEquals(String.valueOf(convertedType), "Infinity");
    }

    @Test
    public void floatCorner()
    {
        Object convertedType = tc.convert("name", float.class, "100");
        assertTrue(convertedType instanceof Float);
        assertEquals(String.valueOf(convertedType), "100" + ".0");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"abc123!#%\" to a Float")
    public void floatIncompatible()
    {
        valueTest(Float.class, "abc123!#%");
    }

    @Test
    public void doubleNominalTest()
    {
        valueTest(Double.class, "3.141");
    }

    @Test
    public void doubleBoundaryMinMinus1()
    {
        Object convertedType = tc.convert("name", double.class, "4.9E-325");
        assertTrue(convertedType instanceof Double);
        assertEquals(String.valueOf(convertedType), "0.0");
    }

    @Test
    public void doubleBoundaryMin()
    {
        valueTest(Double.class, "4.9E-324");
    }

    @Test
    public void doubleBoundaryMinPlus1()
    {
        valueTest(double.class, "4.9E-323");
    }

    @Test
    public void doubleBoundaryMaxMinus1()
    {
        valueTest(Double.class, "1.7976931348623158E307");
    }

    @Test
    public void doubleBoundaryMax()
    {
        valueTest(double.class, "1.7976931348623157E308");
    }

    @Test
    public void doubleBoundaryMaxPlus1()
    {
        Object convertedType = tc.convert("name", Double.class, "1.7976931348623157E309");
        assertTrue(convertedType instanceof Double);
        assertEquals(String.valueOf(convertedType), "Infinity");
    }

    @Test
    public void doubleCorner()
    {
        Object convertedType = tc.convert("name", double.class, "100");
        assertTrue(convertedType instanceof Double);
        assertEquals(String.valueOf(convertedType), "100" + ".0");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"abc123!#%\" to a double")
    public void doubleIncompatible()
    {
        valueTest(double.class, "abc123!#%");
    }

    @Test
    public void fromStringNominalTest()
    {
        valueTest(UUID.class, UUID.randomUUID().toString());
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"abc123!#%\" to a UUID")
    public void fromStringIncompatible()
    {
        valueTest(UUID.class, "abc123!#%");
    }

    @Test
    public void valueOfNominalTest()
    {
        valueTest(Date.class, new Date().toString());
    }

    @Test(expectedExceptions = ParseOptionConversionException.class, expectedExceptionsMessageRegExp = "name: can not convert \"abc123!#%\" to a Date")
    public void valueOfIncompatible()
    {
        valueTest(Date.class, "abc123!#%");
    }

    private void valueTest(Class<?> type, String testValue)
    {
        Object convertedType = tc.convert("name", type, testValue);
        Class<?> expectedClass = Primitives.wrap(type);
        assertEquals(convertedType.getClass(), expectedClass);
        assertEquals(String.valueOf(convertedType), testValue);
    }
}
