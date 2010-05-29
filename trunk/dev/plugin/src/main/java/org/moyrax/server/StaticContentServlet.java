package org.moyrax.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.moyrax.util.ResourceUtils;
import org.springframework.util.FileCopyUtils;

/**
 * This servlet retrieves static files from the configured classpath.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class StaticContentServlet extends HttpServlet {

  /** Default ID for serialization. */
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {

    final String classPathResource = request.getParameter("resource");

    if (classPathResource != null) {
      final InputStream resource = ResourceUtils.getResourceInputStream(
          classPathResource);
      final ByteArrayOutputStream output = new ByteArrayOutputStream();

      FileCopyUtils.copy(resource, output);
      FileCopyUtils.copy(new ByteArrayInputStream(output.toByteArray()),
          response.getOutputStream());

      response.setContentLength(output.size());
    }
  }
}
