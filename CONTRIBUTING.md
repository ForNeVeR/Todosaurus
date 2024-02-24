Contributor Guide
-----------------

Build
-----

### Prerequisites

- OpenJDK-compatible JDK version 17 or later (installed automatically for shell tooling, recommended to acquire separately for IDE)

### Build

To build the plugin, execute this shell command:

```console
$ ./gradlew buildPlugin
```

This action will use [Gradle JVM Wrapper][gradle-jvm-wrapper] to automatically
download the recommended JDK version that's used for builds, and will download a
required Gradle version. If this isn't necessary, you could use your own
versions of Gradle and JRE by running the build task with `gradle buildPlugin`.

After that, the plugin ZIP distribution will be created in the
`build/distributions` directory.

### Run IDE

The following command will build the plugin and run it using a sandboxed
instance of Rider (set the required version via `build.gradle`).

```console
$ ./gradlew runIde
```

### Test

Execute the following shell command:

```console
$ ./gradlew test
```

Development
-----------

## IntelliJ IDEA Setup

It is recommended to get a JDK of the required version separately and set it up as the project JDK.

[gradle-jvm-wrapper]: https://github.com/mfilippov/gradle-jvm-wrapper
