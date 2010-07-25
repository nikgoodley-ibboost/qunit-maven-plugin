package org.moyrax.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.moyrax.javascript.ContextClassLoader;

/**
 * This class provides the required information and flags to execute the
 * tests using the testing server.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class EnvironmentConfiguration {
  /**
   * Base path of the project that is using this plugin.
   */
  private File targetPath;

  /**
   * {@link ClassLoader} used to lookup for exportable resources.
   */
  private ContextClassLoader classLoader;

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
   * Gets the base path of the project that is using the plugin.
   *
   * @return Returns the project's base path.
   */
  public File getTargetPath() {
    if (targetPath == null) {
      throw new IllegalStateException("The project base path is not defined.");
    }

    return targetPath;
  }

  /**
   * Sets the base path of the current project.
   *
   * @param theTargetPath Base path of the project. It cannot be null or empty.
   */
  public void setTargetPath(final File theTargetPath) {
    Validate.notNull(theTargetPath, "The base path cannot be null.");

    targetPath = theTargetPath;
  }

  /**
   * Gets the {@link ClassLoader} used to load external resources in all
   * threads.
   *
   * @return Returns the {@link ClassLoader} configured to lookup for exportable
   *    resources.
   */
  public ClassLoader getClassLoader() {
    try {
      if (classLoader == null) {
        URL[] urls = new URL[] {
            new URL(getTargetPath().toURI().toURL().toExternalForm() +
                "test-classes/"),
    
            new URL(getTargetPath().toURI().toURL().toExternalForm() +
                "classes/")
        };

        classLoader = new ContextClassLoader(urls,
            this.getClass().getClassLoader());
      }

      return classLoader;

    } catch (MalformedURLException ex) {
      throw new RuntimeException("Cannot build the project's classpath.", ex);
    }
  }
}
