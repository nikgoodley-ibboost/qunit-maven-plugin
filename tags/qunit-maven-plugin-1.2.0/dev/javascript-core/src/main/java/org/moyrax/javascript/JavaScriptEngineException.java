package org.moyrax.javascript;

import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;

/**
 * This exception is thrown when there're errors in the {@link JavaScriptEngine}
 * environment configuration.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class JavaScriptEngineException extends RuntimeException {
  /** Default ID for serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new exception with the given message.
   *
   * @param message Exception message.
   */
  public JavaScriptEngineException(final String message) {
    super(message);
  }

  /**
   * Creates a new exception with the given message and wraps an existing
   * exception.
   *
   * @param message Exception message.
   * @param parent Wrapped exception.
   */
  public JavaScriptEngineException(final String message,
      final Throwable parent) {
    super(message, parent);
  }

  /**
   * Creates a new exception wrapping the given one.
   *
   * @param parent Wrapped exception.
   */
  public JavaScriptEngineException(final Throwable parent) {
    super(parent);
  }
}
