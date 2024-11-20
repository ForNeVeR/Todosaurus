// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import me.fornever.todosaurus.TodosaurusBundle

sealed class TestConnectionResult {
    data object Success : TestConnectionResult()

    class Failed(val reason: String? = TodosaurusBundle.message("wizard.steps.chooseIssueTracker.testConnection.unexpectedError")) : TestConnectionResult()
}
