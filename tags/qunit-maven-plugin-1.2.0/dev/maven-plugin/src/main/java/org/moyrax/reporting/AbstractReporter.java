package org.moyrax.reporting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
  private static final Log log = LogFactory.getLog(AbstractReporter.class);

  /**
   * Prefix added to the start of the messages that are sent to the output
   * device.
   */
  private String prefix;

  /**
   * {@inheritDoc}
   */
  public <T> void started(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T> void stopped(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T> void suspended(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T> void skipped(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T> void succeed(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T> void failed(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public <T> void error(final Operation<T> operation,
      final Status<T> status) {
    report(operation, status.getMessage(operation));
  }

  /**
   * {@inheritDoc}
   */
  public void warn(final String message) {
    getLogger().warn(getPrefix() + " " + message);
  }

  /**
   * {@inheritDoc}
   */
  public void info(final String message) {
    getLogger().info(getPrefix() + " " + message);
  }

  /**
   * {@inheritDoc}
   */
  public void debug(final String message) {
    getLogger().debug(getPrefix() + " " + message);
  }

  /**
   * {@inheritDoc}
   */
  public void fatal(final String message) {
    getLogger().fatal(getPrefix() + " " + message);
  }

  /**
   * {@inheritDoc}
   */
  public String getPrefix() {
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
    return new ByteArrayInputStream("".getBytes());
  }

  /**
   * @return Returns the class' logger.
   */
  protected Log getLogger() {
    return log;
  }

  /**
   * Reports an operation.
   *
   * @param operation Operation to report. It cannot be null.
   * @param message Operation related message. It cannot be null.
   */
  protected <T> void report(final Operation<T> operation,
      final String message) {
    // Nothing to handle here.
  }
}
