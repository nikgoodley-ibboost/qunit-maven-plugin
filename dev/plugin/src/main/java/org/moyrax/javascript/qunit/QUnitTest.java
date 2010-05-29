package org.moyrax.javascript.qunit;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.moyrax.javascript.annotation.Function;
import org.moyrax.javascript.annotation.Script;

/**
 * This class represents a QUnit test case.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
@Script
public class QUnitTest {
  /** Timestamp to calculate the total time. */
  private long startTime = new Date().getTime();

  /**
   * Name of this test.
   */
  private String name;

  /**
   * Module which this test belongs to. It may be null if this test is not
   * inside a module.
   */
  private QUnitModule module;

  /**
   * Number of total assertions in this test.
   */
  private int total;

  /**
   * Number of total failures in this test.
   */
  private int failures;

  /**
   * Number of times this test was executed.
   */
  private int executions;

  /**
   * Last test result. A boolean <code>true</code> value means that this test
   * was successfuly executed.
   */
  private boolean lastResult;

  /**
   * Number of milliseconds the test execution took.
   */
  private long totalTime;

  /** Default constructor. Required by Rhino. */
  public QUnitTest() {}

  /**
   * Creates a new test case.
   *
   * @param aName Test name. It cannot be null or empty.
   */
  public QUnitTest(final String aName) {
    Validate.notEmpty(aName, "The test name cannot be null or empty.");

    name = aName;
  }

  /**
   * Notifies that this test is done.
   */
  @Function
  public void done() {
    this.totalTime = new Date().getTime() - startTime;
  }

  /**
   * Returns the name of this test.
   */
  @Function
  public String getName() {
    return name;
  }

  /**
   * Returns the Module which this test belongs to. It may be null if this test
   * is not inside a module.
   */
  public QUnitModule getModule() {
    return module;
  }

  /**
   * Sets the module related to this test.
   *
   * @param aModule Module which this test belongs to.
   */
  public void setModule(final QUnitModule aModule) {
    module = aModule;
  }

  /**
   * Returns the number of total assertions in this test.
   */
  public int getTotal() {
    return total;
  }

  /**
   * Sets the number of total assertions in this test.
   *
   * @param total Number of assertions.
   */
  public void setTotal(int total) {
    this.total = total;
  }

  /**
   * Returns the number of failures in this test.
   */
  public int getFailures() {
    return failures;
  }

  /**
   * Sets the number of failure assertions.
   *
   * @param total Number of failures.
   */
  public void setFailures(int failures) {
    this.failures = failures;
  }

  /**
   * Returns the number of times this test was executed.
   */
  public int getExecutions() {
    return executions;
  }

  /**
   * Returns the last test result. A boolean <code>true</code> value means that
   * this test was successfuly executed.
   */
  public boolean isSuccess() {
    return lastResult;
  }

  /**
   * Returns the number of milliseconds that the test execution took.
   */
  public long getTotalTime() {
    return totalTime;
  }

  public String getClassName() {
    return "QUnitTest";
  }
}
