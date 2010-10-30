package org.moyrax.javascript.qunit;

import org.apache.commons.lang.Validate;
import org.moyrax.reporting.Operation;
import org.moyrax.reporting.Status;
import org.moyrax.reporting.TestCase;
import org.moyrax.reporting.TestSuite;

/**
 * Represents a possible status of a {@link TestSuite}.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.2.0
 */
public enum TestStatus implements Status<TestCase> {
  STARTED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<TestCase> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      TestCase test = operation.getRelatedObject();

      if (!test.isRunning()) {
        test.start();
      }

      String message = "Running " + test.getName();

      return message;
    }
  },

  SUCCEED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<TestCase> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      TestCase test = operation.getRelatedObject();

      String timestamp = String.valueOf(test.getTotalTime() / 1000)
        + " secs.";
      String message = "Tests run: " + test.getTotal() + ", "
        + "Failures: " + test.getFailures() + ", "
        + "Passed: " + (test.getTotal() - test.getFailures()) + ", "
        + "Time elapsed: " + timestamp;

      return message;
    }
  },

  FAILED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<TestCase> operation) {
      String message = TestStatus.SUCCEED.getMessage(operation);

      return message + " <<<<<<<<<< TEST FAILED!";
    }
  },

  ERROR() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<TestCase> operation) {
      String message = TestStatus.SUCCEED.getMessage(operation);

      return message + " <<<<<<<<<< TEST IN ERROR!";
    }
  };

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.toString().toLowerCase();
  }
}
