package org.moyrax.javascript.qunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Tests the {@link TestRunner} class.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com@gmail.com&gt;
 * @since 1.2
 */
public class TestRunnerTest {
  private static final Log log = LogFactory.getLog(TestRunnerTest.class);

  private QUnitReporter reporter = new QUnitReporter(
      System.getProperty("java.io.tmpdir"), log);

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
    File testFile = copyTemp("/org/moyrax/javascript/test-local.html");
    File qunit = copyTemp("/org/moyrax/javascript/lib/qunit.js",
        new File(System.getProperty("java.io.tmpdir"), "qunit.js"));

    try {
      runner.run(new FileInputStream(testFile));
      runner.reportAll();
    } finally {
      testFile.delete();
      qunit.delete();
    }
  }

  private File copyTemp(final String classPath) throws IOException {
    return copyTemp(classPath, File.createTempFile("TestRunner", "Test"));
  }

  private File copyTemp(final String classPath, final File outputFile)
  throws IOException {

    InputStream input = getClass().getResourceAsStream(classPath);

    FileOutputStream output = null;
    File tempFile = outputFile;

    try {
      Validate.notNull(input, "Resource not found: " + classPath);

      output = new FileOutputStream(tempFile);

      IOUtils.copy(input, output);
    } finally {
      output.close();
    }

    return tempFile;
  }
}
