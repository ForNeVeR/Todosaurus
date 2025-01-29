// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.wizard

import me.fornever.todosaurus.core.issueTrackers.IssueTrackerConnectionDetails
import me.fornever.todosaurus.core.issues.IssuePlacementDetails
import me.fornever.todosaurus.core.issues.ToDoItem

data class TodosaurusWizardContext<TItem : ToDoItem>(
    val toDoItem: TItem,
    val connectionDetails: IssueTrackerConnectionDetails = IssueTrackerConnectionDetails(),
    var placementDetails: IssuePlacementDetails? = null
)
