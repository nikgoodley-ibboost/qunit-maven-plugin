# Features #
  * **Test resources configuration using filesets:** the tests to run can be chosen through includes and excludes patterns.

  * **JavaScript context path configuration to include external scripts from inside the tests:** a new concept is introduced here to make easier implement TDD in high-scale projects. There's a **_context path_** now which can be configured to dynamically lookup resources on runtime.

  * **Script loading from Java classpath:** another essential feature for high-scale projects. Often it's needed to reference resources from the Java classpath, for example to add JavaScript resources only available for the test scope. It's possible to retrieve resources from the classpath just adding the proper prefix to the include function.

  * **Benchmarks and reporting:** it's able to trace the test cases carefully and identify which cases are in error, which are not, and report the results with the proper timestamps. It now supports surefire-like reporting.

  * **Running tests from HTML files:** all the tests run by qunit-maven-plugin must be in HTML files. It allows to access the DOM as in a real browser.

# Announcement #
We're glad to announce a new release of qunit-maven-plugin. Please, check out the releases notes for the version 1.2.3:

  * **File reporting support:** the tests results are saved to plain files and it also generates a surefire-compliant XML file.

  * **JavaScript context path fixes and enhancements:** the project's dependencies are default included into the JavaScript context path, which means that the tests are able to include files from the classpath, even if the classpath is part of a dependency artifact.