package org.moyrax.reporting;

import org.apache.commons.lang.Validate;

/**
 * Represents an operation. An operation is an execution unit and it has a
 * configurable lifecycle.
 *
 * @author Matias Mirabelli &lt;lumen.night@gmail.com&gt;
 * @since 0.2.0
 */
public class Operation<T> {
  /**
   * Object bound to this operation.
   */
  private T relatedObject;

  /**
   * Creates a new operation and sets the bound object.
   *
   * @param theRelatedObject Object bound to this operation. It cannot be null.
   */
  public Operation(final T theRelatedObject) {
    Validate.notNull(theRelatedObject, "The related object cannot be null.");

    this.relatedObject = theRelatedObject;
  }

  /**
   * Returns the object bound to this operation, if it's set.
   *
   * @return Returns the object bound to this operation if it's set, or
   *    <code>null</code> otherwise.
   */
  public T getRelatedObject() {
    return relatedObject;
  }

  /**
   * Sets the object bound to this operation.
   *
   * @param theRelatedObject Object bound to this operation. It can be null.
   */
  public void setRelatedObject(final T theRelatedObject) {
    this.relatedObject = theRelatedObject;
  }
}