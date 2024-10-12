package me.fornever.todosaurus.issueTrackers

import me.fornever.todosaurus.issues.IssueModel
import me.fornever.todosaurus.issues.ToDoItem

interface IssueTrackerClient {
    suspend fun createIssue(toDoItem: ToDoItem): IssueModel

    suspend fun getIssue(toDoItem: ToDoItem): IssueModel?
}
