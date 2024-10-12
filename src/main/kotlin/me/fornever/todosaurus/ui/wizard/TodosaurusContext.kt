package me.fornever.todosaurus.ui.wizard

import me.fornever.todosaurus.issueTrackers.IssueTrackerConnectionDetails
import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.issues.ToDoItem

data class TodosaurusContext(
    val toDoItem: ToDoItem,
    val connectionDetails: IssueTrackerConnectionDetails = IssueTrackerConnectionDetails(),
    var placementDetails: IssuePlacementDetails? = null
)
