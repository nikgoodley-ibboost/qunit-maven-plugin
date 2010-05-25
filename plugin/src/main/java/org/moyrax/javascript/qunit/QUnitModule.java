package org.moyrax.javascript.qunit;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang.Validate;
import org.moyrax.javascript.JavaScriptEngineException;
import org.moyrax.javascript.annotation.Function;
import org.moyrax.javascript.annotation.Script;

/**
 * This class represents a QUnit testing module.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
@Script
public class QUnitModule {
  /** Timestamp to calculate the total time. */
  private long startTime = new Date().getTime();

  /**
   * Name of this module.
   */
  private String name;

  /**
   * Number of tests inside the module which failed.
   */
  private int failures;

  /**
   * Number of tests assertions inside the module.
   */
  private int total;

  /**
   * Number of milliseconds the test execution took.
   */
  private float totalTime;

  /**
   * List of tests that this module contains.
   */
  private HashMap<String, QUnitTest> tests = new HashMap<String, QUnitTest>();

  /** Default constructor. Required by Rhino. */
  public QUnitModule() {}

  /**
   * Creates a new QUnit test module.
   *
   * @param moduleName Name for this module. It cannot be null or empty.
   */
  public QUnitModule(final String moduleName) {
    Validate.notEmpty(moduleName, "The module name cannot be null or empty.");

    this.name = moduleName;
  }

  /**
   * Adds a new test to this module.
   *
   * @param aTest {QUnitTest} Test to be added in this module. It cannot be
   *    null.
   */
  @Function
  public void addTest (final QUnitTest aTest) {
    Validate.notNull(aTest, "The test cannot be null.");

    if (tests.containsKey(aTest.getName())) {
      throw new JavaScriptEngineException("The test '" + aTest.getName() +
          "' already exists in this module.");
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
  @Function
  public QUnitTest getTestByName (final String name) {
    if (tests.containsKey(name)) {
      return tests.get(name);
    }

    return null;
  }

  /**
   * Returns a list with all tests in this module.
   */
  @Function
  public Collection<QUnitTest> getTests() {
    return tests.values();
  }

  /**
   * Determines if a test exists in this module.
   *
   * @param name {String} Name of the test to check.
   * @return Returns <code>true</code> if the test exists, false otherwise.
   */
  @Function
  public boolean hasTest(final String name) {
    return tests.containsKey(name);
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
    return failures;
  }

  /**
   * Sets the number of tests that failed.
   *
   * @param failures Number of failures.
   */
  public void setFailures(int failures) {
    this.failures = failures;
  }

  /**
   * Returns the number of tests assertions inside the module.
   */
  public int getTotal() {
    return total;
  }

  /**
   * Sets the number of tests assertions inside the module.
   *
   * @param total Number of assertions.
   */
  public void setTotal(int total) {
    this.total = total;
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
