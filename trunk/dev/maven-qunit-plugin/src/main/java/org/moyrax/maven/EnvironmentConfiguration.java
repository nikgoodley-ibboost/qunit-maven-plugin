package org.moyrax.maven;

import java.io.File;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

/**
 * This class provides the required information and flags to execute the
 * tests using the testing server.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2.0
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
   * List of remote testing resources to be executed.
   */
  private String[] urlFiles;

  /**
   * Base URL from whence remote test resources are sourced.
   */
  private String baseUrl;

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
   * Sets the remote resources to be executed.
   *
   * @param theBaseUrl The base URL to append as prefix to the resources. It
   *    can be null.
   * @param theUrlFiles List of remote resources to run. It can be null.
   */
  public void setUrls(final String theBaseUrl, final String[] theUrlFiles) {
    this.baseUrl = theBaseUrl;
    this.urlFiles = theUrlFiles;
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
    if (baseDirectory != null && !baseDirectory.endsWith(File.separator)) {
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

  /**
   * Returns the list of remote test resources to run.
   */
  public String[] getUrlFiles() {
    return urlFiles;
  }

  /**
   * Returns the base URL added as prefix to all remote resources.
   */
  public String getBaseUrl() {
    return baseUrl;
  }
}

