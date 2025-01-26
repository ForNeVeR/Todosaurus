// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.issues.ui.gutter.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.issues.ToDoService

class OpenReportedIssueWithNumberAction(private val toDoItem: ToDoItem.Reported): AnAction(
	TodosaurusBundle.message(
		"action.OpenReportedIssueWithNumber.text",
		toDoItem.issueNumber
	) // TODO: Form issueNumber using TodosaurusSettings.ISSUE_NUMBER_REPLACEMENT
) {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        ToDoService.getInstance(project).openReportedIssueInBrowser(toDoItem)
    }
}
