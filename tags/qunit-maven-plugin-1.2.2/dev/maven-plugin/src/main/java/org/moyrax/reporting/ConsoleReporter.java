package org.moyrax.reporting;

import java.io.PrintStream;

import org.apache.commons.lang.Validate;

/**
 * Reports operations to the console.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.2.0
 */
public class ConsoleReporter extends AbstractReporter {
  /**
   * Output writer.
   */
  private final PrintStream printer;

  /**
   * Creates a new {@link ConsoleReporter} and uses the standard output stream.
   */
  public ConsoleReporter() {
    this(System.out);
  }

  /**
   * Creates a new {@link ConsoleReporter} and uses the specified output to
   * write the results.
   *
   * @param thePrinter Output to write results. It cannot be null.
   */
  public ConsoleReporter(final PrintStream thePrinter) {
    Validate.notNull(thePrinter, "The printer cannot be null.");

    printer = thePrinter;

    write("\n");
  }

  /**
   * {@inheritDoc}
   * <p>
   * This also writes the operation result message to the configured output.
   * </p>
   */
  @Override
  protected <T extends ReportEntry> void report(final Operation<T> operation,
      final String message) {

    Validate.notNull(operation, "The operation cannot be null.");
    Validate.notNull(message, "The message cannot be null.");

    if (!message.isEmpty()) {
      String outputBuffer = getPrefix() + " " + message;

      printer.println(outputBuffer);
      write(outputBuffer);
    }
  }
}
