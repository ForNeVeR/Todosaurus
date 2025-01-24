// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.issues.ToDoService
import me.fornever.todosaurus.settings.TodosaurusSettings

class OpenReportedIssueInBrowserAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(actionEvent: AnActionEvent) {
        if (!actionEvent.tryActivateOnToDoPanel())
            return

        val toDoRange = actionEvent.getToDoTextRange()
        val todosaurusSettings = TodosaurusSettings.getInstance()
        actionEvent.presentation.isEnabled = toDoRange != null && !ToDoItem(todosaurusSettings.state, toDoRange).isNew
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        val toDoRange = actionEvent.getToDoTextRange() ?: return
        val todosaurusSettings = TodosaurusSettings.getInstance()
        ToDoService.getInstance(project).openReportedIssueInBrowser(ToDoItem(todosaurusSettings.state, toDoRange))
    }
}
