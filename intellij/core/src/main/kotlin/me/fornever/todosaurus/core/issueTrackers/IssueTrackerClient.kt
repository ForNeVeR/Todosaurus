// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

import me.fornever.todosaurus.core.issues.IssueModel
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ui.wizard.IssueOptions

interface IssueTrackerClient {
    suspend fun createIssue(toDoItem: ToDoItem, issueOptions: List<IssueOptions>): IssueModel

    suspend fun getIssue(toDoItem: ToDoItem): IssueModel?
}
