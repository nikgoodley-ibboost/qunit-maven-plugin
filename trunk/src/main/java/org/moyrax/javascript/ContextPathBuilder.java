package org.moyrax.javascript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.moyrax.maven.Entry;

/**
 * This class manages the environment context path.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ContextPathBuilder {
  /**
   * Directories that will be included to search resources.
   */
  private static List<Entry> contextPath = new ArrayList<Entry>();

  /**
   * Add a new mapping to the context path for this plugin. It can be used to
   * force the context path instead of take it from the POM configuration.
   *
   * @param baseDirectory  Base directory. It cannot be null.
   * @param includes List of included directories in the context. It cannot be
   *    null.
   * @param excludes List of excluded directories from the context. It cannot be
   *    null.
   */
  public static void addDefinition(final String baseDirectory,
      final String[] includes, final String[] excludes) {

    Validate.notNull(baseDirectory, "The parameter baseDirectory cannot be " +
        "null.");
    Validate.notNull(baseDirectory, "The parameter includes cannot be null.");
    Validate.notNull(baseDirectory, "The parameter excludes cannot be null.");

    final FileSet fileSet = new FileSet();

    fileSet.setDirectory(baseDirectory);

    for (int i = 0, j = includes.length; i < j; i++) {
      fileSet.addInclude(includes[i]);
    }

    for (int i = 0, j = excludes.length; i < j; i++) {
      fileSet.addExclude(excludes[i]);
    }

    contextPath.add(new Entry(fileSet));
  }

  /**
   * Creates the context path from all the entries defined using the
   * <code>define()</code> method.
   */
  public static void build() {
    build(contextPath);
  }

  /**
   * Creates the context path from the specified entries.
   *
   * @param theContextPath List of entries which defines the context path.
   */
  public static void build(final List<Entry> theContextPath) {
    final FileSetManager fileSetManager = new FileSetManager();
    final HashSet<File> includes = new HashSet<File>();
    final HashSet<File> excludes = new HashSet<File>();

    for (Object entry : theContextPath) {
      final String[] includeNames = fileSetManager.getIncludedDirectories(
          ((Entry)entry).files);
      final String[] excludeNames = fileSetManager.getExcludedDirectories(
          ((Entry)entry).files);

      final String baseDir = ((Entry)entry).files.getDirectory();

      /* Adds the included directories. */
      for (int i = 0, j = includeNames.length; i < j; i++) {
        includes.add(new File(baseDir + includeNames[i]));
      }

      /* Adds the excludes directories. */
      for (int i = 0, j = excludeNames.length; i < j; i++) {
        excludes.add(new File(baseDir + excludeNames[i]));
      }
    }

    /* Sets the context path for this scope. */
    Shell.setContextPath(
        includes.toArray(new File[] {}),
        excludes.toArray(new File[] {}));
  }
}
