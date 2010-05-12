package org.moyrax.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;

/**
 * This class is the main Web Server context which dispatches the internal
 * servlets in order to handle all the testing operations.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class WebContextHandler extends AbstractContextHandler {

  public void handle(final String target, final HttpServletRequest request,
      final HttpServletResponse response, final int dispatch)
        throws IOException, ServletException {

    final HttpServlet servlet = this.getServlet(target);

    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);

    servlet.service(request, response);

    ((Request)request).setHandled(true);
  }
}
