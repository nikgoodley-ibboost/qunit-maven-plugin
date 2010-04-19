package org.moyrax.javascript;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method is a JavaScript function. If this annotation is
 * present in a method, it will be exported by the
 * {@link ScriptableObjectBean} to the host application.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsFunction {
  /**
   * The method's name as it will be used in the JavaScript code. If this field
   * is not specified, the parser will use the method's name where this
   * annotation is placed.
   */
  String name() default "";
}
