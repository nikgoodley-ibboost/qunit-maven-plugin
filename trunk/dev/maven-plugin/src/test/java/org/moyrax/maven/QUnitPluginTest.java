/* vim: set ts=2 et sw=2 cindent fo=qroca: */

package org.moyrax.maven;

import java.util.LinkedList;

import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.util.ReflectionUtils;
import static org.easymock.classextension.EasyMock.*;
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
    
    MavenProject project = createMock(MavenProject.class);
    expect(project.getRuntimeClasspathElements())
      .andReturn(new LinkedList<String>()).anyTimes();
    replay(project);

    runner.setTargetPath(baseDirectory);
    runner.setTestResources(tests);
    runner.addComponentSearchPath("classpath:/org/moyrax/javascript/common/**");
    ReflectionUtils.setVariableValueInObject(runner, "project", project);

    runner.execute();
  }
}

