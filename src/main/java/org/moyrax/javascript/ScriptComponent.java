package org.moyrax.javascript;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.moyrax.javascript.annotation.Function;
import org.moyrax.javascript.annotation.GlobalFunction;
import org.moyrax.javascript.annotation.Script;
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
  private Class<?> klass;

  /**
   * The class's name.
   */
  private String className;

  /**
   * List of global function names.
   */
  private ArrayList<String> globalFunctions = new ArrayList<String>();

  /**
   * List of instance function names.
   */
  private ArrayList<String> instanceFunctions = new ArrayList<String>();

  /**
   * Constructs a new parser for the specified exportable class.
   *
   * @param klass The class to be parsed. It cannot be null.
   *
   * @throws IllegalArgumentException If the specified class is not designed
   *    to be exportable.
   */
  public ScriptComponent(
      final Class<?> theKlass) {

    Validate.notNull(theKlass, "The class cannot be null.");
    Validate.isTrue(ScriptUtils.isExportable(theKlass), "The class must be"
        + " designed to be exportable adding the Script annotation to the"
        + " class.");

    this.klass = theKlass;

    this.parse();
  }

  /**
   * @return Returns the list of global functions defined in the exportable
   *    objects.
   */
  public List<String> getGlobalFunctionNames() {
    return globalFunctions;
  }

  /**
   * @return Returns the list of instance functions defined in the exportable
   *    object.
   */
  public List<String> getFunctionNames() {
    return instanceFunctions;
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
    return klass;
  }

  /**
   * Returns the ECMA implementation of the component's class. By default it
   * uses the HTMLUnit implementation inherited from Rhino.
   */
  public Class<?> getImplementationClass() {
    return klass.getAnnotation(Script.class).implementation();
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
  private ArrayList<String> parseMethods(final Class<?> aKlass,
      Class<? extends Annotation> theAnnotation, boolean searchStatic) {

    Validate.notNull(aKlass, "The class cannot be null.");
    Validate.notNull(theAnnotation, "The annotation cannot be null.");

    final ArrayList<String> functions = new ArrayList<String>();
    final Method[] methods = aKlass.getDeclaredMethods();

    for (Method method : methods) {
      if ((!searchStatic ||
          (searchStatic && Modifier.isStatic(method.getModifiers()))) &&
          method.isAnnotationPresent(theAnnotation)) {

        final Annotation annotation = method.getAnnotation(theAnnotation);

        if (annotation != null) {
          if (annotation.getClass().equals(GlobalFunction.class) &&
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
}
