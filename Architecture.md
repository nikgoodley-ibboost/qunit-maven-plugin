**Table of contents**



# Overview #

QUnit Maven Plugin provides automated unit testing for JavaScript. It's a library which can be used integrated on maven builds as well as inside a Java project, adding the proper dependencies. It works as a client-server application.

The server is able to lookup testing resources which are executed by the client. The searching is made through the configured context path. The context path may be either a classpath, an url, or even a custom protocol. The context path is handled by _resolvers_ that can define new internal protocols.

The client works under the same environment than the server. They share the information provided by the user in the initial configuration (through the host project POM or by the plugin main class's constructor). The client emulates a real-world web browser using HtmlUnit, but additionally it has the ability to load a custom JavaScript environment (new prototypes, global functions and more).

Once the server is started and the JavaScript environment initialized by the client, it proceeds running each testing resource. The results will be written to the target directory using the surefire reports, which are compatible with the jUnit reporting format.

For more information about what's still not implemented, please see the [TODO](TODO.md) page.

# How it works #

This section covers the concepts introduced by the plugin. The following topics are going to be explained:

  * Context path resolvers
  * Component scanning
  * Custom components
  * Browser configuration

## Context path resolvers ##
TODO

## Component scanning ##
TODO

## Custom components ##
TODO

## Browser configuration ##
TODO

# Inside view #
![http://img707.imageshack.us/img707/7819/pluginworkflow.png](http://img707.imageshack.us/img707/7819/pluginworkflow.png)

## QUnitPlugin ##
This object is the plugin entry point defined as a mojo (Maven plain Old Java Object). It contains the POM and base environment configuration. For further information about how maven manages the plugins, please see the [Plugin Developers Centre](http://maven.apache.org/plugin-developers/index.html)

It configures the testing server and waits until the context initialization will be ready. After that it gives control to the testing client which has the needed information to run the configured tests.