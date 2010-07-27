package org.moyrax.javascript.qunit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.moyrax.reporting.TestCase;
import org.moyrax.reporting.TestSuite;

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;

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

  public void reportAll() {
    for (TestHandler handler : handlers) {
      reporterManager.init(handler);

      reporterManager.info(StringUtils.leftPad("", 80, ">"));
      reporterManager.info("Executing " + handler.getTestFile().getName());
      reporterManager.info(StringUtils.leftPad("", 60, "-"));

      for (TestSuite module : handler.getModules()) {
        reporterManager.moduleStart(module);

        for (TestCase test : module.getTests()) {
          reporterManager.testStart(test);

          String[] lines = test.getOutput().split("\n");

          for (String line : lines) {
            reporterManager.info("\t\t" + line);
          }

          reporterManager.testCompleted(test);
        }

        reporterManager.moduleCompleted(module);
        reporterManager.info(StringUtils.leftPad("", 60, "-"));
      }

      reporterManager.done(handler);
    }
  }

  public void run(final InputStream resource) throws IOException,
  ScriptException {
    run(resource, null);
  }

  public void run(final InputStream resource, final String name)
  throws IOException, ScriptException {

    Validate.notNull(resource, "The resource cannot be null.");

    File file;

    if (name != null && !name.isEmpty()) {
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

  public void run(final File file) throws IOException, ScriptException {
    TestHandler handler = new TestHandler(client, file);

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
