package org.moyrax.resolver;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * This class loads JavaScript libraries included in the plugin distribution.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class LibraryResolver extends ClassPathResolver {

  /**
   * The library includes should be loaded from this classpath.
   */
  private String baseClassPath;

  /**
   * Constructs a new {@link LibraryResolver} and sets the classpath which the
   * resolver will use to search for locate library resources.
   *
   * @param baseClassPath Classpath from the resources should be loaded. It
   *    cannot be null.
   */
  public LibraryResolver(final String baseClassPath) {
    Validate.notNull(baseClassPath, "The baseClassPath parameter cannot be " +
        "null.");

    this.baseClassPath = baseClassPath;

    if (!baseClassPath.endsWith("/")) {
      this.baseClassPath += "/";
    }
  }

  /**
   * {@inheritDoc ClassPathResolver#canHandle(String)}
   */
  public int canHandle(final String uri) {
    Validate.notNull(uri, "The uri parameter cannot be null.");

    return super.canHandle(this.makeClasspath(uri));
  }

  /**
   * {@inheritDoc ResourceResolver#resolve(String)}
   *
   * @return If the resource was found, it returns an {@link InputStream},
   *    otherwise returns <code>null</code>.
   */
  public Object resolve(final String uri) {
    if (this.canHandle(uri) == ResourceResolver.CANNOT_HANDLE) {
      return null;
    }

    return super.resolve(this.makeClasspath(uri));
  }

  /**
   * Constructs the classpath from the library declaration.
   *
   * @param uri Library relative path.
   */
  private String makeClasspath(final String uri) {
    String resource = uri;

    if (resource.startsWith("lib:")) {
      resource = StringUtils.substringAfter(resource, "lib:");
    }

    if (resource.startsWith("/")) {
      resource = StringUtils.substringAfter(resource, "/");
    }

    return "classpath:" + baseClassPath + resource;
  }
}
