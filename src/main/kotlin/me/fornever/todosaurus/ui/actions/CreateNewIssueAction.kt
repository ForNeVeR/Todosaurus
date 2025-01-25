// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.issues.ToDoService

class CreateNewIssueAction: AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(actionEvent: AnActionEvent) {
        if (!actionEvent.tryActivateOnToDoPanel())
            return

        val toDoRange = actionEvent.getToDoTextRange()
        actionEvent.presentation.isEnabled = toDoRange != null && ToDoItem.fromRange(toDoRange) is ToDoItem.New
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        val toDoRange = actionEvent.getToDoTextRange() ?: return
        ToDoService.getInstance(project).createNewIssue(ToDoItem.fromRange(toDoRange))
    }
}
