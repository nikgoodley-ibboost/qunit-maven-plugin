package org.moyrax.resolver;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.moyrax.resolver.ContextFileResolver;
import org.moyrax.resolver.ResourceResolver;

/**
 * Tests the {@link ContextFileResolver} class.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class ContextFileResolverTest {
  private ContextFileResolver resolver;

  @Before
  public void setUp() {
    final File basePath = new File(
        Thread.currentThread().getContextClassLoader()
          .getResource("org/moyrax/javascript/lib/qunit.js").getPath())
      .getParentFile();

    final File[] contextPath = new File[] { basePath };

    resolver = new ContextFileResolver(contextPath);
  }

  @Test
  public void testCanHandleFile() {
    Assert.assertEquals(resolver.canHandle("qunit.js"),
        ResourceResolver.HANDLE_SHARED);
    Assert.assertEquals(resolver.canHandle("context:qunit.js"),
        ResourceResolver.HANDLE_SHARED);
    Assert.assertEquals(resolver.canHandle("context:/qunit.js"),
        ResourceResolver.HANDLE_SHARED);
    Assert.assertEquals(resolver.canHandle("lib:qunit.js"),
        ResourceResolver.CANNOT_HANDLE);
    Assert.assertEquals(resolver.canHandle("lib:/qunit.js"),
        ResourceResolver.CANNOT_HANDLE);
  }

  @Test
  public void testResolveFile() throws Exception {
    final String contextFile = "qunit.js";
    final File result = (File)resolver.resolve(contextFile);

    Assert.assertNotNull(result);
    Assert.assertTrue(result.getCanonicalPath().endsWith(
        "/org/moyrax/javascript/lib/qunit.js"));
  }

  @Test
  public void testExcludes() {
    final String contextFile = "qunit.js";

    File result = (File)resolver.resolve(contextFile);

    Assert.assertNotNull(result);

    final File basePath = new File(
        Thread.currentThread().getContextClassLoader()
          .getResource("org/moyrax/javascript/lib/qunit.js").getPath())
      .getParentFile();

    final File[] contextPath = new File[] { basePath.getParentFile() };
    final File[] excludes = new File[] { basePath, new File(".svn") };

    resolver.setContextPath(contextPath, excludes);

    result = (File)resolver.resolve(contextFile);

    Assert.assertNull(result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResolveNull() throws Exception {
    resolver.resolve(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResolveNonHandled() throws Exception {
    resolver.resolve("classpath:/qunit.js");
  }
}
