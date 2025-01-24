// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

import me.fornever.todosaurus.TodosaurusBundle

sealed class TestConnectionResult {
    data object Success : TestConnectionResult()

    class Failed(val reason: String? = TodosaurusBundle.message("wizard.steps.chooseIssueTracker.testConnection.unexpectedError")) : TestConnectionResult()
}
