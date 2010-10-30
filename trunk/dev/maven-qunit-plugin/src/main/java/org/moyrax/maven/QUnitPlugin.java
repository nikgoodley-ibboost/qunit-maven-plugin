/* vim: set ts=2 et sw=2 cindent fo=qroca: */

package org.moyrax.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.moyrax.javascript.ContextClassLoader;
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
   * Local files which will be executed using QUnit.
   *
   * @parameter
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
   * URLs from whence remote tests are executed.
   *
   * @parameter
   */
  private UrlSet remoteTestResources;

  /**
   * List of classpath urls in which lookup for exportable Java classes.
   *
   * @parameter
   */
  private List<String> components = new ArrayList<String>();

  /**
   * List of classpath urls in which lookup for exportable Java classes.
   *
   * @parameter expression="${skipTests}" default-value="false"
   */
  private Boolean skipTests = false;

  /**
   * List of classpath urls in which lookup for exportable Java classes.
   *
   * @parameter expression="${maven.test.skip}" default-value="false"
   */
  private Boolean oldSkipTests = false;

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
   * @component
   */
  private ArtifactResolver artifactResolver;

  /**
   * Used to build the list of artifacts from the project's dependencies.
   *
   * @component
   */
  private ArtifactFactory artifactFactory;

  /**
   * Provides some metadata operations, like querying the remote repository for
   * a list of versions available for an artifact.
   *
   * @component
   */
  private ArtifactMetadataSource metadataSource;

  /**
   * Specifies the repository used for artifact handling.
   *
   * @parameter expression="${localRepository}"
   */
  private ArtifactRepository localRepository;

  /**
   * Executes this plugin when the build reached the defined phase and goal.
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    if (skipTests || oldSkipTests) {
      getLog().info("Tests are skipped.");
      return;
    }

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

    if (testResources != null) {
      env.setFiles(testResources.getDirectory(),
          fileSetManager.getIncludedFiles(testResources),
          fileSetManager.getExcludedFiles(testResources));
    }

    if (remoteTestResources != null) {
      env.setUrls(remoteTestResources.getBaseUrl(),
          remoteTestResources.getUrlFiles());
    }

    env.setLookupPackages(components.toArray(new String[] {}));

    if (contextPath != null) {
      for (Entry entry : contextPath) {
        ContextPathBuilder.addDefinition(entry.files.getDirectory(),
            entry.files.getIncludesArray(), entry.files.getExcludesArray());
      }
    }

    ContextPathBuilder.build();

    ClassLoader projectClassLoader = createProjectClassloader(project);
    ClassPathResolver resolver = new ClassPathResolver(projectClassLoader);

    env.setClassLoader(projectClassLoader);

    client = new TestingClient(runner, env, resolver);
  }

  /**
   * Returns the directory where the reports will be written.
   */
  private String getReportsDirectory() {
    File directory = new File(project.getBuild().getOutputDirectory(),
      "qunit-reports");

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
  private ClassLoader createProjectClassloader(
      final MavenProject theProject) {

    List testClasspathElements;

    try {
      testClasspathElements = theProject.getTestClasspathElements();
    } catch (DependencyResolutionRequiredException e) {
      throw new RuntimeException(e);
    }

    URL[] testUrls = new URL[testClasspathElements.size()];

    for (int i = 0; i < testClasspathElements.size(); i++) {
      String element = (String) testClasspathElements.get(i);
      try {
        testUrls[i] = new File(element).toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }

    ClassLoader depsClassLoader = createDependenciesClassLoader(theProject);

    URLClassLoader newLoader = new URLClassLoader(testUrls, depsClassLoader);

    return new ContextClassLoader(newLoader);
  }

  /**
   * Creates a {@link ClassLoader} which contains all the project's
   * dependencies.
   *
   * @param theProject The maven project under test. It cannot be null.
   *
   * @return Returns the created {@link ClassLoader} containing all the
   *    project's dependencies.
   */
  @SuppressWarnings("unchecked")
  private ClassLoader createDependenciesClassLoader(
      final MavenProject theProject) {

    Validate.notNull(theProject, "The project cannot be null.");

    // Make Artifacts of all the dependencies.
    Set<Artifact> dependencyArtifacts;

    try {
      dependencyArtifacts = MavenMetadataSource.createArtifacts(
          artifactFactory, theProject.getDependencies(), null, null, null );
    } catch (InvalidDependencyVersionException ex) {
      throw new RuntimeException("Cannot resolve dependencies version.", ex);
    }

    // Resolves all dependencies transitively to obtain a comprehensive list
    // of jars.
    ArtifactResolutionResult result;

    try {
      result = artifactResolver.resolveTransitively(
          dependencyArtifacts,
          theProject.getArtifact(),
          Collections.EMPTY_LIST,
          localRepository,
          metadataSource);
    } catch (ArtifactResolutionException ex) {
      throw new RuntimeException("Cannot resolve the artifact.", ex);
    } catch (ArtifactNotFoundException ex) {
      throw new RuntimeException("Artifact not found in the local"
          + " repository.", ex);
    }

    // Retrieves the filesystem path of each dependency jar.
    Set<Artifact> artifacts = result.getArtifacts();

    URL[] urls = new URL[artifacts.size()];

    int i = 0;

    for (Artifact artifact : artifacts) {
      try {
        urls[i++] = artifact.getFile().toURI().toURL();
      } catch (MalformedURLException ex) {
        throw new RuntimeException("Cannot resolve the artifact path.", ex);
      }
    }

    URLClassLoader newLoader = new URLClassLoader(urls,
        Thread.currentThread().getContextClassLoader());

    return newLoader;
  }
}

