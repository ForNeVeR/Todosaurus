// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceStore

class ForgetChoiceAction: AnAction(TodosaurusCoreBundle.message("action.ForgetChoice.text")) {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        UserChoiceStore.getInstance(project).forgetChoice()
    }
}
