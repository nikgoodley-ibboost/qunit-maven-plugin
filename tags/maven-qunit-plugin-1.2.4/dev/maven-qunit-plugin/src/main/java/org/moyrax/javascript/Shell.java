package org.moyrax.javascript;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;
import net.sourceforge.htmlunit.corejs.javascript.NativeObject;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.moyrax.javascript.annotation.GlobalFunction;
import org.moyrax.javascript.annotation.Script;
import org.moyrax.javascript.shell.Global;
import org.moyrax.resolver.ContextFileResolver;
import org.moyrax.resolver.ResourceResolver;
import org.moyrax.util.ScriptUtils;

/**
 * This class provides additional functions to the Rhino shell.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 0.50
 */
@Script(name = "Shell")
public class Shell extends Global {
  /** Default id for serialization. */
  private static final long serialVersionUID = 1L;


  /**
   * Default protocol name for searching resources.
   */
  private static final String DEFAULT_PROTOCOL = "context";

  /**
   * List of resolvers used to load resources on runtime.
   */
  private static Map<String, ResourceResolver> resolvers =
    new HashMap<String, ResourceResolver>();

  /**
   * Locates and retrieves resources from the context path..
   */
  private static ContextFileResolver contextResolver;

  /** Default constructor. */
  public Shell() {}

  /**
   * Includes a JavaScript resource from different locations.
   *
   * @param context Current execution context.
   * @param scope   Script global scope.
   * @param arguments  Arguments passed to this method from the script.
   * @param thisObj Reference to the current javascript object.
   */
  @GlobalFunction
  public static void include(final Context context, final Scriptable scope,
      final Object[] arguments, final Function thisObj) {

    final ArrayList<String> files = new ArrayList<String>();
    final ArrayList<InputStream> input = new ArrayList<InputStream>();

    for (int i = 0, j = arguments.length; i < j; i++) {
      final String resourceUri = (String)arguments[i];

      final Object result = findResolver(resourceUri)
      .resolve(resourceUri);

      if (result != null) {
        if (result.getClass().equals(File.class)) {
          files.add(((File)result).getAbsolutePath());
        } else if (InputStream.class.isInstance(result)) {
          input.add((InputStream)result);
        }
      } else {
        throw new JavaScriptException(arguments[i].toString() +
            " not found in the context path.", "", 0);
      }
    }

    if (files.size() > 0) {
      ScriptUtils.run(context, scope, files.toArray(new String[] {}));
    }

    if (input.size() > 0) {
      ScriptUtils.run(context, scope, input);
    }
  }

  /**
   * Includes HTML pages.
   *
   * @param context Current execution context.
   * @param scope   Script global scope.
   * @param arguments  Arguments passed to this method from the script.
   * @param thisObj Reference to the current javascript object.
   */
  @GlobalFunction
  public static void includePage(final Context context, final Scriptable scope,
      final Object[] arguments, final Function thisObj) {

    final HtmlPageContext pageContext = new HtmlPageContext(scope, context);

    for (int i = 0, j = arguments.length; i < j; i++) {
      String resourceUri;
      double logLevel = 0;

      if (arguments[i].getClass().equals(NativeObject.class)) {
        final NativeObject nativeObject = (NativeObject)arguments[i];

        Validate.isTrue(nativeObject.has("url", scope), "The url parameter " +
        "doesn't exists.");

        resourceUri = (String)nativeObject.get("url", scope);

        if (nativeObject.has("logLevel", scope)) {
          logLevel = (Double)nativeObject.get("logLevel", scope);
        }
      } else {
        resourceUri = (String)arguments[i];
      }

      pageContext.setLocation(resourceUri);
      pageContext.setLogLevel(logLevel);
      pageContext.open();
    }
  }

