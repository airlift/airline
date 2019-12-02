package io.airlift.airline;

import com.google.common.base.Joiner;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestArgumentConversion
{
    @Test
    public void testConstuctorSuccessful()
    {
        ArgConverter("--constructor", "12345");
    }

    @Test
    public void testConstuctorFailure()
    {
        assertThatThrownBy(() -> ArgConverter("--constructor", "123456"))
                .isInstanceOf(ParseOptionConversionException.class)
                .satisfies(e -> assertThat(e.getCause())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("(1) Argument too long."));
    }

    @Test
    public void testValueOfSuccesful()
    {
        ArgConverter("--value-of", "12345");
    }

    @Test
    public void testValueOfFailure()
    {
        assertThatThrownBy(() -> ArgConverter("--value-of", "123456"))
                .isInstanceOf(ParseOptionConversionException.class)
                .satisfies(e -> assertThat(e.getCause())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("(2) Argument too long."));
    }

    @Test
    public void testFromStringSuccessful()
    {
        ArgConverter("--from-string", "12345");
    }

    @Test
    public void testFromStringFailure()
    {
        assertThatThrownBy(() -> ArgConverter("--from-string", "123456"))
                .isInstanceOf(ParseOptionConversionException.class)
                .satisfies(e -> assertThat(e.getCause())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("(3) Argument too long."));
    }

    private void ArgConverter(String... args)
    {
        System.out.println("$ argumentconversion " + Joiner.on(' ').join(args));
        ArgumentConversion.main(args);
        System.out.println();
    }
}
