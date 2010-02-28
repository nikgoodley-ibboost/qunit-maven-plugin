package org.moyrax.maven;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.moyrax.javascript.Shell;
import org.moyrax.resolver.ClassPathResolver;
import org.moyrax.resolver.LibraryResolver;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

/**
 * Tests the {@link Shell} class.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class ShellTest {
  private static Shell shell;
  private static Context context;
  private static Scriptable scope;

  @BeforeClass
  public static void beforeClass() {
    shell = new Shell();

    context = ContextFactory.getGlobal().enterContext();
    context.setOptimizationLevel(-1);
    scope = context.initStandardObjects(shell);

    shell.init(context);

    Shell.setResolver("lib", new LibraryResolver("/org/moyrax/javascript/lib"));
    Shell.setResolver("classpath", new ClassPathResolver());
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
