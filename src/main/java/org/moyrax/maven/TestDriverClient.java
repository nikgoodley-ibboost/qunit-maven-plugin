package org.moyrax.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.moyrax.javascript.ConfigurableEngine;
import org.moyrax.javascript.ContextPathBuilder;
import org.moyrax.javascript.Shell;
import org.moyrax.javascript.shell.Global;

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
public class TestDriverClient {
  /** Default logger for this class. */
  @SuppressWarnings("unused")
  private static final Log logger = LogFactory.getLog(TestDriverClient.class);

  /**
   * Browser which will be connected to the testing server.
   */
  private WebClient browser;

  /**
   * Testing server used to execute the configured tests.
   */
  private TestDriverServer server;

  /**
   * Context configuration for this client.
   */
  private EnvironmentConfiguration context;

  /**
   * List of included testing resources to be executed.
   */
  private String[] includes;

  /**
   * List of excluded testing resources to be executed.
   */
  private String[] excludes;

  /**
   * Configuration file.
   */
  private File configFile;

  /**
   * Creates a new client which will be captured by the specified server.
   *
   * @param server Testing server. It cannot be null.
   */
  public TestDriverClient(final TestDriverServer server) {
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
   * @param version Browser which this instance will emulate.
   */
  public TestDriverClient(final TestDriverServer server,
      final BrowserVersion version) {

    Validate.notNull(server, "The server parameter cannot be null.");

    this.server = server;
    this.context = this.server.getContext();
    this.browser = this.createWebClient(version);

    this.browser.setJavaScriptEngine(this.createJavaScriptEngine());
  }

  /**
   * Executes all configured tests.
   */
  public void runTests() {
    if (this.context.getConfigFile() == null) {
      generateConfig();

      this.context.setConfigFile(this.configFile.getName());
    }

    ContextPathBuilder.build();

    browser.openWindow(this.getResourceUrl(), "Test");

    this.configFile.delete();
  }

  /**
   * Sets the list of patterns to locate testing resources. All resources that
   * matches the patterns will be executed. It will be used if no configuration
   * file was found.
   *
   * @param theBaseDirectory Directory in which the resources are located.
   * @param theIncludes List of included test resources.
   * @param theExcludes List of excluded test resources.
   */
  public void setFiles(final String theBaseDirectory,
      final String[] theIncludes, final String[] theExcludes) {

    this.includes = theIncludes;
    this.excludes = theExcludes;

    ContextPathBuilder.addDefinition(theBaseDirectory, theIncludes,
        theExcludes);
  }

  /**
   * Returns the current list of patterns to locate testing resources.
   */
  public String[] getIncludes() {
    return this.includes;
  }

  /**
   * Returns the list of patterns to exclude from the tests execution.
   */
  public String[] getExcludes() {
    return this.excludes;
  }

  /**
   * Creates a configuration file and saves it in the current context.
   */
  private void generateConfig() {
    StringBuilder builder = new StringBuilder();

    if (this.includes != null) {
      builder.append("load:\n");
      builder.append(formatConfigList(this.includes));
    }

    if (this.excludes != null && this.excludes.length > 0) {
      builder.append("exclude:\n");
      builder.append(formatConfigList(this.excludes));
    }

    try {
      this.configFile = new File("qunit-tests.conf");

      FileUtils.writeStringToFile(this.configFile, builder.toString());
    } catch (IOException e) {
      throw new IllegalArgumentException("Can't write the configuration file.");
    }
  }

  /**
   * Returns a String with the a format that can be readed by the server.
   *
   * @param list List to be converted.
   */
  private String formatConfigList(final String[] list) {
    String result = "";

    for (int i = 0; i < list.length; i++) {
      result += "  - " + list[i] + "\n";
    }

    return result;
  }

  /**
   * Creates and returns the url of the resource to be tested.
   *
   * @return Returns the built {@link URL}.
   * @throws IllegalArgumentException if the working string is not a valid URL.
   */
  private URL getResourceUrl() {
    // TODO(mmirabelli) Allow configurable urls.
    String captureUrl = this.context.getLocalUrl() + "/content/?resource=" +
        "classpath:/org/moyrax/javascript/test.html";

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
    final ConfigurableEngine engine = new ConfigurableEngine(this.browser);

    engine.registerClass(Global.class);
    engine.registerClass(Shell.class);

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
}
