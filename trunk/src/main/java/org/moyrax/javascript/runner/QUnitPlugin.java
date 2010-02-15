package org.moyrax.javascript.runner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

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
  private FileSet contextPath;

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
   * Executes this plugin when the build reached the defined phase and goal.
   */
  public void execute() throws MojoExecutionException {
    final String[] testFiles = fileSetManager.getIncludedFiles(testResources);
    final String directory = testResources.getDirectory();

    this.initContext();
    this.loadContextResources();

    for (int i = 0, j = testFiles.length; i < j; i++) {
      final File file = new File(directory + "/" + testFiles[i]);

      this.run(file);
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

    this.initContext(this.contextPath == null);
    this.loadContextResources();
    this.run(scriptFile);
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
    Validate.notNull(classPath, "classPath cannot be null or empty.");
    Validate.notEmpty(classPath, "classPath cannot be null or empty.");

    this.initContext(this.contextPath == null);
    this.loadContextResources();

    String resourcePath = classPath;

    if (classPath.startsWith("classpath:/")) {
      resourcePath = StringUtils.substringAfter(classPath, "classpath:/");
    }

    if (resourcePath.startsWith("/")) {
      resourcePath = StringUtils.substringAfter(resourcePath, "/");
    }

    this.run(resourcePath);
  }

  /**
   * Sets the context path for this plugin. It can be used to force the context
   * path instead of take it from the POM configuration.
   *
   * @param baseDirectory  Base directory.
   * @param includes       List of included directories in the context.
   * @param excludes       List of excluded directories from the context.
   */
  public void defineContextPath(final String baseDirectory,
      final String[] includes, final String[] excludes) {
    final FileSet contextPath = new FileSet();

    contextPath.setDirectory(baseDirectory);

    for (int i = 0, j = includes.length; i < j; i++) {
      contextPath.addInclude(includes[i]);
    }

    for (int i = 0, j = excludes.length; i < j; i++) {
      contextPath.addExclude(excludes[i]);
    }

    this.contextPath = contextPath;
  }

  /**
   * Executes the specified reader.
   *
   * @param file Script file to be executed. It cannot be null.
   *
   * @return Returns the result of the script execution.
   * @throws Exception
   */
  private Object run(final File file) throws MojoExecutionException {
    Validate.notNull(file, "file cannot be null.");
    try {
      final Reader reader = new FileReader(file);

      return this.run(reader, file.getName());
    } catch (IOException ex) {
      throw new MojoExecutionException("Error opening input file.", ex);
    }
  }

  /**
   * Executes the script from a classpath resource.
   *
   * @param classPath Class path of the resource to be executed.
   *
   * @return Returns the result of the script execution.
   * @throws MojoExecutionException
   */
  private Object run (final String classPath) throws MojoExecutionException {
    /* Retrieves the resource from the class path. */
    final InputStreamReader resource = new InputStreamReader(
        Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(classPath));

    return this.run(resource, StringUtils.substringAfterLast(classPath, "/"));
  }

  /**
   * Executes the script using the specified reader.
   *
   * @param reader Reader from the script will be readed. It cannot be null.
   *
   * @return Returns the result of the script execution.
   * @throws MojoExecutionException
   */
  private Object run(final Reader reader, final String name)
      throws MojoExecutionException {
    Validate.notNull(reader, "reader cannot be null.");

    try {
      /* Executes the script in the current context. */
      return context.evaluateReader(scope, reader, name, 1, null);
    } catch (Exception ex) {
      ex.printStackTrace();

      throw new MojoExecutionException("Script error.", ex);
    }
  }

  /**
   * Initializes the JavaScript engine context and the global environment.
   */
  private void initContext() {
    this.initContext(false);
  }

  /**
   * Initializes the JavaScript engine context and the global environment.
   *
   * @param standalone  If it's <code>true</code>, the maven's related
   *    environment will not be initialized.
   */
  private void initContext(final boolean standalone) {
    final Shell shell = new Shell();

    if (standalone == false) {
      makeContextPath();
    }

    /* Acquires the context and sets the default flags. */
    context = ContextFactory.getGlobal().enterContext();
    context.setOptimizationLevel(-1);
    context.setLanguageVersion(Context.VERSION_1_5);

    /* Initializes the shell functions in this context. */
    shell.init(context);

    /* Adds the extended shell function to the global scope. */
    String[] functionNames = { "include" };

    shell.defineFunctionProperties(functionNames,
       Shell.class, ScriptableObject.DONTENUM);

    /* Sets the global scope */
    scope = context.initStandardObjects(shell);
  }

  /**
   * Initializes the required resources for the test environment.
   *
   * @throws MojoExecutionException
   */
  private void loadContextResources() throws MojoExecutionException {
    final String[] dependencies = new String[] {
      /* QUnit testing framework. */
      "org/moyrax/javascript/qunit.js",
      /* QUnit plugin related objects. */
      "org/moyrax/javascript/qunit-objects.js",
      /* QUnit plugin test handler. */
      "org/moyrax/javascript/qunit-plugin.js"
    };

    for (int i = 0; i < dependencies.length; i++) {
      this.run(dependencies[i]);
    }
  }

  /**
   * Creates the context path from the values specified in the POM.
   */
  private void makeContextPath() {
    final String[] includeNames = fileSetManager.getIncludedDirectories(
        contextPath);
    final String[] excludeNames = fileSetManager.getExcludedDirectories(
        contextPath);
    final ArrayList<File> includes = new ArrayList<File>();
    final ArrayList<File> excludes = new ArrayList<File>();

    final String baseDir = contextPath.getDirectory();

    /* Adds the included directories. */
    for (int i = 0, j = includeNames.length; i < j; i++) {
      includes.add(new File(baseDir + includeNames[i]));
    }

    /* Adds the excludes directories. */
    for (int i = 0, j = excludeNames.length; i < j; i++) {
      excludes.add(new File(baseDir + excludeNames[i]));
    }

    /* Sets the context path for this scope. */
    Shell.setContextPath(
        includes.toArray(new File[] {}),
        excludes.toArray(new File[] {}));
  }
}
