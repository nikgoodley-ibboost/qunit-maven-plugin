package org.moyrax.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.lang.Validate;

/**
 * This class creates an {@link AbstractContextHandler} and set ups the context
 * using the configured information.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ContextHandlerFactory {
  /**
   * Keeps the mapping of paths to servlets.
   */
  private static Map<String, HttpServlet> servletMappings =
      new HashMap<String, HttpServlet>();

  /**
   * Maps a new servlet that will be managed by the handlers. If the mapping
   * already exists it throws an exception.
   *
   * @param servletPath Relative path where the servlet will be exposed. It
   *    cannot be null or empty.
   * @param servlet Servlet that will be executed when the current target is
   *    the specified path. It cannot be null.
   * @throws Throws an {@link IllegalArgumentException} if a servlet is already
   *    registered for the specified path.
   */
  public static void addServletMapping(final String servletPath,
      final HttpServlet servlet) {

    Validate.notEmpty(servletPath, "The servletPath cannot be null or empty.");
    Validate.notNull(servlet, "The parameter servlet cannot be null.");

    if (servletMappings.containsKey(servletPath)) {
      throw new IllegalArgumentException("Already there's a servlet mapped " +
          "in '" + servletPath + "'");
    }

    servletMappings.put(servletPath, servlet);
  }

  /**
   * Builds a new handler of the specified class.
   *
   * @param clazz Class of the handler to create. It cannot be null.
   *
   * @return Returns the new instance of the specified
   *    {@link AbstractContextHandler}.
   * @throws Throws a {@link ServletException} if the context handler cannot be
   *    created.
   */
  public static AbstractContextHandler getHandler(
      final Class<? extends AbstractContextHandler> clazz)
        throws ServletException {

    Validate.notNull(clazz, "The parameter clazz cannot be null.");

    AbstractContextHandler handler = null;

    try {
      handler = clazz.newInstance();
      handler.setServletMappings(servletMappings);
    } catch (IllegalAccessException ex) {
      throw new ServletException("Permission denied building the context "
          + "handler", ex);
    } catch (InstantiationException ex) {
      throw new ServletException("Cannot build the context handler", ex);
    }

    return handler;
  }
}
