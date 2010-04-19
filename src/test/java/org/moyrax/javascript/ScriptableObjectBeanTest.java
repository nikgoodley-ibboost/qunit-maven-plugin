package org.moyrax.javascript;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * Tests the {@link ScriptableObjectBean} class.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 */
public class ScriptableObjectBeanTest {
  @SuppressWarnings("serial")
  private static class TestScriptableObject extends ScriptableObject {
    /**
     * This method will be registered in the global scope.
     *
     * @param context Current execution context.
     * @param scope   Script global scope.
     * @param arguments  Arguments passed to this method from the script.
     * @param thisObj Reference to the current javascript object.
     */
    @JsFunction
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
    public static void testHiddenProc(final Context context,
        final Scriptable scope, final Object[] arguments,
        final Function thisObj) {
      assertInternal("testProc is visible!");
    }

    /**
     * Retrieves the name of the object as will be known in the JavaScript code.
     */
    @Override
    public String getClassName() {
      return "TestClass";
    }
  };

  /**
   * {@link ScriptableObjectBean} for testing.
   */
  private ScriptableObjectBean bean;

  /**
   * Initializes each test.
   */
  @Before
  public void setUp() {
    bean = new ScriptableObjectBean(TestScriptableObject.class);
  }

  @Test
  public void testGetGlobalFunctionNames() {
    String[] functionNames = bean.getFunctionNames();

    Assert.isTrue(ArrayUtils.contains(functionNames, "testProc"));
    Assert.isTrue(!ArrayUtils.contains(functionNames, "testHiddenProc"));
    Assert.isTrue(!ArrayUtils.contains(functionNames, "foo"));
  }

  @Test
  public void testGetClassName() {
    String className = bean.getClassName();

    Assert.isTrue(className.equals("TestScriptableObject"));
  }

  @Test
  public void testGetScriptableClass() {
    Class<?> klass = bean.getScriptableClass();

    Assert.isTrue(klass.equals(TestScriptableObject.class));
  }

  /**
   * Asserts that an object is not null.
   *
   * @param any Some object to assert.
   */
  private static void assertInternal(final Object any) {
    Assert.notNull(any);
  }
}
