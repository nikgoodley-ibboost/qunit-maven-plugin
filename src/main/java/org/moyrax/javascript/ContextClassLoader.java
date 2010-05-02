package org.moyrax.javascript;

import java.io.IOException;
import java.util.HashMap;

import org.moyrax.javascript.instrument.ComponentClassAdapter;
import org.moyrax.util.ScriptUtils;

/**
 * This {@link ClassLoader} procceses all classes designed to be exported as
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
  public Class<?> loadClass(final String name) throws ClassNotFoundException {
    Class<?> klass = super.loadClass(name);

    if (!loaded.containsKey(name) && ScriptUtils.isExportable(klass)) {
      try {
        ComponentClassAdapter adapter = new ComponentClassAdapter(klass);

        byte[] bytecode = adapter.toByteArray();
  
        klass = defineClass(klass.getName(), bytecode, 0, bytecode.length);

        loaded.put(name, klass);
      } catch (IOException ex) {
        throw new ClassNotFoundException("Cannot transform the class "
            + klass.getCanonicalName(), ex);
      }
    } else if (loaded.containsKey(name)) {
      klass = loaded.get(name);
    }

    return klass;
  }
}
