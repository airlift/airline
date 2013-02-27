package io.airlift.command;

import io.airlift.command.command.CommandGroupAnnotationTest;
import org.testng.TestNG;
import org.testng.annotations.BeforeSuite;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   0.6
 * @version 0.6
 */
public class AllTests {

    @BeforeSuite
    public void testSuite() {
        TestNG aTestNG = new TestNG();

        aTestNG.setTestClasses(new Class[] { CommandTest.class, io.airlift.command.CommandTest.class,
                                             ParametersDelegateTest.class, HelpTest.class, GitTest.class, GalaxyCommandLineParser.class,
                                             CommandGroupAnnotationTest.class });

        aTestNG.run();

        if (aTestNG.hasFailure()) {
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        new AllTests().testSuite();
    }
}
