package org.moyrax.maven;

import java.io.File;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

/**
 * This class provides the required information and flags to execute the
 * tests using the testing server.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class EnvironmentConfiguration {
  /**
   * Current project's execution {@link ClassLoader}.
   */
  private ClassLoader classLoader;

  /**
   * List of included testing resources to be executed.
   */
  private String[] includes;

  /**
   * List of excluded testing resources to be executed.
   */
  private String[] excludes;

  /**
   * Base directory where the test resources are located.
   */
  private String baseDirectory;

  /**
   * List of packages that will be parsed to search for JavaScript components.
   */
  private String[] lookupPackages;

  /**
   * Sets the list of patterns to locate testing resources. All resources that
   * matches the patterns will be executed. It will be used if no configuration
   * file was found.
   *
   * @param theBaseDirectory Directory in which the resources are located.
   * @param theIncludes List of included test resources.
   * @param theExcludes List of excluded test resources.
   */
  public void setFiles(final String theBaseDirectory,
      final String[] theIncludes, final String[] theExcludes) {

    this.baseDirectory = theBaseDirectory;
    this.includes = theIncludes;
    this.excludes = theExcludes;
  }

  /**
   * Returns the current list of patterns to locate testing resources.
   */
  public String[] getIncludes() {
    return this.includes;
  }

  /**
   * Returns the list of patterns to exclude from the tests execution.
   */
  public String[] getExcludes() {
    return this.excludes;
  }

  /**
   * Returns the directory where the test resources are located.
   */
  public String getBaseDirectory() {
    if (!baseDirectory.endsWith(File.separator)) {
      baseDirectory += File.separator;
    }

    return baseDirectory;
  }

  /**
   * Sets the list of packages patterns which will be used to search for
   * JavaScript components. All the found classes will be added to the
   * host-scripts global scope.
   *
   * @param thePackages List of package patterns.
   */
  public void setLookupPackages(final String[] thePackages) {
    this.lookupPackages = thePackages;

    this.lookupPackages = (String[])ArrayUtils.add(this.lookupPackages,
        "classpath:/org/moyrax/javascript/qunit/**");
  }

  /**
   * Returns the list of packages used to lookup JavaScript components.
   */
  public String[] getLookupPackages() {
    return this.lookupPackages;
  }

  /**
   * Sets the execution class loader.
   *
   * @param theContextClassLoader Execution class loader. It cannot be null.
   */
  public void setClassLoader(final ClassLoader theContextClassLoader) {
    Validate.notNull(theContextClassLoader, "The class loader cannot be null.");

    classLoader = theContextClassLoader;
  }

  /**
   * Gets the {@link ClassLoader} used to load external resources in all
   * threads.
   *
   * @return Returns the {@link ClassLoader} configured to lookup for exportable
   *    resources.
   */
  public ClassLoader getClassLoader() {
    if (classLoader == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }

    return classLoader;
  }
}
