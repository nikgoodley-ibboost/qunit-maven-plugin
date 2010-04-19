package org.moyrax.javascript;

import java.io.File;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.moyrax.javascript.shell.Global;
import org.moyrax.resolver.ClassPathResolver;
import org.moyrax.resolver.LibraryResolver;

import com.gargoylesoftware.htmlunit.javascript.HtmlUnitContextFactory;

/**
 * Tests the {@link Shell} class.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class ShellTest {
  private static Shell shell;
  private static Context context;
  private static ScriptableObject scope;

  @BeforeClass
  public static void beforeClass() throws Exception {
    shell = new Shell();
    ScriptableObjectBean shellBean = new ScriptableObjectBean(Shell.class);
    ScriptableObjectBean globalBean = new ScriptableObjectBean(Global.class);

    context = HtmlUnitContextFactory.getGlobal().enterContext();
    context.setOptimizationLevel(-1);
    scope = (ScriptableObject)context.initStandardObjects(shell);

    Shell.setResolver("lib", new LibraryResolver("/org/moyrax/javascript/lib"));
    Shell.setResolver("classpath", new ClassPathResolver());

    /* Adds the global functions to the scope. */
    scope.defineFunctionProperties(shellBean.getFunctionNames(),
        shellBean.getScriptableClass(), ScriptableObject.DONTENUM);
    scope.defineFunctionProperties(globalBean.getFunctionNames(),
            globalBean.getScriptableClass(), ScriptableObject.DONTENUM);
  }

  @AfterClass
  public static void afterClass() {
    Shell.setResolver("lib", null);
    Shell.setResolver("classpath", null);
  }

  @Test
  public void testIncludeLib() throws Exception {
    final String[] includes = new String[] {
      "lib:/env.js", "lib:/qunit.js"
    };

    Shell.include(context, scope, includes, null);
  }

  @Test
  public void testIncludeClassPath() throws Exception {
    final String[] includes = new String[] {
      "classpath:/org/moyrax/javascript/lib/env.js"
    };

    Shell.include(context, scope, includes, null);
  }

  @Test
  public void testIncludeResource() throws Exception {
    final String[] includes = new String[] {
      "qunit-objects.js"
    };

    final File basePath = new File(
        Thread.currentThread().getContextClassLoader()
          .getResource("org/moyrax/javascript/lib/env.js").getPath())
      .getParentFile();

    final File[] contextPath = new File[] { basePath };

    Shell.setContextPath(contextPath);

    Shell.include(context, scope, includes, null);
  }

  @Test(expected = JavaScriptException.class)
  public void testInvalidProtocol() throws Exception {
    final String[] includes = new String[] {
      "foo:/env.js", "lib:/qunit.js"
    };

    Shell.include(context, scope, includes, null);
  }
}
