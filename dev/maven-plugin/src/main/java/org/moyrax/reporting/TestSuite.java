package org.moyrax.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.Validate;


/**
 * This class represents a QUnit testing module.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class TestSuite extends TestCase {
  /**
   * List of tests that this module contains.
   */
  private HashMap<String, TestCase> tests = new HashMap<String, TestCase>();

  /** Default constructor. Required by Rhino. */
  public TestSuite() {}

  /**
   * Creates a new QUnit test module.
   *
   * @param moduleName Name for this module. It cannot be null or empty.
   */
  public TestSuite(final String moduleName) {
    super(moduleName);
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
      throw new IllegalArgumentException("The test '" + aTest.getName() +
      "' already exists in this module.");
    }

    aTest.setSuite(this);

    tests.put(aTest.getName(), aTest);
  }

  /**
   * Returns a list with all tests in this module.
   */
  public List<TestCase> getTests() {
    return new ArrayList<TestCase>(tests.values());
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
  public void done() {
    super.done(getTotal(), getFailures());
  }

  /**
   * Returns the number of tests inside the module which failed.
   */
  @Override
  public int getFailures() {
    return getFailed().size();
  }

  /**
   * Returns the number of tests assertions inside the module.
   */
  @Override
  public int getTotal() {
    return tests.size();
  }
}
