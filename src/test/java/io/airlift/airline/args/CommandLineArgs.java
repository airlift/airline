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

package io.airlift.airline.args;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.util.List;

@Command(name = "CommandLineArgs")
public class CommandLineArgs
{

    @Arguments(description = "The XML suite files to run")
    public List<String> suiteFiles = com.google.common.collect.Lists.newArrayList();

    @Option(name = {"-log", "-verbose"}, description = "Level of verbosity")
    public Integer verbose;

    @Option(name = "-groups", description = "Comma-separated list of group names to be run")
    public String groups;

    @Option(name = "-excludedgroups", description = "Comma-separated list of group names to be " +
            "run")
    public String excludedGroups;

    @Option(name = "-d", description = "Output directory")
    public String outputDirectory;

    @Option(name = "-junit", description = "JUnit mode")
    public Boolean junit = Boolean.FALSE;

    @Option(name = "-listener", description = "List of .class files or list of class names" +
            " implementing ITestListener or ISuiteListener")
    public String listener;

    @Option(name = "-methodselectors", description = "List of .class files or list of class " +
            "names implementing IMethodSelector")
    public String methodSelectors;

    @Option(name = "-objectfactory", description = "List of .class files or list of class " +
            "names implementing ITestRunnerFactory")
    public String objectFactory;

    @Option(name = "-parallel", description = "Parallel mode (methods, tests or classes)")
    public String parallelMode;

    @Option(name = "-configfailurepolicy", description = "Configuration failure policy (skip or continue)")
    public String configFailurePolicy;

    @Option(name = "-threadcount", description = "Number of threads to use when running tests " +
            "in parallel")
    public Integer threadCount;

    @Option(name = "-dataproviderthreadcount", description = "Number of threads to use when " +
            "running data providers")
    public Integer dataProviderThreadCount;

    @Option(name = "-suitename", description = "Default name of test suite, if not specified " +
            "in suite definition file or source code")
    public String suiteName;

    @Option(name = "-testname", description = "Default name of test, if not specified in suite" +
            "definition file or source code")
    public String testName;

    @Option(name = "-reporter", description = "Extended configuration for custom report listener")
    public String reporter;

    /**
     * Used as map key for the complete list of report listeners provided with the above argument
     */
    @Option(name = "-reporterslist")
    public String reportersList;

    @Option(name = "-usedefaultlisteners", description = "Whether to use the default listeners")
    public String useDefaultListeners = "true";

    @Option(name = "-skipfailedinvocationcounts")
    public Boolean skipFailedInvocationCounts;

    @Option(name = "-testclass", description = "The list of test classes")
    public String testClass;

    @Option(name = "-testnames", description = "The list of test names to run")
    public String testNames;

    @Option(name = "-testjar", description = "")
    public String testJar;

    @Option(name = "-testRunFactory", description = "")
    public String testRunFactory;

    @Option(name = "-port", description = "The port")
    public Integer port;

    @Option(name = "-host", description = "The host")
    public String host;

    @Option(name = "-master", description = "Host where the master is")
    public String master;

    @Option(name = "-slave", description = "Host where the slave is")
    public String slave;
}
