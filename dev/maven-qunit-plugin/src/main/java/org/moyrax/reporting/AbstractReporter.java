package org.moyrax.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This abstract class contains the basic operations for a {@link Reporter}.
 * The reporters may want to extend this class to simplify common operations
 * instead of implementing the {@link Reporter} interfaz.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 * @since 0.2.0
 */
public abstract class AbstractReporter implements Reporter {
  /** Default class logger. */
  private Log log = LogFactory.getLog(AbstractReporter.class);

  /** Formatter for numeric values. */
  private NumberFormat numberFormat = NumberFormat.getInstance();

  /**
   * Prefix added to the start of the messages that are sent to the output
   * device.
   */
  private String prefix;

  /**
   * Keeps the output there.
   */
  private ByteArrayOutputStream output = new ByteArrayOutputStream();

  /**
   * {@inheritDoc}
   */
  public <T extends ReportEntry> void started(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T extends ReportEntry> void stopped(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T extends ReportEntry> void suspended(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T extends ReportEntry> void skipped(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T extends ReportEntry> void succeed(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T extends ReportEntry> void failed(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T extends ReportEntry> void error(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public void warn(final String message) {
    getLog().warn(getPrefix() + message);
  }

  /**
   * {@inheritDoc}
   */
  public void info(final String message) {
    getLog().info(getPrefix() + message);
  }

  /**
   * {@inheritDoc}
   */
  public void debug(final String message) {
    getLog().debug(getPrefix() + message);
  }

  /**
   * {@inheritDoc}
   */
  public void fatal(final String message, final Throwable cause) {
    getLog().fatal(getPrefix() + message, cause);
  }

  /**
   * {@inheritDoc}
   */
  public String getPrefix() {
    if (prefix == null) {
      prefix = "";
    }

    return prefix;
  }

  /**
   * {@inheritDoc}
   */
  public void setPrefix(final String thePrefix) {
    prefix = thePrefix;
  }

  /**
   * {@inheritDoc}
   */
  public InputStream getOutput() {
    return new ByteArrayInputStream(output.toByteArray());
  }

  /**
   * {@inheritDoc}
   */
  public Log getLog() {
    return log;
  }

  /**
   * {@inheritDoc}
   */
  public void setLog(final Log theLog) {
    Validate.notNull(theLog, "The log cannot be null.");

    log = theLog;
  }

  /**
   * Reports an operation.
   *
   * @param operation Operation to report. It cannot be null.
   * @param message Operation related message. It cannot be null.
   */
  protected <T extends ReportEntry> void report(final Operation<T> operation,
      final String message) {
    // Nothing to handle here.
  }

  /**
   * Writes to the internal output.
   *
   * @param message Message to write. It cannot be null.
   */
  protected void write(final String message) {
    Validate.notNull(message, "The message cannot be null.");

    try {
      String outputBuffer = message + "\n";

      output.write(outputBuffer.getBytes());
    } catch (IOException ex) {
      // Nervermind.
    }
  }

  /**
   * Formats the elapsed time of a report entry.
   *
   * @param totalTime Total elapsed time.
   *
   * @return Returns the formatted string.
   */
  protected String elapsedTimeAsString(final long totalTime) {
    return numberFormat.format((double) totalTime / 1000);
  }
}
