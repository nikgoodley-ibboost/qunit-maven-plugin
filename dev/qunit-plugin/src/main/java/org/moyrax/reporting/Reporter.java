package org.moyrax.reporting;

import java.io.InputStream;

/**
 * This interface must be implemented by any class that wants to be a Reporter.
 * A reporter is a logging unit which traces several operations and address the
 * ouput to some device.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 * @since 0.2.0
 */
public interface Reporter {
  /**
   * Reports that an operation started.
   *
   * @param <T> Class related to the operation. It may be used to bind a test
   *    case or suite to a single operation.
   * @param operation Operation which made the action.
   * @param status Operation status at this instance.
   */
  <T> void started(final Operation<T> operation, final Status<T> status);

  /**
   * Reports that an operation has been stopped.
   *
   * @param <T> Class related to the operation. It may be used to bind a test
   *    case or suite to a single operation.
   * @param operation Operation which made the action.
   * @param status Operation status at this instance.
   */
  <T> void stopped(final Operation<T> operation, final Status<T> status);

  /**
   * Reports that an operation has been suspended.
   *
   * @param <T> Class related to the operation. It may be used to bind a test
   *    case or suite to a single operation.
   * @param operation Operation which made the action.
   * @param status Operation status at this instance.
   */
  <T> void suspended(final Operation<T> operation, final Status<T> status);

  /**
   * Reports that an operation has been skipped.
   *
   * @param <T> Class related to the operation. It may be used to bind a test
   *    case or suite to a single operation.
   * @param operation Operation which made the action.
   * @param status Operation status at this instance.
   */
  <T> void skipped(final Operation<T> operation, final Status<T> status);

  /**
   * Reports that an operation has finished successfully.
   *
   * @param <T> Class related to the operation. It may be used to bind a test
   *    case or suite to a single operation.
   * @param operation Operation which made the action.
   * @param status Operation status at this instance.
   */
  <T> void succeed(final Operation<T> operation, final Status<T> status);

  /**
   * Reports that an operation has finished with an expected error.
   *
   * @param <T> Class related to the operation. It may be used to bind a test
   *    case or suite to a single operation.
   * @param operation Operation which made the action.
   * @param status Operation status at this instance.
   */
  <T> void failed(final Operation<T> operation, final Status<T> status);

  /**
   * Reports that an operation has finished by an unexpected exception.
   *
   * @param <T> Class related to the operation. It may be used to bind a test
   *    case or suite to a single operation.
   * @param operation Operation which made the action.
   * @param status Operation status at this instance.
   */
  <T> void error(final Operation<T> operation, final Status<T> status);

  /**
   * Writes a warning message to the output device. By default uses the class
   * logger.
   *
   * @param message Message to write. It can be null or empty.
   */
  void warn(final String message);

  /**
   * Writes an information message to the output device. By default uses the
   * class logger.
   *
   * @param message Message to write. It can be null or empty.
   */
  void info(final String message);

  /**
   * Writes a debugging message to the output device. By default uses the class
   * logger.
   *
   * @param message Message to write. It can be null or empty.
   */
  void debug(final String message);

  /**
   * Writes a fatal error message to the output device. By default uses the
   * class logger.
   *
   * @param message Message to write. It can be null or empty.
   */
  void fatal(final String message);

  /**
   * Returns the prefix used when messages are written to the output device.
   */
  String getPrefix();

  /**
   * Sets the prefix used when messages are written to the output device.
   *
   * @param thePrefix Prefix to append at the start of the messages. It can be
   *    null or empty.
   */
  void setPrefix(final String thePrefix);

  /**
   * Returns the output buffer for this reporter.
   *
   * @return Returns an {@link InputStream} to read the output buffer.
   */
  InputStream getOutput();
}
