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
import io.airlift.airline.args.CommandWithHelp;
import io.airlift.airline.args.OptionsRequired;
import io.airlift.airline.command.CommandAdd;
import io.airlift.airline.command.CommandCommit;
import io.airlift.airline.model.CommandMetadata;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.Iterables.find;
import static io.airlift.airline.TestUtil.singleCommandParser;

@Test
public class CommandTest
{
    public void simpleArgs()
            throws ParseException
    {
        Args1 args = singleCommandParser(Args1.class).parse("Args1",
                "-debug", "-log", "2", "-float", "1.2", "-double", "1.3", "-bigdecimal", "1.4",
                "-groups", "unit", "a", "b", "c");

        Assert.assertTrue(args.debug);
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
        Args1 args = singleCommandParser(Args1.class).parse("Args1",
                "-debug", "-log=2", "-float=1.2", "-double=1.3", "-bigdecimal=1.4",
                "-groups=unit", "a", "b", "c");

        Assert.assertTrue(args.debug);
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
        ArgsSingleChar args = singleCommandParser(ArgsSingleChar.class).parse("ArgsSingleChar",
                "-lg", "-dsn", "-pa-p", "-2f", "-z", "--Dfoo");

        Assert.assertTrue(args.l);
        Assert.assertTrue(args.g);
        Assert.assertTrue(args.d);
        Assert.assertEquals(args.s, "n");
        Assert.assertEquals(args.p, "a-p");
        Assert.assertFalse(args.n);
        Assert.assertTrue(args.two);
        Assert.assertEquals(args.f, "-z");
        Assert.assertFalse(args.z);
        Assert.assertEquals(args.dir, null);
        Assert.assertEquals(args.parameters, Arrays.asList("--Dfoo"));
    }

    public void classicGetoptFailure()
            throws ParseException
    {
        ArgsSingleChar args = singleCommandParser(ArgsSingleChar.class).parse("ArgsSingleChar",
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
        Cli<Args1> parser = singleCommandParser(Args1.class);
        CommandMetadata command = find(parser.getMetadata().getDefaultGroupCommands(), compose(equalTo("Args1"), CommandMetadata.nameGetter()));
        Assert.assertEquals(command.getAllOptions().size(), 8);
    }

    /**
     * Getting the description of a nonexistent command should throw an exception.
     */
    @Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = "No command specified")
    public void nonexistentCommandShouldThrow()
    {
        singleCommandParser(Args1.class).parse();
    }

