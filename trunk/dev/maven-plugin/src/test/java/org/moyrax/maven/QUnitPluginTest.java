package org.moyrax.maven;

import org.apache.maven.shared.model.fileset.FileSet;
import org.junit.Test;

/**
 * Tests for the basic script runner.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 0.50
 */
public class QUnitPluginTest {
  @Test
  public void testExecute() throws Exception {
    final QUnitPlugin runner = new QUnitPlugin();

    final String baseDirectory = System.getProperty("user.dir");
    final FileSet tests = new FileSet();

    tests.setDirectory(baseDirectory + "/src/test/resources/org/moyrax/");
    tests.addInclude("**/*test.html");

    runner.setTargetPath(baseDirectory);
    runner.setTestResources(tests);
    runner.addComponentSearchPath("classpath:/org/moyrax/javascript/common/**");

    runner.execute();
  }
}
