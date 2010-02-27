package org.moyrax.resolver;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.moyrax.resolver.ClassPathResolver;
import org.moyrax.resolver.ResourceResolver;

/**
 * Tests the {@link ClassPathResolver} class.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class ClassPathResolverTest {
  private ClassPathResolver resolver = new ClassPathResolver();

  @Test
  public void testResolveScript() throws Exception {
    final String scriptClasspath = "classpath:/org/moyrax/javascript/test.js";
    final File result = (File)resolver.resolve(scriptClasspath);

    Assert.assertNotNull(result);
    Assert.assertEquals(resolver.canHandle(scriptClasspath),
        ResourceResolver.HANDLE_EXCLUSIVE);
    Assert.assertTrue(result.getCanonicalPath().endsWith(
        "/org/moyrax/javascript/test.js"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResolveNull() throws Exception {
    resolver.resolve(null);
  }
}
