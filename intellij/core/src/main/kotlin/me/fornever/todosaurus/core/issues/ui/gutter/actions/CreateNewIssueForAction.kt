// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.core.issues.ui.gutter.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ToDoService

class CreateNewIssueForAction(private val toDoItem: ToDoItem.New): AnAction(TodosaurusCoreBundle.message("action.CreateNewIssueFor.text", toDoItem.title)) {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        ToDoService.getInstance(project).createNewIssue(toDoItem)
    }
}
