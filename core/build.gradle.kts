// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("todosaurus.kotlin-conventions")
    id("todosaurus.module-conventions")
}

dependencies {
    intellijPlatform {
        bundledPlugin("Git4Idea")
        bundledPlugin("com.intellij.tasks")

        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit4)
}

tasks {
    test {
        useJUnitPlatform()
    }
}