  /**
   * Reads resources from different locations.
   *
   * @param context Current execution context.
   * @param scope   Script global scope.
   * @param arguments  Arguments passed to this method from the script.
   * @param thisObj Reference to the current javascript object.
   */
  @GlobalFunction
  public static Object readResource(final Context context, final Scriptable scope,
      final Object[] arguments, final Function thisObj) {

    final ArrayList<InputStream> input = new ArrayList<InputStream>();

    for (int i = 0, j = arguments.length; i < j; i++) {
      final String resourceUri = (String)arguments[i];

      final Object result = findResolver(resourceUri)
        .resolve(resourceUri);

      if (result != null) {
        try {
          if (result.getClass().equals(File.class)) {
            input.add(new FileInputStream((File)result));
          } else if (InputStream.class.isInstance(result)) {
            input.add((InputStream)result);
          }
        } catch(IOException ex) {}
      } else {
        throw new JavaScriptException(arguments[i].toString() +
            " not found in the context path.", "", 0);
      }
    }

    if (input.size() > 0) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      for (InputStream resource : input) {
        try {
          IOUtils.copy(resource, out);
        } catch(IOException ex) {
          // Not handled.
        }
      }

      return out.toString();
    }

    return "";
  }

  /**
   * Sets the context path for this shell.
   *
   * @param contextPath List of directory included in the context path. It
   *    cannot be null.
   */
  public static void setContextPath(final File[] contextPath) {
    setContextPath(contextPath, null);
  }

  /**
   * Sets the context path for this shell.
   *
   * @param contextPath List of directory included in the context path. It
   *    cannot be null.
   * @param excludes  List of directories excluded from the context path. It
   *    can be null.
   */
  public static void setContextPath(final File[] contextPath,
      final File[] excludes) {

    Validate.notNull(contextPath, "contextPath cannot be null.");

    /* Creates the default resolver. */
    if (contextResolver == null) {
      contextResolver = new ContextFileResolver();

      setResolver(DEFAULT_PROTOCOL, contextResolver);
    }

    contextResolver.setContextPath(contextPath, excludes);
  }

  /**
   * Adds a new resolver for the specified protocol. The protocol represents the
   * starting part of an URI string. For example, a valid location for a
   * <code>lib</code> protocol coukd be:<br/><br/>
   *
   * lib:/some/path/to/lib.js<br/><br/>
   *
   * When the application tries to load a resource specifying the protocol, the
   * execution will be delegated to the registered resolver.
   *
   * @param protocol Protocol name. It cannot be null.
   * @param resolver {@link ResourceResolver} which handles the protocol. If it
   *    is null, the resolver will be removed.
   *
   * @throws IllegalStateException If the protocol already has a resolver
   *    attached to it.
   */
  public static void setResolver(final String protocol,
      final ResourceResolver resolver) {

    Validate.notNull(protocol, "The protocol parameter cannot be null.");

    if (resolver == null && resolvers.containsKey(protocol)) {
      resolvers.remove(protocol);

      return;
    }

    resolvers.put(protocol, resolver);
  }

  /**
   * Determines which resolver should handle the specified location.
   *
   * @param uri  Location that a resolver must handle. It cannot be null.
   *
   * @throws IllegalArgumentException If the protocol of the URI cannot be
   *    handled by any resolver.
   */
  private static ResourceResolver findResolver(final String uri) {
    Validate.notNull(uri, "The uri parameter cannot be null.");

    /* Is there a protocol? */
    if (uri.indexOf(":") == -1) {
      if (contextResolver == null) {
        /* Oops, I have nothing. */
        throw new IllegalArgumentException("The context path is not "
            + "initialized.");
      }

      return contextResolver;
    }

    final String protocol = uri.substring(0, uri.indexOf(":"));

    /* Doh, this protocol cannot has a defined resolver. Tries to find
       a handler. */
    if (!resolvers.containsKey(protocol)) {
      Collection<ResourceResolver> resolverList = resolvers.values();

      for (ResourceResolver resolver : resolverList) {
        int result = resolver.canHandle(uri);

        if (result == ResourceResolver.HANDLE_EXCLUSIVE ||
            result == ResourceResolver.HANDLE_SHARED) {
          return resolver;
        }
      }

      /* Indeed, I can't handle it. */
      throw new IllegalArgumentException("The protocol '" + protocol + "' "
          + "cannot be handled.");
    }

    return resolvers.get(protocol);
  }
}
