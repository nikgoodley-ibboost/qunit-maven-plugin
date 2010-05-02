package org.moyrax.maven;

import java.io.File;
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
import org.moyrax.javascript.ContextPathBuilder;
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
   * @parameter
   */
  private Integer port;

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
  private static TestDriverServer server;

  /**
   * Testing client.
   */
  private static TestDriverClient client;

  /**
   * Executes this plugin when the build reached the defined phase and goal.
   */
  public void execute() throws MojoExecutionException {
    initServer();
    initClient();

    // TODO(mmirabelli): The next code no longer works. Please see the
    // JsTestDriverTest class for more information in order to fix this method.
    final String[] testFiles = fileSetManager.getIncludedFiles(testResources);
    final String directory = testResources.getDirectory();

    this.context = HtmlUnitContextFactory.getGlobal().enterContext();
    this.scope = this.context.initStandardObjects();

    ContextPathBuilder.build(contextPath);

    this.loadContextResources();

    for (int i = 0, j = testFiles.length; i < j; i++) {
      final File file = new File(directory + "/" + testFiles[i]);

      ScriptUtils.run(context, scope, file);
    }
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
   *
   * @throws MojoExecutionException
   */
  private void loadContextResources() throws MojoExecutionException {
    final String[] dependencies = new String[] {
      /* QUnit testing framework. */
      "org/moyrax/javascript/lib/qunit.js",
      /* QUnit plugin related objects. */
      "org/moyrax/javascript/lib/qunit-objects.js",
      /* QUnit plugin test handler. */
      "org/moyrax/javascript/lib/qunit-plugin.js"
    };

    for (int i = 0; i < dependencies.length; i++) {
      ScriptUtils.run(context, scope, dependencies[i]);
    }
  }

  /**
   * Initializes the environment configuration and starts the testing server.
   */
  private void initServer() {
    if (server != null) {
      return;
    }

    EnvironmentConfiguration env = new EnvironmentConfiguration();

    env.setServerPort(port);

    server = new TestDriverServer(env);

    server.start();
  }

  /**
   * Initializes the testing client.
   */
  private void initClient() {
    if (server == null) {
      throw new IllegalStateException("The server is not initialized.");
    }

    client = new TestDriverClient(server, BrowserVersion.FIREFOX_3);

    client.setFiles(testResources.getDirectory(),
        fileSetManager.getIncludedFiles(testResources),
        fileSetManager.getExcludedFiles(testResources));
  }
}
