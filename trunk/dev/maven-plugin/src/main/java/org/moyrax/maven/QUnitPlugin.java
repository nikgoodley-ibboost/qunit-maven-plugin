/* vim: set ts=2 et sw=2 cindent fo=qroca: */

package org.moyrax.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.moyrax.javascript.qunit.QUnitReporter;
import org.moyrax.javascript.qunit.TestRunner;
import org.moyrax.resolver.ClassPathResolver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

/** Runs a qunit based test.
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
  private QUnitReporter reporter;

  /**
   * Container for running tests.
   *
   * TODO Make the browser type configurable.
   */
  private WebClient browser = new WebClient(BrowserVersion.FIREFOX_3);

  /**
   * Testing runner.
   */
  private TestRunner runner;
  
  /** The Maven project object, used to generate a classloader to access the
   * classpath resources from the project.
   *
   * Injected by maven. This is never null.
   *
   * @parameter expression="${project}" @readonly
   */
  private MavenProject project;
  
  /**
   * Executes this plugin when the build reached the defined phase and goal.
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    // Should have been injected by maven. Checked here as this is the entry
    // point of the module.
    Validate.notNull(project, "The project cannot be null.");

    reporter = new QUnitReporter(getReportsDirectory(),
        new MojoLogAdapter(getLog()));

    runner = new TestRunner(reporter, browser);
    initEnvironment();
    loadContextResources();

    try {
      client.runTests();
    } catch (Exception ex) {
      throw new MojoFailureException(ex.getMessage(), ex);
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

    URLClassLoader projectClassLoader = createProjectClassloader(project);
    ClassPathResolver resolver = new ClassPathResolver(projectClassLoader);
    client = new TestingClient(runner, env, resolver);
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
  
  /** Creates the classloader to load resources from the project under test.
   *
   * @param theProject The maven project under test.
   *
   * @return a classloader initialized from the provided maven project, never
   * null
   */
  @SuppressWarnings("unchecked")
  private URLClassLoader createProjectClassloader(
      final MavenProject theProject) {
    List runtimeClasspathElements;
    try {
      runtimeClasspathElements = theProject.getRuntimeClasspathElements();
    } catch (DependencyResolutionRequiredException e) {
      throw new RuntimeException(e);
    }
    URL[] runtimeUrls = new URL[runtimeClasspathElements.size()];
    for (int i = 0; i < runtimeClasspathElements.size(); i++) {
      String element = (String) runtimeClasspathElements.get(i);
      try {
        runtimeUrls[i] = new File(element).toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    URLClassLoader newLoader = new URLClassLoader(runtimeUrls,
        Thread.currentThread().getContextClassLoader());
    return newLoader;
  }
}

