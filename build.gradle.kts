// SPDX-FileCopyrightText: 2000-2021 JetBrains s.r.o.
// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: Apache-2.0

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerializationPlugin)
    alias(libs.plugins.gradleIntelliJPlatformPlugin)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.gradleJvmWrapper)
}

jvmWrapper {
    linuxAarch64JvmUrl = "https://download.oracle.com/java/21/archive/jdk-21.0.3_linux-aarch64_bin.tar.gz"
    linuxX64JvmUrl = "https://download.oracle.com/java/21/archive/jdk-21.0.3_linux-x64_bin.tar.gz"
    macAarch64JvmUrl = "https://download.oracle.com/java/21/archive/jdk-21.0.3_macos-aarch64_bin.tar.gz"
    macX64JvmUrl = "https://download.oracle.com/java/21/archive/jdk-21.0.3_macos-x64_bin.tar.gz"
    windowsX64JvmUrl = "https://download.oracle.com/java/21/archive/jdk-21.0.3_windows-x64_bin.zip"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    compilerOptions {
        allWarningsAsErrors = true
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(libs.versions.ideaSdk)
        bundledModule("intellij.platform.vcs.dvcs.impl")
        bundledPlugin("Git4Idea")
        bundledPlugin("org.jetbrains.plugins.github")
        bundledPlugin("org.jetbrains.plugins.gitlab")

        instrumentationTools()

        testFramework(TestFrameworkType.Bundled)

        pluginVerifier()
    }

    testImplementation(libs.junit)
    testImplementation(libs.openTest4J)
    implementation(libs.kotlinSerializationLibrary)
}

intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
    }
    pluginVerification.ides {
        recommended()
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl").get()
}

tasks {
    patchPluginXml {
        version = providers.gradleProperty("pluginVersion").get()
        untilBuild = providers.gradleProperty("pluginUntilBuild").get()

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with (it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    publishPlugin {
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    val testIdeaPreview by intellijPlatformTesting.testIde.registering {
        version = libs.versions.ideaSdkPreview
        useInstaller = false
        task {
            enabled = libs.versions.ideaSdk.get() != libs.versions.ideaSdkPreview.get()
        }
    }

    check { dependsOn(testIdeaPreview.name) }
}
