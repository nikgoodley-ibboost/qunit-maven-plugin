package org.moyrax.resolver;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.moyrax.resolver.LibraryResolver;
import org.moyrax.resolver.ResourceResolver;

/**
 * Tests the {@link LibraryResolver} class.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class LibraryResolverTest {
  private LibraryResolver resolver;

  @Before
  public void setUp() {
    resolver = new LibraryResolver("/org/moyrax/javascript/");
  }

  @Test
  public void testResolveLib() throws Exception {
    final String libraryName = "test.js";
    final InputStream result = (InputStream)resolver.resolve(libraryName);

    Assert.assertNotNull(result);
    Assert.assertEquals(resolver.canHandle(libraryName),
        ResourceResolver.HANDLE_EXCLUSIVE);
    Assert.assertTrue(result.available() > 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResolveNull() throws Exception {
    resolver.resolve(null);
  }
}
