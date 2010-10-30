package org.moyrax.javascript.qunit;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.moyrax.reporting.ReportEntry;
import org.moyrax.reporting.TestCase;
import org.moyrax.reporting.TestSuite;

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This class handles a single test file.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.1.2
 */
public class TestHandler extends ReportEntry {
  /** Default class logger. */
  private static final Log log = LogFactory.getLog(TestHandler.class);

  /* Positions of the known-matches */
  private static final int MODULE_NAME = 1;
  private static final int TEST_NAME = 2;
  private static final int NUM_FAILURES = 3;
  private static final int NUM_TESTS = 5;

  /**
   * Pattern to extract the total execution time.
   */
  private Pattern executionTimePattern = Pattern.compile(".*\\s([\\d]+)\\s.*");

  /**
   * Pattern to extract the tests summary.
   */
  private Pattern summary = Pattern.compile(
  "(.*:)?\\s*(.+) \\((.+), (.+), (.+)\\)");

  /**
   * Container to run the tests.
   */
  private WebClient browser;

  /**
   * URL containing the qunit tests.
   */
  private URL testUrl;

  /**
   * List of modules in the test file.
   */
  private HashMap<String, TestSuite> modules =
    new HashMap<String, TestSuite>();

  /**
   * Timestamp to calculate the amount of execution time.
   */
  private long startTime;

  /**
   * Amount of time the whole test file took to execute.
   */
  private long executionTime;

  /**
   * Number of failures.
   */
  private int failures;

  /**
   * Number of tests run.
   */
  private int total;

  /**
   * Constructs a new {@link TestHandler} for the specified file, and uses
   * the web client to run the tests.
   *
   * @param theBrowser The web client in which the tests will run. It cannot be
   *    null.
   * @param theTestFile QUnit test file. It cannot be null.
   * @throws java.net.MalformedURLException If the file path cannot be parsed as a URL
   */
  public TestHandler(final WebClient theBrowser, final File theTestFile)
      throws MalformedURLException {
    Validate.notNull(theBrowser, "The web client cannot be null.");
    Validate.notNull(theTestFile, "The test file cannot be null.");

    browser = theBrowser;
    testUrl = theTestFile.toURI().toURL();
  }

  /**
   * Constructs a new {@link TestHandler} and reads the tests from the specified
   * URL resource. It uses the web client to run the tests.
   *
   * @param theBrowser The web client in which the tests will run. It cannot be
   *    null.
   * @param theTestFile QUnit test file. It cannot be null.
   * @throws java.net.MalformedURLException If the file path cannot be parsed as a URL
   */
  public TestHandler(final WebClient theBrowser, final URL theTestUrl) {
    Validate.notNull(theBrowser, "The web client cannot be null.");
    Validate.notNull(theTestUrl, "The test URL cannot be null.");

    browser = theBrowser;
    testUrl = theTestUrl;
  }

  /**
   * Runs the test and keeps the results.
   *
   * @throws IOException If there're errors reading the test file.
   */
  public void run() throws IOException, ScriptException {
    try {
      startTime = new Date().getTime();

      browser.setAjaxController(new NicelyResynchronizingAjaxController());
      browser.setJavaScriptEnabled(true);

      HtmlPage page = browser.getPage(testUrl);

      // This is to avoid errors when background JS hasn't updated the DOM yet
      // while we're trying to access it.
      // See: http://htmlunit.sourceforge.net/faq.html
       browser.waitForBackgroundJavaScript(10000);

      readTests(page);
      readResults(page);

    } catch (IOException ex) {
      throw new IOException("Cannot run the test file.", ex);
    }
  }

  /**
   * Returns the time that the test file took to execute all tests.
   *
   * @return Return the time, in milliseconds.
   */
  public long getExecutionTime() {
    return executionTime;
  }

  /**
   * Returns the amount of failed tests in the file.
   *
   * @return The number of failed tests.
   */
  public int getFailures() {
    return failures;
  }

  /**
   * Returns the amount of tests executed.
   *
   * @return The number of tests executed.
   */
  public int getTotal() {
    return total;
  }

