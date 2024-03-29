package org.moyrax.reporting;

import java.util.Date;

import org.apache.commons.lang.Validate;

/**
 * This class represents a QUnit test case.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class TestCase extends ReportEntry {
  /** Timestamp to calculate the total time. */
  private long startTime;

  /**
   * Number of total assertions in this test.
   */
  private int total = -1;

  /**
   * Number of total failures in this test.
   */
  private int failures = -1;

  /**
   * Number of times this test was executed.
   */
  private int executions;

  /**
   * Last test result. A boolean <code>true</code> value means that this test
   * was successfuly executed.
   */
  private boolean success;

  /**
   * Number of milliseconds the test execution took.
   */
  private long totalTime;

  /**
   * Module which this test belongs to. It may be null if this test is not
   * inside a module.
   */
  private TestSuite suite;

  /** Keeps the output buffer. */
  private StringBuilder output = new StringBuilder();


  /** Default constructor. Required by Rhino. */
  public TestCase() {}

  /**
   * Creates a new test case.
   *
   * @param aName Test name. It cannot be null or empty.
   */
  public TestCase(final String aName) {
    super(aName);
  }

  /**
   * Notifies that this test completed the execution.
   *
   * @param asserts Number of asserts. It must be greater than or equals to 0.
   * @param theFailures Number of failed assertions. It must be greater than or
   *    equals to 0.
   */
  public void done(final int asserts, final int theFailures) {
    Validate.isTrue(asserts >= 0, "The number of asserts must be greater"
        + " than or equals to 0");
    Validate.isTrue(theFailures >= 0, "The number of failures must be greater"
        + " than or equals to 0");

    if (startTime > 0) {
      total = asserts;
      failures = theFailures;
      totalTime = new Date().getTime() - startTime;

      if (failures == 0) {
        success = true;
      }
    }
  }

  /**
   * Prints a new line to the test output.
   *
   * @param message Message to print. It cannot be null.
   */
  public void print(final String message) {
    Validate.notNull(message, "The message cannot be null.");

    output.append(message);
  }

  /**
   * Starts the test execution and sets the reference time used to calculate
   * how long was the execution.
   *
   * @param startTimestamp Start time reference. It must be greater than 0.
   */
  public void start(final long startTimestamp) {
    Validate.isTrue(startTimestamp >= 0, "The number of asserts must be"
        + " greater than 0");

    startTime = startTimestamp;
  }

  /**
   * Starts the test execution.
   */
  public void start() {
    if (startTime == 0) {
      startTime = new Date().getTime();
    }
  }

  /**
   * Returns the test output string.
   *
   * @return Return a string containing all output entries added by the method
   *    print().
   */
  public String getOutput() {
    return output.toString();
  }

  /**
   * Returns the number of total assertions in this test.
   *
   * @return Returns the number of assertions or -1 if the test is still
   *    executing.
   */
  public int getTotal() {
    return total;
  }

  /**
   * Returns the number of failures in this test.
   *
   * @return Returns the number of failures or -1 if the test is still
   *    executing.
   */
  public int getFailures() {
    return failures;
  }

  /**
   * Returns the number of times this test was executed.
   */
  public int getExecutions() {
    return executions;
  }

  @Override
  public String getGroup() {
    String group = null;

    if (getSuite() != null) {
      group = getSuite().getName();
    }

    return group;
  }

  /**
   * Returns the suite which this test belongs to. It may be null if this test
   * is not inside a {@link TestSuite}.
   */
  public TestSuite getSuite() {
    return suite;
  }

  /**
   * Sets the suite related to this test.
   *
   * @param theSuite Suite which this test belongs to.
   */
  public void setSuite(final TestSuite theSuite) {
    suite = theSuite;
  }

  /**
   * Returns the last test result. A boolean <code>true</code> value means that
   * this test was successfuly executed.
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * Determines if this test is currently running or not.
   *
   * @return Returns <code>true</code> if the test is still running.
   */
  public boolean isRunning() {
    return totalTime == 0 && startTime > 0;
  }

  /**
   * Returns the number of milliseconds that the test execution took.
   */
  public long getTotalTime() {
    return totalTime;
  }
}
