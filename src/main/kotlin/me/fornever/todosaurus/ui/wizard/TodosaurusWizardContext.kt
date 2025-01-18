// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.wizard

import me.fornever.todosaurus.issueTrackers.IssueTrackerConnectionDetails
import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.issues.ToDoItem

data class TodosaurusWizardContext(
    val toDoItem: ToDoItem,
    val connectionDetails: IssueTrackerConnectionDetails = IssueTrackerConnectionDetails(),
    var placementDetails: IssuePlacementDetails? = null
)
