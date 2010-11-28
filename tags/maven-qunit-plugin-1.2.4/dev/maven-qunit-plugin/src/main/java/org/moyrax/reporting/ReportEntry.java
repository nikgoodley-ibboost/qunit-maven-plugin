package org.moyrax.reporting;

import org.apache.commons.lang.Validate;

/**
 * Contains the basic information of a report generated by a {@link Reporter}.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail&gt;
 * @since 1.2.2
 */
public abstract class ReportEntry {
  /**
   * The report's name.
   */
  private String name;

  /**
   * Group which this entry belongs to.
   */
  private String group;

  /**
   * Name of the source file related to this test.
   */
  private String sourceName;

  /** Default constructor. */
  public ReportEntry() {}

  /**
   * Creates a new test case.
   *
   * @param aName Test name. It cannot be null or empty.
   */
  public ReportEntry(final String aName) {
    Validate.notEmpty(aName, "The name cannot be null or empty.");

    this.name = aName;
  }

  /**
   * Returns the report's name. The report name will be used to generate
   * meaningful output from the {@link Reporter}s.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets this report's name.
   *
   * @param aName Report name. It cannot be null or empty.
   */
  public void setName(final String aName) {
    this.name = aName;
  }

  /**
   * Returns the group which this entry belongs to.
   */
  public String getGroup() {
    return group;
  }

  /**
   * Sets the group which this entry belongs to.
   *
   * @param aGroup Name of the group. It can be null.
   */
  public void setGroup(final String aGroup) {
    this.group = aGroup;
  }

  /**
   * Returns the name of the source file related to this test.
   *
   * @return A String representing the source file name.
   */
  public String getSourceName() {
    return sourceName;
  }

  /**
   * Sets the name of the source file related to this test.
   *
   * @param theSourceName Name of the source file related to this test. It can
   *    be null.
   */
  public void setSourceName(final String theSourceName) {
    this.sourceName = theSourceName;
  }
}
