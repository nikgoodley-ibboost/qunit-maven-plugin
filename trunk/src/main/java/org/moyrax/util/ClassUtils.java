package org.moyrax.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.Validate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * Miscellaneous class utility methods. This provides a set of methods to
 * help classes instrospection.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ClassUtils {
  /**
   * Searches for all classes in the given package. It just returns the classes
   * which are visible for current {@link ClassLoader}.
   *
   * @param packagePattern A regular expression pattern which contains the path
   *    to list the classes. It cannot be null or empty.
   *
   * @return If the pattern contains classes it returns a list of those classes,
   *    otherwise returns an empty {@link List}.
   */
  public static ArrayList<Class<?>> lookup(final String packagePattern) {
    Validate.notEmpty(packagePattern, "The package cannot be null or empty.");

    return lookup(packagePattern,
        Thread.currentThread().getContextClassLoader());
  }

  /**
   * Searches for all classes in the given package and apply the a filter to
   * the result. It uses the specified {@link ClassLoader} to lookup the
   * classes.
   *
   * @param packagePattern A regular expression pattern which contains the path
   *    to list the classes. It cannot be null or empty.
   * @param filter Object used to filter the results. It cannot be null.
   *
   * @return If the pattern contains classes it returns a list of those classes,
   *    otherwise returns an empty {@link List}.
   */
  public static ArrayList<Class<?>> lookup(final String packagePattern,
      final Predicate filter) {

    Validate.notNull(filter, "The filter cannot be null");

    return lookup(packagePattern, filter,
        Thread.currentThread().getContextClassLoader());
  }

  /**
   * Searches for all classes in the given package and apply the a filter to
   * the result. It uses the specified {@link ClassLoader} to lookup the
   * classes.
   *
   * @param packagePattern A regular expression pattern which contains the path
   *    to list the classes. It cannot be null or empty.
   * @param filter Object used to filter the results. It cannot be null.
   * @param classLoader ClassLoader that contains the classes. It cannot be
   *    null.
   *
   * @return If the pattern contains classes it returns a list of those classes,
   *    otherwise returns an empty {@link List}.
   */
  public static ArrayList<Class<?>> lookup(final String packagePattern,
      final Predicate filter, final ClassLoader classLoader) {

    Validate.notNull(filter, "The filter cannot be null");
    Validate.notNull(classLoader, "The ClassLoader cannot be null");

    ArrayList<Class<?>> classes = lookup(packagePattern, classLoader);

    CollectionUtils.filter(classes, filter);

    return classes;
  }

  /**
   * Searches for all classes in the given package. It just returns the classes
   * which are visible for the specified {@link ClassLoader}.
   *
   * @param packagePattern A regular expression pattern which contains the path
   *    to list the classes. It cannot be null or empty.
   * @param classLoader ClassLoader that contains the classes. It cannot be
   *    null.
   *
   * @return If the pattern contains classes it returns a list of those classes,
   *    otherwise returns an empty {@link List}.
   */
  public static ArrayList<Class<?>> lookup(final String packagePattern,
      final ClassLoader classLoader) {

    // TODO(mmirabelli): There's a bug with some patterns because the used
    // ResourcePatternResolver matches the whole file names (including the
    // .class extension) of the classes. It implies that if the pattern does
    // not ends with *, the classes won't be discovered. Find a way to solve it.
    Validate.notEmpty(packagePattern, "The package cannot be null or empty.");
    Validate.notNull(classLoader, "The class loader cannot be null.");

    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

    try {
      final PathMatchingResourcePatternResolver resolver =
          new PathMatchingResourcePatternResolver(classLoader);

      final Resource[] resources = resolver.getResources(packagePattern);

      for (Resource resource : resources) {
        Class<?> clazz = getClassFromResource(resource, resolver, classLoader);

        if (clazz != null) {
          classes.add(clazz);
        }
      }
    } catch (IOException ex) {
      throw new IllegalArgumentException("Cannot list classes in the given "
          + "package: " + packagePattern, ex);
    }

    return classes;
  }

  /**
   * Returns a {@link MetadataReader} for the specified {@link Resource}.
   *
   * @param resource Resource related to class to search for. It cannot be null.
   *
   * @return If the resource represents a valid class, returns the {@link Class}
   *    object that it represents, otherwise returns <code>null</code>.
   */
  public static Class<?> getClassFromResource(final Resource resource) {

    PathMatchingResourcePatternResolver resolver =
      new PathMatchingResourcePatternResolver();

    Validate.notNull(resource, "The resource cannot be null.");

    return getClassFromResource(resource, resolver,
        Thread.currentThread().getContextClassLoader());
  }

  /**
   * Returns a {@link MetadataReader} for the specified {@link Resource}.
   *
   * @param resource Resource related to class to search for. It cannot be null.
   * @param resolver Resolver used to lookup the resource. It cannot be null.
   *
   * @return If the resource represents a valid class, returns the {@link Class}
   *    object that it represents, otherwise returns <code>null</code>.
   */
  public static Class<?> getClassFromResource(final Resource resource,
      final ResourcePatternResolver resolver, final ClassLoader classLoader) {

    Validate.notNull(resource, "The resource cannot be null.");
    Validate.notNull(resolver, "The resolver cannot be null.");

    if (!resource.isReadable() ||
        !resource.getFilename().endsWith(".class")) {
      return null;
    }

    Class<?> clazz = null;
    MetadataReader reader = null;

    try {
      final MetadataReaderFactory metadataReaderFactory =
          new CachingMetadataReaderFactory(resolver);

      reader = metadataReaderFactory.getMetadataReader(resource);

      clazz = classLoader.loadClass(reader.getClassMetadata().getClassName());

    } catch (IOException ex) {
      // We don't need to check this exception since it means that probably the
      // given resource is not a valid class.
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException("The class related to the given "
          + "resource cannot be loaded using the current ClassLoader.", ex);
    }

    return clazz;
  }
}
