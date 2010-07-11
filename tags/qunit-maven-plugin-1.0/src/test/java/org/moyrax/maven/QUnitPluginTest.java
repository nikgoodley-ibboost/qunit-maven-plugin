package org.moyrax.maven;

import java.io.File;

import org.junit.Test;
import org.moyrax.maven.QUnitPlugin;

/**
 * Tests for the basic script runner.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 0.50
 */
public class QUnitPluginTest {
  private static final String baseDirectory = "/mnt/development/src/web/debug-js/";

  @Test
  public void testSimpleScript() throws Exception {
    final QUnitPlugin runner = new QUnitPlugin();
    final File scriptFile = new File(baseDirectory +
        "src/test/resources/org/moyrax/debug-js/Debugger-test.js");

    final String[] includes = new String[] {};
    final String[] excludes = new String[] { "**/.svn/**" };

    runner.defineContextPath(baseDirectory, includes, excludes);
    runner.execute(scriptFile);
  }
}
