package org.moyrax.reflect;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import sun.reflect.annotation.AnnotationParser;
import sun.reflect.annotation.AnnotationType;

/**
 * This class reads a {@link Class} resource and parses all the fields and
 * methods. It allows to retrieve information without loading the class into
 * memory, so it makes possible to instrument the resource before mapping it
 * to the JVM cache.
 *
 * The class also support the basic Java Reflection, and will use it if
 * available instead of ASM.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ClassResource extends ClassAdapter implements Serializable,
  GenericDeclaration, java.lang.reflect.Type, AnnotatedElement {

  /** Default id for serialization. */
  private static final long serialVersionUID = -8120951650118655721L;

  /** Internal cache types. */
  private enum CacheType {
    ANNOTATIONS,
    FIELDS,
    METHODS
  }

  /** {@link ClassReader} to parse the script class. */
  private ClassReader reader;

  /**
   * Indicates if the class is already parsed. Default is <code>false</code>.
   */
  private boolean transformed;

  /**
   * {@link ClassLoader} used to locate resources and related classes.
   */
  private ClassLoader classLoader;

  /** This cache contains the loaded fields and methods in order to keep a
     unique instance of each object in the class. */
  private HashMap<CacheType, HashMap<String, Object>> cache = new
    HashMap<CacheType, HashMap<String, Object>>();

  /**
   * Creates a new {@link ClassResource} to read the specified class.
   *
   * @param klass Class to read. It cannot be null.
   *
   * @throws IOException If the class is not found or it cannot be readed.
   */
  public ClassResource (final Class<?> klass) throws IOException {
    this(klass, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates a new {@link ClassResource} to read the specified class.
   *
   * @param klass Class to read. It cannot be null.
   *
   * @throws IOException If the class is not found or it cannot be readed.
   */
  public ClassResource (final Class<?> klass, ClassLoader classLoader)
    throws IOException {

    this(klass.getName(), classLoader);
  }

  /**
   * Creates a new {@link ClassResource} to read the specified class.
   *
   * @param name Class to read. It cannot be null or empty.
   *
   * @throws IOException If the class is not found or it cannot be readed.
   */
  public ClassResource (final String name) throws IOException {
    this(name, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates a new {@link ClassResource} to read the specified class. Also uses
   * the given {@link ClassLoader} to locate the class.
   *
   * @param name Class to read. It cannot be null or empty.
   * @param classLoader The {@link ClassLoader} used to search for the class. It
   *    cannot be null.
   *
   * @throws IOException If the class is not found or it cannot be readed.
   */
  public ClassResource (final String name,
      final ClassLoader theClassLoader) throws IOException {

    this(new ClassReader(theClassLoader.getResourceAsStream(
        name.replace(".", "/") + ".class")));

    this.classLoader = theClassLoader;
  }

  /**
   * This constructor is used to keep the {@link ClassReader} and initializes
   * the proper visitor.
   *
   * @param classReader {@link ClassReader} related to the working class. It
   *    shouldn't be null.
   */
  private ClassResource (final ClassReader classReader) {
    super(new ClassNode());

    this.reader = classReader;

    /* Initializes the caches. */
    cache.put(CacheType.ANNOTATIONS, new HashMap<String, Object>());
    cache.put(CacheType.FIELDS, new HashMap<String, Object>());
    cache.put(CacheType.METHODS, new HashMap<String, Object>());
  }

  /**
   * If the class or interface represented by this Class object is a member of
   * another class, returns the Class object representing the class in which
   * it was declared. This method returns null if this class or interface is
   * not a member of any other class. If this Class object represents an array
   * class, a primitive type, or void,then this method returns null.
   *
   * @return The declaring class for this class.
   */
  public Class<?> getDeclaringClass() {
    Class<?> result;

    try {
      result = classLoader.loadClass(getNode().name.replace("/", "."));
    } catch (ClassNotFoundException ex) {
      throw new IllegalStateException("Cannot load the class "
          + getNode().name, ex);
    }

    return result;
  }

  /**
   * Returns the name of the entity (class, interface, array class, primitive
   * type, or void) represented by this Class object, as a {@link String}.
   *
   * @return The class name.
   */
  public String getName() {
    return getNode().name.replace("/", ".");
  }

  /**
   * Returns the name of the entity (class, interface, array class, primitive
   * type, or void) represented by this Class object, as a {@link String}.
   *
   * @return The class name.
   */
  public String getCanonicalName() {
    return getNode().name.replace("/", ".").replace("$", ".") + "[]";
  }

  /**
   * Determines if the class contains the specified annotation.
   *
   * @param className Name of the annotation's class. It cannot be null or
   *    empty.
   *
   * @return Returns <code>true</code> if the class contains the given
   *    annotation, <code>false</code> otherwise.
   */
  public boolean isAnnotationPresent(final String className) {
    return findAnnotation(className) != null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return isAnnotationPresent(annotationClass.getName());
  }

  /**
   * {@inheritDoc}
   */
  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }

  /**
   * Returns this element's annotation for the specified type if such an
   * annotation is present, otherwise returns <code>null</code>.
   *
   * @param annotationClass Annotation's class.
   */
  @SuppressWarnings("unchecked")
  public <A extends Annotation> A getAnnotation(final Class<A> annotationClass) {
    return (A)buildAnnotation(findAnnotation(annotationClass.getName()));
  }

  /**
   * Returns this element's annotation for the specified type if such an
   * annotation is present, otherwise returns <code>null</code>.
   *
   * @param className Annotation's class name.
   */
  public Annotation getAnnotation(final String className) {
    return buildAnnotation(findAnnotation(className));
  }

  /**
   * {@inheritDoc}
   */
  public Annotation[] getAnnotations() {
    return getAnnotations(getNode().visibleAnnotations);
  }

  /**
   * Returns the methods which has this class.
   */
  public Method[] getMethods() {
    ClassNode node = getNode();

    if (node.methods == null) {
      return new Method[0];
    }

    Method[] result = new Method[node.methods.size()];

    for (int i = 0, j = node.methods.size(); i < j; i++) {
      result[i] = buildMethod((MethodNode)node.methods.get(i));
    }

    return result;
  }

  /**
   * Converts a list of {@link AnnotationNode} to the related
   * {@link Annotation}s objects.
   *
   * @param annotations List of annotations. If it's null, the method will
   *    return an empty array.
   */
  private Annotation[] getAnnotations(final List<?> annotations) {
    if (annotations == null) {
      return new Annotation[0];
    }

    Annotation[] result = new Annotation[annotations.size()];

    for (int i = 0, j = annotations.size(); i < j; i++) {
      result[i] = buildAnnotation((AnnotationNode)annotations.get(i));
    }

    return result;
  }

  /**
   * @return Returns the byte array which contains the code of the transformed
   * class.
   */
  private ClassNode getNode() {
    if (!transformed) {
      reader.accept(this, 8);

      transformed = true;
    }

    return (ClassNode)cv;
  }

  /**
   * Searches for an annotation from its name.
   *
   * @param name Annotation to search for. It cannot be null or empty.
   *
   * @return Returns the {@link AnnotationNode} representing the required
   *   annotation, or <code>null</code> if it's not found.
   */
  private AnnotationNode findAnnotation(final String name) {
    Validate.notEmpty(name, "The annotation name cannot be null or empty.");

    ClassNode node = getNode();

    if (node.visibleAnnotations != null) {
      for (int i = 0, j = node.visibleAnnotations.size(); i < j; i++) {
        AnnotationNode ann = (AnnotationNode)node.visibleAnnotations.get(i);
        String descriptor = "L" + name.replace(".", "/") + ";";
  
        if (ann.desc.equals(descriptor)) {
          return ann;
        }
      }
    }

    return null;
  }

  /**
   * Creates a Java {@link Annotation} from the related {@link AnnotationNode}.
   *
   * @param node Annotation information. It cannot be null.
   *
   * @return Returns the new {@link Annotation}.
   */
  @SuppressWarnings("unchecked")
  private Annotation buildAnnotation(final AnnotationNode node) {
    Validate.notNull(node, "The annotation node cannot be null.");

    if (!cache.get(CacheType.ANNOTATIONS).containsKey(node.desc)) {
      try {
        HashMap<String, Object> values = new HashMap<String, Object>();

        /* Retrieves all values for this annotation. */
        if (node.values != null) {
          for (int i = 0, j = node.values.size(); i < j; i += 2) {
            String name = (String)node.values.get(i);
            Object value = node.values.get(i + 1);

            if (value.getClass().equals(Type.class)) {
              value = TypeResolver.resolve((Type)value);
            }

            values.put(name, value);
          }
        }

        /* Extracts the annotation's class. */
        String className = Type.getType(node.desc).getClassName();

        /* Tries to get the annotation class using the current class loader. */
        Class<? extends Annotation> klass =
          (Class<? extends Annotation>)this.getClass()
          .getClassLoader().loadClass(className);

        if (values.isEmpty()) {
          /* No values specified for the annotation, use the default values. */
          values = (HashMap<String, Object>)AnnotationType.getInstance(klass)
              .memberDefaults();
        }

        /* Creates a proxy for the annotation. */
        Object proxy = AnnotationParser.annotationForMap(klass, values);

        /* Puts the annotation into the cache. */
        cache.get(CacheType.ANNOTATIONS).put(node.desc, proxy);
      } catch (ClassNotFoundException ex) {
        throw new IllegalArgumentException("The annotation cannot be"
            + " found.", ex);
      }
    }

    return (Annotation)cache.get(CacheType.ANNOTATIONS).get(node.desc);
  }

  /**
   * Creates a Java {@link Annotation} from the related {@link AnnotationNode}.
   *
   * @param node Annotation information. It cannot be null.
   *
   * @return Returns the new {@link Annotation}.
   */
  @SuppressWarnings("unchecked")
  private Method buildMethod(final MethodNode node) {
    Validate.notNull(node, "The annotation node cannot be null.");

    if (!cache.get(CacheType.METHODS).containsKey(node.desc + node.name)) {
      String declaringClass = getNode().name;
      String[] exceptions = (String[])node.exceptions.toArray(new String[] {});
      Annotation[] annotations = getAnnotations(node.visibleAnnotations);
      Type[] parameters = Type.getArgumentTypes(node.desc);
      Type returnType = Type.getReturnType(node.desc);
      Annotation[][] parameterAnnotations = new Annotation[0][];

      if (node.visibleParameterAnnotations != null) {
        parameterAnnotations =
          new Annotation[node.visibleParameterAnnotations.length][];

        for (int i = 0, j = parameterAnnotations.length; i < j; i++) {
          parameterAnnotations[i] = getAnnotations(
              node.visibleParameterAnnotations[i]);
        }
      }

      /* Puts the annotation into the cache. */
      cache.get(CacheType.METHODS).put(node.desc + node.name, new Method(
          declaringClass, exceptions, node.name, node.access, parameters,
          returnType, annotations, parameterAnnotations, node.desc
      ));
    }

    return (Method)cache.get(CacheType.METHODS).get(node.desc + node.name);
  }

  /**
   * {@inheritDoc}
   */
  public TypeVariable<?>[] getTypeParameters() {
    throw new NotImplementedException("This method is still not implemented.");
  }
}
