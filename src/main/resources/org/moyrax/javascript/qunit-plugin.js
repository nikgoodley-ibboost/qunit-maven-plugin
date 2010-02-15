
/**
 * This object handles all QUnit results and allows to log the output to
 * different devices.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 */
QUnitPlugin = {
  /**
   * Occurs whenever an assertion is completed.
   *
   * @param result {Boolean} It's <code>true</code> for passing,
   *    <code>false</code> for failing.
   * @param message {String} Description provided by the assertion.
   */
  log : function (result, message) {

  },

  /**
   * This method is called whenever a new test batch of assertions starts
   * running.
   *
   * @param name {String} Name of the test batch.
   */
  testStart : function(name) {
    QUnitPluginHelper.addTest(name);

    print("Starting test: " + name);
  },

  /**
   * Occurs whenever a batch of assertions finishes running.
   *
   * @param name {String} Name of the test batch.
   * @param failures {Number} Number of test failures that occurred.
   * @param total {Number} Number of test assertions that occurred.
   */
  testDone : function (name, failures, total) {
    QUnitPluginHelper.releaseTest(name, failures, total);
  },

  /**
   * Occurs whenever a new module of tests starts running.
   *
   * @param name {String} Name of the module.
   */
  moduleStart : function (name) {
    QUnitPluginHelper.addModule(name);
  },

  /**
   * Occurs whenever a module finishes running.
   *
   * @param name {String} Name of the module.
   * @param failures {Number} Number of tests inside the module which failed.
   * @param total {Number} Number of tests assertions inside the module.
   */
  moduleDone : function (name, failures, total) {
    QUnitPluginHelper.releaseCurrentModule(failures, total);
  },

  /**
   * Occurs whenever all the tests have finished running.
   *
   * @param failures {Number} Number of failures that ocurred.
   * @param total {Number} Number of assertions that ocurred.
   */
  done : function (failures, total) {
    var modules = QUnitPluginHelper._modules;

    for (var moduleName in modules) {
      if (modules.hasOwnProperty(moduleName)) {
        var tests = modules[moduleName].getTests();

        for (var i = 0, j = tests.length; i < j; i++) {
          print("Test '" + tests[i].name + "' finished: " +
            QUnitPluginHelper.formatResults(
              tests[i].failures, tests[i].total));
        }
      }
    }
  }
};

/**
 * Helper object which contains methods related to the plugin operations.
 */
QUnitPluginHelper = {
  /**
   * List of modules.
   *
   * @type Array[QUnitModule]
   */
  _modules : [],

  /**
   * List of all tests, without or out of their modules.
   *
   * @type Array[QUnitTest]
   */
  _tests : [],

  /**
   * Name of the current working module.
   */
  _currentModule : "",

  /**
   * Format the number of test failures and assertions and returns an
   * human-readable string.
   *
   * @param failures {Number} Number of test failures. It can be null.
   * @param total {Number} Number of test assertions. It can be null.
   */
  formatResults : function (failures, total) {
    var failureStr = "";
    var assertStr = "";

    if (failures && !isNaN(failures)) {
      if (failures === 0) {
        failureStr = "No failures";
      } else if (failures === 1) {
        failureStr = "1 failure";
      } else if (failures > 1) {
        failureStr = failures + " failures";
      }
    }

    if (total && !isNaN(total)) {
      if (total === 0) {
        assertStr = "No tests to run";
      } else if (total > 0) {
        assertStr = total + " total";
      }
    }

    return failureStr + ", " + assertStr;
  },

  /**
   * Add a new module. This module becames the new working module.
   *
   * @param name {String} Name of the module. If it's null, the current module
   *    will be set to null.
   */
  addModule : function (name) {
    if (name === null) {
      this._currentModule = null;

      return;
    }

    if (this.getModuleByName(name) !== null) {
      throw "A module '"+ name +"' already exists in this test suite.";
    }

    this._currentModule = name;
    this._modules[name] = new QUnitModule(name);
  },

  /**
   * Sets the current module results and releases it to start a new module.
   *
   * @param failures {Number} Number of tests inside the module which failed.
   * @param total {Number} Number of tests assertions inside the module.
   */
  releaseCurrentModule : function (failures, total) {
    if (this._currentModule) {
      var module = this.getModuleByName(this._currentModule);

      module.failures = failures;
      module.total = total;

      module.done();

      this._currentModule = null;
    }
  },

  /**
   * Adds a new test to the current module.
   *
   * @param name {String}  Name of the test.
   */
  addTest : function(name) {
    var test = new QUnitTest(name);

    if (this._currentModule) {
      this._modules[this._currentModule].addTest(test);
    } else {
      if (this._tests.hasOwnProperty(name)) {
        throw "A test '" + name + "' already exists in this test suite.";
      }

      this._tests[name] = test;
    }
  },

  /**
   * Marks the current test as released.
   *
   * @param name {String} Name of the test.
   * @param failures {Number} Number of test failures that occurred.
   * @param total {Number} Number of test assertions that occurred.
   */
  releaseTest : function (name, failures, total) {
    var test = this.getTestByName(name);

    if (test == null) {
      throw "The test '" + name + "' does not exists.";
    }

    test.failures = failures;
    test.total = total;

    test.done();
  },

  /**
   * Returns a module by its name.
   *
   * @param name {String} Name of the required module.
   */
  getModuleByName : function(name) {
    return this._modules.hasOwnProperty(name) ? this._modules[name] : null;
  },

  /**
   * Returns a test by its name.
   *
   * @param name {String} Name of the required test.
   */
  getTestByName : function(name) {
    for (var i = 0, j = this._tests.length; i < j; i++) {
      if (this._tests[i].name == name) {
        return this._tests[i];
      }
    }

    /* Search in modules */
    for (var module in this._modules) {
      if (this._modules.hasOwnProperty(module) &&
          this._modules[module].hasTest(name)) {
        return this._modules[module].getTestByName(name);
      }
    }

    return null;
  }
};

/* Initializes the QUnit proxies. */
(function init () {
  QUnit.testStart = QUnitPlugin.testStart;
  QUnit.testDone = QUnitPlugin.testDone;
  QUnit.moduleStart = QUnitPlugin.moduleStart;
  QUnit.moduleDone = QUnitPlugin.moduleDone;
  QUnit.done = QUnitPlugin.done;
})();
