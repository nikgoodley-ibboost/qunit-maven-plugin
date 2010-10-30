package org.moyrax.reporting;

import org.apache.commons.lang.Validate;

/**
 * Represents a possible status of a {@link ReportEntry}.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.2.2
 */
public enum ReportStatus implements Status<ReportEntry> {
  STARTED() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<ReportEntry> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      return "\n\n@file " + operation.getRelatedObject().getName();
    }
  },

  DONE() {
    /**
     * {@inheritDoc}
     */
    public String getMessage(final Operation<ReportEntry> operation) {
      Validate.notNull(operation, "The operation cannot be null.");

      return "";
    }
  };

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return this.toString().toLowerCase();
  }
}
