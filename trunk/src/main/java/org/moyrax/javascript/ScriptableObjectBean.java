package org.moyrax.javascript;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.commons.lang.Validate;

/**
 * This class parses an {@link ScriptableObject} class and extracts the methods
 * which can be invoked from the JavaScript sources. Each
 * {@link ScriptableObjectBean} represents a unique {@link ScriptableObject}.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ScriptableObjectBean {
  /**
   * {@link ScriptableObject}'s class to be parsed.
   */
  private Class<? extends ScriptableObject> klass;

  /**
   * The class's name.
   */
  private String className;

  /**
   * List of global function names.
   */
  private ArrayList<String> functionNames = new ArrayList<String>();

  /**
   * Constructs a new parser for the specified Scriptable class.
   *
   * @param klass The class to be parsed. It cannot be null.
   */
  public ScriptableObjectBean(
      final Class<? extends ScriptableObject> theKlass) {

    Validate.notNull(theKlass, "The class cannot be null.");

    this.klass = theKlass;

    this.parse();
  }

  /**
   * @return Returns the list of global functions defined in the
   * {@link ScriptableObject}.
   */
  public String[] getFunctionNames() {
    return functionNames.toArray(new String[] {});
  }

  /**
   * @return Returns the class name of the related {@link ScriptableObject}.
   */
  public String getClassName() {
    return className;
  }

  /**
   * @return Returns the {@link ScriptableObject}'s class.
   */
  public Class< ? extends ScriptableObject > getScriptableClass() {
    return klass;
  }

  /**
   * Parses the {@link ScriptableObject}.
   */
  private void parse() {
    // Retrieves the class's name.
    className = klass.getName().substring(klass.getCanonicalName()
        .lastIndexOf(".") + 1);

    // Retrieves the global methods.
    this.parseMethods(klass);
  }

  /**
   * Retrieves all global methods in the class.
   *
   * @param aKlass Class to extract the functions. It cannot be null.
   */
  private void parseMethods(final Class<? extends ScriptableObject> aKlass) {
    Validate.notNull(aKlass, "The class cannot be null.");

    final Method[] methods = aKlass.getDeclaredMethods();

    for (Method method : methods) {
      if (Modifier.isStatic(method.getModifiers()) &&
          method.isAnnotationPresent(JsFunction.class)) {
        final JsFunction annotation = method.getAnnotation(JsFunction.class);

        if (annotation.name().equals("")) {
          functionNames.add(method.getName());
        } else {
          functionNames.add(annotation.name());
        }
      }
    }
  }
}
