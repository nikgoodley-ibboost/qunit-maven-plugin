package org.moyrax.javascript;

import java.util.ArrayList;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.Validate;
import org.moyrax.util.ClassUtils;
import org.moyrax.util.ScriptUtils;

/**
 * This class lookup classes in a set of configured packages and determines
 * which class can be initialized using the {@link ScriptComponent} class.
 * The classes found will be registered in the global host-script scope.
 * 
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ScriptComponentScanner {
  /**
   * List of packages which will be parsed to find {@link Scriptable} classes.
   */
  private String[] packages;

  /**
   * ClassLoader which contains the loaded classes.
   */
  private ClassLoader classLoader;

  /**
   * List of {@link Scriptable} classes.
   */
  private ArrayList<Class<?>> classes;

  /**
   * Creates a new scanner and sets the list of packages which will be parsed
   * to search for {@link Scriptable} classes.
   *
   * @param thePackages List of packages which will be parsed to find
   *    {@link Scriptable} classes. It cannot be null or empty.
   */
  public ScriptComponentScanner(final String[] thePackages) {
    this(thePackages, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates a new scanner and sets the list of packages which will be parsed
   * to search for {@link Scriptable} classes.
   *
   * @param thePackages List of packages which will be parsed to find
   *    {@link Scriptable} classes. It cannot be null or empty.
   * @param theClassLoader {@link ClassLoader} used to lookup for components. It
   *    cannot be null.
   */
  public ScriptComponentScanner(final String[] thePackages,
      final ClassLoader theClassLoader) {
    Validate.notEmpty(thePackages, "The packages cannot be null or empty.");
    Validate.notNull(theClassLoader, "The ClassLoader cannot be null.");

    this.packages = thePackages;
    this.classLoader = theClassLoader;
  }

  /**
   * Scans the configured packages for {@link Scriptable} classes.
   */
  public void scan() {
    this.classes = new ArrayList<Class<?>>();

    for (String element : this.packages) {
      this.classes.addAll(ClassUtils.lookup(element, filter, this.classLoader));
    }
  }

  /**
   * @return Returns the list of scanned classes.
   */
  public ArrayList<Class<?>> getClasses() {
    return this.classes;
  }

  /**
   * Replaces the {@link ClassLoader} used to lookup the classes.
   *
   * @param theClassLoader New {@link ClassLoader}.
   */
  public void setClassLoader(final ClassLoader theClassLoader) {
    this.classLoader = theClassLoader;
  }

  /**
   * This object is used to filter classes that implements the
   * {@link Scriptable} interface.
   */
  private static final Predicate filter = new Predicate() {
    /**
     * Returns <code>true</code> if the class is a valid {@link Scriptable}
     * class, <code>false</code> otherwise.
     */
    public boolean evaluate(final Object object) {
      return ScriptUtils.isExportable((Class<?>)object);
    }
  };
}
