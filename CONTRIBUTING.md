Contributor Guide
=================
<!-- REUSE-IgnoreStart -->

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

Upgrade IDE Version
-------------------
To upgrade the IDE version targeted by the plugin, follow these steps.
1. Update the `platformVersion` in `gradle.properties`.
2. Update the `kotlin` version in the `plugins` section of the `gradle/libs.versions.toml` (see the comment for the link there).

License Automation
------------------
If the CI asks you to update the file licenses, follow one of these:
1. Update the headers manually (look at the existing files), something like this:
   ```csharp
   // SPDX-FileCopyrightText: %year% %your name% <%your contact info, e.g. email%>
   //
   // SPDX-License-Identifier: MIT
   ```
   (accommodate to the file's comment style if required).
2. Alternately, use [REUSE][reuse] tool:
   ```console
   $ reuse annotate --license MIT --copyright '%your name% <%your contact info, e.g. email%>' %file names to annotate%
   ```

(Feel free to attribute the changes to "Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>" instead of your name in a multi-author file, or if you don't want your name to be mentioned in the project's source: this doesn't mean you'll lose the copyright.)

[gradle-jvm-wrapper]: https://github.com/mfilippov/gradle-jvm-wrapper
[reuse]: https://reuse.software/

<!-- REUSE-IgnoreEnd -->
