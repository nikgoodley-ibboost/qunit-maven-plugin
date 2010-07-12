package org.moyrax.javascript.qunit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.moyrax.reporting.Operation;
import org.moyrax.reporting.Reporter;
import org.moyrax.reporting.Status;

/**
 * Broadcasts operations to a set of reporters.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 0.1.2
 */
public class ReporterManager {
  /**
   * Available reporters.
   */
  private List<Reporter> reporters;

  /**
   * Creates a new {@link ReporterManager} and initializes the available
   * reporters.
   *
   * @param theReporters Initialized reporters. It cannot be null or empty.
   * @param thePrefix Prefix appended to all messages before being sent to the
   *    output device. It can be null or empty.
   */
  public ReporterManager(final List<Reporter> theReporters, final Log theLog) {
    this(theReporters, theLog, "[qunit]");
  }

  /**
   * Creates a new {@link ReporterManager} and initializes the available
   * reporters.
   *
   * @param theReporters Initialized reporters. It cannot be null or empty.
   * @param thePrefix Prefix appended to all messages before being sent to the
   *    output device. It can be null or empty.
   */
  public ReporterManager(final List<Reporter> theReporters, final Log theLog,
      final String thePrefix) {

    Validate.notEmpty(theReporters, "The reporters list cannot be null or"
        + " empty.");

    reporters = theReporters;

    if (thePrefix != null) {
      for (Reporter reporter : reporters) {
        reporter.setPrefix(thePrefix);
        reporter.setLog(theLog);
      }
    }
  }

  /**
   * Occurs when a {@link Module} started its execution.
   *
   * @param module The module which made the action. It cannot be null.
   */
  public void moduleStart(final Module module) {
    Validate.notNull(module, "The module cannot be null.");

    report(new Operation<Module>(module), ModuleStatus.STARTED);
  }

  /**
   * Occurs when a {@link Module} completed its execution.
   *
   * @param module The module which made the action. It cannot be null.
   * @param result The operation result. It cannot be null.
   */
  public void moduleCompleted(final Module module) {
    Validate.notNull(module, "The module cannot be null.");

    ModuleStatus result = ModuleStatus.SUCCEED;

    if (module.getFailures() > 0) {
      result = ModuleStatus.FAILED;
    }

    report(new Operation<Module>(module), result);
  }

  /**
   * Occurs when a {@link TestCase} started its execution.
   *
   * @param test The test case which made the action. It cannot be null.
   */
  public void testStart(final TestCase test) {
    Validate.notNull(test, "The test cannot be null.");

    report(new Operation<TestCase>(test), TestStatus.STARTED);
  }

  /**
   * Occurs when a {@link TestCase} completed its execution.
   *
   * @param test The module which made the action. It cannot be null.
   * @param result The operation result. It cannot be null.
   */
  public void testCompleted(final TestCase test) {
    Validate.notNull(test, "The test cannot be null.");

    TestStatus result = TestStatus.SUCCEED;

    if (test.getFailures() > 0) {
      result = TestStatus.FAILED;
    }

    report(new Operation<TestCase>(test), result);
  }

  /**
   * Finalizes the reporting.
   *
   * @param total Number of tests executed. It must be greater than or equals
   *    to 0.
   * @param failures Number of tests in failure. It must be greater than or
   *    equals to 0.
   */
  public void done(final long total, final long failures) {
    Validate.isTrue(total >= 0, "The total tests must be greater than 0.");
    Validate.isTrue(failures >= 0, "The total tests must be greater than 0.");

    String message = "QUnit finished with ";

    if (failures > 0) {
      message += "errors";
    } else {
      message += "no errors";
    }

    for (Reporter reporter : reporters) {
      reporter.info(message);
    }

    if (failures > 0) {
      fail();
    }
  }

  /**
   * Writes an information message to the output device. By default uses the
   * class logger.
   *
   * @param message Message to write. It can be null or empty.
   */
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
   * Reports the status of an operation.
   *
   * @param <T> Operation related object type.
   * @param operation Operation to report. It cannot be null.
   * @param status Current operation status. It cannot be null.
   */
  private <T> void report(final Operation<T> operation,
      final Status<T> status) {
    String name = status.getName();

    for (Reporter reporter : reporters) {
      if (name.equals(Status.STARTED)) {
        reporter.started(operation, status);
      } else if (name.equals(Status.STOPPED)) {
        reporter.stopped(operation, status);
      } else if (name.equals(Status.SUSPENDED)) {
        reporter.suspended(operation, status);
      } else if (name.equals(Status.SKIPPED)) {
        reporter.skipped(operation, status);
      } else if (name.equals(Status.SUCCEED)) {
        reporter.succeed(operation, status);
      } else if (name.equals(Status.FAILED)) {
        reporter.failed(operation, status);
      } else if (name.equals(Status.ERROR)) {
        reporter.error(operation, status);
      }
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
