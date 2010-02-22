package org.moyrax.javascript;

import java.util.concurrent.Semaphore;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.jstestdriver.JsTestDriverServer;

/**
 * This class in the server which will capture the browsers in order to execute
 * all the test cases defined in the plugin.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 *
 */
public class TestDriverServer extends Thread {
  /** Default logger for this class. */
  private static final Log logger = LogFactory.getLog(TestDriverServer.class);

  /**
   * Context used to initialize this testing server.
   */
  private TestContext context;

  /**
   * Semaphore which will wait for the server initialization.
   */
  private Semaphore semaphore;

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
  public TestDriverServer(final TestContext context) {
    Validate.notNull(context, "The context parameter cannot be null.");

    this.setName("QUnit Testing Server Thread");

    this.context = context;
  }

  /**
   * Starts this server and causes the given object to wait until the server
   * be fully loaded.
   *
   * @param semaphore Semaphore which will wait for the initialization.
   */
  public synchronized void start(final Semaphore semaphore) {
    this.semaphore = semaphore;

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
    JsTestDriverServer.main(context.getServerParameters());

    this.started = true;

    this.semaphore.release();

    while (this.started) {}
  }

  /**
   * Returns the current testing context.
   */
  public TestContext getContext() {
    return context;
  }
}
