// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.actions.extensions.getToDoTextRange
import me.fornever.todosaurus.actions.extensions.tryActivateThisAction
import me.fornever.todosaurus.services.ToDoItem
import me.fornever.todosaurus.services.ToDoService

class CreateIssueAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        if (!e.tryActivateThisAction())
            return

        val range = e.getToDoTextRange()
        e.presentation.isEnabled = range != null && ToDoItem(range).isNew
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val range = e.getToDoTextRange() ?: return
        ToDoService.getInstance(project).showCreateIssueDialog(range)
    }
}
