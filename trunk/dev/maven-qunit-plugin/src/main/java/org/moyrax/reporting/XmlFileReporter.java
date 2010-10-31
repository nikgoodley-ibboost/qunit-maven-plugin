package org.moyrax.reporting;

import java.io.PrintWriter;

import org.apache.commons.lang.Validate;
import org.moyrax.javascript.qunit.ModuleStatus;

/**
 * Report operations to the file system in XML surefire-compliant format.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 * @since 1.2.2
 */
public class XmlFileReporter extends PlainFileReporter {
  /**
   * Reporter which writes and formats the results.
   */
  private XmlReporter reporter;

  /** XML file writer. */
  private PrintWriter writer;

  /**
   * Creates a new file reporter and sets the output directory.
   *
   * @param theOutputDir Directory to write the reports. It cannot be null or
   *    empty, and it must exist.
   */
  public XmlFileReporter(final String theOutputDir) {
    super(theOutputDir);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void started(final Operation<T> operation,
      final Status<T> status) {
    Validate.notNull(operation, "The operation cannot be null.");

    if (status == ReportStatus.STARTED) {
      openFile(operation);
      createReporter();
    }

    reporter.started(operation, status);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void succeed(final Operation<T> operation,
      final Status<T> status) {
    stopped(operation, status);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void failed(final Operation<T> operation,
      final Status<T> status) {
    stopped(operation, status);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void error(final Operation<T> operation,
      final Status<T> status) {
    stopped(operation, status);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void stopped(final Operation<T> operation,
      final Status<T> status) {
    Validate.notNull(operation, "The operation cannot be null.");

    reporter.stopped(operation, status);

    if (status == ReportStatus.DONE || status == ModuleStatus.FAILED) {
      closeFile(operation);
    }
  }

  /**
   * Creates the XML reporter.
   */
  private void createReporter() {
    if (reporter == null) {
      writer = new PrintWriter(getFileOut());
      reporter = new XmlReporter(writer);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String buildFileName(final Operation<?> operation) {
    return buildFileName(operation, ".xml");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void closeFile(final Operation<?> operation) {
    writer.close();

    super.closeFile(operation);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void info(final String message) {
  }
}
