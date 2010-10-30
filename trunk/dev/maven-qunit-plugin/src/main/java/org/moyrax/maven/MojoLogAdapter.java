package org.moyrax.maven;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;

/**
 * Wraps a maven mojo logger to the apache commons logging strategy.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 * @since 1.2.1
 */
public class MojoLogAdapter implements Log {
  /**
   * Maven logger.
   */
  private org.apache.maven.plugin.logging.Log log;

  /**
   * Creates a new Mojo Log adapter and sets the Mojo Log.
   *
   * @param theLog Log to wrap around the apache common Log. It cannot be null.
   */
  public MojoLogAdapter(final org.apache.maven.plugin.logging.Log theLog) {
    Validate.notNull(theLog, "The log cannot be null.");

    log = theLog;
  }

  /**
   * {@inheritDoc}
   */
  public void debug(final Object message) {
    log.debug(message.toString());
  }

  /**
   * {@inheritDoc}
   */
  public void debug(final Object message, final Throwable cause) {
    log.debug(message.toString(), cause);
  }

  /**
   * {@inheritDoc}
   */
  public void error(final Object message) {
    log.error(message.toString());
  }

  /**
   * {@inheritDoc}
   */
  public void error(final Object message, final Throwable cause) {
    log.error(message.toString(), cause);
  }

  /**
   * {@inheritDoc}
   */
  public void fatal(final Object message) {
    log.error(message.toString());
  }

  /**
   * {@inheritDoc}
   */
  public void fatal(final Object message, final Throwable cause) {
    log.error(message.toString(), cause);
  }

  /**
   * {@inheritDoc}
   */
  public void info(final Object message) {
    log.info(message.toString());
  }

  /**
   * {@inheritDoc}
   */
  public void info(final Object message, final Throwable cause) {
    log.info(message.toString(), cause);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isErrorEnabled() {
    return log.isErrorEnabled();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isFatalEnabled() {
    return log.isErrorEnabled();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isTraceEnabled() {
    return log.isDebugEnabled();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isWarnEnabled() {
    return log.isWarnEnabled();
  }

  /**
   * {@inheritDoc}
   */
  public void trace(final Object message) {
    log.debug(message.toString());
  }

  /**
   * {@inheritDoc}
   */
  public void trace(final Object message, final Throwable cause) {
    log.debug(message.toString(), cause);
  }

  /**
   * {@inheritDoc}
   */
  public void warn(final Object message) {
    log.warn(message.toString());
  }

  /**
   * {@inheritDoc}
   */
  public void warn(final Object message, final Throwable cause) {
    log.warn(message.toString(), cause);
  }
}
