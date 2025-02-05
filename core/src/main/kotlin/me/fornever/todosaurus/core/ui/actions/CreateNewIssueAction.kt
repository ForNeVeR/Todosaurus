// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ToDoService
import me.fornever.todosaurus.core.settings.TodosaurusSettings

class CreateNewIssueAction: AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(actionEvent: AnActionEvent) {
        if (!actionEvent.tryActivateOnToDoPanel())
            return

        val toDoRange = actionEvent.getToDoTextRange()
        val todosaurusSettings = TodosaurusSettings.getInstance()
        actionEvent.presentation.isEnabled = toDoRange != null && ToDoItem.fromRange(toDoRange, todosaurusSettings.state) is ToDoItem.New
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        val toDoRange = actionEvent.getToDoTextRange() ?: return
        val todosaurusSettings = TodosaurusSettings.getInstance()
        ToDoService.getInstance(project).createNewIssue(ToDoItem.fromRange(toDoRange, todosaurusSettings.state) as ToDoItem.New)
    }
}
