package org.moyrax.maven;

import org.apache.commons.lang.Validate;

import com.gargoylesoftware.htmlunit.ScriptException;

/**
 * This exception is thrown when any error occurs running QUnit tests.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.2.4
 */
public class QUnitException extends RuntimeException {
  /** Default id for serialization. */
  private static final long serialVersionUID = 1L;

  private ScriptException cause;

  public QUnitException(final ScriptException theCause) {
    Validate.notNull(theCause, "The cause cannot be null.");

    cause = theCause;
  }

  @Override
  public String getMessage() {
    return cause.getMessage();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("Error executing test: " + cause.getPage().getWebResponse()
        .getRequestSettings().getUrl() + "\n");
    builder.append("Line: " + cause.getFailingLineNumber() + "\n");
    builder.append(cause.getLocalizedMessage());

    return builder.toString();
  }
}
