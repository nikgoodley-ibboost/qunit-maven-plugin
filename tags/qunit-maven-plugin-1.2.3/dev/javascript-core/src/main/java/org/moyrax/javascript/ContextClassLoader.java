package org.moyrax.javascript;

import java.io.IOException;
import java.util.HashMap;

import org.moyrax.javascript.instrument.ComponentClassAdapter;
import org.moyrax.util.ScriptUtils;

/** This {@link ClassLoader} processes all classes designed to be exported as
 * JavaScript components. It must be used by the engine in order to identify
 * the exportable classes to register in the global context.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ContextClassLoader extends ClassLoader {
  /** Keep track of the loaded Script classes. */
  private HashMap<String, Class<?>> loaded = new HashMap<String, Class<?>>();

  /**
   * {@inheritDoc}
   */
  public ContextClassLoader(final ClassLoader parent) {
    super(parent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Class<?> findClass(final String name) throws ClassNotFoundException {
    if (ScriptUtils.isExportable(name, this)) {
      return this.loadClass(name);
    } else {
      return super.findClass(name);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<?> loadClass(final String name) throws ClassNotFoundException {
    return transform(name);
  }

  private Class<?> transform(final String className)
      throws ClassNotFoundException {

    Class<?> result;

    if (!loaded.containsKey(className) &&
        ScriptUtils.isExportable(className, this)) {
      try {
        ComponentClassAdapter adapter = new ComponentClassAdapter(className,
            this);

        byte[] bytecode = adapter.toByteArray();

        result = defineClass(className, bytecode, 0, bytecode.length);

        loaded.put(className, result);
      } catch (IOException ex) {
        throw new ClassNotFoundException("Cannot transform the class "
            + className, ex);
      } catch (ClassFormatError ex) {
        throw new ClassNotFoundException("Class transformation failed due a"
            + " bad result format.", ex);
      } catch (LinkageError ex) {
        throw new ClassNotFoundException("Cannot instrument the class "
            + className, ex);
      }
    } else if (loaded.containsKey(className)) {
      result = loaded.get(className);
    } else {
      result = super.loadClass(className);
    }

    return result;
  }
}
