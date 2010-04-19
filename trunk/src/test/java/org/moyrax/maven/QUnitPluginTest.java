package org.moyrax.maven;

import org.junit.Ignore;
import org.junit.Test;
import org.moyrax.javascript.ContextPathBuilder;

/**
 * Tests for the basic script runner.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 0.50
 */
public class QUnitPluginTest {
  @Test
  @Ignore
  public void testSimpleScript() throws Exception {
    final QUnitPlugin runner = new QUnitPlugin();

    final String[] includes = new String[] {};
    final String[] excludes = new String[] { "**/.svn/**" };

    final String baseDirectory = System.getProperty("user.dir");

    ContextPathBuilder.addDefinition(baseDirectory, includes, excludes);

    runner.execute("classpath:/org/moyrax/javascript/test.js");
  }
}
