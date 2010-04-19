package org.moyrax.maven;

/**
 * This class provides the required information and flags to execute the
 * tests using the testing server.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
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
   * @param serverPort Port number.
   */
  public void setServerPort(final Integer serverPort) {
    this.serverPort = serverPort;
  }

  /**
   * Returns the url where the testing server is listening.
   */
  public String getLocalUrl() {
    return "http://127.0.0.1:" + serverPort;
  }
}
