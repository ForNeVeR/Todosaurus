// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

plugins {
    id("todosaurus.kotlin-conventions")
    id("todosaurus.module-conventions")
}

dependencies {
    implementation(project(mapOf("path" to ":core")))
    compileOnly(project(":core"))
    intellijPlatform {
        bundledPlugin("org.jetbrains.plugins.github")
    }
}
