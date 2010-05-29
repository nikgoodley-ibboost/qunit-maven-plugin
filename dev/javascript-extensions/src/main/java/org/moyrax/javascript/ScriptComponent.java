package org.moyrax.javascript;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.moyrax.javascript.annotation.Constructor;
import org.moyrax.javascript.annotation.Function;
import org.moyrax.javascript.annotation.GlobalFunction;
import org.moyrax.javascript.annotation.Script;
import org.moyrax.reflect.ClassResource;
import org.moyrax.reflect.Method;
import org.moyrax.util.ScriptUtils;

/**
 * This class parses a class designed to be a component in the client
 * application and extracts the methods which can be invoked from the JavaScript
 * sources.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ScriptComponent {
  /**
   * {@link ScriptableObject}'s class to be parsed.
   */
  private ClassResource klass;

  /**
   * The class's name.
   */
  private String className;

  /**
   * List of global function names.
   */
  private HashSet<String> globalFunctions = new HashSet<String>();

  /**
   * List of instance function names.
   */
  private HashSet<String> instanceFunctions = new HashSet<String>();

  /**
   * Constructor method.
   */
  private Method constructor;

  /**
   * Constructs a new parser for the specified exportable class.
   *
   * @param klass The class to be parsed. It cannot be null.
   *
   * @throws IllegalArgumentException If the specified class is not designed
   *    to be exportable.
   */
  public ScriptComponent(final Class<?> theKlass) {
    this(theKlass, ScriptComponent.class.getClassLoader());
  }

  /**
   * Constructs a new parser for the specified exportable class.
   *
   * @param klass The class to be parsed. It cannot be null.
   * @param classLoader The {@link ClassLoader} to locate class's resources. It
   *    cannot be null.
   *
   * @throws IllegalArgumentException If the specified class is not designed
   *    to be exportable.
   */
  public ScriptComponent(final Class<?> klass,
      final ClassLoader classLoader) {
    this(klass.getName(), classLoader);
  }

  /**
   * Constructs a new parser for the specified exportable class.
   *
   * @param klass The name of class to be parsed. It cannot be null or empty.
   * @param classLoader The {@link ClassLoader} to locate class's resources. It
   *    cannot be null.
   *
   * @throws IllegalArgumentException If the specified class is not designed
   *    to be exportable.
   */
  public ScriptComponent(final String klassName,
      final ClassLoader classLoader) {

    Validate.notEmpty(klassName, "The class cannot be null.");
    Validate.notNull(classLoader, "The class loader cannot be null.");
    Validate.isTrue(ScriptUtils.isExportable(klassName, classLoader),
        "The class must be"
        + " designed to be exportable adding the Script annotation to the"
        + " class.");

    try {
      this.klass = new ClassResource(klassName, classLoader);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Cannot read the specified"
          + " class resource", ex);
    }

    this.parse();
  }

  /**
   * @return Returns the list of global functions defined in the exportable
   *    objects.
   */
  public List<String> getGlobalFunctionNames() {
    return new ArrayList<String>(globalFunctions);
  }

  /**
   * @return Returns the list of instance functions defined in the exportable
   *    object.
   */
  public List<String> getFunctionNames() {
    return new ArrayList<String>(instanceFunctions);
  }

  /**
   * @return Returns the class name of the related exportable object.
   */
  public String getClassName() {
    return className;
  }

  /**
   * @return Returns the exportable object's class.
   */
  public Class<?> getScriptableClass() {
    return klass.getDeclaringClass();
  }

  /**
   * @return Returns the name of the exportable object's class.
   */
  public String getScriptableClassName() {
    return klass.getName();
  }

  /**
   * Returns the ECMA implementation of the component's class. By default it
   * uses the HTMLUnit implementation inherited from Rhino.
   */
  public Class<?> getImplementationClass() {
    return klass.getAnnotation(Script.class).implementation();
  }

  /**
   * Returns a {@link Method} which represent the constructor used to create
   * a host object. This method will be invoked every time the client
   * application creates a new instance of the class.
   */
  public Method getConstructor() {
    return constructor;
  }

  /**
   * Parses the exportable object.
   */
  private void parse() {
    // Retrieves the class's name.
    className = klass.getAnnotation(Script.class).name();

    if (className.equals("")) {
      className = klass.getName().substring(klass.getCanonicalName()
          .lastIndexOf(".") + 1);
    }

    // Retrieves the global methods.
    globalFunctions = this.parseMethods(klass, GlobalFunction.class, true);

    // Retrieves the instance methods.
    instanceFunctions = this.parseMethods(klass, Function.class, false);

    // Retrieves the constructor.
    constructor = findConstructor();
  }

  /**
   * Retrieves all global methods in the class.
   *
   * @param aKlass Class to extract the functions. It cannot be null.
   * @param theAnnotation The annotation which will be present to identify the
   *    method as exportable. It cannot be null.
   * @param searchStatic Indicates if the method must search only in static
   *    methods. Default is <code>false</code>.
   */
  private HashSet<String> parseMethods(final ClassResource aKlass,
      Class<? extends Annotation> theAnnotation, boolean searchStatic) {

    Validate.notNull(aKlass, "The class cannot be null.");
    Validate.notNull(theAnnotation, "The annotation cannot be null.");

    final HashSet<String> functions = new HashSet<String>();
    final Method[] methods = aKlass.getMethods();

    for (Method method : methods) {
      if ((!searchStatic ||
          (searchStatic && Modifier.isStatic(method.getModifiers()))) &&
          method.isAnnotationPresent(theAnnotation)) {

        final Annotation annotation = method.getAnnotation(theAnnotation);

        if (annotation != null) {
          if (annotation.annotationType().equals(GlobalFunction.class) &&
              !((GlobalFunction)annotation).name().equals("")) {
            functions.add(((GlobalFunction)annotation).name());
          } else {
            functions.add(method.getName());
          }
        }
      }
    }

    return functions;
  }

  /**
   * Finds the first method annotated with the {@link Constructor} field.
   *
   * @return Returns the first {@link Method} which contains the
   *    {@link Constructor} annotation, or <code>null</code> if there's no one.
   */
  private Method findConstructor() {
    final Method[] methods = klass.getMethods();

    for (Method method : methods) {
      if (!Modifier.isStatic(method.getModifiers()) &&
          method.isAnnotationPresent(Constructor.class)) {
        return method;
      }
    }

    return null;
  }
}
