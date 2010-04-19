package org.moyrax.maven;

import java.util.concurrent.Semaphore;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.moyrax.server.ContextHandlerFactory;
import org.moyrax.server.StaticContentServlet;
import org.moyrax.server.WebContextHandler;

/**
 * This class in the server which will capture the browsers in order to execute
 * all the test cases defined in the plugin.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.2
 */
public class TestDriverServer extends Thread {
  /** Default logger for this class. */
  private static final Log logger = LogFactory.getLog(TestDriverServer.class);

  /**
   * Context used to initialize this testing server.
   */
  private EnvironmentConfiguration context;

  /**
   * Semaphore which will wait for the server initialization.
   */
  private static Semaphore semaphore;

  /**
   * If the server is started, it's will be set to <code>true</code>.
   */
  private boolean started;

  /**
   * Creates a new server using the specified context.
   *
   * @param context Context information for the testing server. It cannot
   *    be null.
   */
  public TestDriverServer(final EnvironmentConfiguration context) {
    Validate.notNull(context, "The context parameter cannot be null.");

    this.setName("QUnit Testing Server Thread");

    this.context = context;
  }

  /**
   * Starts this server and causes the given semaphore to wait until the server
   * be fully loaded.
   *
   * @param aSemaphore Semaphore which will wait for the initialization.
   */
  public synchronized void start(final Semaphore aSemaphore) {
    semaphore = aSemaphore;

    super.start();
  }

  /**
   * Stops the server and finalizes the thread.
   */
  public void stopServer() {
    this.started = false;
  }

  /**
   * Runs the testing server.
   */
  public void run() {
    try {
      Server server = new Server(context.getServerPort());

      ContextHandlerFactory.addServletMapping("/content/",
          new StaticContentServlet());

      server.setHandler(ContextHandlerFactory.getHandler(
          WebContextHandler.class));

      server.start();
    } catch (Exception ex) {
      throw new RuntimeException("Couldn't start the HTTP Server.", ex);
    }

    logger.info("Testing Server started on port " + context.getServerPort());

    this.started = true;

    if (semaphore != null) {
      semaphore.release();
    }

    while (this.started) {}

    logger.info("Shutting down Testing Server...");
  }

  /**
   * Returns the current testing context.
   */
  public EnvironmentConfiguration getContext() {
    return context;
  }
}
