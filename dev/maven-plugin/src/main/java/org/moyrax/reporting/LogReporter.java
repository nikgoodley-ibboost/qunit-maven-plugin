package org.moyrax.reporting;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;

/**
 * Reports operations to the console.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.2.1
 */
public class LogReporter extends AbstractReporter {
  /** Default constructor. */
  public LogReporter() {}

  /**
   * Creates a new {@link LogReporter} and uses the specified Log to
   * write the results.
   *
   * @param theLog Logger to write results. It cannot be null.
   */
  public LogReporter(final Log theLog) {
    Validate.notNull(theLog, "The logger cannot be null.");

    setLog(theLog);
  }

  /**
   * {@inheritDoc}
   * <p>
   * This also writes the operation result message to the configured output.
   * </p>
   */
  @Override
  protected <T> void report(final Operation<T> operation,
      final String message) {

    Validate.notNull(operation, "The operation cannot be null.");
    Validate.notNull(message, "The message cannot be null.");

    if (!message.isEmpty()) {
      getLog().info(getPrefix() + " " + message);
    }
  }
}
