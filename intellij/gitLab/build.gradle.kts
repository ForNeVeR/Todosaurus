// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

plugins {
    id("todosaurus.kotlin-conventions")
    id("todosaurus.module-conventions")
}

dependencies {
    compileOnly(project(":core"))
    intellijPlatform {
        bundledPlugin("com.intellij.tasks")
        bundledPlugin("org.jetbrains.plugins.gitlab")
    }
}
