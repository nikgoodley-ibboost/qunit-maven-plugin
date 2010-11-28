package org.moyrax.javascript.qunit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.moyrax.reporting.Operation;
import org.moyrax.reporting.Status;
import org.moyrax.reporting.TestSuite;

/**
 * Represents a possible status of a {@link TestSuite}.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.2.0
 */
public enum ModuleStatus implements Status<TestSuite> {
  STARTED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<TestSuite> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      TestSuite module = operation.getRelatedObject();

      String message = StringUtils.leftPad("", 55, "-")
          + "\n T E S T S - " + module.getName() + "\n"
          + StringUtils.leftPad("", 55, "-");

      return message;
    }
  },

  SUCCEED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<TestSuite> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      TestSuite module = operation.getRelatedObject();
      String timestamp = String.valueOf(module.getTotalTime() / 1000)
          + " secs.";

      String message = "";

      message += "\nResults:\n\n"
          + "Tests run: " + module.getTotal() + ", "
          + "Failures: " + module.getFailures() + ", "
          + "Passed: " + (module.getTotal() - module.getFailures()) + ", "
          + "Time elapsed: " + timestamp + "\n\n";

      return message;
    }
  },

  FAILED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<TestSuite> operation) {
      String message = ModuleStatus.SUCCEED.getMessage(operation);

      return message.substring(0, message.length() - 2)
          + " <<<<<<<<<< FAILED!\n\n";
    }
  },

  ERROR() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<TestSuite> operation) {
      String message = ModuleStatus.SUCCEED.getMessage(operation);

      return message + " <<<<<<<<<< THERE'RE TESTS IN ERROR!";
    }
  };

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.toString().toLowerCase();
  }
}
