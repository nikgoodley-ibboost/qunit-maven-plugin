package org.moyrax.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Goal which touches a timestamp file.
 *
 * @goal qunit
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
  public List<String> components = new ArrayList<String>();

  /**
   * Port in which the testing server will be started. Default is 3137.
   *
   * @parameter default-value="3137"
   */
  private Integer port;

  /**
   * The greeting to display.
   *
   * @parameter expression="${project.build.directory}" default-value="${project.build.directory}"
   */
  private File projectBasePath;

  /**
   * Object to ask the files specified in the plugin configuration.
   */
  private final FileSetManager fileSetManager = new FileSetManager();

  /**
   * Testing server.
   */
  private static TestingServer server;

  /**
   * Testing client.
   */
  private static TestingClient client;

  /**
   * Executes this plugin when the build reached the defined phase and goal.
   */
  public void execute() throws MojoExecutionException {
    initEnvironment();
    loadContextResources();

    client.runTests();
  }

  /**
   * Sets the project's base path.
   *
   * @param basePath Base path. It cannot be null or empty.
   */
  public void setProjectBasePath(final String basePath) {
    Validate.notEmpty(basePath, "The base path cannot be null or empty.");

    projectBasePath = new File(basePath);

    Validate.isTrue(projectBasePath.exists() &&
        projectBasePath.isDirectory(), "The base path is not a valid "
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
   * Adds a new entry to the context path.
   *
   * @param entry Entry to add. It cannot be null.
   */
  public void addContextPath(final Entry entry) {
    Validate.notNull(entry, "The entry cannot be null.");

    contextPath.add(entry);
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
   * Sets the testing server port.
   *
   * @param aPort The testing server port. It cannot be null.
   */
  public void setServerPort(Integer aPort) {
    Validate.notNull(aPort, "The port cannot be null.");

    port = aPort;
  }

  /**
   * Initializes the required resources for the test environment.
   */
  private void loadContextResources() {
    final String[] dependencies = new String[] {
      /* QUnit testing framework. */
      "org/moyrax/javascript/lib/qunit.js",
      /* QUnit plugin test handler. */
      "org/moyrax/javascript/lib/qunit-plugin.js"
    };

    for (int i = 0; i < dependencies.length; i++) {
      client.addGlobalResource(dependencies[i]);
    }
  }

  /**
   * Initializes the environment configuration and starts the testing server.
   */
  private void initEnvironment() {
    if (server != null) {
      return;
    }

    EnvironmentConfiguration env = new EnvironmentConfiguration();

    try {
      env.setServerPort(port);
      env.setFiles(testResources.getDirectory(),
          fileSetManager.getIncludedFiles(testResources),
          fileSetManager.getExcludedFiles(testResources));

      env.setLookupPackages(components.toArray(new String[] {}));
      env.setProjectBasePath(projectBasePath.toURI().toURL());

      for (Entry entry : contextPath) {
        ContextPathBuilder.addDefinition(entry.files.getDirectory(),
            entry.files.getIncludesArray(), entry.files.getExcludesArray());
      }

    } catch (MalformedURLException e) {
      throw new RuntimeException("Cannot retrieve the project build "
          + "directory.");
    }

    ContextPathBuilder.build();

    server = new TestingServer(env);
    server.start();

    client = new TestingClient(server, BrowserVersion.FIREFOX_3);
  }
}
