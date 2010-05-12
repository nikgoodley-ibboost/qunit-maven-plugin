package org.moyrax.maven;

import java.util.List;

import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Represents a context path entry in the POM file.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class Entry {
  /**
   * Files to be included in the context path.
   *
   * @parameter
   */
  public FileSet files;

  /**
   * List of classpath urls in which lookup for exportable Java classes.
   *
   * @parameter
   */
  public List<String> components;

  /** Default constructor. */
  public Entry() {}

  /**
   * Creates a new {@link Entry} and sets the default configuration.
   *
   * @param theFiles {@link FileSet} to be added in the context path.
   */
  public Entry(final FileSet theFiles) {
    this.files = theFiles;
  }
}
