package org.moyrax.javascript;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

/**
 * This class provides additional functions to the Rhino shell.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 0.50
 */
public class Shell extends Global {
  /** Default id for serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * List of directories included in the context path of this shell.
   */
  private static ArrayList<File> contextPath = new ArrayList<File>();

  /**
   * List of directories included in the context path of this shell.
   */
  private static ArrayList<File> excludes = new ArrayList<File>();

  /* Default constructor. */
  public Shell() {}

  /**
   *
   * @param context Current execution context.
   * @param scope   Script global scope.
   * @param arguments  Arguments passed to this method from the script.
   * @param thisObj Reference to the current javascript object.
   */
  public static void include(final Context context, final Scriptable scope,
      final Object[] arguments, final Function thisObj) {

    final ArrayList<String> files = new ArrayList<String>();

    for (int i = 0, j = arguments.length; i < j; i++) {
      final File file = getResourceFromPath((String)arguments[i]);

      if (file != null) {
        files.add(file.getAbsolutePath());
      } else {
        throw new JavaScriptException(arguments[i].toString() +
            " not found in the context path.", "", 0);
      }
    }

    load(context, scope, files.toArray(), thisObj);
  }

  /**
   * Sets the context path for this shell.
   *
   * @param contextPath List of directory included in the context path. It
   *    cannot be null.
   */
  public static void setContextPath(final File[] contextPath) {
    setContextPath(contextPath, null);
  }

  /**
   * Sets the context path for this shell.
   *
   * @param contextPath List of directory included in the context path. It
   *    cannot be null.
   * @param excludes  List of directories excluded from the context path. It
   *    can be null.
   */
  public static void setContextPath(final File[] contextPath,
      final File[] excludes) {

    Validate.notNull(contextPath, "contextPath cannot be null.");

    if (excludes != null) {
      for (int i = 0, j = excludes.length; i < j; i++) {
        Shell.excludes.add(excludes[i]);
      }
    }

    for (int i = 0, j = contextPath.length; i < j; i++) {
      parseContextPath(contextPath[i]);
    }
  }

  /**
   * Resolves the absolute path of a resource and returns a File object related
   * to the required resource. If the resource is not found, this method will
   * return <code>null</code>.
   *
   * @param resourceName Name of the resource to search for. It cannot be null
   *    or empty.
   */
  private static File getResourceFromPath(final String resourceName) {
    Validate.notNull(resourceName, "resourceName cannot be null or empty.");
    Validate.notEmpty(resourceName, "resourceName cannot be null or empty.");

    for (int i = 0, j = contextPath.size(); i < j; i++) {
      final File file = new File(((File)contextPath.get(i)).getAbsolutePath() +
          "/" + resourceName);

      if (file.exists()) {
        return file;
      }
    }

    /* File not found in the context path. Tries to retrieve the resource from
       the class path. */
    return getResourceFromClassPath(resourceName);
  }

  /**
   * Retrieves the specified resource from the classpath.
   *
   * @param resourceName Name (including classpath) of the required resource.
   *
   * @return The File representing the required resource, or null if the
   *    resource was not found.
   */
  private static File getResourceFromClassPath(final String resourceName) {

    String classPath = resourceName;

    if (classPath.startsWith("/")) {
      classPath = StringUtils.substringAfter(classPath, "/");
    }

    final URL resourceUrl = Thread.currentThread().getContextClassLoader()
         .getResource(classPath);

    File file = null;

    if (resourceUrl != null) {
      file = new File(resourceUrl.getFile());
    }

    return file;
  }

  /**
   * Takes a directory and creates a list of all its subdirectories which will
   * be used as the context path for this Rhino context.
   *
   * @param directory Directory from subdirs will be listed.
   */
  private static void parseContextPath(final File directory) {
    final File[] subdirs = directory.listFiles(directoryFilter);

    if (subdirs == null) {
      return;
    }

    for (int i = 0, j = subdirs.length; i < j; i++) {
      if (!contextPath.contains(subdirs[i])) {
        contextPath.add(subdirs[i]);
      }

      parseContextPath(subdirs[i]);
    }
  }

  /**
   * This filter allow retrieve only directories from a file listing.
   */
  private static final FileFilter directoryFilter = new FileFilter() {
    public boolean accept(final File pathname) {
      return pathname.exists() && pathname.isDirectory() &&
        !excludes.contains(pathname);
    }
  };
}
