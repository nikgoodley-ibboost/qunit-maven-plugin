package org.moyrax.maven;

import org.junit.Test;
import org.moyrax.maven.QUnitPlugin;

/**
 * Tests for the basic script runner.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 0.50
 */
public class QUnitPluginTest {
  @Test
  public void testSimpleScript() throws Exception {
    final QUnitPlugin runner = new QUnitPlugin();

    final String[] includes = new String[] {};
    final String[] excludes = new String[] { "**/.svn/**" };

    final String baseDirectory = System.getProperty("user.dir");

    runner.defineContextPath(baseDirectory, includes, excludes);
    runner.execute("classpath:/org/moyrax/javascript/test.js");
  }
}
