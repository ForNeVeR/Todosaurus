// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT AND Apache-2.0

import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.exceptions.MissingVersionException
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("todosaurus.kotlin-conventions")
    id("todosaurus.plugin-conventions")
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

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
    }
    pluginVerification.ides {
        recommended()
    }
}

changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl").get()
}

dependencies {
    intellijPlatform {
        pluginModule(implementation(project(":core")))
        pluginModule(implementation(project(":github")))
    }
}

tasks {
    patchPluginXml {
        version = properties("pluginVersion").get()
        untilBuild = properties("pluginUntilBuild").get()

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

        val latestChangelog = try {
            changelog.getUnreleased()
        } catch (_: MissingVersionException) {
            changelog.getLatest()
        }
        changeNotes.set(provider {
            changelog.renderItem(
                latestChangelog
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        })
    }

    publishPlugin {
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }
}
