package org.moyrax.javascript.qunit;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.moyrax.reporting.ConsoleReporter;
import org.moyrax.reporting.Reporter;

import com.gargoylesoftware.htmlunit.WebClient;

@Ignore
public class TestRunnerTest {

  private ReporterManager reporter = new ReporterManager(
      new ArrayList<Reporter>(Arrays.asList(new Reporter[] {
          new ConsoleReporter() })));
  /**
   * Container for running tests.
   */
  private WebClient client = new WebClient();

  /**
   * Runner to test.
   */
  private TestRunner runner;

  @Before
  public void setUp() {
    runner = new TestRunner(reporter, client);
  }

  @Test
  public void testRunAll() throws Exception {
    runner.run(getClass()
        .getResourceAsStream("/org/moyrax/javascript/test.html"));

    runner.reportAll();
  }
}