    /**
     * Getting the description of a nonexistent command should throw an exception.
     */
    @Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = "Command 'foo' not recognized")
    public void unrecognizedCommandShouldThrow()
    {
        singleCommandParser(Args1.class).parse("foo");
    }

    /**
     * Required options with multiple names should work with all names.
     */
    private void multipleNames(String option)
    {
        Args1 args = singleCommandParser(Args1.class).parse("Args1", option, "2");
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
        ArgsArityString args = singleCommandParser(ArgsArityString.class).parse("ArgsArityString", "-pairs", "pair0", "pair1", "rest");

        Assert.assertEquals(args.pairs.size(), 2);
        Assert.assertEquals(args.pairs.get(0), "pair0");
        Assert.assertEquals(args.pairs.get(1), "pair1");
        Assert.assertEquals(args.rest.size(), 1);
        Assert.assertEquals(args.rest.get(0), "rest");
    }

    @Test(expectedExceptions = ParseException.class)
    public void arity2Fail()
    {
        singleCommandParser(ArgsArityString.class).parse("ArgsArityString", "-pairs", "pair0");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void multipleUnparsedFail()
    {
        singleCommandParser(ArgsMultipleUnparsed.class).parse();
    }

    public void privateArgs()
    {
        ArgsPrivate args = singleCommandParser(ArgsPrivate.class).parse("ArgsPrivate", "-verbose", "3");
        Assert.assertEquals(args.getVerbose().intValue(), 3);
    }

    private void argsBoolean1(String[] params, Boolean expected)
    {
        params = ImmutableList.builder().add("ArgsBooleanArity").add(params).build().toArray(new String[0]);
        ArgsBooleanArity args = singleCommandParser(ArgsBooleanArity.class).parse(params);
        Assert.assertEquals(args.debug, expected);
    }

    private void argsBoolean0(String[] params, Boolean expected)
    {
        params = ImmutableList.builder().add("ArgsBooleanArity0").add(params).build().toArray(new String[0]);

        ArgsBooleanArity0 args = singleCommandParser(ArgsBooleanArity0.class).parse(params);
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
        singleCommandParser(Args1.class).parse("Args1", "-log", "foo");
    }

    @Test(expectedExceptions = ParseException.class)
    public void badParameterShouldThrowParameter2Exception()
    {
        singleCommandParser(Args1.class).parse("Args1", "-long", "foo");
    }

    public void listParameters()
    {
        Args2 a = singleCommandParser(Args2.class).parse("Args2", "-log", "2", "-groups", "unit", "a", "b", "c", "-host", "host2");
        Assert.assertEquals(a.verbose.intValue(), 2);
        Assert.assertEquals(a.groups, "unit");
        Assert.assertEquals(a.hosts, Arrays.asList("host2"));
        Assert.assertEquals(a.parameters, Arrays.asList("a", "b", "c"));
    }

    public void inheritance()
    {
        ArgsInherited args = singleCommandParser(ArgsInherited.class).parse("ArgsInherited", "-log", "3", "-child", "2");
        Assert.assertEquals(args.child.intValue(), 2);
        Assert.assertEquals(args.log.intValue(), 3);
    }

    public void negativeNumber()
    {
        Args1 a = singleCommandParser(Args1.class).parse("Args1", "-verbose", "-3");
        Assert.assertEquals(a.verbose.intValue(), -3);
    }

    @Test(expectedExceptions = ParseException.class)
    public void requiredMainParameters()
    {
        singleCommandParser(ArgsRequired.class).parse("ArgsRequired");
    }

    @Test(expectedExceptions = ParseException.class, expectedExceptionsMessageRegExp = ".*option.*missing.*")
    public void requiredOptions()
    {
        singleCommandParser(OptionsRequired.class).parse("OptionsRequired");
    }

    @Test
    public void ignoresOptionalOptions()
    {
        singleCommandParser(OptionsRequired.class).parse("OptionsRequired", "--required", "foo");
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
        singleCommandParser(Arity1.class).parse("Arity1", "-inspect");
    }

    public void arity1Success1()
    {
        Arity1 arguments = singleCommandParser(Arity1.class).parse("Arity1", "-inspect", "true");
        Assert.assertTrue(arguments.inspect);
    }

    public void arity1Success2()
    {
        Arity1 arguments = singleCommandParser(Arity1.class).parse("Arity1", "-inspect", "false");
        Assert.assertFalse(arguments.inspect);
    }

    @Test(expectedExceptions = ParseException.class,
            description = "Verify that the main parameter's type is checked to be a List")
    public void wrongMainTypeShouldThrow()
    {
        singleCommandParser(ArgsRequiredWrongMain.class).parse("f1", "f2");
    }

    @Test(description = "This used to run out of memory")
    public void oom()
    {
        singleCommandParser(ArgsOutOfMemory.class).parse("ArgsOutOfMemory");
    }

    @Test
    public void getParametersShouldNotNpe()
    {
        singleCommandParser(Args1.class).parse("Args1");
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
        ArgsEnum args = singleCommandParser(ArgsEnum.class).parse("ArgsEnum", "-choice", "ONE");
        Assert.assertEquals(args.choice, ArgsEnum.ChoiceType.ONE);
    }

    @Test(expectedExceptions = ParseException.class)
    public void enumArgsFail()
    {
        singleCommandParser(ArgsEnum.class).parse("-choice", "A");
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
        singleCommandParser(A.class).parse("-lon", "32");
    }

	public void helpOption()
	{
		CommandWithHelp command = singleCommandParser(CommandWithHelp.class).parse("CommandWithHelp", "-h");
		Assert.assertTrue(command.helpOption.showHelpIfRequested());
	}
}