  /**
   * Returns an immutable list of modules executed in the file. If some tests
   * are executed out of a module, a default module will be created for them.
   *
   * @return The list of executed modules.
   */
  public List<TestSuite> getModules() {
    return new ArrayList<TestSuite>(modules.values());
  }

  /**
   * @return Returns the URL that contains the executed tests.
   */
  public URL getTestUrl() {
    return testUrl;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return StringUtils.substringBeforeLast(getSourceName(), ".");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSourceName() {
    return new File(getTestUrl().getFile()).getName();
  }

  /**
   * Reads the tests results from the browser output and creates the modules
   * and tests objects from it.
   *
   * @param page Page which contains the tests results. It cannot be null.
   */
  private void readTests(final HtmlPage page) {
    Validate.notNull(page, "The page cannot be null.");

    HtmlElement element = page.getHtmlElementById("qunit-tests");

    List<HtmlElement> testResults = element.getElementsByTagName("li");

    TestSuite currentModule = null;

    for (HtmlElement result : testResults) {
      Matcher matcher = summary.matcher(result.asText());

      List<HtmlElement> testOutput = result.getElementsByTagName("li");

      if (matcher.lookingAt()) {
        TestSuite module = getModule(matcher);
        TestCase test = buildTest(matcher);

        if (currentModule != module) {
          if (currentModule != null) {
            currentModule.done();
          }

          currentModule = module;
        }

        for (HtmlElement outputLine : testOutput) {
          test.print(outputLine.getFirstChild().asXml());
        }

        module.addTest(test);
      }
    }
  }

  /**
   * Read the summary from the browser output.
   *
   * @param page Page which contains the tests results. It cannot be null.
   */
  private void readResults(final HtmlPage page) {
    Validate.notNull(page, "The page cannot be null.");

    HtmlElement element = page.getHtmlElementById("qunit-testresult");
    List<HtmlElement> resultItems = element.getElementsByTagName("span");

    Matcher matcher = executionTimePattern.matcher(element.getFirstChild()
        .asXml());

    if (matcher.lookingAt()) {
      executionTime = Integer.parseInt(matcher.group(1));
    }

    for (HtmlElement resultItem : resultItems) {
      String className = resultItem.getAttribute("class");

      if (className.equals("failed")) {
        failures = Integer.valueOf(resultItem.asText().trim());
      } else if (className.equals("total")) {
        total = Integer.valueOf(resultItem.asText().trim());
      }
    }
  }

  /**
   * Creates a module from the group of strings which represents each field
   * of the module.
   *
   * @param matcher Matcher which contains the fields. It cannot be null.
   *
   * @return If the module already exists, return the existing module,
   *    otherwise returns the created module from the matcher's fields.
   */
  private TestSuite getModule(final Matcher matcher) {
    Validate.notNull(matcher, "The matcher cannot be null.");

    String moduleName = "default";

    if (matcher.group(MODULE_NAME) != null) {
      moduleName = matcher.group(MODULE_NAME).trim();
    }

    if (moduleName.endsWith(":")) {
      moduleName = StringUtils.substringBeforeLast(moduleName, ":");
    }

    if (!modules.containsKey(moduleName)) {
      modules.put(moduleName, new TestSuite(moduleName));
    }

    return modules.get(moduleName);
  }

  /**
   * Creates a new test from the data contained in matched fields.
   *
   * @param matcher Matcher which contains the test fields. It cannot be null.
   *
   * @return Return the created test.
   */
  private TestCase buildTest(final Matcher matcher) {
    Validate.notNull(matcher, "The matcher cannot be null.");

    if (matcher.group(TEST_NAME) == null) {
      throw new RuntimeException("The test name cannot be null.");
    }

    TestCase test = new TestCase(matcher.group(TEST_NAME));

    test.start(startTime);

    test.done(Integer.valueOf(matcher.group(NUM_TESTS)),
        Integer.valueOf(matcher.group(NUM_FAILURES)));

    return test;
  }
}
