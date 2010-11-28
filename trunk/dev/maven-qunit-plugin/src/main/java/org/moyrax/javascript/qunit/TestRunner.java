package org.moyrax.javascript.qunit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.moyrax.reporting.TestCase;
import org.moyrax.reporting.TestSuite;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Runs a set of qunit tests and displays the results.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2.1
 */
public class TestRunner {

  /**
   * Reporter manager used to write the tests output.
   */
  private QUnitReporter reporterManager;

  /**
   * Tests handlers containing the results.
   */
  private List<TestHandler> handlers = new ArrayList<TestHandler>();

  /**
   * Container for running tests.
   */
  private WebClient client;

  /** Creates a new {@link TestRunner} and uses the given client as the
   * container for running tests.
   *
   * @param theReporter The reporter manager to write results. It cannot be
   * null.
   * @param theClient Web client to use. It cannot be null.
   */
  public TestRunner(final QUnitReporter theReporterManager,
      final WebClient theClient) {

    Validate.notNull(theReporterManager, "The reporter manager cannot be"
        + " null.");
    Validate.notNull(theClient, "The client cannot be null.");

    reporterManager = theReporterManager;
    client = theClient;
  }

  /**
   * Reports the tests results using the configured QUnitReporter.
   * The results will be available once the tests execution is completed.
   */
  public void reportAll() {
    for (TestHandler handler : handlers) {
      reporterManager.init(handler);

      for (TestSuite module : handler.getModules()) {
        reporterManager.moduleStart(module);

        if (module.getTests().size() == 0) {
          reporterManager.info("There are no tests to run.");
        } else {
          for (TestCase test : module.getTests()) {
            reporterManager.testStart(test);
            reporterManager.testCompleted(test);
          }
        }

        reporterManager.moduleCompleted(module);
      }

      reporterManager.done(handler);
    }
  }

  /**
   * Runs the JavaScript source contained by the specified InputStream.
   *
   * @param resource InputStream to read the JavaScript source. It cannot be
   *    null.
   */
  public void run(final InputStream resource) throws IOException {
    run(resource, null);
  }

  /**
   * Runs the JavaScript source contained by the specified InputStream.
   *
   * @param resource InputStream to read the JavaScript source. It cannot be
   *    null.
   * @param name Name of the test that's being run. It can be null.
   */
  public void run(final InputStream resource, final String name)
      throws IOException {

    Validate.notNull(resource, "The resource cannot be null.");

    File file;

    if (name != null && !StringUtils.isBlank(name)) {
      String fileName = name;

      if (name.contains(File.separator)) {
        fileName = StringUtils.substringAfterLast(name, File.separator);
      }

      file = new File(System.getProperty("java.io.tmpdir"), fileName);
    } else {
      file = File.createTempFile("qunit", "test");
    }

    FileOutputStream output = new FileOutputStream(file);

    try {
      IOUtils.copy(resource, output);

      run(file);
    } finally {
      output.close();
      file.delete();
    }
  }

  /**
   * Runs all tests in the specified JavaScript source file.
   *
   * @param file JavaScript source file to execute. It cannot be null.
   */
  public void run(final File file) throws IOException {
    Validate.notNull(file, "The file cannot be null.");

    TestHandler handler = new TestHandler(client, file);

    handlers.add(handler);

    handler.run();
  }

  /**
   * Runs all tests in the specified JavaScript resource.
   *
   * @param url Url where the JavaSript resource is located. It cannot be null.
   */
  public void run(final URL url) throws IOException {
    Validate.notNull(url, "The url cannot be null.");

    TestHandler handler = new TestHandler(client, url);

    handlers.add(handler);

    handler.run();
  }

  /**
   * @return Returns the reporter manager configured for this runner.
   */
  public QUnitReporter getReporterManager() {
    return reporterManager;
  }

  /**
   * @return Returns the configured container for running tests.
   */
  public WebClient getClient() {
    return client;
  }
}
