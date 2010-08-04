package org.moyrax.javascript;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.moyrax.javascript.annotation.GlobalFunction;
import org.moyrax.javascript.annotation.Script;

/**
 * Tests the {@link ScriptComponent} class.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 */
public class ScriptComponentTest {
  @Script
  private static class TestScriptableObject {
    /**
     * This method will be registered in the global scope.
     *
     * @param context Current execution context.
     * @param scope   Script global scope.
     * @param arguments  Arguments passed to this method from the script.
     * @param thisObj Reference to the current javascript object.
     */
    @SuppressWarnings("unused")
    @GlobalFunction
    public static void testProc(final Context context, final Scriptable scope,
        final Object[] arguments, final Function thisObj) {
      assertInternal("testProc is visible!");
    }

    /**
     * This method will not be registered in the global scope.
     *
     * @param context Current execution context.
     * @param scope   Script global scope.
     * @param arguments  Arguments passed to this method from the script.
     * @param thisObj Reference to the current javascript object.
     */
    @SuppressWarnings("unused")
    public static void testHiddenProc(final Context context,
        final Scriptable scope, final Object[] arguments,
        final Function thisObj) {
      assertInternal("testProc is visible!");
    }
  };

  /**
   * {@link ScriptComponent} for testing.
   */
  private ScriptComponent bean;

  /**
   * Initializes each test.
   */
  @Before
  public void setUp() {
    bean = new ScriptComponent(TestScriptableObject.class);
  }

  @Test
  public void testGetGlobalFunctionNames() {
    String[] functionNames = bean.getGlobalFunctionNames()
    .toArray(new String[] {});

    assertTrue(ArrayUtils.contains(functionNames, "testProc"));
    assertTrue(!ArrayUtils.contains(functionNames, "testHiddenProc"));
    assertTrue(!ArrayUtils.contains(functionNames, "foo"));
  }

  @Test
  public void testGetClassName() {
    String className = bean.getClassName();

    assertTrue(className.equals("TestScriptableObject"));
  }

  @Test
  public void testGetScriptableClass() {
    Class<?> klass = bean.getScriptableClass();

    assertTrue(klass.equals(TestScriptableObject.class));
  }

  /**
   * Asserts that an object is not null.
   *
   * @param any Some object to assert.
   */
  private static void assertInternal(final Object any) {
    assertNotNull(any);
  }
}
