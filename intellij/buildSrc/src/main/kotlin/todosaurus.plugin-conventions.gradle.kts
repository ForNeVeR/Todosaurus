// SPDX-FileCopyrightText: 2025-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

plugins {
    id("org.jetbrains.intellij.platform")
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val intelliJVersion = versionCatalogs.named("libs").findVersion("ideaSdk").get().requiredVersion
        intellijIdea(intelliJVersion) {
            useInstaller = !intelliJVersion.endsWith("EAP")
        }
        pluginVerifier()
    }
}
