package org.moyrax.maven;

import java.util.concurrent.Semaphore;

import org.junit.Ignore;
import org.junit.Test;
import org.moyrax.javascript.TestContext;
import org.moyrax.javascript.TestDriverClient;
import org.moyrax.javascript.TestDriverServer;

import com.gargoylesoftware.htmlunit.BrowserVersion;

public class JsTestDriverTest {
  private TestContext context = new TestContext();

  private Semaphore semaphore = new Semaphore(0, true);

  @Test
  @Ignore
  public void testApplication() throws Exception {
    final TestDriverServer server = new TestDriverServer(context);
    final TestDriverClient testDriverClient = new TestDriverClient(
        server, BrowserVersion.INTERNET_EXPLORER_6);

    context.setTestOutputDirectory("test/");

    /* Starts the server. */
    server.start(semaphore);

    this.semaphore.acquire();

    testDriverClient.setIncludes(new String[] {
      "test/src/*.js",
      "test/src-test/*.js"
    });

    /* Binds the client to the server. */
    testDriverClient.capture(this.semaphore);

    this.semaphore.acquire();

    /* Run the configured tests. */
    try {
      /*
       * TODO(mmirabelli) For some reason js-test-driver locks down inside the
       * while loop in CommandServlet, because it's expecting a response from
       * the captured browser, and the response queue in the SlaveBrowser class
       * is empty when the servlet invokes the getResponse() method.
       *
       * I actually think that's a bug in htmlunit implementation, but I didn't
       * search in deep for it.
       *
       * @see com.google.jstestdriver.SlaveBrowser:146
       * @see com.google.jstestdriver.CommandServlet:71
       * @see com.google.jstestdriver.CommandTask:144
       */
      testDriverClient.runTests();
    } catch(Exception ex) {
      throw new RuntimeException("Error initializing tests.", ex);
    }

    /* Stops the server and waits for it until finalizes. */
    server.stopServer();
    server.join();

    System.out.println("Test finished.");
  }
}
