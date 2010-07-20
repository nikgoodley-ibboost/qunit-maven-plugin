package org.moyrax.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.moyrax.javascript.qunit.ReporterManager;
import org.moyrax.javascript.qunit.TestRunner;
import org.moyrax.reporting.LogReporter;
import org.moyrax.reporting.PlainFileReporter;
import org.moyrax.reporting.Reporter;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Goal which touches a timestamp file.
 *
 * @goal test
 * @phase test
 */
public class QUnitPlugin extends AbstractMojo {
  /**
   * Files which will be executed using QUnit.
   *
   * @parameter
   * @required
   */
  private FileSet testResources;

  /**
   * Directories that will be included to search resources.
   *
   * @parameter
   * @required
   */
  private List<Entry> contextPath = new ArrayList<Entry>();

  /**
   * List of classpath urls in which lookup for exportable Java classes.
   *
   * @parameter
   */
  private List<String> components = new ArrayList<String>();

  /**
   * The greeting to display.
   *
   * @parameter expression="${project.build.directory}" default-value="${project.build.directory}"
   */
  private File targetPath;

  /**
   * Object to ask the files specified in the plugin configuration.
   */
  private final FileSetManager fileSetManager = new FileSetManager();

  /**
   * Testing client.
   */
  private static TestingClient client;

  /**
   * Reporting results to the console.
   */
  private ReporterManager reporter;

  /**
   * Container for running tests.
   */
  private WebClient browser = new WebClient();

  /**
   * Testing runner.
   */
  private TestRunner runner;

  /**
   * Executes this plugin when the build reached the defined phase and goal.
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    ArrayList<Reporter> reporters = 
      new ArrayList<Reporter>(Arrays.asList(new Reporter[] {
          new LogReporter(),
          new PlainFileReporter(getReportsDirectory())
      }));

    reporter = new ReporterManager(reporters, new MojoLogAdapter(getLog()));
    runner = new TestRunner(reporter, browser);

    initEnvironment();
    loadContextResources();

    try {
      client.runTests();
    } catch (Exception ex) {
      throw new MojoFailureException(getClass().getSimpleName(), "",
          ex.getMessage());
    }
  }

  /**
   * Sets the build target path.
   *
   * @param theTargetPath Base path. It cannot be null or empty.
   */
  public void setTargetPath(final String theTargetPath) {
    Validate.notEmpty(theTargetPath, "The base path cannot be null or empty.");

    targetPath = new File(theTargetPath);

    Validate.isTrue(targetPath.exists() &&
        targetPath.isDirectory(), "The base path is not a valid "
        + "directory.");
  }

  /**
   * Sets the {@link FileSet} which contains the rules to retrieve the testing
   * files.
   *
   * @param theResources Test resources. It cannot be null.
   */
  public void setTestResources(final FileSet theResources) {
    Validate.notNull(theResources, "The resources cannot be null.");

    testResources = theResources;
  }

  /**
   * Adds a new search path to scan for JavaScript components.
   *
   * @param classPath Classpath to search for resources. It cannot be null or
   *    empty.
   */
  public void addComponentSearchPath(final String classPath) {
    Validate.notEmpty(classPath, "The class path cannot be null or empty.");

    components.add(classPath);
  }

  /**
   * Initializes the required resources for the test environment.
   */
  private void loadContextResources() {
    final String[] dependencies = new String[] {
      /* QUnit testing framework. */
      "org/moyrax/javascript/lib/qunit.js"
    };

    for (int i = 0; i < dependencies.length; i++) {
      client.addGlobalResource(dependencies[i]);
    }
  }

  /**
   * Initializes the environment configuration and starts the testing server.
   */
  private void initEnvironment() {
    EnvironmentConfiguration env = new EnvironmentConfiguration();

    env.setFiles(testResources.getDirectory(),
        fileSetManager.getIncludedFiles(testResources),
        fileSetManager.getExcludedFiles(testResources));

    env.setLookupPackages(components.toArray(new String[] {}));
    env.setTargetPath(targetPath);

    for (Entry entry : contextPath) {
      ContextPathBuilder.addDefinition(entry.files.getDirectory(),
          entry.files.getIncludesArray(), entry.files.getExcludesArray());
    }

    ContextPathBuilder.build();

    client = new TestingClient(runner, env);
  }

  /**
   * Returns the directory where the reports will be written.
   */
  private String getReportsDirectory() {
    File directory = new File(targetPath.getAbsolutePath(),
        "target/qunit-reports");

    if (!directory.exists()) {
      directory.mkdirs();
    }

    return directory.getAbsolutePath();
  }
}
