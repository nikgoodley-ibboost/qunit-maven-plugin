package org.moyrax.javascript.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.moyrax.javascript.ScriptComponent;

/**
 * Indicates that a method is a JavaScript function visible in the current scope.
 * If this annotation is present in a method, it will be exported by the
 * {@link ScriptComponent} to the host application.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Function {
}
