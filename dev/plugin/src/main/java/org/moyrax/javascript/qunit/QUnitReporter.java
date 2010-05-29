package org.moyrax.javascript.qunit;

import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;

import org.codehaus.surefire.report.ConsoleReporter;
import org.codehaus.surefire.report.FileReporter;
import org.codehaus.surefire.report.ReportEntry;
import org.codehaus.surefire.report.Reporter;
import org.codehaus.surefire.report.ReporterManager;
import org.moyrax.javascript.annotation.Function;
import org.moyrax.javascript.annotation.Script;

/**
 * This component is used to report tests execution and results.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
@Script
public class QUnitReporter {
  /**
   * Surefire reporting manager.
   */
  private ReporterManager reporting;

  /**
   * List of modules.
   */
  private HashMap<String, QUnitModule> modules = 
    new HashMap<String, QUnitModule>();

  /**
   * List of all tests, without or out of their modules.
   */
  private HashMap<String, QUnitTest> tests = new HashMap<String, QUnitTest>();

  /**
   * Name of the current working module.
   */
  private String currentModule;

  /** Default constructor. Required by Rhino. */
  public QUnitReporter() {
    ArrayList<Reporter> reporters = new ArrayList<Reporter>();

    Reporter fileReporter = new FileReporter();
    Reporter consoleReporter = new ConsoleReporter();

    //TODO(mmirabelli): retrieve the target path to write the tests results.
    fileReporter.setReportsDirectory(".");

    reporters.add(consoleReporter);
    reporters.add(fileReporter);

    reporting = new ReporterManager(reporters, ".");
  }

  /**
   * Occurs whenever an assertion is completed.
   *
   * @param result {Boolean} It's <code>true</code> for passing,
   *    <code>false</code> for failing.
   * @param message {String} Description provided by the assertion.
   */
  @Function
  public void log (final boolean result, final String message) {
    System.out.println(message);
  }

  /**
   * This method is called whenever a new test batch of assertions starts
   * running.
   *
   * @param name {String} Name of the test batch.
   */
  @Function
  public void testStart (final String name) {
    addTest(name);
  }

  /**
   * Occurs whenever a batch of assertions finishes running.
   *
   * @param name {String} Name of the test batch.
   * @param failures {Number} Number of test failures that occurred.
   * @param total {Number} Number of test assertions that occurred.
   */
  @Function
  public void testDone (final String name, final int failures,
      final int total) {
    releaseTest(name, failures, total);
  }

  /**
   * Occurs whenever a new module of tests starts running.
   *
   * @param name {String} Name of the module.
   */
  @Function
  public void moduleStart (final String name) {
    addModule(name);
  }

  /**
   * Occurs whenever a module finishes running.
   *
   * @param name {String} Name of the module.
   * @param failures {Number} Number of tests inside the module which failed.
   * @param total {Number} Number of tests assertions inside the module.
   */
  @Function
  public void moduleDone (final String name, final int failures,
      final int total) {
    releaseCurrentModule(failures, total);
  }

  /**
   * Occurs whenever all the tests have finished running.
   *
   * @param failures {Number} Number of failures that ocurred.
   * @param total {Number} Number of assertions that ocurred.
   */
  @Function
  public void done (final int failures, final int total) {
    if (failures == 0) {
      reporting.runCompleted();
    } else {
      reporting.runAborted(new ReportEntry(this, "qunit-plugin",
          "There're tests in failure.", new Exception("")));
    }
  }

  /**
   * Add a new module. This module becames the new working module.
   *
   * @param name {String} Name of the module. If it's null, the current module
   *    will be set to null.
   */
  private void addModule (final String name) {
    if (name == null) {
      this.currentModule = null;

      return;
    }

    if (modules.containsKey(name)) {
      throw new JavaScriptException("The module '"+ name +
          "' already exists in this test suite.");
    }

    this.currentModule = name;

    QUnitModule module = new QUnitModule(name);

    ReportEntry entry = new ReportEntry(module, name, "Starting module "
        + name);
    reporting.batteryStarting(entry);

    this.modules.put(name, module);
  }

  /**
   * Sets the current module results and releases it to start a new module.
   *
   * @param failures {Number} Number of tests inside the module which failed.
   * @param total {Number} Number of tests assertions inside the module.
   */
  private void releaseCurrentModule (final int failures, final int total) {
    if (this.currentModule != null) {
      QUnitModule module = modules.get(this.currentModule);

      module.setFailures(failures);
      module.setTotal(total);

      module.done();

      ReportEntry entry;

      if (failures > 0) {
        entry = new ReportEntry(module, module.getName() + "()", "Module "
            + module.getName() + " has tests in errors.",
            new IllegalStateException());
      } else {
        entry = new ReportEntry(module, module.getName(), "Module "
            + module.getName() + " ends successfully.");
      }

      reporting.batteryCompleted(entry);

      this.currentModule = null;
    }
  }

  /**
   * Adds a new test to the current module.
   *
   * @param name {String}  Name of the test.
   */
  private void addTest (final String name) {
    QUnitTest test = new QUnitTest(name);

    if (this.currentModule != null) {
      modules.get(this.currentModule).addTest(test);
    } else {
      if (tests.containsKey(name)) {
        throw new JavaScriptException("The test '" + name +
            "' already exists in this test suite.");
      }
      tests.put(name, test);
    }

    ReportEntry entry = new ReportEntry(test, test.getName() + "()", "Test "
        + test.getName() + " started.");

    reporting.testStarting(entry);
  }

  /**
   * Marks the current test as released.
   *
   * @param name {String} Name of the test.
   * @param failures {Number} Number of test failures that occurred.
   * @param total {Number} Number of test assertions that occurred.
   */
  private void releaseTest (final String name, final int failures,
      final int total) {
    QUnitTest test = getTestByName(name);

    if (test == null) {
      return;
    }

    test.setFailures(failures);
    test.setTotal(total);

    test.done();

    ReportEntry entry;

    if (failures > 0) {
      entry = new ReportEntry(test, test.getName() + "()", "Test "
          + test.getName() + " in error.", new IllegalStateException());

      reporting.testFailed(entry);
    } else {
      entry = new ReportEntry(test, test.getName() + "()", "Test "
          + test.getName() + " ends successfully.");

      reporting.testSucceeded(entry);
    }

  }

  /**
   * Returns a test by its name.
   *
   * @param name {String} Name of the required test.
   */
  private QUnitTest getTestByName (final String name) {
    if (tests.containsKey(name)) {
      return tests.get(name);
    }

    /* Search in modules */
    for (QUnitModule module : modules.values()) {
      if (module.hasTest(name)) {
        return module.getTestByName(name);
      }
    }

    return null;
  }

  public String getClassName() {
    return "QUnitReporter";
  }
}
