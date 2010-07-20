package org.moyrax.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Report operations to the file system in plain text.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 * @since 1.2.2
 */
public class PlainFileReporter extends AbstractReporter {
  /** Class' logger. */
  private static final Log logger = LogFactory.getLog(PlainFileReporter.class);

  /**
   * Report's output directory.
   */
  private File outputDir;

  /**
   * Opened {@link OutputStream} for the current report file.
   */
  private OutputStream fileOut;

  /**
   * Creates a new file reporter and sets the output directory.
   *
   * @param theOutputDir Directory to write the reports. It cannot be null or
   *    empty, and it must exist.
   */
  public PlainFileReporter(final String theOutputDir) {
    Validate.notEmpty(theOutputDir, "The output directory cannot be null or"
        + " empty.");

    outputDir = new File(theOutputDir);

    Validate.isTrue(outputDir.exists(), "The output directory must exist.");
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> void started(final Operation<T> operation,
      final Status<T> status) {
    Validate.notNull(operation, "The operation cannot be null.");

    if (status == ReportStatus.STARTED) {
      openFile((Operation<ReportInfo>)operation);
    } else {
      super.started(operation, status);
    }
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> void stopped(final Operation<T> operation,
      final Status<T> status) {
    Validate.notNull(operation, "The operation cannot be null.");

    if (status == ReportStatus.DONE) {
      closeFile((Operation<ReportInfo>)operation);
    } else {
      super.stopped(operation, status);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void info(final String message) {
    report(null, message);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected <T> void report(final Operation<T> operation,
      final String message) {

    OutputStream output = getFileOut();

    try {
      output.write(message.concat("\n").getBytes());
    } catch (IOException ex) {
      String fileName = buildFileName(operation);
      logger.error("Cannot write to the report file: " + fileName, ex);
    }
  }

  /**
   * Constructs a report's file name for the specified operation.
   *
   * @param operation Operation to build the file name. It cannot be null.
   *
   * @return Returns the generated file name. Never returns empty.
   */
  protected String buildFileName(final Operation<?> operation) {
    Validate.notNull(operation, "The operation cannot be null.");

    String fileName = operation.getName();

    return "TEST-" + fileName + ".txt";
  }

  /**
   * Creates a new report file for the specified operation.
   *
   * @param operation Operation for which the report file will be created. It
   *    cannot be null.
   */
  protected void openFile(final Operation<?> operation) {
    Validate.notNull(operation, "The operation cannot be null.");

    File output = new File(outputDir, buildFileName(operation));

    try {
      output.createNewFile();

      fileOut = new FileOutputStream(output);
    } catch (IOException ex) {
      throw new RuntimeException("Cannot create the report file.", ex);
    }
  }

  /**
   * Returns the {@link OutputStream} used to write data to the operation's
   * report file.
   *
   * @return Returns the {@link OutputStream} for writing the report file
   *    related to the current operation, or <code>null</code> if the report
   *    file is not created.
   */
  protected OutputStream getFileOut() {
    return fileOut;
  }

  /**
   * Closes the report file for the specified operation.
   *
   * @param operation Operation related to the report file to close. It cannot
   *    be null.
   */
  protected void closeFile(final Operation<?> operation) {
    Validate.notNull(operation, "The operation cannot be null.");

    OutputStream out = getFileOut();

    if (out != null) {

      try {
        out.close();
      } catch (IOException ex) {
        // Nevermind.
      }
    }
  }
}
