/* vim: set ts=2 et sw=2 cindent fo=qroca: */

package org.moyrax.maven;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.util.ReflectionUtils;
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
    Artifact artifact = createMock(Artifact.class);
    ArtifactFactory artifactFactory = createMock(ArtifactFactory.class);
    ArtifactResolver artifactResolver = createMock(ArtifactResolver.class);
    ArtifactRepository repository = createMock(ArtifactRepository.class);
    ArtifactMetadataSource metadataSource = createMock(
        ArtifactMetadataSource.class);
    ArtifactResolutionResult results = createMock(
        ArtifactResolutionResult.class);
    Build build = createMock(Build.class);

    expect(project.getBasedir())
        .andReturn(new File(baseDirectory)).once();
    expect(project.getBuild()).andReturn(build).once();
    expect(project.getTestClasspathElements())
      .andReturn(new LinkedList<String>()).anyTimes();
    expect(project.getDependencies()).andReturn(new LinkedList<Dependency>())
      .once();
    expect(project.getArtifact()).andReturn(artifact).once();
    expect(artifactResolver.resolveTransitively(
        new HashSet<Artifact>(), artifact,
        Collections.EMPTY_LIST, repository, metadataSource))
        .andReturn(results).anyTimes();
    expect(results.getArtifacts()).andReturn(new HashSet<Artifact>());

    replay(artifact);
    replay(project);
    replay(repository);
    replay(metadataSource);
    replay(artifactResolver);
    replay(results);

    runner.setTestResources(tests);
    runner.addComponentSearchPath("classpath:/org/moyrax/javascript/common/**");

    ReflectionUtils.setVariableValueInObject(runner, "project", project);
    ReflectionUtils.setVariableValueInObject(
        runner, "artifactFactory", artifactFactory);
    ReflectionUtils.setVariableValueInObject(
        runner, "artifactResolver", artifactResolver);
    ReflectionUtils.setVariableValueInObject(
        runner, "metadataSource", metadataSource);
    ReflectionUtils.setVariableValueInObject(
        runner, "localRepository", repository);

    runner.execute();
  }
}

