// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.intelliJPlatformModulePlugin)
    implementation(libs.intelliJPlatformPlugin)
}
