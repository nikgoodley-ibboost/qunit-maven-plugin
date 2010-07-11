
/**
 * This object contains common methods used by the QUnitPlugin objects.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 */
QUnitUtils = {
  /**
   * Extends an object with another. If a method exists in the target
   * object, it will be overriden and the first parameter of the new
   * method will be the overriden method.
   *
   * @param toObject {Object} Target object.
   * @param fromObject {Object} Source object.
   *
   * @return Returns <code>toObject</code> parameter.
   */
  extend : function(toObject, fromObject) {
    for (var property in fromObject) {
      if (fromObject.hasOwnProperty(property)) {
        if (!toObject[property]) {
          toObject[property] = fromObject[property];
        } else if (toObject[property] instanceof Function) {
          // If the method exists, creates a proxy.
          toObject[property] = this.inherit(
              toObject[property], fromObject[property]);
        }
      }
    }

    return toObject;
  },

  /**
   * Creates a proxy function which emulates inheritance from another function.
   * The first argument is the method that's being extended, and the second
   * parameter is the method that's extending the first one.
   *
   * The first argument of the extending method will be the extended function,
   * so to invoke the "super" method it should be executed. For example:
   *
   * <code>
   * var superProc = function (arg) {
   *   alert(arg);
   * };
   *
   * var subProc = function ($super, param1, param2) {
   *   $super(param1);
   *
   *   alert(param2);
   * };
   * </code>
   *
   * Calling <code>subProc("Hello", "world!")</code> will result in two alerts,
   * the first shown by <code>superProc()</code> with the message "Hello", and
   * the second one shown by <code>subProc</code> itself, displaying the
   * message "world!".
   *
   * The $super argument is automagically handled by the <code>inherit</code>
   * method.
   *
   * @param aMethod {Function} Function that will be extended.
   * @param aWrapper {Function} Function that's extending the first one.
   *
   * @return Returns a new <code>Function</code> that could be invoked as was
   *    explained above.
   */
  inherit : function (aMethod, aWrapper) {
    return function () {
      var _arguments = new Array(arguments.length);

      for (var i = 0, j = arguments.length; i < j; i++) {
        _arguments[i] = arguments[i];
      }

      _arguments = [aMethod].concat(_arguments);

      return aWrapper.apply(this, _arguments);
    };
  }
};

/**
 * This class represents a test module from QUnit.
 *
 * @param aName {String} Name for this module.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @class
 */
QUnitModule = function (aName) {
  var startTime = new Date().getTime();

  /* Visible object. */
  var publicInterface = {
    /**
     * Name of this module.
     *
     * @type String
     */
    name : aName,

    /**
     * Number of tests inside the module which failed.
     *
     * @type Number
     */
    failures : 0,

    /**
     * Number of tests assertions inside the module.
     *
     * @type Number
     */
    total : 0,


    /**
     * Number of milliseconds the test execution took.
     *
     * @type Number
     */
    totalTime : 0,

    /**
     * List of tests that this module contains.
     *
     * @type Array[QUnitTest]
     */
    _tests : [],

    /**
     * Adds a new test to this module.
     *
     * @param aTest {QUnitTest} Test to be added in this module. It cannot be
     *    null.
     */
    addTest : function (aTest) {
      if (!aTest) {
        throw 'aTest cannot be null.';
      }

      if (!(aTest instanceof QUnitTest)) {
        throw 'aTest should be an instance of QUnitTest.';
      }

      if (this.getTestByName(aTest.name) !== null) {
        throw "A test '" + aTest.name + "' already exists in this module.";
      }

      aTest.module = this;

      this._tests[aTest.name] = aTest;
    },

    /**
     * Returns a test by its name.
     *
     * @param name {String} Required test name.
     * @return Returns the required QUnitTest, or null if it does not exists in
     *    this module.
     */
    getTestByName : function (name) {
      if (this._tests.hasOwnProperty(name) &&
          this._tests[name] instanceof QUnitTest) {
        return this._tests[name];
      }

      return null;
    },

    /**
     * Returns a list with all tests in this module.
     */
    getTests : function () {
      var tests = [];

      for (var testName in this._tests) {
        if (this._tests.hasOwnProperty(testName)) {
          tests.push(this._tests[testName])
        }
      }

      return tests;
    },

    /**
     * Determines if a test exists in this module.
     *
     * @param name {String} Name of the test to check.
     * @return Returns <code>true</code> if the test exists, false otherwise.
     */
    hasTest : function (name) {
      return (this.getTestByName(name) !== null);
    },

    /**
     * Notifies that this module is done.
     */
    done : function () {
      this.totalTime = new Date().getTime() - startTime;
    }
  };

  return QUnitUtils.extend(this, publicInterface);
};

/**
 * This class represents a test case from QUnit.
 *
 * @param aName {String} Name for this module.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @class
 */
QUnitTest = function (aName) {
  var startTime = new Date().getTime();

  /* Visible object. */
  var publicInterface = {
    /**
     * Name of this test.
     *
     * @type String
     */
    name : aName,

    /**
     * Module which this test belongs to. It may be null if this test is not
     * inside a module.
     *
     * @type QUnitModule
     */
    module : null,

    /**
     * Number of total assertions in this test.
     *
     * @type Number
     */
    total : 0,

    /**
     * Number of total failures in this test.
     *
     * @type Number
     */
    failures : 0,

    /**
     * Number of times this test was executed.
     *
     * @type Number
     */
    executions : 0,

    /**
     * Last test result. A boolean <code>true</code> value means that this test
     * was successfuly executed.
     *
     * @type Boolean
     */
    lastResult : false,

    /**
     * Number of milliseconds the test execution took.
     *
     * @type Number
     */
    totalTime : 0,

    /**
     * Notifies that this test is done.
     */
    done : function () {
      this.totalTime = new Date().getTime() - startTime;
    }
  };

  return QUnitUtils.extend(this, publicInterface);
};