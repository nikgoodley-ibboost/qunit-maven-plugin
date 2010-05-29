package org.moyrax.javascript;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.commons.lang.Validate;
import org.moyrax.util.ScriptUtils;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;

/**
 * This {@link JavaScriptEngine} allows dynamically to configure the pages's
 * scope, extending the basic HtmlUnit host's features.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class ConfigurableEngine extends JavaScriptEngine {
  /** Default ID for serialization. */
  private static final long serialVersionUID = 1L;

  /**
   * List of beans registered in the executions scope.
   */
  private List<ScriptComponent> components;

  /**
   * Global context, shared by all windows.
   */
  private Context globalContext;

  /**
   * List of JavaScript resources which will be executed at scope level. All
   * of these resources will be loaded before executing the code inside the
   * scope.
   */
  private ArrayList<String> scopeResources = new ArrayList<String>();

  /**
   * Creates a new {@link ConfigurableEngine} and sets the enclosing
   * {@link WebClient}.
   *
   * @param theWebClient The web client that will use this engine. It cannot
   *    be null.
   */
  public ConfigurableEngine(final WebClient theWebClient) {
    super(theWebClient);

    // Entering to the context, Rhino binds the context to this thread.
    // If there's no context binded to the thread, Rhino will create a new
    // temporary context each time an action is going to be executed.
    // We need to keep the context in order to load the scope resources.
    globalContext = getContextFactory().enterContext();
  }

  /**
   * Executes the specified JavaScript code in the scope of the given page.
   *
   * @param htmlPage The page in which the code resides.
   * @param sourceCode The JavaScript code to be executed.
   * @param sourceName The name of the source file.
   * @param startLine The line in which the code will start the execution.
   *
   * @return Returns the result of the script execution.
   * @throws JavaScriptException if there are any errors in the script
   *    execution.
   * @throws JavaScriptEngineException If there are errors in the environment
   *    configuration.
   */
  @Override
  public Object execute(final HtmlPage htmlPage, final String sourceCode,
      final String sourceName, final int startLine)
        throws JavaScriptException, JavaScriptEngineException {

    final ScriptableObject scope = (ScriptableObject)htmlPage
        .getEnclosingWindow().getScriptObject();

    this.initializeScope(scope);
    this.loadScopeResources(scope);

    return super.execute(htmlPage, sourceCode, sourceName, startLine);
  }

  /**
   * Registers a new {@link ScriptableObject} class that will be available in
   * the execution scopes created by this engine.
   *
   * @param klass Class to register in all scopes. It cannot be null.
   */
  public void registerClass(final Class<?> klass) {
    registerClass(klass, getClass().getClassLoader());
  }

  /**
   * Registers a new {@link ScriptableObject} class that will be available in
   * the execution scopes created by this engine.
   *
   * @param klass Class to register in all scopes. It cannot be null.
   * @param classLoader {@link ClassLoader} used to locate the class related
   *    resources. It cannot be null.
   */
  public void registerClass(final Class<?> klass,
      final ClassLoader classLoader) {
    Validate.notNull(klass, "The class cannot be null.");
    Validate.notNull(classLoader, "The class loader cannot be null.");

    if (components == null) {
      components = new ArrayList<ScriptComponent>();
    }

    components.add(new ScriptComponent(klass, classLoader));
  }

  /**
   * Adds a new resource which will be registered in the Window scope. It's
   * useful to initialize the client environment before executing the tests.
   *
   * @param classPath Resource located in the classpath. It cannot be null or
   *    empty.
   */
  public void addGlobalResource(final String classPath) {
    Validate.notEmpty(classPath, "The resource classpath cannot be null.");

    scopeResources.add(classPath);
  }

  /**
   * Initializes the specified {@link Scriptable} object adding all registered
   * classes to the scope.
   *
   * @param scope Scope to initialize. It cannot be null.
   */
  @SuppressWarnings("unchecked")
  private void initializeScope(final ScriptableObject scope) {
    Validate.notNull(scope, "The scope cannot be null.");

    /* Registers all global functions. */
    for (ScriptComponent bean : components) {
      String[] globalFunctions = bean.getGlobalFunctionNames().toArray(
          new String[] {});

      /* Adds the global functions to the scope. */
      scope.defineFunctionProperties(globalFunctions,
          bean.getScriptableClass(), ScriptableObject.DONTENUM);
    }

    try {
      /* Tries to register the defined classes. */
      for (ScriptComponent bean : components) {
        ScriptableObject.defineClass(scope,
            (Class<? extends ScriptableObject>)bean.getScriptableClass());
      }
    } catch (Exception ex) {
      throw new JavaScriptEngineException("Error initializing the scope.", ex);
    }
  }

  /**
   * Executes the configured global resources in the given scope.
   *
   * @param scope Scope to load the configured resources. It cannot be null.
   */
  private void loadScopeResources(final ScriptableObject scope) {
    Validate.notNull(scope, "The scope cannot be null.");

    for (String classPath : scopeResources) {
      ScriptUtils.run(globalContext, scope, classPath);
    }
  }
}
