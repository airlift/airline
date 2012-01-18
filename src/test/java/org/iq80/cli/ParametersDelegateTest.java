package org.iq80.cli;

import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author dain
 * @author rodionmoiseev
 */
public class ParametersDelegateTest
{

    @Command(name = "command")
    public static class DelegatingEmptyClassHasNoEffect
    {
        public static class EmptyDelegate
        {
            public String nonParamString = "a";
        }

        @Option(options = "-a")
        public boolean isA;
        @Option(options = {"-b", "--long-b"})
        public String bValue = "";
        @Options
        public EmptyDelegate delegate = new EmptyDelegate();
    }

    @Test
    public void delegatingEmptyClassHasNoEffect()
    {
        DelegatingEmptyClassHasNoEffect p = CommandParser.create(DelegatingEmptyClassHasNoEffect.class).parse("-a", "-b", "someValue");
        Assert.assertTrue(p.isA);
        Assert.assertEquals(p.bValue, "someValue");
        Assert.assertEquals(p.delegate.nonParamString, "a");
    }

    // ========================================================================================================================


    @Command(name = "command")
    public static class DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams
    {
        public static class ComplexDelegate
        {
            @Option(options = "-c")
            public boolean isC;
            @Option(options = {"-d", "--long-d"})
            public Integer d;
        }

        @Option(options = "-a")
        public boolean isA;
        @Option(options = {"-b", "--long-b"})
        public String bValue = "";
        @Options
        public ComplexDelegate delegate = new ComplexDelegate();
    }

    @Test
    public void delegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams()
    {

        DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams p = CommandParser.create(DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams.class)
                .parse("-c", "--long-d", "123", "--long-b", "bValue");
        Assert.assertFalse(p.isA);
        Assert.assertEquals(p.bValue, "bValue");
        Assert.assertTrue(p.delegate.isC);
        Assert.assertEquals(p.delegate.d, Integer.valueOf(123));
    }


    // ========================================================================================================================

    @Command(name = "command")
    public static class CombinedAndNestedDelegates
    {
        public static class LeafDelegate
        {
            @Option(options = "--list")
            public List<String> list = newArrayList("value1", "value2");

            @Option(options = "--bool")
            public boolean bool;
        }

        public static class NestedDelegate1
        {
            @Options
            public LeafDelegate leafDelegate = new LeafDelegate();

            @Option(options = {"-d", "--long-d"})
            public Integer d;
        }

        public static class NestedDelegate2
        {
            @Option(options = "-c")
            public boolean isC;

            @Options
            public NestedDelegate1 nestedDelegate1 = new NestedDelegate1();
        }

        @Option(options = "-a")
        public boolean isA;

        @Option(options = {"-b", "--long-b"})
        public String bValue = "";

        @Options
        public NestedDelegate2 nestedDelegate2 = new NestedDelegate2();
    }

    @Test
    public void combinedAndNestedDelegates()
    {
        CombinedAndNestedDelegates p = CommandParser.create(CombinedAndNestedDelegates.class)
                .parse("-d", "234", "--list", "a", "--list", "b", "-a");
        Assert.assertEquals(p.nestedDelegate2.nestedDelegate1.leafDelegate.list, newArrayList("value1", "value2", "a", "b"));
        Assert.assertFalse(p.nestedDelegate2.nestedDelegate1.leafDelegate.bool);
        Assert.assertEquals(p.nestedDelegate2.nestedDelegate1.d, Integer.valueOf(234));
        Assert.assertFalse(p.nestedDelegate2.isC);
        Assert.assertTrue(p.isA);
        Assert.assertEquals(p.bValue, "");
    }

    // ========================================================================================================================

    @Command(name = "command")
    public static class CommandTest
    {
        public static class Delegate
        {
            @Option(options = "-a")
            public String a = "b";
        }

        @Options
        public Delegate delegate = new Delegate();
    }

    @Test
    public void commandTest()
    {
        CommandTest c = CommandParser.create(CommandTest.class).parse("-a", "a");
        Assert.assertEquals(c.delegate.a, "a");
    }

    // ========================================================================================================================

    @Command(name = "command")
    public static class NullDelegatesAreProhibited
    {
        public static class ComplexDelegate
        {
            @Option(options = "-a")
            public boolean a;
        }

        @Options
        public ComplexDelegate delegate;
    }

    @Test
    public void nullDelegatesAreAllowed()
    {

        NullDelegatesAreProhibited value = CommandParser.create(NullDelegatesAreProhibited.class).parse("-a");
        Assert.assertEquals(value.delegate.a, true);
    }

    // ========================================================================================================================

    @Command(name = "command")
    public static class DuplicateDelegateAllowed
    {
        public static class Delegate
        {
            @Option(options = "-a")
            public String a;
        }

        @Options
        public Delegate d1 = new Delegate();
        @Options
        public Delegate d2 = new Delegate();
    }

    @Test
    public void duplicateDelegateAllowed()
    {
        DuplicateDelegateAllowed value = CommandParser.create(DuplicateDelegateAllowed.class).parse("-a", "value");
        Assert.assertEquals(value.d1.a, "value");
        Assert.assertEquals(value.d2.a, "value");
    }

    // ========================================================================================================================

    @Command(name = "command")
    public static class DuplicateMainParametersAreNotAllowed
    {
        public static class Delegate1
        {
            @Arguments
            public List<String> mainParams1 = newArrayList();
        }

        public static class Delegate2
        {
            @Arguments
            public List<String> mainParams2 = newArrayList();
        }

        @Options
        public Delegate1 delegate1 = new Delegate1();

        @Options
        public Delegate2 delegate2 = new Delegate2();
    }

    @Test(expectedExceptions = ParseException.class)
    public void duplicateMainParametersAreNotAllowed()
    {
        CommandParser.create(DuplicateMainParametersAreNotAllowed.class).parse("main", "params");
    }
}
