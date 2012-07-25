package org.iq80.cli;

import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.iq80.cli.TestUtil.singleCommandParser;

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

        @Option(name = "-a")
        public boolean isA;
        @Option(name = {"-b", "--long-b"})
        public String bValue = "";
        @Inject
        public EmptyDelegate delegate = new EmptyDelegate();
    }

    @Test
    public void delegatingEmptyClassHasNoEffect()
    {
        DelegatingEmptyClassHasNoEffect p = Cli.buildCli("foo", DelegatingEmptyClassHasNoEffect.class)
                .withCommand(DelegatingEmptyClassHasNoEffect.class)
                .build()
                .parse("command", "-a", "-b", "someValue");

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
            @Option(name = "-c")
            public boolean isC;
            @Option(name = {"-d", "--long-d"})
            public Integer d;
        }

        @Option(name = "-a")
        public boolean isA;
        @Option(name = {"-b", "--long-b"})
        public String bValue = "";
        @Inject
        public ComplexDelegate delegate = new ComplexDelegate();
    }

    @Test
    public void delegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams()
    {

        DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams p = singleCommandParser(DelegatingSetsFieldsOnBothMainParamsAndTheDelegatedParams.class)
                .parse("command", "-c", "--long-d", "123", "--long-b", "bValue");
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
            @Option(name = "--list")
            public List<String> list = newArrayList("value1", "value2");

            @Option(name = "--bool")
            public boolean bool;
        }

        public static class NestedDelegate1
        {
            @Inject
            public LeafDelegate leafDelegate = new LeafDelegate();

            @Option(name = {"-d", "--long-d"})
            public Integer d;
        }

        public static class NestedDelegate2
        {
            @Option(name = "-c")
            public boolean isC;

            @Inject
            public NestedDelegate1 nestedDelegate1 = new NestedDelegate1();
        }

        @Option(name = "-a")
        public boolean isA;

        @Option(name = {"-b", "--long-b"})
        public String bValue = "";

        @Inject
        public NestedDelegate2 nestedDelegate2 = new NestedDelegate2();
    }

    @Test
    public void combinedAndNestedDelegates()
    {
        CombinedAndNestedDelegates p = singleCommandParser(CombinedAndNestedDelegates.class)
                .parse("command", "-d", "234", "--list", "a", "--list", "b", "-a");
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
            @Option(name = "-a")
            public String a = "b";
        }

        @Inject
        public Delegate delegate = new Delegate();
    }

    @Test
    public void commandTest()
    {
        CommandTest c = singleCommandParser(CommandTest.class).parse("command", "-a", "a");
        Assert.assertEquals(c.delegate.a, "a");
    }

    // ========================================================================================================================

    @Command(name = "command")
    public static class NullDelegatesAreProhibited
    {
        public static class ComplexDelegate
        {
            @Option(name = "-a")
            public boolean a;
        }

        @Inject
        public ComplexDelegate delegate;
    }

    @Test
    public void nullDelegatesAreAllowed()
    {

        NullDelegatesAreProhibited value = singleCommandParser(NullDelegatesAreProhibited.class).parse("command", "-a");
        Assert.assertEquals(value.delegate.a, true);
    }

    // ========================================================================================================================

    @Command(name = "command")
    public static class DuplicateDelegateAllowed
    {
        public static class Delegate
        {
            @Option(name = "-a")
            public String a;
        }

        @Inject
        public Delegate d1 = new Delegate();
        @Inject
        public Delegate d2 = new Delegate();
    }

    @Test
    public void duplicateDelegateAllowed()
    {
        DuplicateDelegateAllowed value = singleCommandParser(DuplicateDelegateAllowed.class).parse("command", "-a", "value");
        Assert.assertEquals(value.d1.a, "value");
        Assert.assertEquals(value.d2.a, "value");
    }

    // ========================================================================================================================

    @Command(name = "command")
    public static class DuplicateMainParametersAreAllowed
    {
        public static class Delegate1
        {
            @Arguments
            public List<String> mainParams1 = newArrayList();
        }

        public static class Delegate2
        {
            @Arguments
            public List<String> mainParams1 = newArrayList();
        }

        @Inject
        public Delegate1 delegate1 = new Delegate1();

        @Inject
        public Delegate2 delegate2 = new Delegate2();
    }

    @Test
    public void duplicateMainParametersAreAllowed()
    {
        DuplicateMainParametersAreAllowed value = singleCommandParser(DuplicateMainParametersAreAllowed.class).parse("command", "main", "params");
        Assert.assertEquals(value.delegate1.mainParams1, ImmutableList.of("main", "params"));
        Assert.assertEquals(value.delegate2.mainParams1, ImmutableList.of("main", "params"));
    }

    // ========================================================================================================================

    @Command(name = "command")
    public static class ConflictingMainParametersAreNotAllowed
    {
        public static class Delegate1
        {
            @Arguments(description = "foo")
            public List<String> mainParams1 = newArrayList();
        }

        public static class Delegate2
        {
            @Arguments(description = "bar")
            public List<String> mainParams1 = newArrayList();
        }

        @Inject
        public Delegate1 delegate1 = new Delegate1();

        @Inject
        public Delegate2 delegate2 = new Delegate2();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void conflictingMainParametersAreNotAllowed()
    {
        singleCommandParser(ConflictingMainParametersAreNotAllowed.class).parse("command", "main", "params");

    }
}
