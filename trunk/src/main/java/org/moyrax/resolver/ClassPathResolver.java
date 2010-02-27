package org.moyrax.resolver;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * This class loads JavaScript files from the application classpath.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class ClassPathResolver implements ResourceResolver {
  /**
   * {@inheritDoc ResourceResolver#canHandle(String)}
   */
  public int canHandle(final String uri) {
    Validate.notNull(uri, "The uri parameter cannot be null.");

    int result = ResourceResolver.CANNOT_HANDLE;

    if (uri.startsWith("classpath:")) {
      result = ResourceResolver.HANDLE_EXCLUSIVE;
    }

    return result;
  }

  /**
   * {@inheritDoc ResourceResolver#resolve(String)}
   *
   * @return If the resource was found, it returns the related {@link File},
   *    otherwise returns <code>null</code>.
   */
  public Object resolve(final String uri) {
    if (this.canHandle(uri) == ResourceResolver.CANNOT_HANDLE) {
      return null;
    }

    String classPath = uri;

    if (classPath.startsWith("classpath:")) {
      classPath = StringUtils.substringAfter(classPath, "classpath:");
    }

    if (classPath.startsWith("/")) {
      classPath = StringUtils.substringAfter(classPath, "/");
    }

    final URL resourceUrl = Thread.currentThread().getContextClassLoader()
         .getResource(classPath);

    File file = null;

    if (resourceUrl != null) {
      file = new File(resourceUrl.getFile());
    }

    return file;
  }

}
