package org.moyrax.reporting;

import org.apache.commons.lang.Validate;
import org.moyrax.reporting.Operation;
import org.moyrax.reporting.Status;

/**
 * Represents a possible status of a {@link ReportInfo}.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.2.2
 */
public enum ReportStatus implements Status<ReportInfo> {
  STARTED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<ReportInfo> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      return operation.getRelatedObject().getName() + " is started.";
    }
  },

  DONE() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<ReportInfo> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      return operation.getRelatedObject().getName() + " is done.";
    }
  };

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.toString().toLowerCase();
  }
}
