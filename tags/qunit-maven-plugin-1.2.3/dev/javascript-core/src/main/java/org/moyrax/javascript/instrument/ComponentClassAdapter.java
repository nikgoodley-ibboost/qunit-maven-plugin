package org.moyrax.javascript.instrument;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.moyrax.javascript.ScriptComponent;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Processes an exportable class and generates the needed code to be recognized
 * in the client application.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public final class ComponentClassAdapter extends ClassAdapter {
  /**
   * This class contains the information that describes a method as it will
   * be saved in bytecode.
   *
   * @author Matias Mirabelli <lumen.night@gmail.com>
   */
  private class MethodDescriptor {
    public MethodDescriptor(final String theName,
        final String theDesc, final String theSignature,
        final String[] theExceptions) {

      this.name = theName;
      this.desc = theDesc;
      this.signature = theSignature;
      this.exceptions = theExceptions;
    }

    /**
     * The method's name.
     */
    public String name;

    /**
     * The method's descriptor.
     *
     * For more information refer to the following url: <a href="
     *  http://java.sun.com/docs/books/jvms/second_edition/html/
     *ClassFile.doc.html#7035">JVM Specification - Method Descriptors</a>
     */
    public String desc;

    /**
     * The method's signature.
     */
    public String signature;

    /**
     * List of exceptions thrown by this method.
     */
    public String[] exceptions;

    /**
     * Indicates if this method is a constructor. Default is <code>false</code>.
     */
    public boolean constructor;
  }

  /** Class which will be modified. */
  private ScriptComponent script;

  /** {@link ClassReader} to parse the script class. */
  private ClassReader reader;

  /** List of methods that will be exposed as JavaScript functions. */
  private List<String> functions;

  /** List of MethodDescriptor objects which represent the methods that will be
   * generated. */
  private ArrayList<MethodDescriptor> descriptors =
    new ArrayList<MethodDescriptor>();

  /**
   * Indicates if the script class was already transformed. Default is
   * <code>false</code>.
   */
  private boolean transformed;

  /**
   * Creates an adapter for the specified class.
   *
   * @param hostClass Class to instrument. It cannot be null.
   * @throws IOException If the class cannot be found.
   */
  public ComponentClassAdapter (final Class<?> hostClass) throws IOException {
    this(hostClass, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates an adapter for the specified class and uses the specified
   * {@link ClassLoader} to locate the resource.
   *
   * @param hostClass Class to instrument. It cannot be null.
   * @param classLoader The class loader to search for the resource. It cannot
   *    be null.
   *
   * @throws IOException If the class cannot be found.
   */
  public ComponentClassAdapter (final Class<?> hostClass,
      final ClassLoader classLoader) throws IOException {

    this(new ClassReader(classLoader.getResourceAsStream(
        hostClass.getName().replace(".", "/") + ".class")));

    this.script = new ScriptComponent(hostClass, classLoader);
    this.functions = script.getFunctionNames();
  }

  /**
   * Creates an adapter for the specified class and uses the specified
   * {@link ClassLoader} to locate the resource.
   *
   * @param hostClass Name of the class to instrument. It cannot be null.
   * @param classLoader The class loader to search for the resource. It cannot
   *    be null.
   *
   * @throws IOException If the class cannot be found.
   */
  public ComponentClassAdapter (final String hostClass,
      final ClassLoader classLoader) throws IOException {
    this(new ClassReader(classLoader.getResourceAsStream(
        hostClass.replace(".", "/") + ".class")));

    this.script = new ScriptComponent(hostClass, classLoader);
    this.functions = script.getFunctionNames();
  }

  private ComponentClassAdapter (final ClassReader classReader) {
    super(new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES));

    this.reader = classReader;
  }

  /**
   * This method is invoked before any other visit. It allows to make changes
   * to the class definition.
   *
   * @param version VM version.
   * @param access Type access.
   * @param name Class name.
   * @param signature Class signature.
   * @param superName Name of the superclass.
   * @param interfaces List of implementing interfaces.
   */
  @Override
  public void visit(final int version, final int access, final String name,
      final String signature, final String superName,
      final String[] interfaces) {

    super.visit(version,
        ACC_PUBLIC + ACC_SUPER, /* So sorry, I need to be public. */
        name,
        signature,
        script.getImplementationClass().getName().replace(".", "/"),
        interfaces);
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String name,
      final String desc, final String signature, final String[] exceptions) {

    MethodVisitor mv = super.visitMethod(access, name, desc, signature,
        exceptions);

    if (this.functions.contains(name) ||
        (script.getConstructor() != null &&
            script.getConstructor().getName().equals(name))) {

      MethodDescriptor descriptor = new MethodDescriptor(name, desc, signature,
          exceptions);

      if (script.getConstructor() != null &&
          script.getConstructor().getName().equals(name)) {
        descriptor.constructor = true;
      }

      descriptors.add(descriptor);
    }

    /* Creates an adapter to replace the default constructor for the
       one needed by the new superclass. */
    if (name.equals("<init>")) {
      final String className = script.getImplementationClass().getName()
      .replace(".", "/");

      /* Generates the default constructor adapter.
        TODO(mmirabelli): find how to change the access of the default
        constructor, since that it seems to be private by default in a private
        class. It cause this assembly to break in private inner classes, unless
        an explicit public constructor will be defined.
       */
      mv = new MethodAdapter(mv) {
        private boolean alreadySet = false;

        @Override
        public void visitMethodInsn(final int opcode, final String klass, final String method,
            final String desc) {
          if ((opcode == INVOKESPECIAL) && !alreadySet) {
            super.visitMethodInsn(opcode, className, method, desc);

            alreadySet = true;
          } else {
            super.visitMethodInsn(opcode, klass, method, desc);
          }
        }
      };
    }

    return mv;
  }

  /**
   * @return Returns the byte array which contains the code of the transformed
   * class.
   */
  public byte[] toByteArray() {
    if (!transformed) {
      reader.accept(this, 0);

      transformed = true;
    }

    return ((ClassWriter)cv).toByteArray();
  }

  /**
   * Creates the JavaScript function-compliant method for the specified
   * Java method.
   *
   * @param descriptor The method information to create the Function.
   */
  private void generateFunction(final MethodDescriptor descriptor) {

    String methodName = descriptor.name;
    String functionPrefix = "jsFunction_";

    if (descriptor.constructor) {
      methodName = "jsConstructor";
      functionPrefix = "";
    }

    /* Creates the new method. */
    MethodVisitor mv = cv.visitMethod(
        ACC_PUBLIC, /* Please understand, I also need to be public. */
        functionPrefix + methodName,
        descriptor.desc,
        descriptor.signature,
        descriptor.exceptions);

    String className = script.getScriptableClassName().replace(".", "/");

    Type[] argumentTypes = Type.getArgumentTypes(descriptor.desc);

    /* Starts generating code. Equivalent asm code is:
     *
     * ALOAD 0
     * xLOAD n
     * ...
     * INVOKEVIRTUAL my/class/name methodName (Ljava/lang/String;)V
     * RETURN
     */
    mv.visitCode();
    /* Puts the "this" object into the stack. */
    mv.visitVarInsn(ALOAD, 0);

    /* Puts all the parameters into the stack. */
    for (int i = 0; i < argumentTypes.length; i++) {
      int opcode = argumentTypes[i].getOpcode(ILOAD);

      mv.visitVarInsn(opcode, i + 1);
    }

    /* Invokes the original method. */
    mv.visitMethodInsn(INVOKEVIRTUAL, className, descriptor.name,
        descriptor.desc);

    /* Return TYPE. */
    mv.visitInsn(Type.getReturnType(descriptor.desc).getOpcode(IRETURN));
    /* The stack frame will be calculated by the ClassWriter. */
    mv.visitMaxs(0, 0);
    /* The code generation ends. */
    mv.visitEnd();
  }

  /**
   * Instruments the getter needed by Rhino to retrieve the component's name.
   * The component name is the same that will be used in JavaScript. By default,
   * the component name is set to the class name.
   */
  private void addClassNameGetter() {
    // Adds a private field to the class to hold the component's name.
    String fieldName = "$className";

    cv.visitField(ACC_PRIVATE, fieldName, "Ljava/lang/String;", "",
        script.getClassName());

    // Creates the getter method which returns the created field.
    String methodName = "getClassName";
    MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, methodName,
        "()Ljava/lang/String;", null, null);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD,
        script.getScriptableClassName().replace(".", "/"),
        fieldName, "Ljava/lang/String;");
    mv.visitInsn(Type.getType(String.class).getOpcode(IRETURN));
    mv.visitMaxs(0, 0);
  }

  /**
   * Generates the JavaScript functions and ends generating the class.
   */
  @Override
  public void visitEnd() {
    for (MethodDescriptor descriptor : descriptors) {
      generateFunction(descriptor);
    }

    addClassNameGetter();

    cv.visitEnd();
  }
}

