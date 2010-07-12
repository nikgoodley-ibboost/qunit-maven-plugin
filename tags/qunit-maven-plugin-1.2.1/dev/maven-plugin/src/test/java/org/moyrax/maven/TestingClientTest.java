package org.moyrax.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.junit.Before;
import org.junit.Test;
import org.moyrax.javascript.Shell;
import org.moyrax.javascript.qunit.ReporterManager;
import org.moyrax.javascript.qunit.TestRunner;
import org.moyrax.reporting.LogReporter;
import org.moyrax.reporting.Reporter;
import org.moyrax.resolver.ClassPathResolver;
import org.moyrax.resolver.LibraryResolver;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Tests the {@link TestingClient} class.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 */
public class TestingClientTest {
  private static final Log log = LogFactory.getLog(TestingClientTest.class);

  /** Test environment configuration. */
  private EnvironmentConfiguration context = new EnvironmentConfiguration();

  /**
   * Reporting results to the console.
   */
  private ReporterManager reporter = new ReporterManager(
      new ArrayList<Reporter>(Arrays.asList(new Reporter[] {
          new LogReporter() })), log);

  /**
   * Container for running tests.
   */
  private WebClient client = new WebClient();

  /**
   * Testing runner.
   */
  private TestRunner runner;

  /**
   * Object to ask the files specified in the plugin configuration.
   */
  private final FileSetManager fileSetManager = new FileSetManager();

  @Before
  public void setUp() {
    runner = new TestRunner(reporter, client);
  }

  @Test
  public void testApplication() throws Exception {
    final TestingClient testDriverClient = new TestingClient(runner,context);

    context.setTestOutputDirectory("test/");
    context.setProjectBasePath(new File(".").toURI().toURL());

    final String baseDirectory = System.getProperty("user.dir");
    final FileSet tests = new FileSet();

    tests.setDirectory(baseDirectory + "/src/test/resources/org/moyrax/");
    tests.addInclude("**/*.html");

    context.setFiles(tests.getDirectory(),
        fileSetManager.getIncludedFiles(tests),
        fileSetManager.getExcludedFiles(tests));

    context.setLookupPackages(new String[] {
        "classpath:/org/moyrax/javascript/common/**"
    });

    Shell.setResolver("lib", new LibraryResolver("/org/moyrax/javascript/lib"));
    Shell.setResolver("classpath", new ClassPathResolver());

    loadContextResources(testDriverClient);

    testDriverClient.runTests();
  }

  /**
   * Initializes the required resources for the test environment.
   */
  private void loadContextResources(final TestingClient client) {
    final String[] dependencies = new String[] {
      /* QUnit testing framework. */
      "org/moyrax/javascript/lib/qunit.js"
    };

    for (int i = 0; i < dependencies.length; i++) {
      client.addGlobalResource(dependencies[i]);
    }
  }
}
