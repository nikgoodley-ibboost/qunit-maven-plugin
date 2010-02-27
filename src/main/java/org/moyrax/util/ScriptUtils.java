package org.moyrax.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

/**
 * This class holds all purpose methods to handle and to proccess javascript
 * resources.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class ScriptUtils {
  /**
   * Executes the specified reader.
   *
   * @param context Current script context. It cannot be null.
   * @param scope Actual execution scope. It cannot be null.
   * @param file Script file to be executed. It cannot be null.
   *
   * @return Returns the result of the script execution.
   * @throws JavaScriptExceptionJavaScriptException
   */
  public static Object run(final Context context, final Scriptable scope,
      final File file) throws JavaScriptException {

    Validate.notNull(context, "The context parameter cannot be null.");
    Validate.notNull(scope, "The scope parameter cannot be null.");
    Validate.notNull(file, "The file parameter cannot be null.");

    try {
      final Reader reader = new FileReader(file);

      return run(context, scope, reader, file.getName());
    } catch (IOException ex) {
      throw new JavaScriptException("Error opening input file.",
          file.getAbsolutePath(), 0);
    }
  }

  /**
   * Executes the script from a classpath resource.
   *
   * @param context Current script context. It cannot be null.
   * @param scope Actual execution scope. It cannot be null.
   * @param classPath Class path of the resource to be executed.
   *
   * @return Returns the result of the script execution.
   * @throws JavaScriptException
   */
  public static Object run (final Context context, final Scriptable scope,
      final String classPath) throws JavaScriptException {

    /* Retrieves the resource from the class path. */
    final InputStreamReader resource = new InputStreamReader(
        Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(classPath));

    return run(context, scope, resource,
        StringUtils.substringAfterLast(classPath, "/"));
  }

  /**
   * Executes the script using the specified reader.
   *
   * @param context Current script context. It cannot be null.
   * @param scope Actual execution scope. It cannot be null.
   * @param reader Reader from the script will be readed. It cannot be null.
   *
   * @return Returns the result of the script execution.
   * @throws JavaScriptException
   */
  public static Object run(final Context context, final Scriptable scope,
      final Reader reader, final String name) throws JavaScriptException {

    Validate.notNull(context, "The context parameter cannot be null.");
    Validate.notNull(scope, "The scope parameter cannot be null.");
    Validate.notNull(reader, "The reader parameter cannot be null.");

    try {
      /* Executes the script in the current context. */
      return context.evaluateReader(scope, reader, name, 1, null);
    } catch (Exception ex) {
      ex.printStackTrace();

      throw new JavaScriptException("Error loading script..", name, 0);
    }
  }
}
