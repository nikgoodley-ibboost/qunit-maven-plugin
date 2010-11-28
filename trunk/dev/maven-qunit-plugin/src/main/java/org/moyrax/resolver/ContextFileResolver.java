package org.moyrax.resolver;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * This class loads JavaScript resources from the defined context path.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class ContextFileResolver implements ResourceResolver {
  /**
   * List of directories included in the current context path.
   */
  private ArrayList<File> contextPath = new ArrayList<File>();

  /**
   * List of directories excluded from the current context path.
   */
  private ArrayList<File> excludes = new ArrayList<File>();

  /** Default constructor. */
  public ContextFileResolver() {}

  /**
   * Constructs a new {@link ContextFileResolver} and sets the context path.
   *
   * @param contextPath List of directories included in the context path. It
   *    cannot be null.
   */
  public ContextFileResolver(final File[] contextPath) {
    Validate.notNull(contextPath, "The contextPath parameter cannot be null.");

    this.setContextPath(contextPath);
  }

  /**
   * Constructs a {@link ContextFileResolver} and sets the context path.
   *
   * @param contextPath List of directories included in the context path. It
   *    cannot be null.
   * @param excludes  List of directories excluded from the context path. It
   *    can be null.
   */
  public ContextFileResolver(final File[] contextPath, final File[] excludes) {
    Validate.notNull(contextPath, "The contextPath parameter cannot be null.");
    Validate.notNull(excludes, "The excludes parameter cannot be null.");

    this.setContextPath(contextPath, excludes);
  }

  /**
   * {@inheritDoc ResourceResolver#canHandle(String)}
   */
  public int canHandle(final String uri) {
    Validate.notNull(uri, "The uri parameter cannot be null.");

    if (uri.indexOf(":") == -1 || uri.startsWith("context:")) {
      return HANDLE_SHARED;
    }

    return CANNOT_HANDLE;
  }

  /**
   * {@inheritDoc ResourceResolver#resolve(String)}
   *
   * @return If the resource was found, it returns the related {@link File},
   *    otherwise returns <code>null</code>.
   */
  public Object resolve(final String fileName) {
    if (this.canHandle(fileName) == ResourceResolver.CANNOT_HANDLE) {
      throw new IllegalArgumentException("Cannot handle the protocol for: " +
          fileName);
    }

    String resourceName = fileName;

    if (resourceName.startsWith("context:")) {
      resourceName = StringUtils.substringAfter(resourceName, "context:");
    }

    if (resourceName.startsWith("/")) {
      resourceName = StringUtils.substringAfter(resourceName, "/");
    }

    return getResourceFromPath(resourceName);
  }

  /**
   * Sets the context path for this shell.
   *
   * @param contextPath List of directories included in the context path. It
   *    cannot be null.
   */
  public void setContextPath(final File[] contextPath) {
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
  public void setContextPath(final File[] contextPath,
      final File[] excludes) {

    Validate.notNull(contextPath, "contextPath cannot be null.");

    if (excludes != null) {
      for (int i = 0, j = excludes.length; i < j; i++) {
        this.excludes.add(excludes[i]);
      }
    }

    this.contextPath = new ArrayList<File>();

    for (int i = 0, j = contextPath.length; i < j; i++) {
      parseContextPath(contextPath[i]);

      this.contextPath.add(contextPath[i]);
    }
  }

  /**
   * Takes a directory and creates a list of all its subdirectories which will
   * be used as the context path for this Rhino context.
   *
   * @param directory Directory from subdirs will be listed.
   */
  private void parseContextPath(final File directory) {
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
   * Resolves the absolute path of a resource and returns a File object related
   * to the required resource. If the resource is not found, this method will
   * return <code>null</code>.
   *
   * @param resourceName Name of the resource to search for. It cannot be null
   *    or empty.
   */
  private File getResourceFromPath(final String resourceName) {
    Validate.notNull(resourceName, "resourceName cannot be null or empty.");
    Validate.notEmpty(resourceName, "resourceName cannot be null or empty.");

    String relativePath = "";
    String finalName = resourceName;

    if (resourceName.contains("/")) {
      finalName = StringUtils.substringAfterLast(resourceName, "/");
      relativePath = StringUtils.substringBeforeLast(resourceName, "/");
    }

    if (!StringUtils.isBlank(relativePath)) {
      if (!relativePath.endsWith("/")) {
        relativePath += "/";
      }

      relativePath = relativePath.replaceAll("/", File.separator);
    }

    for (int i = 0, j = contextPath.size(); i < j; i++) {
      final File file = new File((contextPath.get(i)).getAbsolutePath() +
          "/" + relativePath + finalName);

      if (file.exists()) {
        return file;
      }
    }

    return null;
  }

  /**
   * This filter allow retrieve only directories from a file listing.
   */
  private final FileFilter directoryFilter = new FileFilter() {
    public boolean accept(final File pathname) {
      return pathname.exists() && pathname.isDirectory() &&
        !excludes.contains(pathname);
    }
  };
}
