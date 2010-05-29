package org.moyrax.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang.Validate;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * The {@link AbstractContextHandler} class must be extended by those classes
 * that tents to manage a set of servlet contexts. It allows to register several
 * initialization strategies and set ups the servlets context.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public abstract class AbstractContextHandler extends AbstractHandler {
  /**
   * Keeps the mapping of paths to servlets.
   */
  private Map<String, HttpServlet> servletMappings =
      new HashMap<String, HttpServlet>();

  /**
   * Sets the servlet mappings for this context.
   *
   * @param theServletMappings The mapping from paths to servlets. It cannot be
   *    null.
   */
  public void setServletMappings(Map<String, HttpServlet> theServletMappings) {
    Validate.notNull(theServletMappings);

    this.servletMappings = theServletMappings;
  }

  /**
   * Retrieves a servlet that's listening in the specified path.
   *
   * @param servletPath The path of the required servlet. It cannot be null.
   *
   * @return Returns the mapped {@link HttpServlet} published in the specified
   *    path, or throws an exception if there's no servlet.
   * @throws Throws a {@link ServletException} if there's no servlet published
   *    in the specified path.
   */
  protected HttpServlet getServlet(final String servletPath)
      throws ServletException {

    Validate.notNull(servletPath, "The servletPath cannot be null.");

    if (!this.servletMappings.containsKey(servletPath)) {
      throw new ServletException("There's not servlet in the specified url: " +
          this.servletMappings);
    }

    return this.servletMappings.get(servletPath);
  }
}
