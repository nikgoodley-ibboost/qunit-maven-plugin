package org.moyrax.javascript;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.google.jstestdriver.JsTestDriverServer;

/**
 * This class uses HTMLUnit to initialize a browser environment which will be
 * used to be captured by the js-test-driver server.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 */
public class TestDriverClient {
  /** Default logger for this class. */
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
  private TestContext context;

  /**
   * Is this browser binded to the server?
   */
  private boolean captured;

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
   * This semaphore waits for client-server operations.
   */
  private Semaphore semaphore;

  /**
   * Creates a new client which will be captured by the specified server.
   *
   * @param server Testing server. It cannot be null.
   */
  public TestDriverClient(final TestDriverServer server) {
    Validate.notNull(server, "The server parameter cannot be null.");

    this.server = server;
    this.context = this.server.getContext();
    this.browser = new WebClient();
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
    this.browser = new WebClient(version);
  }

  /**
   * Binds this client to the configured server. If this client is already
   * binded to the server, this method will throws an exception.
   *
   * @param semaphore Semaphore to wait for the capture be already done.
   */
  public void capture(final Semaphore semaphore) {
    this.semaphore = semaphore;

    this.capture();
  }

  /**
   * Binds this client to the configured server. If this client is already
   * binded to the server, this method will throws an exception.
   */
  public void capture() {
    if (this.captured) {
      throw new IllegalStateException("This client is already binded to " +
          "the server.");
    }

    try {
      browser.setRedirectEnabled(true);
      browser.setJavaScriptEnabled(true);

      browser.getPage(this.getCaptureUrl());

      this.captured = true;

      if (this.semaphore != null) {
        semaphore.release();
      }
    } catch (MalformedURLException ex) {
      logger.error("Error connecting to the testing server.");
    } catch (IOException ex) {
      logger.error("Connection refused by the testing server.");
    }
  }

  /**
   * Disconnects this client from the testing server.
   */
  public void release() {
    JsTestDriverServer.main(new String[] {
      "--reset", "--server", this.context.getLocalUrl()
    });

    this.captured = false;
  }

  /**
   * Executes all configured tests.
   */
  public void runTests() {
    if (!this.captured) {
      throw new IllegalStateException("The client is not connected to the" +
          "testing server.");
    }

    if (this.context.getConfigFile() == null) {
      generateConfig();

      this.context.setConfigFile(this.configFile.getName());
    }

    final Thread client = new Thread() {
      @Override
      public void run() {
        this.setName("QUnit Testing Client Thread");

        JsTestDriverServer.main(context.getClientParameters());
      }
    };

    try {
      client.start();
      client.join();
    } catch(InterruptedException ex) {}

    this.configFile.delete();
  }

  /**
   * Returns the current list of patterns to locate testing resources.
   */
  public String[] getIncludes() {
    return this.includes;
  }

  /**
   * Sets the list of patterns to locate testing resources. All resources that
   * matches the patterns will be executed. It will be used if no configuration
   * file was found.
   *
   * @param includes List of test resources.
   */
  public void setIncludes(final String[] includes) {
    this.includes = includes;
  }

  /**
   * Returns the list of patterns to exclude from the tests execution.
   */
  public String[] getExcludes() {
    return this.excludes;
  }

  /**
   * Sets the list of patterns to exclude from the tests execution. It will be
   * used if no configuration file was found.
   *
   * @param excludes List of test resources.
   */
  public void setExcludes(final String[] excludes) {
    this.excludes = excludes;
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

  private URL getCaptureUrl() throws MalformedURLException {
    final BrowserVersion versionInfo = this.browser.getBrowserVersion();

    String captureUrl = this.context.getLocalUrl() + "/capture";

    captureUrl += "?version=" + versionInfo.getApplicationVersion();
    captureUrl += "&os=" + versionInfo.getPlatform();

    return new URL(captureUrl);
  }
}
