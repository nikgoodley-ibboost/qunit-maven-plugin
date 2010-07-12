package org.moyrax.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.core.io.ClassPathResource;

/**
 * Utility methods for resolving resource locations to files in several ways.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 */
public class ResourceUtils {
  /**
   * Returns an {@link InputStream} for the specified script resource. The
   * <code>src</code> parameter could be a path to a file in the filesystem,
   * a classpath resource or even a valid url.
   *
   * @param src  Any uri of a valid javascript file. It cannot be null or empty.
   *
   * @throws IOException
   */
  public static InputStream getResourceInputStream(final String src)
      throws IOException {
    Validate.notEmpty(src, "The resource url cannot be null.");

    return getResourceInputStream(src, null);
  }

  /**
   * Returns an {@link InputStream} for the specified resource. The
   * <code>uri</code> parameter could be a path to a file in the filesystem,
   * a classpath resource or even a valid url.
   *
   * @param uri  Location of any valid file. It cannot be null or empty.
   * @param baseDirectory A root directory.It's used to construct the filename
   *                      if the resource represents a relative path in the
   *                      filesystem. It can be null.
   *
   * @throws IOException
   */
  public static InputStream getResourceInputStream(final String uri,
      final String baseDirectory) throws IOException {

    Validate.notEmpty(uri, "The resource url cannot be null.");

    InputStream result = null;

    if (isClassPathResource(uri)) {
      /* Checks if the source is an internal resource. */

      final String classPath = StringUtils.substringAfter(uri, "classpath:/");

      result = new ClassPathResource(classPath).getInputStream();

    } else if (isValidUrl(uri)) {
      /* Checks if the source is a remote resource. */

      final HttpClient client = new HttpClient();
      final HttpMethod method = new GetMethod(uri);

      client.executeMethod(method);
      result = new ByteArrayInputStream(method.getResponseBody());
    } else {
      /* Is the source just a file? */
      File local = new File(uri);

      /* If the file doesn't exists, uses the base directory to reference it. */
      if (!local.exists()) {
        local = new File(baseDirectory + uri);
      }

      result = new FileInputStream(local);
    }

    return result;
  }

  /**
   * Checks if a field has a valid classpath resource path.
   *
   * @param src The value validation is being performed on.
   * @return true if the path is from a resource.
   */
  public static boolean isClassPathResource(final String src) {
    return src.trim().startsWith("classpath:");
  }

  /**
   * Checks if a field has a valid url address.
   *
   * @param url The value validation is being performed on.
   * @return true if the url is valid.
   */
  public static boolean isValidUrl(final String url) {
    if (isClassPathResource(url)) {
      return false;
    }

    return org.springframework.util.ResourceUtils.isUrl(url);
  }
}
