package org.moyrax.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.Type;

/**
 * A Method provides information about, and access to, a single method on a
 * class or interface. The reflected method may be a class method or an
 * instance method (including an abstract method).
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class Method extends AccessibleObject implements Member {
  /** Class name representing the class or interface that declares the method
     represented by this Method object. */
  private final String declaringClass;

  /** Class names that represent the types of the exceptions declared to be
     thrown by the underlying method represented by this Method object. */
  private final String[] exceptions;

  /** Name of the method represented by this Method object, as a String. */
  private final String name;

  /** Java language modifiers for the method represented by this Method object,
      as an integer. */
  private final int modifiers;

  /** Array of Class names that represent the formal parameter types, in
     declaration order, of the method represented by this Method object. */
  private final Type[] parameterTypes;

  /** Class object that represents the formal return type of the method
     represented by this Method object. */
  private final Type returnType;

  /** Annotations that are directly present on this element. */
  private final Annotation[] annotations;

  /** Annotations that are directly present on the Method parameters. */
  private final Annotation[][] parameterAnnotations;

  /** The method's descriptor. */
  private final String descriptor;

  /**
   * Creates a new Method and initializes its information.
   *
   * @param theDeclaringClass Class name representing the class or interface
   *    that declares the method represented by this Method object. It cannot be
   *    null or empty.
   *
   * @param theExceptions Class names that represent the types of the
   *    exceptions declared to be thrown by the underlying method represented
   *    by this Method object. It cannot be null.
   *
   * @param theName Name of the method represented by this Method object, as a
   *    String. It cannot be null or empty.
   *
   * @param theModifiers Java language modifiers for the method represented by
   *    this Method object, as an integer.
   *
   * @param theParameterTypes Array of Class names that represent the formal
   *    parameter types, in declaration order, of the method represented by
   *    this Method object. It cannot be null.
   *
   * @param theReturnType Class object that represents the formal return type
   *    of the method represented by this Method object. It can be null for
   *    void methods.
   *
   * @param theAnnotations Annotations that are directly present on this
   *    element. It can be null.
   * 
   * @param theDescriptor The method's descriptor. It cannot be null or empty.
   */
  public Method(final String theDeclaringClass,
      final String[] theExceptions, final String theName,
      final int theModifiers, final Type[] theParameterTypes,
      final Type theReturnType, final Annotation[] theAnnotations,
      final Annotation[][] theParameterAnnotations,
      final String theDescriptor) {

    Validate.notEmpty(theDeclaringClass, "The declaring class cannot be null or"
        + " empty.");
    Validate.notNull(theExceptions, "The exceptions cannot be null.");
    Validate.notEmpty(theName, "The method's name cannot be null or empty.");
    Validate.notNull(theParameterTypes, "The parameter types cannot be null.");
    Validate.notEmpty(theDescriptor, "The method's name cannot be null or empty.");

    declaringClass = theDeclaringClass;
    exceptions = theExceptions;
    name = theName;
    modifiers = theModifiers;
    parameterTypes = theParameterTypes;
    returnType = theReturnType;
    annotations = theAnnotations;
    parameterAnnotations = theParameterAnnotations;
    descriptor = theDescriptor;
  }

  /**
   * Returns the <code>Class</code> object representing the class or interface
   * that declares the method represented by this <code>Method</code> object.
   */
  public Class<?> getDeclaringClass() {
    return TypeResolver.resolve(Type.getObjectType(declaringClass));
  }

  /**
   * Returns the <code>String</code> object representing the class or interface
   * name that declares the method represented by this <code>Method</code> object.
   */
  public String getDeclaringClassName() {
    return declaringClass;
  }

  /**
   * Returns an array of <code>Class</code> objects that represent 
   * the types of the exceptions declared to be thrown
   * by the underlying method
   * represented by this <code>Method</code> object.  Returns an array of length
   * 0 if the method declares no exceptions in its <code>throws</code> clause.
   * 
   * @return the exception types declared as being thrown by the
   * method this object represents
   */
  public Class<?>[] getExceptionTypes() {
    Class<?>[] params = new Class<?>[parameterTypes.length];

    for (int i = 0, j = parameterTypes.length; i < j; i++) {
      params[i] = TypeResolver.resolve(parameterTypes[i]);
    }

    return params;
  }

  /**
   * Returns an array of <code>String</code> objects that represent 
   * the name of the exceptions types declared to be thrown
   * by the underlying method
   * represented by this <code>Method</code> object.  Returns an array of length
   * 0 if the method declares no exceptions in its <code>throws</code> clause.
   * 
   * @return the exception types declared as being thrown by the
   * method this object represents
   */
  public String[] getExceptionTypeNames() {
    return exceptions;
  }

  /**
   * Returns the name of the method represented by this <code>Method</code> 
   * object, as a <code>String</code>.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the Java language modifiers for the method represented
   * by this <code>Method</code> object, as an integer. The <code>Modifier</code> class should
   * be used to decode the modifiers.
   *
   * @see Modifier
   */
  public int getModifiers() {
    return modifiers;
  }

  /**
   * Returns an array of <code>Class</code> objects that represent the formal
   * parameter types, in declaration order, of the method
   * represented by this <code>Method</code> object.  Returns an array of length
   * 0 if the underlying method takes no parameters.
   * 
   * @return the parameter types for the method this object
   * represents
   */
  public Class<?>[] getParameterTypes() {
    Class<?>[] params = new Class<?>[parameterTypes.length];

    for (int i = 0, j = parameterTypes.length; i < j; i++) {
      params[i] = TypeResolver.resolve(parameterTypes[i]);
    }

    return params;
  }

  /**
   * Returns an array of <code>String</code> objects that represent the formal
   * parameter types, in declaration order, of the method
   * represented by this <code>Method</code> object.  Returns an array of length
   * 0 if the underlying method takes no parameters.
   * 
   * @return the parameter types for the method this object
   * represents
   */
  public String[] getParameterTypeNames() {
    String[] params = new String[parameterTypes.length];

    for (int i = 0, j = parameterTypes.length; i < j; i++) {
      params[i] = parameterTypes[i].getClassName();
    }

    return params;
  }

  /**
   * Returns a <code>Class</code> object that represents the formal return type
   * of the method represented by this <code>Method</code> object.
   * 
   * @return the return type for the method this object represents
   */
  public Class<?> getReturnType() {
    return TypeResolver.resolve(returnType);
  }

  /**
   * Returns a <code>String</code> object that represents the formal return type
   * class of the method represented by this <code>Method</code> object.
   * 
   * @return the return type for the method this object represents
   */
  public String getReturnTypeName() {
    return returnType.getClassName();
  }

  /**
   * {@inheritDoc}
   */
  public Annotation[] getAnnotations() {
    return annotations;
  }

  /**
   * {@inheritDoc}
   */
  public Annotation[] getDeclaredAnnotations() {
    return annotations;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public <T extends Annotation> T getAnnotation(
      final Class<T> annotationClass) {

    for (Annotation ann : annotations) {
      if (ann.annotationType().equals(annotationClass)) {
        return (T)ann;
      }
    }

    return null;
  }

  /**
   * Returns an array of arrays that represent the annotations on the formal
   * parameters, in declaration order, of the method represented by
   * this <tt>Method</tt> object. (Returns an array of length zero if the
   * underlying method is parameterless.  If the method has one or more
   * parameters, a nested array of length zero is returned for each parameter
   * with no annotations.) The annotation objects contained in the returned
   * arrays are serializable.  The caller of this method is free to modify
   * the returned arrays; it will have no effect on the arrays returned to
   * other callers.
   *
   * @return an array of arrays that represent the annotations on the formal
   *    parameters, in declaration order, of the method represented by this
   *    Method object
   */
  public Annotation[][] getParameterAnnotations() {
    return parameterAnnotations;
  }

  /**
   * Determines if this method is annotated with the specified annotation.
   *
   * @param annotationClass Annotation to check if is present in this method. It
   *    cannot be null.
   *
   * @return Returns <code>true</code> if the annotation is present in this
   *    method, <code>false</code> otherwise.
   */
  public boolean isAnnotationPresent(
      final Class<? extends Annotation> annotationClass) {

    Validate.notNull(annotationClass, "The annotation class cannot be null.");

    return getAnnotation(annotationClass) != null;
  }

  /**
   * Returns <tt>true</tt> if this method is a synthetic
   * method; returns <tt>false</tt> otherwise.
   *
   * @return true if and only if this method is a synthetic
   * method as defined by the Java Language Specification.
   * @since 1.5
   */
  public boolean isSynthetic() {
    final int SYNTHETIC = 0x00001000;

    return (getModifiers() & SYNTHETIC) != 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    String string = this.getReturnTypeName();
    string += " " + this.getName();
    string += ArrayUtils.toString(this.getParameterTypeNames(), ", ");

    return string.replace("{", "(").replace("}", ")");
  }

  @Override
  public int hashCode() {
    return descriptor.hashCode();
  }
}
