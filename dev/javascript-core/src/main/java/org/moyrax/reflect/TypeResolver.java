package org.moyrax.reflect;

import java.util.HashMap;

import org.apache.commons.lang.Validate;
import org.objectweb.asm.Type;

/**
 * Resolves asm {@link Type}s to the related classes.
 *
 * @author Matías Mirabelli <lumen.night@gmail.com>
 */
public class TypeResolver {
  /** Mapping of the primitive types string code to the primitive class. */
  private static HashMap<String, Class<?>> types;

  /**
   * Returns the class represented by the specified type.
   *
   * @param descriptor Type to resolve. It cannot be null.
   */
  public static Class<?> resolve(final Type descriptor) {
    Validate.notNull(descriptor, "The descriptor cannot be null.");

    try {
      if (getTypes().containsKey(descriptor.getDescriptor())) {
        return getTypes().get(descriptor.getDescriptor());
      } else {
        return TypeResolver.class.getClassLoader().loadClass(
            descriptor.getClassName());
      }
    } catch (ClassNotFoundException ex) {
      throw new IllegalAccessError("The class " + descriptor.getClassName()
          + " cannot be loaded.");
    }
  }

  private static HashMap<String, Class<?>> getTypes() {
    if (types == null) {
      types = new HashMap<String, Class<?>>();

      /* Maps the primitive types. */
      types.put("Z", boolean.class);
      types.put("C", char.class);
      types.put("B", byte.class);
      types.put("S", short.class);
      types.put("I", int.class);
      types.put("F", float.class);
      types.put("J", long.class);
      types.put("D", double.class);
  
      types.put("[Z", boolean[].class);
      types.put("[C", char[].class);
      types.put("[B", byte[].class);
      types.put("[S", short[].class);
      types.put("[I", int[].class);
      types.put("[F", float[].class);
      types.put("[J", long[].class);
      types.put("[D", double[].class);
    }

    return types;
  }
}
