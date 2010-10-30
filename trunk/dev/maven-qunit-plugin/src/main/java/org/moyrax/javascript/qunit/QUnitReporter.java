package org.moyrax.javascript.qunit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.moyrax.reporting.AbstractReporter;
import org.moyrax.reporting.ConsoleReporter;
import org.moyrax.reporting.Operation;
import org.moyrax.reporting.PlainFileReporter;
import org.moyrax.reporting.ReportEntry;
import org.moyrax.reporting.ReportStatus;
import org.moyrax.reporting.Reporter;
import org.moyrax.reporting.Status;
import org.moyrax.reporting.TestCase;
import org.moyrax.reporting.TestSuite;
import org.moyrax.reporting.XmlFileReporter;

/**
 * Broadcasts operations to a set of reporters.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.1.2
 */
public class QUnitReporter extends AbstractReporter {
  /**
   * Available reporters.
   */
  private List<Reporter> reporters;

  /**
   * Creates a new {@link QUnitReporter} and initializes the reporting
   * configuration.
   *
   * @param theOutputDir Directory to write the reports. It cannot be
   *    null or empty.
   * @param theLog Logger for writing results. It cannot be null.
   */
  public QUnitReporter(final String theOutputDir, final Log theLog) {

    Validate.notEmpty(theOutputDir, "The output directory cannot be null or"
        + " empty.");
    Validate.notNull(theLog, "The log cannot be null.");

    reporters = new ArrayList<Reporter>(Arrays.asList(new Reporter[] {
        new ConsoleReporter(System.out),
        new PlainFileReporter(theOutputDir),
        new XmlFileReporter(theOutputDir)
    }));

    for (Reporter reporter : reporters) {
      reporter.setLog(theLog);
    }
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void started(final Operation<T> operation,
      final Status<T> status) {
    for (Reporter reporter : reporters) {
      reporter.started(operation, status);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void succeed(final Operation<T> operation,
      final Status<T> status) {
    for (Reporter reporter : reporters) {
      reporter.succeed(operation, status);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void failed(final Operation<T> operation,
      final Status<T> status) {
    for (Reporter reporter : reporters) {
      reporter.failed(operation, status);
    }
  }

  /**
   * Occurs when a {@link TestSuite} started its execution.
   *
   * @param module The module which made the action. It cannot be null.
   */
  public void moduleStart(final TestSuite module) {
    Validate.notNull(module, "The module cannot be null.");

    started(new Operation<TestSuite>(module), ModuleStatus.STARTED);
  }

  /**
   * Occurs when a {@link TestSuite} completed its execution.
   *
   * @param module The module which made the action. It cannot be null.
   * @param result The operation result. It cannot be null.
   */
  public void moduleCompleted(final TestSuite module) {
    Validate.notNull(module, "The module cannot be null.");

    if (module.getFailures() > 0) {
      failed(new Operation<TestSuite>(module), ModuleStatus.FAILED);
    } else {
      succeed(new Operation<TestSuite>(module), ModuleStatus.SUCCEED);
    }
  }

  /**
   * Occurs when a {@link Test} started its execution.
   *
   * @param test The test case which made the action. It cannot be null.
   */
  public void testStart(final TestCase test) {
    Validate.notNull(test, "The test cannot be null.");

    started(new Operation<TestCase>(test), TestStatus.STARTED);
  }

  /**
   * Occurs when a {@link Test} completed its execution.
   *
   * @param test The module which made the action. It cannot be null.
   * @param result The operation result. It cannot be null.
   */
  public void testCompleted(final TestCase test) {
    Validate.notNull(test, "The test cannot be null.");

    if (test.getFailures() > 0) {
      failed(new Operation<TestCase>(test), TestStatus.FAILED);
    } else {
      succeed(new Operation<TestCase>(test), TestStatus.SUCCEED);
    }
  }

  /**
   * Initializes the reporting for the specified {@link TestHandler}.
   *
   * @param handler Handler which executed the current test report. It cannot
   *    be null.
   */
  public void init(final TestHandler handler) {
    Validate.notNull(handler, "The test handler cannot be null.");

    started(new Operation<ReportEntry>(handler, handler.getName()),
        ReportStatus.STARTED);
  }

  /**
   * Finalizes the reporting.
   *
   * @param handler Handler which executed the current test report. It cannot
   *    be null.
   */
  public void done(final TestHandler handler) {
    Validate.notNull(handler, "The test handler cannot be null.");

    int failures = handler.getFailures();

    Validate.isTrue(failures >= 0, "The total tests must be greater than 0.");

    if (failures > 0) {
      fail();
    }

    succeed(new Operation<ReportEntry>(handler, handler.getName()),
        ReportStatus.DONE);
  }

  /**
   * Writes an information message to the output device. By default uses the
   * class logger.
   *
   * @param message Message to write. It can be null or empty.
   */
  @Override
  public void info(final String message) {
    for (Reporter reporter : reporters) {
      reporter.info(message);
    }
  }

  /**
   * Writes a warning message to the output device. By default uses the class
   * logger.
   *
   * @param message Message to write. It can be null or empty.
   */
  @Override
  public void warn(final String message) {
    for (Reporter reporter : reporters) {
      reporter.warn(message);
    }
  }

  /**
   * Writes a debugging message to the output device. By default uses the class
   * logger.
   *
   * @param message Message to write. It can be null or empty.
   */
  @Override
  public void debug(final String message) {
    for (Reporter reporter : reporters) {
      reporter.debug(message);
    }
  }

  /**
   * Writes a debugging message to the output device. By default uses the class
   * logger.
   *
   * @param message Message to write. It can be null or empty.
   */
  public void error(final String message, final Throwable cause) {
    for (Reporter reporter : reporters) {
      reporter.fatal(message, cause);
    }
  }

  /**
   * Throws an exception with the failure information.
   */
  private void fail() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    for (Reporter reporter : reporters) {
      try {
        IOUtils.copy(reporter.getOutput(), output);
      } catch (IOException ex) {
        String message = "Cannot read the output for "
          + reporter.getClass().getName();

        try {
          output.write(message.getBytes());
        } catch (IOException wtf) {
          // What a terrible failure: Nevermind
        }
      }
    }

    throw new IllegalStateException("THERE'RE TESTS IN FAILURE\n");
  }
}
