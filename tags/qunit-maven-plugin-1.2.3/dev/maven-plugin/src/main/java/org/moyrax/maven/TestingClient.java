/* vim: set ts=2 et sw=2 cindent fo=qroca: */

package org.moyrax.maven;

import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.moyrax.javascript.ConfigurableEngine;
import org.moyrax.javascript.ScriptComponentScanner;
import org.moyrax.javascript.Shell;
import org.moyrax.javascript.qunit.TestRunner;
import org.moyrax.javascript.shell.Global;
import org.moyrax.resolver.LibraryResolver;
import org.moyrax.resolver.ResourceResolver;
import org.moyrax.util.ResourceUtils;

import com.gargoylesoftware.htmlunit.ScriptException;

/**
 * This class uses HTMLUnit to initialize a browser environment which will be
 * used to be captured by the js-test-driver server.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 * @since 1.2.0
 */
public class TestingClient {
  /** Default logger for this class. */
  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(TestingClient.class);

  /**
   * Testing server used to execute the configured tests.
   */
  private TestRunner runner;

  /**
   * Context configuration for this client.
   */
  private EnvironmentConfiguration context;

  /** The resource resolver to use to search for javascript resources.
   *
   * This is never null.
   */
   private ResourceResolver resourceResolver;

  /**
   * JavaScript engine used to execute the tests.
   */
  private ConfigurableEngine engine;

  /**
   * Creates a new client which uses the specified runner to run tests, and
   * emulates the specified browser version.
   *
   * @param theClassLoader The classloader to load classpath: prefixed
   * resources. It cannot be null.
   *
   * @param theRunner Test runner. It cannot be null.
   * @param theContext Testing context configuration. It cannot be null.
   * @param version Browser which this instance will emulate. It cannot be null.
   */
  public TestingClient(final TestRunner theRunner,
      final EnvironmentConfiguration theContext,
      final ResourceResolver theResourceResolver) {

    Validate.notNull(theRunner, "The test runner cannot be null.");
    Validate.notNull(theContext, "The context cannot be null.");
    Validate.notNull(theResourceResolver, "The resolver cannot be null.");

    runner = theRunner;
    context = theContext;
    resourceResolver = theResourceResolver;

    configureWebClient();
    setUpJavaScriptEngine();
  }

  /**
   * Executes all configured tests.
   */
  public void runTests() throws ScriptException {
    this.loadClientComponents();

    String[] includes = context.getIncludes();
    String basePath = context.getBaseDirectory();

    for (String include : includes) {
      String resource = basePath + include;

      try {
        runner.run(ResourceUtils.getResourceInputStream(
            resource), include);
      } catch (IOException ex) {
        runner.getReporterManager().error("Error reading test resource: "
            + resource, ex);
      }
    }

    runner.reportAll();
  }

  /**
   * Adds a new resource which will be registered in the Window scope. It's
   * useful to initialize the client environment before executing the tests.
   *
   * @param classPath Resource located in the classpath. It cannot be null or
   *    empty.
   */
  protected void addGlobalResource(final String classPath) {
    Validate.notEmpty(classPath, "The resource classpath cannot be null.");

    engine.addGlobalResource(classPath);
  }

  /**
   * Creates and initializes a new {@link ConfigurableEngine}.
   *
   * @return Returns the created {@link ConfigurableEngine}.
   */
  private void setUpJavaScriptEngine() {
    engine = new ConfigurableEngine(this.runner.getClient());

    engine.registerClass(Global.class);
    engine.registerClass(Shell.class);

    Shell.setResolver("lib", new LibraryResolver("/org/moyrax/javascript/lib"));
    Shell.setResolver("classpath", resourceResolver);

    runner.getClient().setJavaScriptEngine(engine);
  }

  /**
   * Sets up the runner's web client configuration needed for this client.
   */
  private void configureWebClient() {
    runner.getClient().setRedirectEnabled(true);
    runner.getClient().setJavaScriptEnabled(true);
  }

  /**
   * Lookups the configured packages and loads the classes that will be
   * available in the client-side scripts.
   */
  private void loadClientComponents() {
    ScriptComponentScanner scanner = new ScriptComponentScanner(
        context.getLookupPackages(), context.getClassLoader());

    scanner.scan();

    for (Class<?> clazz : scanner.getClasses()) {
      engine.registerClass(clazz, this.context.getClassLoader());
    }
  }
}
