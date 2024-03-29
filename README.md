## Apache Maven SCM Subversion Provider - Java Impl.

The [Apache Maven SCM](https://maven.apache.org/scm) Provider Impl is based on svnkit and so use Java process to run [`svn` subversion][subversion] operations instead of forking a command line as the default Apache Maven SCM svn implementation

[Check releases](https://github.com/olamy/maven-scm-provider-svnjava/releases)

### Maven Dependency

```
<dependency>
  <groupId>com.google.code.maven-scm-provider-svnjava</groupId>
  <artifactId>maven-scm-provider-svnjava</artifactId>
  <version>2.2.0</version>
</dependency>
```

### Using SNAPSHOT build

You can use snapshot build of the project by adding the repository:

```xml
  <repositories>
    <repository>
      <id>oss.sonatype.snapshots</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```

### Building

Requirements: JDK 1.8+, Maven 3.6.3+

```
git clone https://github.com/olamy/maven-scm-provider-svnjava.git
cd maven-scm-provider-svnjava
mvn install
```

[subversion]: https://subversion.apache.org/
