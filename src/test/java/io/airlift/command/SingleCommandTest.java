/**
 * Copyright (C) 2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.airlift.command;

import com.google.common.collect.ImmutableList;
import io.airlift.command.Cli.CliBuilder;
import io.airlift.command.args.Args1;
import io.airlift.command.args.Args2;
import io.airlift.command.args.ArgsAllowedValues;
import io.airlift.command.args.ArgsArityString;
import io.airlift.command.args.ArgsBooleanArity;
import io.airlift.command.args.ArgsBooleanArity0;
import io.airlift.command.args.ArgsEnum;
import io.airlift.command.args.ArgsInherited;
import io.airlift.command.args.ArgsMultipleUnparsed;
import io.airlift.command.args.ArgsOutOfMemory;
import io.airlift.command.args.ArgsPrivate;
import io.airlift.command.args.ArgsRequired;
import io.airlift.command.args.ArgsSingleChar;
import io.airlift.command.args.Arity1;
import io.airlift.command.args.OptionsRequired;
import io.airlift.command.command.CommandAdd;
import io.airlift.command.command.CommandCommit;
import io.airlift.command.model.CommandMetadata;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;
import static io.airlift.command.SingleCommand.singleCommand;
import static org.testng.Assert.assertTrue;

@Test
public class SingleCommandTest
{
    public void simpleArgs()
            throws ParseException
    {
        Args1 args = singleCommand(Args1.class).parse(
                "-debug", "-log", "2", "-float", "1.2", "-double", "1.3", "-bigdecimal", "1.4",
                "-groups", "unit", "a", "b", "c");

        assertTrue(args.debug);
        Assert.assertEquals(args.verbose.intValue(), 2);
        Assert.assertEquals(args.groups, "unit");
        Assert.assertEquals(args.parameters, Arrays.asList("a", "b", "c"));
        Assert.assertEquals(args.floa, 1.2f, 0.1f);
        Assert.assertEquals(args.doub, 1.3f, 0.1f);
        Assert.assertEquals(args.bigd, new BigDecimal("1.4"));
    }

    public void equalsArgs()
            throws ParseException
    {
        Args1 args = singleCommand(Args1.class).parse(
                "-debug", "-log=2", "-float=1.2", "-double=1.3", "-bigdecimal=1.4",
                "-groups=unit", "a", "b", "c");

        assertTrue(args.debug);
        Assert.assertEquals(args.verbose.intValue(), 2);
        Assert.assertEquals(args.groups, "unit");
        Assert.assertEquals(args.parameters, Arrays.asList("a", "b", "c"));
        Assert.assertEquals(args.floa, 1.2f, 0.1f);
        Assert.assertEquals(args.doub, 1.3f, 0.1f);
        Assert.assertEquals(args.bigd, new BigDecimal("1.4"));
    }

    public void classicGetoptArgs()
            throws ParseException
    {
        ArgsSingleChar args = singleCommand(ArgsSingleChar.class).parse(
                "-lg", "-dsn", "-pa-p", "-2f", "-z", "--Dfoo");

        assertTrue(args.l);
        assertTrue(args.g);
        assertTrue(args.d);
        Assert.assertEquals(args.s, "n");
        Assert.assertEquals(args.p, "a-p");
        Assert.assertFalse(args.n);
        assertTrue(args.two);
        Assert.assertEquals(args.f, "-z");
        Assert.assertFalse(args.z);
        Assert.assertEquals(args.dir, null);
        Assert.assertEquals(args.parameters, Arrays.asList("--Dfoo"));
    }

    public void classicGetoptFailure()
            throws ParseException
    {
        ArgsSingleChar args = singleCommand(ArgsSingleChar.class).parse(
                "-lgX");

        Assert.assertFalse(args.l);
        Assert.assertFalse(args.g);
        Assert.assertEquals(args.parameters, Arrays.asList("-lgX"));
    }

    /**
     * Make sure that if there are args with multiple names (e.g. "-log" and "-verbose"),
     * the usage will only display it once.
     */
    public void repeatedArgs()
    {
        SingleCommand<Args1> parser = singleCommand(Args1.class);
        CommandMetadata command = find(ImmutableList.of(parser.getCommandMetadata()), compose(equalTo("Args1"), CommandMetadata.nameGetter()));
        Assert.assertEquals(command.getAllOptions().size(), 8);
    }

    /**
     * Required options with multiple names should work with all names.
     */
    private void multipleNames(String option)
    {
        Args1 args = singleCommand(Args1.class).parse(option, "2");
        Assert.assertEquals(args.verbose.intValue(), 2);
    }

    public void multipleNames1()
    {
        multipleNames("-log");
    }

    public void multipleNames2()
    {
        multipleNames("-verbose");
    }

    public void arityString()
    {
        ArgsArityString args = singleCommand(ArgsArityString.class).parse("-pairs", "pair0", "pair1", "rest");

        Assert.assertEquals(args.pairs.size(), 2);
        Assert.assertEquals(args.pairs.get(0), "pair0");
        Assert.assertEquals(args.pairs.get(1), "pair1");
        Assert.assertEquals(args.rest.size(), 1);
        Assert.assertEquals(args.rest.get(0), "rest");
    }

    @Test(expectedExceptions = ParseException.class)
    public void arity2Fail()
    {
        singleCommand(ArgsArityString.class).parse("-pairs", "pair0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void multipleUnparsedFail()
    {
        singleCommand(ArgsMultipleUnparsed.class).parse();
    }

    public void privateArgs()
    {
        ArgsPrivate args = singleCommand(ArgsPrivate.class).parse("-verbose", "3");
        Assert.assertEquals(args.getVerbose().intValue(), 3);
    }

    private void argsBoolean1(String[] params, Boolean expected)
    {
        params = ImmutableList.builder().add(params).build().toArray(new String[0]);
        ArgsBooleanArity args = singleCommand(ArgsBooleanArity.class).parse(params);
        Assert.assertEquals(args.debug, expected);
    }

    private void argsBoolean0(String[] params, Boolean expected)
    {
        params = ImmutableList.builder().add(params).build().toArray(new String[0]);

        ArgsBooleanArity0 args = singleCommand(ArgsBooleanArity0.class).parse(params);
        Assert.assertEquals(args.debug, expected);
    }

    public void booleanArity1()
    {
        argsBoolean1(new String[]{}, Boolean.FALSE);
        argsBoolean1(new String[]{"-debug", "true"}, Boolean.TRUE);
    }

    public void booleanArity0()
    {
        argsBoolean0(new String[]{}, Boolean.FALSE);
        argsBoolean0(new String[]{"-debug"}, Boolean.TRUE);
    }

    @Test(expectedExceptions = ParseException.class)
    public void badParameterShouldThrowParameter1Exception()
    {
        singleCommand(Args1.class).parse("-log", "foo");
    }

    @Test(expectedExceptions = ParseException.class)
    public void badParameterShouldThrowParameter2Exception()
    {
        singleCommand(Args1.class).parse("-long", "foo");
    }
    
    @Test
    public void allowedValues1()
    {
        ArgsAllowedValues a = singleCommand(ArgsAllowedValues.class).parse("-mode", "a");
        Assert.assertEquals(a.mode, "a");
        a = singleCommand(ArgsAllowedValues.class).parse("-mode", "b");
        Assert.assertEquals(a.mode, "b");
        a = singleCommand(ArgsAllowedValues.class).parse("-mode", "c");
        Assert.assertEquals(a.mode, "c");
    }
    
    @Test
    public void allowedValues2()
    {
        ArgsAllowedValues a = singleCommand(ArgsAllowedValues.class).parse("-mode=a");
        Assert.assertEquals(a.mode, "a");
        a = singleCommand(ArgsAllowedValues.class).parse("-mode=b");
        Assert.assertEquals(a.mode, "b");
        a = singleCommand(ArgsAllowedValues.class).parse("-mode=c");
        Assert.assertEquals(a.mode, "c");
    }
    
    @Test(expectedExceptions = ParseException.class)
    public void allowedValuesShouldThrowIfNotAllowed1()
    {
        ArgsAllowedValues a = singleCommand(ArgsAllowedValues.class).parse("-mode", "d");
    }
    
    @Test(expectedExceptions = ParseException.class)
    public void allowedValuesShouldThrowIfNotAllowed2()
    {
        ArgsAllowedValues a = singleCommand(ArgsAllowedValues.class).parse("-mode=d");
    }

    public void listParameters()
    {
        Args2 a = singleCommand(Args2.class).parse("-log", "2", "-groups", "unit", "a", "b", "c", "-host", "host2");
        Assert.assertEquals(a.verbose.intValue(), 2);
        Assert.assertEquals(a.groups, "unit");
        Assert.assertEquals(a.hosts, Arrays.asList("host2"));
        Assert.assertEquals(a.parameters, Arrays.asList("a", "b", "c"));
    }

    public void inheritance()
    {
        ArgsInherited args = singleCommand(ArgsInherited.class).parse("-log", "3", "-child", "2");
        Assert.assertEquals(args.child.intValue(), 2);
        Assert.assertEquals(args.log.intValue(), 3);
    }

    public void negativeNumber()
    {
        Args1 a = singleCommand(Args1.class).parse("-verbose", "-3");
        Assert.assertEquals(a.verbose.intValue(), -3);
    }

    @Test(expectedExceptions = ParseException.class)
    public void requiredMainParameters()
    {
        singleCommand(ArgsRequired.class).parse();
    }

    @Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = ".*option.*missing.*")
    public void requiredOptions()
    {
        singleCommand(OptionsRequired.class).parse();
    }

    @Test
    public void ignoresOptionalOptions()
    {
        singleCommand(OptionsRequired.class).parse("--required", "foo");
    }

    private void verifyCommandOrdering(String[] commandNames, Class<?>... commands)
    {
        CliBuilder<Object> builder = Cli.builder("foo");
        for (Class<?> command : commands) {
            builder = builder.withCommand(command);
        }
        Cli<?> parser = builder.build();

        final List<CommandMetadata> commandParsers = parser.getMetadata().getDefaultGroupCommands();
        Assert.assertEquals(commandParsers.size(), commands.length);

        int i = 0;
        for (CommandMetadata commandParser : commandParsers) {
            Assert.assertEquals(commandParser.getName(), commandNames[i++]);
        }
    }

    public void commandsShouldBeShownInOrderOfInsertion()
    {
        verifyCommandOrdering(new String[]{"add", "commit"},
                CommandAdd.class, CommandCommit.class);
        verifyCommandOrdering(new String[]{"commit", "add"},
                CommandCommit.class, CommandAdd.class);
    }

    @DataProvider
    public static Object[][] f()
    {
        return new Integer[][]{
                new Integer[]{3, 5, 1},
                new Integer[]{3, 8, 1},
                new Integer[]{3, 12, 2},
                new Integer[]{8, 12, 2},
                new Integer[]{9, 10, 1},
        };
    }

    @Test(expectedExceptions = ParseException.class)
    public void arity1Fail()
    {
        singleCommand(Arity1.class).parse("-inspect");
    }

    public void arity1Success1()
    {
        Arity1 arguments = singleCommand(Arity1.class).parse("-inspect", "true");
        assertTrue(arguments.inspect);
    }

    public void arity1Success2()
    {
        Arity1 arguments = singleCommand(Arity1.class).parse("-inspect", "false");
        Assert.assertFalse(arguments.inspect);
    }

    @Test(expectedExceptions = ParseException.class,
            description = "Verify that the main parameter's type is checked to be a List")
    public void wrongMainTypeShouldThrow()
    {
        singleCommand(ArgsRequiredWrongMain.class).parse("f2");
    }

    @Test(description = "This used to run out of memory")
    public void oom()
    {
        singleCommand(ArgsOutOfMemory.class).parse();
    }

    @Test
    public void getParametersShouldNotNpe()
    {
        singleCommand(Args1.class).parse();
    }

    private static final List<String> V = Arrays.asList("a", "b", "c", "d");

    @DataProvider
    public Object[][] variable()
    {
        return new Object[][]{
                new Object[]{0, V.subList(0, 0), V},
                new Object[]{1, V.subList(0, 1), V.subList(1, 4)},
                new Object[]{2, V.subList(0, 2), V.subList(2, 4)},
                new Object[]{3, V.subList(0, 3), V.subList(3, 4)},
                new Object[]{4, V.subList(0, 4), V.subList(4, 4)},
        };
    }

    public void enumArgs()
    {
        ArgsEnum args = singleCommand(ArgsEnum.class).parse("-choice", "ONE");
        Assert.assertEquals(args.choice, ArgsEnum.ChoiceType.ONE);
    }

    @Test(expectedExceptions = ParseException.class)
    public void enumArgsFail()
    {
        singleCommand(ArgsEnum.class).parse("A");
    }

    @Test(expectedExceptions = ParseException.class)
    public void shouldThrowIfUnknownOption()
    {
        @Command(name = "A")
        class A
        {
            @Option(name = "-long")
            public long l;
        }
        singleCommand(A.class).parse("32");
    }

    @Test
    public void testSingleCommandHelpOption()
    {
        CommandTest commandTest = singleCommand(CommandTest.class).parse("-h", "-i", "foo");
        assertTrue(commandTest.helpOption.showHelpIfRequested());
    }

    @Command(name = "test", description = "TestCommand")
    public static class CommandTest
    {
        @Inject
        public HelpOption helpOption;

        @Arguments(description = "Patterns of files to be added")
        public List<String> patterns;

        @Option(name = "-i", description = "Interactive add mode")
        public Boolean interactive = false;

    }
}
