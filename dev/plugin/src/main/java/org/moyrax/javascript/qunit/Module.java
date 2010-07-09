package org.moyrax.javascript.qunit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;

import org.apache.commons.lang.Validate;


/**
 * This class represents a QUnit testing module.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class Module {
  /** Timestamp to calculate the total time. */
  private long startTime = new Date().getTime();

  /**
   * Name of this module.
   */
  private String name;

  /**
   * Number of milliseconds the test execution took.
   */
  private float totalTime;

  /**
   * List of tests that this module contains.
   */
  private HashMap<String, TestCase> tests = new HashMap<String, TestCase>();

  /** Default constructor. Required by Rhino. */
  public Module() {}

  /**
   * Creates a new QUnit test module.
   *
   * @param moduleName Name for this module. It cannot be null or empty.
   */
  public Module(final String moduleName) {
    Validate.notEmpty(moduleName, "The module name cannot be null or empty.");

    this.name = moduleName;
  }

  /**
   * Adds a new test to this module.
   *
   * @param aTest {QUnitTest} Test to be added in this module. It cannot be
   *    null.
   */
  public void addTest (final TestCase aTest) {
    Validate.notNull(aTest, "The test cannot be null.");

    if (tests.containsKey(aTest.getName())) {
      throw new JavaScriptException(this, "The test '" + aTest.getName() +
          "' already exists in this module.", 0);
    }

    aTest.setModule(this);

    tests.put(aTest.getName(), aTest);
  }

  /**
   * Returns a test by its name.
   *
   * @param name {String} Required test name.
   * @return Returns the required QUnitTest, or null if it does not exists in
   *    this module.
   */
  public TestCase getTestByName (final String name) {
    if (tests.containsKey(name)) {
      return tests.get(name);
    }

    return null;
  }

  /**
   * Returns a list with all tests in this module.
   */
  public List<TestCase> getTests() {
    return new ArrayList<TestCase>(tests.values());
  }

  /**
   * Determines if a test exists in this module.
   *
   * @param name {String} Name of the test to check.
   * @return Returns <code>true</code> if the test exists, false otherwise.
   */
  public boolean hasTest(final String name) {
    return tests.containsKey(name);
  }

  public boolean hasFailed() {
    return getFailed().size() > 0;
  }

  /**
   * Retrieves the list of failed tests.
   *
   * @return A list of failed tests, or an empty list if there're no test
   *    failure.
   */
  public List<TestCase> getFailed() {
    ArrayList<TestCase> failed = new ArrayList<TestCase>();

    for (TestCase test : tests.values()) {
      if (!test.isSuccess()) {
        failed.add(test);
      }
    }

    return failed;
  }

  /**
   * Notifies that this module is done.
   */
  public void done () {
    this.totalTime = new Date().getTime() - startTime;
  }

  /**
   * Returns the name of this module.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the number of tests inside the module which failed.
   */
  public int getFailures() {
    return getFailed().size();
  }

  /**
   * Returns the number of tests assertions inside the module.
   */
  public int getTotal() {
    return tests.size();
  }

  /**
   * Returns the number of milliseconds the test execution took.
   */
  public float getTotalTime() {
    return totalTime;
  }

  public String getClassName() {
    return "QUnitModule";
  }
}
