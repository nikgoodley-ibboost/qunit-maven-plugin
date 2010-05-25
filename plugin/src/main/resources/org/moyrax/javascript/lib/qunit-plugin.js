
/**
 * This object handles all QUnit results and allows to log the output to
 * different devices.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 */
var reporter = new QUnitReporter();

QUnitPlugin = {
  /**
   * Occurs whenever an assertion is completed.
   *
   * @param result {Boolean} It's <code>true</code> for passing,
   *    <code>false</code> for failing.
   * @param message {String} Description provided by the assertion.
   */
  log : function (result, message) {
    reporter.log(result, message);
  },

  /**
   * This method is called whenever a new test batch of assertions starts
   * running.
   *
   * @param name {String} Name of the test batch.
   */
  testStart : function(name) {
    reporter.testStart(name);
  },

  /**
   * Occurs whenever a batch of assertions finishes running.
   *
   * @param name {String} Name of the test batch.
   * @param failures {Number} Number of test failures that occurred.
   * @param total {Number} Number of test assertions that occurred.
   */
  testDone : function (name, failures, total) {
    reporter.testDone(name, failures, total);
  },

  /**
   * Occurs whenever a new module of tests starts running.
   *
   * @param name {String} Name of the module.
   */
  moduleStart : function (name) {
    reporter.moduleStart(name);
  },

  /**
   * Occurs whenever a module finishes running.
   *
   * @param name {String} Name of the module.
   * @param failures {Number} Number of tests inside the module which failed.
   * @param total {Number} Number of tests assertions inside the module.
   */
  moduleDone : function (name, failures, total) {
    reporter.moduleDone(name, failures, total);
  },

  /**
   * Occurs whenever all the tests have finished running.
   *
   * @param failures {Number} Number of failures that ocurred.
   * @param total {Number} Number of assertions that ocurred.
   */
  done : function (failures, total) {
    reporter.done(failures, total);
  }
};

/* Initializes the QUnit proxies. */
QUnit.testStart = QUnitPlugin.testStart;
QUnit.testDone = QUnitPlugin.testDone;
QUnit.moduleStart = QUnitPlugin.moduleStart;
QUnit.moduleDone = QUnitPlugin.moduleDone;
QUnit.done = QUnitPlugin.done;
