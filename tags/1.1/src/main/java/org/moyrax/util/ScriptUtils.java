package org.moyrax.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
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
   * Executes the specified file.
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
   * Executes the script from an {@link InputStream}.
   *
   * @param context Current script context. It cannot be null.
   * @param scope Actual execution scope. It cannot be null.
   * @param input Input stream from the script will be readed.
   *
   * @return Returns the result of the script execution.
   * @throws JavaScriptException
   */
  public static Object run (final Context context, final Scriptable scope,
      final InputStream input) throws JavaScriptException {

    final InputStreamReader resource = new InputStreamReader(input);

    return run(context, scope, resource, "");
  }

  /**
   * Executes the script from a list of {@link InputStream}.
   *
   * @param context Current script context. It cannot be null.
   * @param scope Actual execution scope. It cannot be null.
   * @param inputList List of {@link InputStream} that will be executed.
   *
   * @return Returns the result of the script execution.
   * @throws JavaScriptException
   */
  public static void run (final Context context, final Scriptable scope,
      final List<InputStream> inputList) throws JavaScriptException {

    for (InputStream input : inputList) {
      run(context, scope, input);
    }
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
    } catch (EcmaError ex) {

      final JavaScriptException wrappedEx = new JavaScriptException(
          "Error executing script: " + name, name, 0);

      wrappedEx.initCause(ex);

      throw wrappedEx;
    } catch (IOException ex) {
      throw new JavaScriptException("Error reading script: " + name, name, 0);
    }
  }
}
