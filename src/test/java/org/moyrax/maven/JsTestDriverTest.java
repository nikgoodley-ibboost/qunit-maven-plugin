package org.moyrax.maven;

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
    final TestDriverServer server = new TestDriverServer(context);
    final TestDriverClient testDriverClient = new TestDriverClient(
        server, BrowserVersion.FIREFOX_3);

    context.setTestOutputDirectory("test/");

    server.start(semaphore);

    this.semaphore.acquire();

    testDriverClient.setFiles("", new String[] {
      "test/src/*.js",
      "test/src-test/*.js"
    }, new String[] {});


    testDriverClient.setLookupPackages(new String[] {
        "classpath:/org/moyrax/javascript/common/**"
    });

    Shell.setResolver("lib", new LibraryResolver("/org/moyrax/javascript/lib"));
    Shell.setResolver("classpath", new ClassPathResolver());

    try {
      // TODO(mmirabelli): Remove the Ignore annotation when the TODO's defined
      // in the classes TestDriverClient and Global will be resolved.
      testDriverClient.runTests();
    } catch(Exception ex) {
      throw new RuntimeException("Error initializing tests.", ex);
    }

    server.stopServer();
    server.join();

    System.out.println("Test finished.");
  }
}
