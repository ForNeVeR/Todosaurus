// SPDX-FileCopyrightText: 2025-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

plugins {
    id("todosaurus.kotlin-conventions")
    id("todosaurus.module-conventions")
}

dependencies {
    compileOnly(project(":core"))
    intellijPlatform {
        bundledModule("intellij.platform.tasks")
        bundledPlugin("org.jetbrains.plugins.gitlab")
        plugin("com.intellij.tasks", libs.versions.tasksPlugin.get())
    }
}
