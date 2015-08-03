## Overview ##

Pom dependency and configuration.

WARN: This is preliminary documentation and it's subject to change. Please refer to the TODO page to check what's is going to change and to be improved.

## Details ##

```
<!-- Plugin to execute tests using QUnit.-->
<plugin>
  <groupId>org.moyrax</groupId>
  <artifactId>qunit-plugin</artifactId>

  <version>1.2-SNAPSHOT</version>
   <executions>
    <execution>
      <goals>
        <goal>qunit</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <!-- The testing server port. It's optional. Default is 1337. -->
    <port>1234</port>

    <!-- List of packages which will be scanned for exportable
      components. For more information see the PluginIntegration page
      in the wiki. -->
    <components>
      <value>classpath:/org/moyrax/javascript/component/**</value>
    </components>

    <!-- Configures the lookup directory for JavaScript resources. -->
    <contextPath>
      <entry>
        <!-- Files included in the JavaScript context path. -->
        <files>
          <directory>src/main/resources/org/moyrax/</directory>
          <includes>
            <include>**/*</include>
          </includes>
          <excludes>
            <exclude>**/.svn/**</exclude>
          </excludes>
        </files>
      </entry>
    </contextPath>

    <!-- Test files to execute. All of matching files will be loaded into
        an HtmlUnit page and they will be able to use qunit. Take into account
        that qunit is available in the global scope, so you cannot need to load
        it through the include() method. -->
    <testResources>
      <directory>src/test/resources/org/moyrax/debug.js/</directory>
      <includes>
        <include>**/*-test.html</include>
      </includes>
    </testResources>
  </configuration>
</plugin>
```