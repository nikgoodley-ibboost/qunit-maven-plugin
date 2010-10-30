package org.moyrax.maven;

import org.apache.commons.lang.Validate;

/**
 * Maven plugin settings for remote script execution.
 */
public class UrlSet {
  /**
   * @parameter
   * @required
   */
  private String baseUrl;

  /**
   * @parameter
   * @required
   */
  private String[] urlFiles;

  /**
   * Default constructor, needed by maven.
   */
  public UrlSet() {}

  /**
   * Constructs a new UrlSet and sets the initial configuration.
   *
   * @param baseUrl Base url added as prefix to all remote test resources. It
   *    cannot be null.
   * @param urlFiles List of remote resources to run. It cannot be null.
   */
  public UrlSet(final String baseUrl, final String[] urlFiles) {
    Validate.notNull(baseUrl, "The base url cannot be null.");
    Validate.notNull(urlFiles, "The remote resources cannot be null.");

    this.baseUrl = baseUrl;
    this.urlFiles = urlFiles;
  }

  /**
   * Returns the base url added as prefix to the remote test resources.
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Returns the list of remote test resources.
   */
  public String[] getUrlFiles() {
    return urlFiles;
  }
}
