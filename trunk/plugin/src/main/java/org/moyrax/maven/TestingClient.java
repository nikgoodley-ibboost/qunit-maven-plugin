package org.moyrax.maven;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.moyrax.javascript.ConfigurableEngine;
import org.moyrax.javascript.ScriptComponentScanner;
import org.moyrax.javascript.Shell;
import org.moyrax.javascript.shell.Global;
import org.moyrax.resolver.ClassPathResolver;
import org.moyrax.resolver.LibraryResolver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;

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
   * Browser which will be connected to the testing server.
   */
  private WebClient browser;

  /**
   * Testing server used to execute the configured tests.
   */
  private TestingServer server;

  /**
   * Context configuration for this client.
   */
  private EnvironmentConfiguration context;

  /**
   * JavaScript engine used to execute the tests.
   */
  private ConfigurableEngine engine;

  /**
   * Creates a new client which will be captured by the specified server.
   *
   * @param server Testing server. It cannot be null.
   */
  public TestingClient(final TestingServer server) {
    Validate.notNull(server, "The server parameter cannot be null.");

    this.server = server;
    this.context = this.server.getContext();
    this.browser = this.createWebClient();

    this.browser.setJavaScriptEngine(this.createJavaScriptEngine());
  }

  /**
   * Creates a new client which will be captured by the specified server,
   * and emulates the specified browser version.
   *
   * @param server Testing server. It cannot be null.
   * @param version Browser which this instance will emulate. It cannot be null.
   */
  public TestingClient(final TestingServer server,
      final BrowserVersion version) {

    Validate.notNull(server, "The server parameter cannot be null.");
    Validate.notNull(version, "The browser version cannot be null.");

    this.server = server;
    this.context = this.server.getContext();
    this.browser = this.createWebClient(version);

    this.browser.setJavaScriptEngine(this.createJavaScriptEngine());
  }

  /**
   * Executes all configured tests.
   */
  public void runTests() {
    try {
      this.loadClientComponents();
    } catch (MalformedURLException ex) {
      
    }

    String resource = "classpath:/org/moyrax/javascript/test.html";

    browser.openWindow(this.buildResourceUrl(resource), "Test");
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
   * Creates and returns the url of the resource to be tested.
   *
   * @param resourcePath The test file location. It cannot be null or empty.
   * @return Returns the built {@link URL}.
   * @throws IllegalArgumentException if the working string is not a valid URL.
   */
  private URL buildResourceUrl(final String resourcePath) {
    Validate.notEmpty(resourcePath, "The resource path cannot be null or "
        + "empty.");

    // TODO(mmirabelli) Allow configurable urls.
    String captureUrl = this.context.getLocalUrl() + "/content/?resource="
        + resourcePath;

    URL url;

    try {
      url = new URL(captureUrl);
    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException("The specified string is not a "
          + "valid URL.", ex);
    }

    return url;
  }

  /**
   * Creates and initializes a new {@link ConfigurableEngine}.
   *
   * @return Returns the created {@link ConfigurableEngine}.
   */
  private JavaScriptEngine createJavaScriptEngine() {
    engine = new ConfigurableEngine(this.browser);

    engine.registerClass(Global.class);
    engine.registerClass(Shell.class);

    Shell.setResolver("lib", new LibraryResolver("/org/moyrax/javascript/lib"));
    Shell.setResolver("classpath", new ClassPathResolver());

    return engine;
  }

  /**
   * Creates a new {@link WebClient} and sets the default options.
   *
   * @return Returns the new {@link WebClient} instance.
   */
  private WebClient createWebClient() {
    return createWebClient(BrowserVersion.FIREFOX_3);
  }

  /**
   * Creates a new {@link WebClient} and sets the default options.
   *
   * @param version The version of the browser which will be emulated by this
   *    client. It cannot be null.
   *
   * @return Returns the new {@link WebClient} instance.
   */
  private WebClient createWebClient(final BrowserVersion version) {
    Validate.notNull(version, "The version cannot be null.");

    WebClient client = new WebClient(version);

    client.setRedirectEnabled(true);
    client.setJavaScriptEnabled(true);

    return client;
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
