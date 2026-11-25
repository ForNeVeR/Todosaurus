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
        bundledModule("intellij.platform.collaborationTools")
        bundledModule("intellij.platform.tasks")
        bundledModule("intellij.platform.vcs.dvcs")
        bundledModule("intellij.platform.vcs.dvcs.impl")
        bundledModule("intellij.vcs.gitlab.backend")
        bundledPlugin("Git4Idea")
        bundledPlugin("org.jetbrains.plugins.gitlab")
        compatiblePlugin("com.intellij.tasks")
    }
}
