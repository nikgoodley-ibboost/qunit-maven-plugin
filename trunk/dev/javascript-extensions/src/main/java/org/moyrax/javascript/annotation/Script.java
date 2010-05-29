package org.moyrax.javascript.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.moyrax.javascript.ScriptComponent;

import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

/**
 * Indicates that a class can be exported to the client application as a global
 * component. If this annotation is present in a class, the
 * {@link ScriptComponent} will lookup the class for fields and methods
 * exposed to the host application.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Script {
  /**
   * The name used to reference this class in the client application. By default
   * the class name will be used.
   */
  String name() default "";
  /**
   * Allows to change the ECMA implementation. By default it uses the HTMLUnit
   * implementation inherited from Rhino.
   */
  Class<?> implementation() default ScriptableObject.class;
}
