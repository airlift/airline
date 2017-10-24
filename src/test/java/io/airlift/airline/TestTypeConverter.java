package io.airlift.airline;

import org.testng.annotations.Test;

import java.util.Date;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestTypeConverter {
    private final String DEFAULT_NAME = "name";
    private final String INCOMPATIBLE_VALUE = "abc123!#%";

    private final String NOMINAL_FLOAT_VALUE = "3.141";
    private final String NOMINAL_INT_VALUE = "100";

    private TypeConverter tc = new TypeConverter();

    /***  NULL ARG TESTS  ***/

    @Test(expectedExceptions = NullPointerException.class)
    public void nullNameShouldThrowException() {
        tc.convert(null, Boolean.class, "true");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullTypeShouldThrowException() {
        valueTest(null, "true");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void nullValueShouldThrowException() {
        valueTest(Byte.class, null);
    }

    /***  STRING TESTS  ***/

    @Test
    public void stringNominal() {
        valueTest(String.class, "test");
    }

    @Test
    public void stringCorner() {
        valueTest(String.class,"");
    }

    /***  BOOLEAN TESTS  ***/

    @Test
    public void booleanNominalTrue() {
        valueTest(Boolean.class, "true");
    }

    @Test
    public void booleanNominalFalse() {
        valueTest(boolean.class, "false");
    }

    @Test
    public void booleanIncompatible() {
        Object obj = tc.convert(DEFAULT_NAME, Boolean.class, INCOMPATIBLE_VALUE);
        assertTrue(Boolean.class.isInstance(obj));
        assertEquals((boolean) obj, false);
    }

    /***  BYTE TESTS  ***/

    @Test
    public void byteNominalTest() {
        valueTest(Byte.class, NOMINAL_INT_VALUE);
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void byteBoundaryMinMinus1() {
        valueTest(byte.class, "-129");
    }

    @Test
    public void byteBoundaryMin() {
        valueTest(Byte.class, "-128");
    }

    @Test
    public void byteBoundaryMinPlus1() {
        valueTest(byte.class, "-127");
    }

    @Test
    public void byteBoundaryMaxMinus1() {
        valueTest(Byte.class, "126");
    }

    @Test
    public void byteBoundaryMax() {
        valueTest(byte.class, "127");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void byteBoundaryMaxPlus1() {
        valueTest(Byte.class, "128");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void byteIncompatible() {
        valueTest(byte.class, INCOMPATIBLE_VALUE);
    }

    /***  SHORT TESTS  ***/

    @Test
    public void shortNominalTest() {
        valueTest(Short.class, NOMINAL_INT_VALUE);
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void shortBoundaryMinMinus1() {
        valueTest(short.class, "-32769");
    }

    @Test
    public void shortBoundaryMin() {
        valueTest(Short.class, "-32768");
    }

    @Test
    public void shortBoundaryMinPlus1() {
        valueTest(short.class, "-32767");
    }

    @Test
    public void shortBoundaryMaxMinus1() {
        valueTest(Short.class, "32766");
    }

    @Test
    public void shortBoundaryMax() {
        valueTest(short.class, "32767");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void shortBoundaryMaxPlus1() {
        valueTest(Short.class, "32768");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void shortIncompatible() {
        valueTest(short.class, INCOMPATIBLE_VALUE);
    }

    /***  INTEGER TESTS  ***/

    @Test
    public void intNominalTest() {
        valueTest(Integer.class, NOMINAL_INT_VALUE);
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void intBoundaryMinMinus1() {
        valueTest(int.class, "-2147483649");
    }

    @Test
    public void intBoundaryMin() {
        valueTest(Integer.class, "-2147483648");
    }

    @Test
    public void intBoundaryMinPlus1() {
        valueTest(int.class, "-2147483647");
    }

    @Test
    public void intBoundaryMaxMinus1() {
        valueTest(Integer.class, "2147483646");
    }

    @Test
    public void intBoundaryMax() {
        valueTest(int.class, "2147483647");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void intBoundaryMaxPlus1() {
        valueTest(Integer.class, "2147483648");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void intIncompatible() {
        valueTest(int.class, INCOMPATIBLE_VALUE);
    }

    /***  LONG TESTS  ***/

    @Test
    public void longNominalTest() {
        valueTest(Long.class, NOMINAL_INT_VALUE);
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void longBoundaryMinMinus1() {
        valueTest(long.class, "-9223372036854775809");
    }

    @Test
    public void longBoundaryMin() {
        valueTest(Long.class, "-9223372036854775808");
    }

    @Test
    public void longBoundaryMinPlus1() {
        valueTest(long.class, "-9223372036854775807");
    }

    @Test
    public void longBoundaryMaxMinus1() {
        valueTest(Long.class, "9223372036854775806");
    }

    @Test
    public void longBoundaryMax() {
        valueTest(long.class, "9223372036854775807");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void longBoundaryMaxPlus1() {
        valueTest(Long.class, "9223372036854775808");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void longIncompatible() {
        valueTest(long.class, INCOMPATIBLE_VALUE);
    }

    /***  FLOAT TESTS  ***/

    @Test
    public void floatNominalTest() {
        valueTest(Float.class, NOMINAL_FLOAT_VALUE);
    }

    @Test
    public void floatBoundaryMinMinus1() {
        Object convertedType = tc.convert(DEFAULT_NAME, float.class, "-1.4E-46");
        assertTrue(convertedType instanceof Float);
        assertEquals(String.valueOf(convertedType), "-0.0");
    }

    @Test
    public void floatBoundaryMin() {
        valueTest(Float.class, "-1.4E-45");
    }

    @Test
    public void floatBoundaryMinPlus1() {
        valueTest(float.class, "-1.4E-44");
    }

    @Test
    public void floatBoundaryMaxMinus1() {
        valueTest(Float.class, "3.4E37");
    }

    @Test
    public void floatBoundaryMax() {
        valueTest(float.class, "3.4E38");
    }

    @Test
    public void floatBoundaryMaxPlus1() {
        Object convertedType = tc.convert(DEFAULT_NAME, Float.class, "3.4E39");
        assertTrue(convertedType instanceof Float);
        assertEquals(String.valueOf(convertedType), "Infinity");
    }

    @Test
    public void floatCorner() {
        Object convertedType = tc.convert(DEFAULT_NAME, float.class, NOMINAL_INT_VALUE);
        assertTrue(convertedType instanceof Float);
        assertEquals(String.valueOf(convertedType), NOMINAL_INT_VALUE+".0");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void floatIncompatible() {
        valueTest(Float.class, INCOMPATIBLE_VALUE);
    }

    /***  DOUBLE TESTS  ***/

    @Test
    public void doubleNominalTest() {
        valueTest(Double.class, NOMINAL_FLOAT_VALUE);
    }

    @Test
    public void doubleBoundaryMinMinus1() {
        Object convertedType = tc.convert(DEFAULT_NAME, double.class, "4.9E-325");
        assertTrue(convertedType instanceof Double);
        assertEquals(String.valueOf(convertedType), "0.0");
    }

    @Test
    public void doubleBoundaryMin() {
        valueTest(Double.class, "4.9E-324");
    }

    @Test
    public void doubleBoundaryMinPlus1() {
        valueTest(double.class, "4.9E-323");
    }

    @Test
    public void doubleBoundaryMaxMinus1() {
        valueTest(Double.class, "1.7976931348623158E307");
    }

    @Test
    public void doubleBoundaryMax() {
        valueTest(double.class, "1.7976931348623157E308");
    }

    @Test
    public void doubleBoundaryMaxPlus1() {
        Object convertedType = tc.convert(DEFAULT_NAME, Double.class, "1.7976931348623157E309");
        assertTrue(convertedType instanceof Double);
        assertEquals(String.valueOf(convertedType), "Infinity");
    }

    @Test
    public void doubleCorner() {
        Object convertedType = tc.convert(DEFAULT_NAME, double.class, NOMINAL_INT_VALUE);
        assertTrue(convertedType instanceof Double);
        assertEquals(String.valueOf(convertedType), NOMINAL_INT_VALUE+".0");
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void doubleIncompatible() {
        valueTest(double.class, INCOMPATIBLE_VALUE);
    }

    /***  FROM-STRING TESTS  ***/

    @Test
    public void fromStringNominalTest() {
        valueTest(UUID.class, UUID.randomUUID().toString());
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void fromStringIncompatible() {
        valueTest(UUID.class, INCOMPATIBLE_VALUE);
    }

    /***  VALUE-OF TESTS  ***/

    @Test
    public void valueOfNominalTest() {
        valueTest(Date.class, new Date().toString());
    }

    @Test(expectedExceptions = ParseOptionConversionException.class)
    public void valueOfIncompatible() {
        valueTest(Date.class, INCOMPATIBLE_VALUE);
    }

    private void valueTest(Class<?> type, String testValue) {
        Object convertedType = tc.convert(DEFAULT_NAME, type, testValue);
        String expectedClassType;
        switch (type.getSimpleName()) {
            case "int": expectedClassType = Integer.class.getSimpleName(); break;
            case "char": expectedClassType = Character.class.getSimpleName(); break;
            default: expectedClassType = capitalize(type.getSimpleName());
        }
        assertEquals(convertedType.getClass().getSimpleName(), expectedClassType);
        assertEquals(String.valueOf(convertedType), testValue);
    }
}
