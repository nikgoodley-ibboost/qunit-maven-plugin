## Overview ##

POM configuration. For information about downloading QMP, see the
[Plugin Repository](PluginRepository.md) page in this wiki.

## Details ##

The following plugin configuration is a complete reference for get working QMP.

```
<plugin>
  <groupId>org.moyrax.qunit</groupId>
  <artifactId>qunit-maven-plugin</artifactId>
  <version>1.2.1</version>

  <executions>
    <execution>
      <goals>
        <goal>test</goal>
      </goals>
    </execution>
  </executions>

  <configuration>
    <!-- Configures the lookup directory for JavaScript resources. -->
    <contextPath>
      <entry>
        <!-- Files included in the JavaScript context path. -->
        <files>
          <directory>src/main/resources/org/moyrax/</directory>
          <includes>
            <include>**/*.js</include>
          </includes>
          <excludes>
            <exclude>**/.svn/**</exclude>
          </excludes>
        </files>
      </entry>
    </contextPath>

    <!-- Test files to run -->
    <testResources>
      <directory>src/test/resources/org/moyrax/</directory>
      <includes>
        <include>**/*-test.html</include>
      </includes>
    </testResources>
  </configuration>
</plugin>
```
