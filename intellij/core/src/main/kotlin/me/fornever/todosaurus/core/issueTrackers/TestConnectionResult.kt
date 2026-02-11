// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

import me.fornever.todosaurus.core.TodosaurusCoreBundle

sealed class TestConnectionResult {
    @Suppress("unused") // IDEA-367243: In a Gradle project, cross-module navigation doesn't work correctly
    data object Success : TestConnectionResult()

    class Failed(val reason: String? = TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.testConnection.unexpectedError")) : TestConnectionResult()
}
