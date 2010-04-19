package org.moyrax.javascript;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

import org.apache.commons.lang.Validate;
import org.moyrax.util.ScriptUtils;

/**
 * The {@link HtmlPageContext} class allows to load HTML pages from tests. It
 * initializes the needed context to display pages without a browser.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public class HtmlPageContext {
  /**
   * Current script context.
   */
  private Context currentContext;

  /**
   * Actual execution scope.
   */
  private Scriptable scope;

  /**
   * Url of the page which will be loaded.
   */
  private String url;

  /**
   * Output log level.
   */
  private double logLevel;

  /**
   * Creates a new {@link HtmlPageContext} and initializes the libraries
   * needed to load HTML pages.
   *
   * @param scope Actual execution scope. It cannot be null.
   */
  public HtmlPageContext(final Scriptable scope) {
    Validate.notNull(scope, "The scope parameter cannot be null.");

    initContext(scope, null);
  }

  /**
   * Creates a new {@link HtmlPageContext} and initializes the libraries
   * needed to load HTML pages. Also allows to specify the current context
   * in which the page will be loaded.
   *
   * @param scope Actual execution scope. It cannot be null.
   * @param context Current script context. It cannot be null.
   */
  public HtmlPageContext(final Scriptable scope, final Context context) {
    Validate.notNull(scope, "The scope parameter cannot be null.");
    Validate.notNull(context, "The context parameter cannot be null.");

    initContext(scope, context);
  }

  /**
   * Loads the current page.
   */
  public void open() {
    currentContext.evaluateString(scope, getBootstrapScript(), "boot", 1, null);
  }

  /**
   * Returns the current html page location.
   */
  public String getLocation() {
    return url;
  }

  /**
   * Sets the page location.
   *
   * @param url Address of the page to be loaded.
   */
  public void setLocation(final String url) {
    this.url = url;
  }

  /**
   * Returns the current output log level.
   */
  public double getLogLevel() {
    return logLevel;
  }

  /**
   * Sets the output log level.
   *
   * @param logLevel Log level value.
   */
  public void setLogLevel(final double logLevel) {
    this.logLevel = logLevel;
  }

  /**
   * Initializes the libraries needed to load HTML pages.
   *
   * @param scope Actual execution scope. It cannot be null.
   * @param context Current script context.
   */
  private void initContext(final Scriptable scope, final Context context) {
    if (context == null) {
      currentContext = Context.getCurrentContext();
    } else {
      currentContext = context;
    }

    this.scope = scope;

    final String[] dependencies = new String[] {
      /* Browser implementation for Rhino. */
      "org/moyrax/javascript/lib/env.js"
    };

    for (int i = 0; i < dependencies.length; i++) {
      ScriptUtils.run(currentContext, scope, dependencies[i]);
    }
  }

  /**
   * Retrieves the script string that loads the html page in a Env.js
   * environment.
   */
  private String getBootstrapScript() {
    final StringBuilder builder = new StringBuilder();

    Validate.notNull(this.url, "The page url is not set.");

    builder.append("Envjs('" + this.url + "', {");
    builder.append("  logLevel: " + this.logLevel + ",");
    builder.append("  scriptTypes: {");
    builder.append("    'text/javascript': true");
    builder.append("  }");
    builder.append("});");
    builder.append("Envjs.wait();");

    return builder.toString();
  }
}
