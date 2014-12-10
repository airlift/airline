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

package io.airlift.airline;

import com.google.common.collect.ImmutableList;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.args.Args1;
import io.airlift.airline.args.Args2;
import io.airlift.airline.args.ArgsAllowedValues;
import io.airlift.airline.args.ArgsArityString;
import io.airlift.airline.args.ArgsBooleanArity;
import io.airlift.airline.args.ArgsBooleanArity0;
import io.airlift.airline.args.ArgsEnum;
import io.airlift.airline.args.ArgsInherited;
import io.airlift.airline.args.ArgsMultipleUnparsed;
import io.airlift.airline.args.ArgsOutOfMemory;
import io.airlift.airline.args.ArgsPrivate;
import io.airlift.airline.args.ArgsRequired;
import io.airlift.airline.args.ArgsSingleChar;
import io.airlift.airline.args.Arity1;
import io.airlift.airline.args.OptionsRequired;
import io.airlift.airline.command.CommandAdd;
import io.airlift.airline.command.CommandCommit;
import io.airlift.airline.model.CommandMetadata;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;
import static io.airlift.airline.SingleCommand.singleCommand;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestSingleCommand
{
    @Test
    public void simpleArgs()
            throws ParseException
    {
        Args1 args = singleCommand(Args1.class).parse(
                "-debug", "-log", "2", "-float", "1.2", "-double", "1.3", "-bigdecimal", "1.4",
                "-groups", "unit", "a", "b", "c");

        assertTrue(args.debug);
        assertEquals(args.verbose.intValue(), 2);
        assertEquals(args.groups, "unit");
        assertEquals(args.parameters, Arrays.asList("a", "b", "c"));
        assertEquals(args.floa, 1.2f, 0.1f);
        assertEquals(args.doub, 1.3f, 0.1f);
        assertEquals(args.bigd, new BigDecimal("1.4"));
    }

    @Test
    public void equalsArgs()
            throws ParseException
    {
        Args1 args = singleCommand(Args1.class).parse(
                "-debug", "-log=2", "-float=1.2", "-double=1.3", "-bigdecimal=1.4",
                "-groups=unit", "a", "b", "c");

        assertTrue(args.debug);
        assertEquals(args.verbose.intValue(), 2);
        assertEquals(args.groups, "unit");
        assertEquals(args.parameters, Arrays.asList("a", "b", "c"));
        assertEquals(args.floa, 1.2f, 0.1f);
        assertEquals(args.doub, 1.3f, 0.1f);
        assertEquals(args.bigd, new BigDecimal("1.4"));
    }

    @Test
    public void classicGetoptArgs()
            throws ParseException
    {
        ArgsSingleChar args = singleCommand(ArgsSingleChar.class).parse(
                "-lg", "-dsn", "-pa-p", "-2f", "-z", "--Dfoo");

        assertTrue(args.l);
        assertTrue(args.g);
        assertTrue(args.d);
        assertEquals(args.s, "n");
        assertEquals(args.p, "a-p");
        assertFalse(args.n);
        assertTrue(args.two);
        assertEquals(args.f, "-z");
        assertFalse(args.z);
        assertEquals(args.dir, null);
        assertEquals(args.parameters, Arrays.asList("--Dfoo"));
    }

    @Test
    public void classicGetoptFailure()
            throws ParseException
    {
        ArgsSingleChar args = singleCommand(ArgsSingleChar.class).parse(
                "-lgX");

        assertFalse(args.l);
        assertFalse(args.g);
        assertEquals(args.parameters, Arrays.asList("-lgX"));
    }

    /**
     * Make sure that if there are args with multiple names (e.g. "-log" and "-verbose"),
     * the usage will only display it once.
     */
    @Test
    public void repeatedArgs()
    {
        SingleCommand<Args1> parser = singleCommand(Args1.class);
        CommandMetadata command = find(ImmutableList.of(parser.getCommandMetadata()), compose(equalTo("Args1"), CommandMetadata.nameGetter()));
        assertEquals(command.getAllOptions().size(), 8);
    }

    /**
     * Required options with multiple names should work with all names.
     */
    private void multipleNames(String option)
    {
        Args1 args = singleCommand(Args1.class).parse(option, "2");
        assertEquals(args.verbose.intValue(), 2);
    }

    @Test
    public void multipleNames1()
    {
        multipleNames("-log");
    }

    @Test
    public void multipleNames2()
    {
        multipleNames("-verbose");
    }

    @Test
    public void arityString()
    {
        ArgsArityString args = singleCommand(ArgsArityString.class).parse("-pairs", "pair0", "pair1", "rest");

        assertEquals(args.pairs.size(), 2);
        assertEquals(args.pairs.get(0), "pair0");
        assertEquals(args.pairs.get(1), "pair1");
        assertEquals(args.rest.size(), 1);
        assertEquals(args.rest.get(0), "rest");
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

    @Test
    public void privateArgs()
    {
        ArgsPrivate args = singleCommand(ArgsPrivate.class).parse("-verbose", "3");
        assertEquals(args.getVerbose().intValue(), 3);
    }

    private void argsBoolean1(String[] params, Boolean expected)
    {
        ArgsBooleanArity args = singleCommand(ArgsBooleanArity.class).parse(params);
        assertEquals(args.debug, expected);
    }

    private void argsBoolean0(String[] params, Boolean expected)
    {
        ArgsBooleanArity0 args = singleCommand(ArgsBooleanArity0.class).parse(params);
        assertEquals(args.debug, expected);
    }

    @Test
    public void booleanArity1()
    {
        argsBoolean1(new String[] {}, Boolean.FALSE);
        argsBoolean1(new String[] {"-debug", "true"}, Boolean.TRUE);
    }

    @Test
    public void booleanArity0()
    {
        argsBoolean0(new String[] {}, Boolean.FALSE);
        argsBoolean0(new String[] {"-debug"}, Boolean.TRUE);
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
        assertEquals(a.mode, "a");
        a = singleCommand(ArgsAllowedValues.class).parse("-mode", "b");
        assertEquals(a.mode, "b");
        a = singleCommand(ArgsAllowedValues.class).parse("-mode", "c");
        assertEquals(a.mode, "c");
    }
    
    @Test
    public void allowedValues2()
    {
        ArgsAllowedValues a = singleCommand(ArgsAllowedValues.class).parse("-mode=a");
        assertEquals(a.mode, "a");
        a = singleCommand(ArgsAllowedValues.class).parse("-mode=b");
        assertEquals(a.mode, "b");
        a = singleCommand(ArgsAllowedValues.class).parse("-mode=c");
        assertEquals(a.mode, "c");
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

    @Test
    public void listParameters()
    {
        Args2 a = singleCommand(Args2.class).parse("-log", "2", "-groups", "unit", "a", "b", "c", "-host", "host2");
        assertEquals(a.verbose.intValue(), 2);
        assertEquals(a.groups, "unit");
        assertEquals(a.hosts, Arrays.asList("host2"));
        assertEquals(a.parameters, Arrays.asList("a", "b", "c"));
    }

    @Test
    public void inheritance()
    {
        ArgsInherited args = singleCommand(ArgsInherited.class).parse("-log", "3", "-child", "2");
        assertEquals(args.child.intValue(), 2);
        assertEquals(args.log.intValue(), 3);
    }

    @Test
    public void negativeNumber()
    {
        Args1 a = singleCommand(Args1.class).parse("-verbose", "-3");
        assertEquals(a.verbose.intValue(), -3);
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
        assertEquals(commandParsers.size(), commands.length);

        int i = 0;
        for (CommandMetadata commandParser : commandParsers) {
            assertEquals(commandParser.getName(), commandNames[i++]);
        }
    }

    @Test
    public void commandsShouldBeShownInOrderOfInsertion()
    {
        verifyCommandOrdering(new String[] {"add", "commit"}, CommandAdd.class, CommandCommit.class);
        verifyCommandOrdering(new String[] {"commit", "add"}, CommandCommit.class, CommandAdd.class);
    }

    @DataProvider
    public static Object[][] f()
    {
        return new Integer[][] {
                new Integer[] {3, 5, 1},
                new Integer[] {3, 8, 1},
                new Integer[] {3, 12, 2},
                new Integer[] {8, 12, 2},
                new Integer[] {9, 10, 1},
        };
    }

    @Test(expectedExceptions = ParseException.class)
    public void arity1Fail()
    {
        singleCommand(Arity1.class).parse("-inspect");
    }

    @Test
    public void arity1Success1()
    {
        Arity1 arguments = singleCommand(Arity1.class).parse("-inspect", "true");
        assertTrue(arguments.inspect);
    }

    @Test
    public void arity1Success2()
    {
        Arity1 arguments = singleCommand(Arity1.class).parse("-inspect", "false");
        assertFalse(arguments.inspect);
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
        return new Object[][] {
                new Object[] {0, V.subList(0, 0), V},
                new Object[] {1, V.subList(0, 1), V.subList(1, 4)},
                new Object[] {2, V.subList(0, 2), V.subList(2, 4)},
                new Object[] {3, V.subList(0, 3), V.subList(3, 4)},
                new Object[] {4, V.subList(0, 4), V.subList(4, 4)},
        };
    }

    @Test
    public void enumArgs()
    {
        ArgsEnum args = singleCommand(ArgsEnum.class).parse("-choice", "ONE");
        assertEquals(args.choice, ArgsEnum.ChoiceType.ONE);
    }

    @Test(expectedExceptions = ParseException.class)
    public void enumArgsFail()
    {
        singleCommand(ArgsEnum.class).parse("A");
    }

    @SuppressWarnings("UnusedDeclaration")
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
