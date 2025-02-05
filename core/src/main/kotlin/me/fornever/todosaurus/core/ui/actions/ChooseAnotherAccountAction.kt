// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ToDoService
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceStore

class ChooseAnotherAccountAction private constructor(private val retryAction: (toDoService: ToDoService) -> Unit): AnAction(TodosaurusCoreBundle.message("action.ChooseAnotherAccount.text")) {
    companion object {
        fun thenTryAgainToCreateNewIssue(toDoItem: ToDoItem): ChooseAnotherAccountAction
            = ChooseAnotherAccountAction {
                it.createNewIssue(toDoItem)
            }

        fun thenTryAgainToOpenIssueInBrowser(toDoItem: ToDoItem): ChooseAnotherAccountAction
            = ChooseAnotherAccountAction {
                it.openReportedIssueInBrowser(toDoItem)
            }
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        UserChoiceStore.getInstance(project).forgetChoice()
        retryAction(ToDoService.getInstance(project))
    }
}

