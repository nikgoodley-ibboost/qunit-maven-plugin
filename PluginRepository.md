## Plugin Repository ##

Following is shown a maven plugin repository to retrieve qunit-maven-plugin without installing it manually.

```
<pluginRepositories>
  <pluginRepository>
    <id>moyrax-releases</id>
    <name>Moyrax Artifactory</name>
    <url>http://www.moyrax.com:8081/nexus/content/repositories/moyrax-releases/</url>
    <layout>default</layout>

    <snapshots>
      <enabled>false</enabled>
    </snapshots>

    <releases>
      <updatePolicy>never</updatePolicy>
    </releases>
  </pluginRepository>
</pluginRepositories>
```

Dependency:

```
<dependency>
  <groupId>org.moyrax.qunit</groupId>
  <artifactId>qunit-maven-plugin</artifactId>
  <version>1.2.1</version>
</dependency>
```