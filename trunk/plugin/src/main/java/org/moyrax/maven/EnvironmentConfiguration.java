package org.moyrax.maven;

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
   * File that contains the js-test-driver configuration. By default, it will
   * be jsTestDriver.conf.
   */
  private String configFile;

  /**
   * Directory that will be used by the testing server to write the results.
   */
  private String testOutputDirectory;

  /**
   * Port used by the testing server. Default is 3137.
   */
  private Integer serverPort = 3137;

  /**
   * Base path of the project that is using this plugin.
   */
  private URL projectBasePath;

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
   * List of packages that will be parsed to search for JavaScript components.
   */
  private String[] lookupPackages;

  /**
   * Returns the list of parameters configured in this context in the format
   * that could be passed to the server main class.
   */
  public String[] getServerParameters() {
    String parameters = "";

    if (serverPort <= 0) {
      throw new IllegalArgumentException("The port must be a number greater " +
          "than 0.");
    }

    parameters += "--port " + serverPort;

    return parameters.split(" ");
  }

  /**
   * Returns the list of parameters configured in this context in the format
   * that could be passed to the client main class.
   */
  public String[] getClientParameters() {
    String parameters = "";

    if (configFile != null) {
      parameters += "--config " + configFile;
    }

    if (testOutputDirectory != null) {
      parameters += " --testOutput " + testOutputDirectory;
    }

    parameters += " --server " + this.getLocalUrl();
    parameters += " --tests all --verbose --captureConsole";

    return parameters.split(" ");
  }

  /**
   * Returns the configuration file. For more information about this file,
   * please look at the following url:
   *
   * {@linkplain http://code.google.com/p/js-test-driver/wiki/ConfigurationFile}
   */
  public String getConfigFile() {
    return configFile;
  }

  /**
   * Sets the configuration file used in this context.
   *
   * @param configFile New path and file name of the configuration file.
   */
  public void setConfigFile(final String configFile) {
    this.configFile = configFile;
  }

  /**
   * Returns the directory that the testing server will use to write the
   * results.
   */
  public String getTestOutputDirectory() {
    return testOutputDirectory;
  }

  /**
   * Sets the directory that the testing server will use to write the results.
   *
   * @param testOutputDirectory New output directory.
   */
  public void setTestOutputDirectory(final String testOutputDirectory) {
    this.testOutputDirectory = testOutputDirectory;
  }

  /**
   * Returns the port number that the testing server will use to listen testing
   * requests.
   */
  public Integer getServerPort() {
    return serverPort;
  }

  /**
   * Sets the port that the testing server will use to listen testing requests.
   *
   * @param serverPort Port number. It cannot be null.
   */
  public void setServerPort(final Integer serverPort) {
    Validate.notNull(serverPort, "The server port cannot be null.");

    this.serverPort = serverPort;
  }

  /**
   * Returns the url where the testing server is listening.
   */
  public String getLocalUrl() {
    return "http://127.0.0.1:" + serverPort;
  }

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

    this.includes = theIncludes;
    this.excludes = theExcludes;

    ContextPathBuilder.addDefinition(theBaseDirectory, theIncludes,
        theExcludes);
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
  public URL getProjectBasePath() {
    if (projectBasePath == null) {
      throw new IllegalStateException("The project base path is not defined.");
    }

    return projectBasePath;
  }

  /**
   * Sets the base path of the current project.
   *
   * @param basePath Base path of the project. It cannot be null or empty.
   */
  public void setProjectBasePath(final URL basePath) {
    Validate.notNull(basePath, "The base path cannot be null.");

    projectBasePath = basePath;
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
            new URL(getProjectBasePath().toExternalForm() +
                "test-classes/"),
    
            new URL(getProjectBasePath().toExternalForm() +
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
