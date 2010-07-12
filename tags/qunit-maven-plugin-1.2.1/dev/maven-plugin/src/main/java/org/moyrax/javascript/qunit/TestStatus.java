package org.moyrax.javascript.qunit;

import org.apache.commons.lang.Validate;
import org.moyrax.reporting.Operation;
import org.moyrax.reporting.Status;

/**
 * Represents a possible status of a {@link Module}.
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

      String message = "";

      if (test.getModule() != null) {
        message += "\t";
      }

      message += "Test started: " + test.getName() + "()";

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

      String message = "";

      if (test.getModule() != null) {
        message += "\t";
      }

      message += "Test completed: " + test.getName() + "(" + test.getTotal()
          + " asserts, " + test.getFailures() + " failed, "
          + (test.getTotal() - test.getFailures()) + " passed)";

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
