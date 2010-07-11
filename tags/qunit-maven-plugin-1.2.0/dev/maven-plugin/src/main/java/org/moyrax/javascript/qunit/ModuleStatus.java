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
public enum ModuleStatus implements Status<Module> {
  STARTED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<Module> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      Module module = operation.getRelatedObject();

      return "Module started: " + module.getName();
    }
  },

  SUCCEED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<Module> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      Module module = operation.getRelatedObject();
      String timestamp = String.valueOf(module.getTotalTime() / 1000) + " secs.";

      return "Module completed: " + module.getName() + "(" + module.getTotal()
          + " total, " + module.getFailures() + " failed, "
          + (module.getTotal() - module.getFailures()) + " passed) " + timestamp;
    }
  },

  FAILED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<Module> operation) {
      String message = ModuleStatus.SUCCEED.getMessage(operation);

      return message + " <<<<<<<<<< THERE'RE TESTS IN FAILURE!";
    }
  },

  ERROR() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<Module> operation) {
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
