package org.moyrax.reporting;

import java.io.PrintWriter;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomWriter;
import org.moyrax.javascript.qunit.ModuleStatus;

/**
 * Reports operations in XML format. The output format is surefire-reports
 * compliant.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 * @since 1.2.2
 */
public class XmlReporter extends AbstractReporter {
  /** Writer for printing the output. */
  private PrintWriter writer;

  /** List of executed test suites. */
  private Map<Integer, Xpp3Dom> suites = new HashMap<Integer, Xpp3Dom>();

  /** List of executed tests. */
  private Map<Integer, Xpp3Dom> tests = new HashMap<Integer, Xpp3Dom>();

  /**
   * Creates a new {@link XmlReporter} which writes the output to the specified
   * {@link PrintWriter}.
   *
   * @param theWriter Writer for printing the output. It cannot be null.
   */
  public XmlReporter(final PrintWriter theWriter) {
    Validate.notNull(theWriter, "The writer cannot be null.");

    writer = theWriter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends ReportEntry> void started(final Operation<T> operation,
      final Status<T> status) {

    Validate.notNull(operation, "The operation cannot be null.");

    if (TestSuite.class.equals(operation.getRelatedObject().getClass())) {
      notifyTestSuiteElement((TestSuite)operation.getRelatedObject());
    } else if (TestCase.class.equals(operation.getRelatedObject().getClass())) {
      notifyTestElement((TestCase)operation.getRelatedObject());
    }
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

    if (status == ReportStatus.DONE || status == ModuleStatus.FAILED) {
      writeResults();
    } else {
      super.stopped(operation, status);
    }
  }

  /**
   * Creates or updates the XML element for the specified {@link TestCase}.
   *
   * @param test Test case related to the element to create or update. It
   *    cannot be null.
   */
  private void notifyTestElement(final TestCase test) {
    Validate.notNull(test, "The test cannot be null.");

    Xpp3Dom element = tests.get(test.hashCode());

    if (element == null) {
      element = new Xpp3Dom("testcase");

      tests.put(test.hashCode(), element);
    }

    writeAttributes(element, test);
    writeOutput(element, test);

    if (test.getSuite() != null) {
      Xpp3Dom suiteEl = suites.get(test.getSuite().hashCode());

      if (suiteEl != null) {
        suiteEl.addChild(element);
      } else {
        throw new ConcurrentModificationException("The test is being executed"
            + " before the related suite is created (test " + test.getName()
            + " on suite " + test.getSuite().getName() + ")");
      }
    }
  }

  /**
   * Creates or updates the XML element for the specified {@link TestSuite}.
   *
   * @param suite Suite related to the element to create or update. It
   *    cannot be null.
   */
  private void notifyTestSuiteElement(final TestSuite suite) {
    Validate.notNull(suite, "The test suite cannot be null.");

    Xpp3Dom element = suites.get(suite.hashCode());

    if (element == null) {
      element = new Xpp3Dom("testsuite");

      suites.put(suite.hashCode(), element);
    }

    writeAttributes(element, suite);
    writeOutput(element, suite);

  }

  /**
   * Writes the default attributes for the specified {@link TestCase}.
   *
   * @param element Element to write the attributes. It cannot be null.
   * @param source Source {@link TestCase} to retrieve the attributes data. It
   *    cannot be null.
   */
  private void writeAttributes(final Xpp3Dom element, final TestCase source) {
    element.setAttribute("name", source.getName());

    if (source.getGroup() != null) {
      element.setAttribute("group", source.getGroup());
    }

    if (source.getSourceName() != null) {
      element.setAttribute("classname", source.getSourceName() );
    }

    element.setAttribute("time", elapsedTimeAsString(source.getTotalTime()));
  }

  /**
   * Writes the output data (if any) of the {@link TestCase}.
   *
   * @param element Element to write the output. It cannot be null.
   * @param source Source {@link TestCase} to retrieve the output data. It
   *    cannot be null.
   */
  private void writeOutput(final Xpp3Dom element, final TestCase source) {
    if (!source.getOutput().isEmpty()) {
      Xpp3Dom output = element.getChild("system-out");

      if (output == null) {
        output = new Xpp3Dom("system-out");

        element.addChild(output);
      }

      output.setValue(source.getOutput());
    }
  }

  /**
   * Writes the results to the output device. It's invoked only once when the
   * execution is already finished.
   */
  private void writeResults() {
    writer.write( "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");

    for (Xpp3Dom suite : suites.values()) {
      Xpp3DomWriter.write(new PrettyPrintXMLWriter( writer ), suite);
    }
  }
}
