package org.moyrax.javascript;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.moyrax.javascript.annotation.Function;
import org.moyrax.javascript.annotation.Script;
import org.moyrax.javascript.instrument.ComponentClassAdapter;

/**
 * Test for the class {@link ComponentClassAdapter}.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 */
public class ScriptClassAdapterTest {
  @SuppressWarnings("unused")
  @Script(name = "Foo", implementation = ScriptableObject.class)
  private static class TestPrivateScriptWithPublicConstructor {
    public TestPrivateScriptWithPublicConstructor() {}

    @Function
    public void testProc(final String foo) {
      assertTrue(foo.equals(TEST_VALUE));
    }
  };

  @SuppressWarnings("unused")
  @Script
  private static class TestPrivateScriptWithDefaultConstructor {
    @Function
    public void testProc(final String foo) {
      assertTrue(foo.equals(TEST_VALUE));
    }
  };

  @SuppressWarnings("unused")
  @Script
  private static class TestPublicScriptWithPublicConstructor {
    public TestPublicScriptWithPublicConstructor() {}

    @Function
    public void testProc(final String foo) {
      assertTrue(foo.equals(TEST_VALUE));
    }
  };

  @SuppressWarnings("unused")
  @Script
  private static class TestPublicScriptWithDefaultConstructor {
    @Function
    public void testProc(final String foo) {
      assertTrue(foo.equals(TEST_VALUE));
    }
  };

  private static final String TEST_VALUE = "Testing Script Classes";

  private ClassLoader classLoader;

  @Before
  public void setUp() throws IOException {
    if (classLoader == null) {
      classLoader = new ContextClassLoader(
          Thread.currentThread().getContextClassLoader());
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  @Test
  public void testPrivateScriptWithPublicConstructor() throws Exception {
    classLoader.loadClass(
        TestPrivateScriptWithPublicConstructor.class.getName()).newInstance();
  }

  @Test
  @Ignore
  public void testPrivateScriptWithDefaultConstructor() throws Exception {
    // TODO(mmirabelli): unignore this test when the bug described in the
    // ComponentClassAdapter#visitMethod will be fixed.
    classLoader.loadClass(
        TestPrivateScriptWithDefaultConstructor.class.getName()).newInstance();
  }

  @Test
  public void testPublicScriptWithPublicConstructor() throws Exception {
    classLoader.loadClass(
        TestPublicScriptWithPublicConstructor.class.getName()).newInstance();
  }

  @Test
  @Ignore
  public void testPublicScriptWithDefaultConstructor() throws Exception {
    // TODO(mmirabelli): unignore this test when the bug described in the
    // ComponentClassAdapter#visitMethod will be fixed.
    classLoader.loadClass(
        TestPublicScriptWithDefaultConstructor.class.getName()).newInstance();
  }
}
