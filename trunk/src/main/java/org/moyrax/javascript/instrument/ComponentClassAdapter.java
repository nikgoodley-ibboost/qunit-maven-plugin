package org.moyrax.javascript.instrument;

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

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;

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

  public ComponentClassAdapter (final Class<?> hostClass) throws IOException {

    this(new ClassReader(
        ClassLoader.getSystemClassLoader().getResourceAsStream(
            hostClass.getName().replace(".", "/") + ".class")));

    this.script = new ScriptComponent(hostClass);
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

    if (this.functions.contains(name)) {
      descriptors.add(new MethodDescriptor(name, desc, signature, exceptions));
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
        @Override
        public void visitMethodInsn(int opcode, String klass, String method,
            String desc) {
          if (opcode == INVOKESPECIAL) {
            super.visitMethodInsn(opcode, className, method, desc);
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

    /* Creates the new method. */
    MethodVisitor mv = cv.visitMethod(
        ACC_PUBLIC, /* Please understand, I also need to be public. */
        "jsFunction_" + descriptor.name,
        descriptor.desc,
        descriptor.signature,
        descriptor.exceptions);

    String className = script.getScriptableClass().getName().replace(".", "/");

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
    /* Returns void. */
    mv.visitInsn(RETURN);
    /* The stack frame will be calculated by the ClassWriter. */
    mv.visitMaxs(0, 0);
    /* The code generation ends. */
    mv.visitEnd();
  }

  /**
   * Generates the JavaScript functions and ends generating the class.
   */
  @Override
  public void visitEnd() {
    for (MethodDescriptor descriptor : descriptors) {
      generateFunction(descriptor);
    }

    cv.visitEnd();
  }
}
