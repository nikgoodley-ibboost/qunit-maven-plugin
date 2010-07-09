package org.moyrax.maven;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.moyrax.javascript.ConfigurableEngine;
import org.moyrax.javascript.ScriptComponentScanner;
import org.moyrax.javascript.Shell;
import org.moyrax.javascript.qunit.TestRunner;
import org.moyrax.javascript.shell.Global;
import org.moyrax.resolver.ClassPathResolver;
import org.moyrax.resolver.LibraryResolver;
import org.moyrax.util.ResourceUtils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptException;

/**
 * This class uses HTMLUnit to initialize a browser environment which will be
 * used to be captured by the js-test-driver server.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
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

  /**
   * JavaScript engine used to execute the tests.
   */
  private ConfigurableEngine engine;

  /**
   * Creates a new client which uses the specified runner to run tests.
   *
   * @param theRunner Test runner. It cannot be null.
   * @param theContext Testing context configuration. It cannot be null.
   */
  public TestingClient(final TestRunner theRunner,
      final EnvironmentConfiguration theContext) {
    this(theRunner, theContext, BrowserVersion.getDefault());
  }

  /**
   * Creates a new client which uses the specified runner to run tests, and
   * emulates the specified browser version.
   *
   * @param theRunner Test runner. It cannot be null.
   * @param theContext Testing context configuration. It cannot be null.
   * @param version Browser which this instance will emulate. It cannot be null.
   */
  public TestingClient(final TestRunner theRunner,
      final EnvironmentConfiguration theContext, final BrowserVersion version) {

    Validate.notNull(theRunner, "The test runner cannot be null.");
    Validate.notNull(theContext, "The context cannot be null.");
    Validate.notNull(version, "The browser version cannot be null.");

    runner = theRunner;
    context = theContext;

    configureWebClient();
    setUpJavaScriptEngine();
  }

  /**
   * Executes all configured tests.
   */
  public void runTests() throws ScriptException {
    try {
      this.loadClientComponents();
    } catch (MalformedURLException ex) {
      
    }

    String[] includes = context.getIncludes();
    String basePath = context.getBaseDirectory();

    for (String include : includes) {
      String resource = basePath + include;

      try {
        runner.run(ResourceUtils.getResourceInputStream(
            resource));
      } catch (IOException ex) {
        runner.getReporterManager().info("Error reading test resource: "
            + resource);
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
  public void addGlobalResource(final String classPath) {
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
    Shell.setResolver("classpath", new ClassPathResolver());

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
  private void loadClientComponents() throws MalformedURLException {
    ScriptComponentScanner scanner = new ScriptComponentScanner(
        context.getLookupPackages(), context.getClassLoader());

    scanner.scan();

    for (Class<?> clazz : scanner.getClasses()) {
      engine.registerClass(clazz, this.context.getClassLoader());
    }
  }
}
