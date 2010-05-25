package org.moyrax.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.moyrax.util.ScriptUtils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.javascript.HtmlUnitContextFactory;

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
   * Global engine context.
   */
  private Context context;

  /**
   * Shared scope.
   */
  private Scriptable scope;

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
   * Executes the specified file as a QUnit test.
   *
   * @param scriptFile  File to be executed as the test. It cannot be null.
   *
   * @throws MojoExecutionException
   */
  public void execute(final File scriptFile) throws MojoExecutionException {
    Validate.notNull(scriptFile, "scriptFile cannot be null.");

    this.context = HtmlUnitContextFactory.getGlobal().enterContext();
    this.scope = this.context.initStandardObjects();

    this.loadContextResources();

    ScriptUtils.run(context, scope, scriptFile);
  }

  /**
   * Executes the specified resource from the classpath.
   *
   * @param classPath Classpath resource to be executed as QUnit test. It cannot
   *    be null or empty.
   *
   * @throws MojoExecutionException
   */
  public void execute(final String classPath) throws MojoExecutionException {
    Validate.notEmpty(classPath, "classPath cannot be null or empty.");

    this.context = HtmlUnitContextFactory.getGlobal().enterContext();
    this.scope = this.context.initStandardObjects();

    this.loadContextResources();

    String resourcePath = classPath;

    if (classPath.startsWith("classpath:/")) {
      resourcePath = StringUtils.substringAfter(classPath, "classpath:/");
    }

    if (resourcePath.startsWith("/")) {
      resourcePath = StringUtils.substringAfter(resourcePath, "/");
    }

    ScriptUtils.run(context, scope, resourcePath);
  }

  /**
   * Initializes the required resources for the test environment.
   */
  private void loadContextResources() {
    final String[] dependencies = new String[] {
      /* QUnit testing framework. */
      "org/moyrax/javascript/lib/qunit.js",
      /* QUnit plugin related objects. */
      "org/moyrax/javascript/lib/qunit-objects.js",
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

      for (Entry entry : contextPath) {
        // TODO(mmirabelli): append instead of replace the lookup packages.
        env.setLookupPackages(entry.components.toArray(new String[] {}));
      }

      env.setProjectBasePath(projectBasePath.toURI().toURL());
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
