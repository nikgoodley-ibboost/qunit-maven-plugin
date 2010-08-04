package org.moyrax.javascript.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method is a JavaScript constructor.
 * If this annotation is present in a method, it will be invoked when the client
 * script tries to create a new instance of the object.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Constructor {
}
