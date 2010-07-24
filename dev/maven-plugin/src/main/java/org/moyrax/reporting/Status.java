package org.moyrax.reporting;

/**
 * This interface defines an abstract Status for an {@link Operation}.
 *
 * @param <T> Operation type related to this status.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.2.0
 */
public interface Status<T extends ReportEntry> {
  /* Constant containing the known status names. */
  public final String STARTED = "started";
  public final String STOPPED = "stopped";
  public final String SUSPENDED = "suspended";
  public final String SKIPPED = "skipped";
  public final String SUCCEED = "succeed";
  public final String FAILED = "failed";
  public final String ERROR = "error";

  /**
   * Returns the status message for the specified operation.
   *
   * @param operation Operation to build the status message. It cannot be null.
   *
   * @return Returns a String representing the status for the operation.
   */
  String getMessage(final Operation<T> operation);

  /**
   * Determines the status name.
   *
   * @return Returns a String representing the status name.
   */
  String getName();
}
