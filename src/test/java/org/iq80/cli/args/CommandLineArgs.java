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

package org.iq80.cli.args;

import org.iq80.cli.Arguments;
import org.iq80.cli.Command;
import org.iq80.cli.Option;

import java.util.List;

@Command(name="CommandLineArgs")
public class CommandLineArgs
{

    @Arguments(description = "The XML suite files to run")
    public List<String> suiteFiles = com.google.common.collect.Lists.newArrayList();

    @Option(options = {"-log", "-verbose"}, description = "Level of verbosity")
    public Integer verbose;

    @Option(options = "-groups", description = "Comma-separated list of group names to be run")
    public String groups;

    @Option(options = "-excludedgroups", description = "Comma-separated list of group names to be " +
            "run")
    public String excludedGroups;

    @Option(options = "-d", description = "Output directory")
    public String outputDirectory;

    @Option(options = "-junit", description = "JUnit mode")
    public Boolean junit = Boolean.FALSE;

    @Option(options = "-listener", description = "List of .class files or list of class names" +
            " implementing ITestListener or ISuiteListener")
    public String listener;

    @Option(options = "-methodselectors", description = "List of .class files or list of class " +
            "names implementing IMethodSelector")
    public String methodSelectors;

    @Option(options = "-objectfactory", description = "List of .class files or list of class " +
            "names implementing ITestRunnerFactory")
    public String objectFactory;

    @Option(options = "-parallel", description = "Parallel mode (methods, tests or classes)")
    public String parallelMode;

    @Option(options = "-configfailurepolicy", description = "Configuration failure policy (skip or continue)")
    public String configFailurePolicy;

    @Option(options = "-threadcount", description = "Number of threads to use when running tests " +
            "in parallel")
    public Integer threadCount;

    @Option(options = "-dataproviderthreadcount", description = "Number of threads to use when " +
            "running data providers")
    public Integer dataProviderThreadCount;

    @Option(options = "-suitename", description = "Default name of test suite, if not specified " +
            "in suite definition file or source code")
    public String suiteName;

    @Option(options = "-testname", description = "Default name of test, if not specified in suite" +
            "definition file or source code")
    public String testName;

    @Option(options = "-reporter", description = "Extended configuration for custom report listener")
    public String reporter;

    /**
     * Used as map key for the complete list of report listeners provided with the above argument
     */
    @Option(options = "-reporterslist")
    public String reportersList;

    @Option(options = "-usedefaultlisteners", description = "Whether to use the default listeners")
    public String useDefaultListeners = "true";

    @Option(options = "-skipfailedinvocationcounts")
    public Boolean skipFailedInvocationCounts;

    @Option(options = "-testclass", description = "The list of test classes")
    public String testClass;

    @Option(options = "-testnames", description = "The list of test names to run")
    public String testNames;

    @Option(options = "-testjar", description = "")
    public String testJar;

    @Option(options = "-testRunFactory", description = "")
    public String testRunFactory;

    @Option(options = "-port", description = "The port")
    public Integer port;

    @Option(options = "-host", description = "The host")
    public String host;

    @Option(options = "-master", description = "Host where the master is")
    public String master;

    @Option(options = "-slave", description = "Host where the slave is")
    public String slave;

}
