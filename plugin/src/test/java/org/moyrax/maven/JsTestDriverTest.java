package org.moyrax.maven;

import java.io.File;
import java.util.concurrent.Semaphore;

import org.junit.Test;
import org.moyrax.javascript.Shell;
import org.moyrax.resolver.ClassPathResolver;
import org.moyrax.resolver.LibraryResolver;

import com.gargoylesoftware.htmlunit.BrowserVersion;

public class JsTestDriverTest {
  private EnvironmentConfiguration context = new EnvironmentConfiguration();

  private Semaphore semaphore = new Semaphore(0, true);

  @Test
  public void testApplication() throws Exception {
    final TestingServer server = new TestingServer(context);
    final TestingClient testDriverClient = new TestingClient(
        server, BrowserVersion.FIREFOX_3);

    context.setTestOutputDirectory("test/");
    context.setProjectBasePath(new File(".").toURI().toURL());

    server.start(semaphore);

    this.semaphore.acquire();

    context.setFiles("", new String[] {
      "test/src/*.js",
      "test/src-test/*.js"
    }, new String[] {});

    context.setLookupPackages(new String[] {
        "classpath:/org/moyrax/javascript/common/**"
    });

    Shell.setResolver("lib", new LibraryResolver("/org/moyrax/javascript/lib"));
    Shell.setResolver("classpath", new ClassPathResolver());

    loadContextResources(testDriverClient);

    testDriverClient.runTests();

    server.stopServer();
    server.join();

    System.out.println("Test finished.");
  }

  /**
   * Initializes the required resources for the test environment.
   */
  private void loadContextResources(final TestingClient client) {
    final String[] dependencies = new String[] {
      /* QUnit testing framework. */
      "org/moyrax/javascript/lib/qunit.js",
      /* QUnit plugin related objects. */
      "org/moyrax/javascript/lib/qunit-objects.js",
      /* QUnit plugin test handler. */
      "org/moyrax/javascript/lib/qunit-plugin.js"
    };

    for (int i = 0; i < dependencies.length; i++) {
      client.addGlobalResource(dependencies[i]);
    }
  }
}
