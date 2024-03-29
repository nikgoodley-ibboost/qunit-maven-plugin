package org.moyrax.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.junit.Before;
import org.junit.Test;
import org.moyrax.javascript.ContextClassLoader;
import org.moyrax.javascript.Shell;
import org.moyrax.javascript.qunit.QUnitReporter;
import org.moyrax.javascript.qunit.TestRunner;
import org.moyrax.resolver.ClassPathResolver;
import org.moyrax.resolver.LibraryResolver;
import org.moyrax.util.ResourceUtils;

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
  private QUnitReporter reporter = new QUnitReporter(
      System.getProperty("java.io.tmpdir"), log);

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
  public void testTestingClient() throws Exception {
    final TestingClient testingClient;
    testingClient = new TestingClient(runner,context, new ClassPathResolver(
        Thread.currentThread().getContextClassLoader()));

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

    context.setClassLoader(new ContextClassLoader(
        Thread.currentThread().getContextClassLoader()));

    Shell.setResolver("lib", new LibraryResolver("/org/moyrax/javascript/lib"));
    Shell.setResolver("classpath", new ClassPathResolver(
        Thread.currentThread().getContextClassLoader()));

    loadContextResources(testingClient);

    testingClient.runTests();
  }

  /**
   * Initializes the required resources for the test environment.
   */
  private void loadContextResources(final TestingClient client)
      throws IOException {

    final String[] dependencies = new String[] {
        /* QUnit testing framework. */
        "org/moyrax/javascript/lib/qunit.js"
    };

    for (int i = 0; i < dependencies.length; i++) {
      client.addGlobalResource(dependencies[i]);
    }

    // Copies the qunit source file for the local tests.
    IOUtils.copy(
        ResourceUtils.getResourceInputStream(
            "classpath:org/moyrax/javascript/lib/qunit.js"),
        new FileOutputStream(new File(System.getProperty("java.io.tmpdir"),
            "qunit.js"))
    );
  }
}
